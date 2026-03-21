package com.example.url_system.services;

import com.example.url_system.models.UserSubscription;
import com.example.url_system.repositories.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionUpdateParams;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {

    private final UserSubscriptionRepository userSubscriptionRepository;

    public SubscriptionService(UserSubscriptionRepository userSubscriptionRepository) {
        this.userSubscriptionRepository = userSubscriptionRepository;
    }

    @Transactional
    public void cancelMySubscription(Long userId) throws StripeException {
        UserSubscription userSubscription = userSubscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Subscription for user not found"));

        String stripeSubscriptionId = userSubscription.getStripeSubscriptionId();
        if (stripeSubscriptionId == null || stripeSubscriptionId.isBlank()) {
            throw new RuntimeException("User does not have Stripe subscription id");
        }

        Subscription subscription = Subscription.retrieve(stripeSubscriptionId);

        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true)
                .build();

        subscription.update(params);
    }
}