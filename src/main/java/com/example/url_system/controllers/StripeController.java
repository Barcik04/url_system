package com.example.url_system.controllers;

import com.example.url_system.dtos.stripe.StripeCheckoutResponse;
import com.example.url_system.models.User;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.services.StripeService;
import com.stripe.exception.StripeException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stripe")
public class StripeController {

    private final StripeService stripeService;
    private final UserRepository userRepository;

    public StripeController(StripeService stripeService, UserRepository userRepository) {
        this.stripeService = stripeService;
        this.userRepository = userRepository;
    }

    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority('USER')")
    public ResponseEntity<StripeCheckoutResponse> createCheckoutSession(Authentication authentication) throws StripeException {
        String email = authentication.getName();

        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        StripeCheckoutResponse response = stripeService.createCheckoutSession(user.getId());

        return ResponseEntity.ok(response);
    }
}