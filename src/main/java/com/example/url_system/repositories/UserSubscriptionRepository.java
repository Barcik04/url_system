package com.example.url_system.repositories;

import com.example.url_system.models.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findByUserId(Long userId);
    Optional<UserSubscription> findByStripeCustomerId(String stripeCustomerId);
    Optional<UserSubscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    boolean existsByUserId(Long userId);
}