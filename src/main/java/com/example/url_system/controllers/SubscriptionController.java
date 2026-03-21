package com.example.url_system.controllers;

import com.example.url_system.repositories.UserRepository;
import com.example.url_system.services.SubscriptionService;
import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    public SubscriptionController(SubscriptionService subscriptionService,
                                  UserRepository userRepository) {
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
    }

    @PatchMapping("/cancel")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Map<String, String>> cancelMySubscription(
            @AuthenticationPrincipal UserDetails principal
    ) throws StripeException {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        String username = principal.getUsername();

        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();

        subscriptionService.cancelMySubscription(userId);

        return ResponseEntity.ok(Map.of(
                "message", "Subscription scheduled for cancellation at period end."
        ));
    }



    @PatchMapping("/resume")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<Map<String, String>> resumeMySubscription(
            @AuthenticationPrincipal UserDetails principal
    ) throws StripeException {

        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        String username = principal.getUsername();

        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"))
                .getId();

        subscriptionService.resumeMySubscription(userId);

        return ResponseEntity.ok(Map.of(
                "message", "Subscription cancellation has been removed."
        ));
    }
}