package com.example.url_system.services;

import com.example.url_system.models.Plan;
import com.example.url_system.models.SubscriptionStatus;
import com.example.url_system.models.User;
import com.example.url_system.models.UserSubscription;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.repositories.UserSubscriptionRepository;
import com.example.url_system.utils.config.stripe.StripeProperties;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class StripeWebhookService {

    private final StripeProperties stripeProperties;
    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;

    public StripeWebhookService(StripeProperties stripeProperties,
                                UserRepository userRepository,
                                UserSubscriptionRepository userSubscriptionRepository) {
        this.stripeProperties = stripeProperties;
        this.userRepository = userRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    public Event constructEvent(String payload, String signatureHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(
                payload,
                signatureHeader,
                stripeProperties.getWebhookSecret()
        );
    }

    @Transactional
    public void handleEvent(Event event) {
        switch (event.getType()) {
            case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
            case "customer.subscription.created", "customer.subscription.updated" ->
                    handleCustomerSubscriptionChanged(event);
            case "customer.subscription.deleted" -> handleCustomerSubscriptionDeleted(event);
            default -> System.out.println("Unhandled event type: " + event.getType());
        }
    }

    @Transactional
    public void handleCheckoutSessionCompleted(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new RuntimeException("Cannot deserialize Stripe event object"));

        Session session = (Session) stripeObject;

        System.out.println("=== CHECKOUT SESSION COMPLETED ===");
        System.out.println("Session id: " + session.getId());
        System.out.println("Customer: " + session.getCustomer());
        System.out.println("Subscription: " + session.getSubscription());
        System.out.println("Metadata: " + session.getMetadata());

        if (session.getMetadata() == null || session.getMetadata().get("userId") == null) {
            throw new RuntimeException("Missing userId in Stripe session metadata");
        }

        Long userId = Long.parseLong(session.getMetadata().get("userId"));
        System.out.println("Parsed userId: " + userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for id: " + userId));

        UserSubscription userSubscription = userSubscriptionRepository.findByUserId(userId)
                .orElseGet(UserSubscription::new);

        userSubscription.setUser(user);
        userSubscription.setStripeCustomerId(session.getCustomer());
        userSubscription.setStripeSubscriptionId(session.getSubscription());
        userSubscription.setStatus(SubscriptionStatus.INCOMPLETE);
        userSubscription.setUpdatedAt(Instant.now());

        if (userSubscription.getCreatedAt() == null) {
            userSubscription.setCreatedAt(Instant.now());
        }

        userSubscriptionRepository.save(userSubscription);

        System.out.println("Saved subscription for userId: " + userId);
    }

    @Transactional
    public void handleCustomerSubscriptionChanged(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new RuntimeException("Cannot deserialize Stripe event object"));

        Subscription subscription = (Subscription) stripeObject;

        System.out.println("=== SUBSCRIPTION CHANGED ===");
        System.out.println("Subscription id: " + subscription.getId());
        System.out.println("Subscription customer: " + subscription.getCustomer());
        System.out.println("Subscription status: " + subscription.getStatus());
        System.out.println("Subscription metadata: " + subscription.getMetadata());

        UserSubscription userSubscription = userSubscriptionRepository
                .findByStripeSubscriptionId(subscription.getId())
                .orElseGet(UserSubscription::new);

        if (userSubscription.getUser() == null) {
            if (subscription.getMetadata() == null || subscription.getMetadata().get("userId") == null) {
                throw new RuntimeException("Missing userId in subscription metadata");
            }

            Long userId = Long.parseLong(subscription.getMetadata().get("userId"));

            User foundUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found for id: " + userId));

            userSubscription.setUser(foundUser);
        }

        userSubscription.setStripeCustomerId(subscription.getCustomer());
        userSubscription.setStripeSubscriptionId(subscription.getId());
        userSubscription.setStatus(mapStripeStatus(subscription.getStatus()));
        userSubscription.setUpdatedAt(Instant.now());

        Instant subscriptionEnd = extractSubscriptionEnd(subscription);
        if (subscriptionEnd != null) {
            userSubscription.setSubscriptionEnd(subscriptionEnd);
        }

        if (subscription.getItems() != null
                && subscription.getItems().getData() != null
                && !subscription.getItems().getData().isEmpty()
                && subscription.getItems().getData().getFirst().getPrice() != null) {
            userSubscription.setStripePriceId(
                    subscription.getItems().getData().getFirst().getPrice().getId()
            );
        }

        if (userSubscription.getCreatedAt() == null) {
            userSubscription.setCreatedAt(Instant.now());
        }

        User user = userSubscription.getUser();

        if (userSubscription.getStatus() == SubscriptionStatus.ACTIVE
                || userSubscription.getStatus() == SubscriptionStatus.TRIALING) {
            user.setPlan(Plan.PREMIUM);
        } else {
            user.setPlan(Plan.REGULAR);
        }

        userSubscriptionRepository.save(userSubscription);
        userRepository.save(user);

        System.out.println("Saved/updated subscription: " + userSubscription.getStripeSubscriptionId());
        System.out.println("Saved status: " + userSubscription.getStatus());
        System.out.println("Saved subscriptionEnd: " + userSubscription.getSubscriptionEnd());
    }

    @Transactional
    public void handleCustomerSubscriptionDeleted(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new RuntimeException("Cannot deserialize Stripe event object"));

        Subscription subscription = (Subscription) stripeObject;

        System.out.println("=== SUBSCRIPTION DELETED ===");
        System.out.println("Subscription id: " + subscription.getId());
        System.out.println("Subscription metadata: " + subscription.getMetadata());

        UserSubscription userSubscription = userSubscriptionRepository
                .findByStripeSubscriptionId(subscription.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Subscription not found in database: " + subscription.getId()
                ));

        userSubscription.setStatus(SubscriptionStatus.CANCELED);
        userSubscription.setUpdatedAt(Instant.now());

        Instant subscriptionEnd = extractSubscriptionEnd(subscription);
        userSubscription.setSubscriptionEnd(subscriptionEnd != null ? subscriptionEnd : Instant.now());

        User user = userSubscription.getUser();
        user.setPlan(Plan.REGULAR);

        userSubscriptionRepository.save(userSubscription);
        userRepository.save(user);

        System.out.println("Subscription canceled locally: " + subscription.getId());
    }

    private Instant extractSubscriptionEnd(Subscription subscription) {
        if (subscription.getItems() == null
                || subscription.getItems().getData() == null
                || subscription.getItems().getData().isEmpty()) {
            return null;
        }

        Long currentPeriodEnd = subscription.getItems()
                .getData()
                .getFirst()
                .getCurrentPeriodEnd();

        if (currentPeriodEnd == null) {
            return null;
        }

        return Instant.ofEpochSecond(currentPeriodEnd);
    }

    private SubscriptionStatus mapStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "unpaid" -> SubscriptionStatus.UNPAID;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            case "incomplete_expired" -> SubscriptionStatus.INCOMPLETE_EXPIRED;
            default -> throw new RuntimeException("Unhandled Stripe subscription status: " + stripeStatus);
        };
    }
}