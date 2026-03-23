package com.example.url_system.services;


import com.example.url_system.dtos.UserPlanResponse;
import com.example.url_system.models.Plan;
import com.example.url_system.models.SubscriptionStatus;
import com.example.url_system.models.User;
import com.example.url_system.models.UserSubscription;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.repositories.UserSubscriptionRepository;
import com.stripe.exception.StripeException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionService subscriptionService;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserSubscriptionRepository userSubscriptionRepository, SubscriptionService subscriptionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionService = subscriptionService;
    }


    @Transactional
    public void deleteAccount(Long userId, String password) throws StripeException {
        if (userId == null) {
            throw new IllegalArgumentException("userdid cant be null");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("password cant be null/blank");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));


        if  (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("passwords incorrect");
        }

        Optional<UserSubscription> optionalSubscription = userSubscriptionRepository
                .findByUserId(userId);

        if (optionalSubscription.isPresent()) {
            UserSubscription userSubscription = optionalSubscription.get();

            if (userSubscription.getStatus() != SubscriptionStatus.CANCELED
                    && userSubscription.getStripeSubscriptionId() != null
                    && !userSubscription.getStripeSubscriptionId().isBlank()) {
                subscriptionService.cancelSubscriptionImmediately(userId);
            }

            userSubscriptionRepository.delete(userSubscription);
        }

        userRepository.delete(user);
    }


    @Transactional(readOnly = true)
    public UserPlanResponse getSubscriptionPlan(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userid cant be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        UserSubscription userSubscription = user.getUserSubscription();

        if (userSubscription == null) {
            return new UserPlanResponse(
                    Plan.REGULAR,
                    false,
                    null
            );
        }

        return new UserPlanResponse(
                user.getPlan(),
                userSubscription.getCancellationScheduled(),
                userSubscription.getSubscriptionEnd()
        );
    }
}

