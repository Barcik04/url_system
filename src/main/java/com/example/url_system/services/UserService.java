package com.example.url_system.services;


import com.example.url_system.models.User;
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
}
