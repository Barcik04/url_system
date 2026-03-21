package com.example.url_system.services;


import com.example.url_system.dtos.UserPlanResponse;
import com.example.url_system.models.Plan;
import com.example.url_system.models.SubscriptionStatus;
import com.example.url_system.models.User;
import com.example.url_system.models.UserSubscription;
import com.example.url_system.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void deleteAccount(Long userId, String password) {
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

        userRepository.delete(user);
    }

    public UserPlanResponse getSubscriptionPlan(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userid cant be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("user not found"));

        UserSubscription userSubscription = user.getUserSubscription();

        if (userSubscription == null) {
            throw new NoSuchElementException("userSubscription not found");
        }

        return new UserPlanResponse(
                user.getPlan(),
                userSubscription.getCancellationScheduled(),
                userSubscription.getSubscriptionEnd()
        );
    }
}

