package com.example.url_system.services;

import com.example.url_system.models.UserSubscription;
import com.example.url_system.repositories.UserSubscriptionRepository;
import com.example.url_system.utils.config.stripe.StripeProperties;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.param.SubscriptionUpdateParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;
    private final StripeClient stripeClient;

    public SubscriptionService(UserSubscriptionRepository userSubscriptionRepository,
                               StripeProperties stripeProperties) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.stripeClient = new StripeClient(stripeProperties.getSecretKey());
    }

    @Transactional
    public void cancelMySubscription(Long userId) throws StripeException {
        UserSubscription userSubscription = userSubscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Subscription for user not found"));

        String stripeSubscriptionId = userSubscription.getStripeSubscriptionId();

        if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank()) {
            throw new RuntimeException("User does not have Stripe subscription id");
        }

        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true)
                .build();

        stripeClient.v1().subscriptions().update(stripeSubscriptionId, params);
    }



    @Transactional
    public void cancelSubscriptionImmediately(Long userId) throws StripeException {
        UserSubscription userSubscription = userSubscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Subscription for user not found"));

        String stripeSubscriptionId = userSubscription.getStripeSubscriptionId();

        if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank()) {
            throw new RuntimeException("User does not have Stripe subscription id");
        }

        stripeClient.v1().subscriptions().cancel(stripeSubscriptionId);
    }




    @Transactional
    public void resumeMySubscription(Long userId) throws StripeException {
        UserSubscription userSubscription = userSubscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Subscription for user not found"));

        String stripeSubscriptionId = userSubscription.getStripeSubscriptionId();

        if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank()) {
            throw new RuntimeException("User does not have Stripe subscription id");
        }

        if (!Boolean.TRUE.equals(userSubscription.getCancellationScheduled())) {
            throw new RuntimeException("Subscription is not scheduled for cancellation");
        }

        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(false)
                .build();

        stripeClient.v1().subscriptions().update(stripeSubscriptionId, params);
    }
}