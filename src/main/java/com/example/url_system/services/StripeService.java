package com.example.url_system.services;

import com.example.url_system.dtos.stripe.StripeCheckoutResponse;
import com.example.url_system.models.User;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.utils.config.stripe.StripeProperties;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    private final StripeClient stripeClient;
    private final StripeProperties stripeProperties;
    private final UserRepository userRepository;

    public StripeService(StripeClient stripeClient,
                         StripeProperties stripeProperties,
                         UserRepository userRepository) {
        this.stripeClient = stripeClient;
        this.stripeProperties = stripeProperties;
        this.userRepository = userRepository;
    }

    public StripeCheckoutResponse createCheckoutSession(Long userId) throws StripeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(stripeProperties.getSuccessUrl())
                .setCancelUrl(stripeProperties.getCancelUrl())
                .setCustomerEmail(user.getUsername())
                .putMetadata("userId", user.getId().toString())
                .setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .putMetadata("userId", user.getId().toString())
                                .build()
                )
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPrice(stripeProperties.getPremiumPriceId())
                                .build()
                )
                .build();

        Session session = stripeClient.v1().checkout().sessions().create(params);

        return new StripeCheckoutResponse(session.getUrl());
    }
}