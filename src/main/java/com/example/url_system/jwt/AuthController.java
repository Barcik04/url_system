package com.example.url_system.jwt;

import com.example.url_system.models.Role;
import com.example.url_system.models.User;
import com.example.url_system.repositories.OutboxEventRepository;
import com.example.url_system.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.GrantedAuthority;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthenticationManager authManager;
    private final JwtUtils jwt;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final OutboxEventRepository outboxEventRepository;


    public AuthController(AuthenticationManager authManager, JwtUtils jwt, UserRepository userRepo, PasswordEncoder passwordEncoder, OutboxEventRepository outboxEventRepository) {
        this.authManager = authManager;
        this.jwt = jwt;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.outboxEventRepository = outboxEventRepository;
    }



    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
            );
            UserDetails user = (UserDetails) auth.getPrincipal();
            assert user != null;
            String token = jwt.generateTokenFromUsername(user);

            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            return ResponseEntity.ok(new LoginResponse(token, user.getUsername(), roles));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(Map.of("message", "Bad credentials"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body("Username already taken");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.USER);

        userRepo.save(user);
        return ResponseEntity.ok("Registered");
    }


}
