package com.example.url_system.jwt;

import com.example.url_system.dtos.OutboxPayloadDto;
import com.example.url_system.models.OutboxEvent;
import com.example.url_system.models.Role;
import com.example.url_system.models.User;
import com.example.url_system.repositories.OutboxEventRepository;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.utils.redis.RedisCacheClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
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


import java.time.Duration;
import java.time.Instant;
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
    private final ObjectMapper objectMapper;
    private final RedisCacheClient redisCacheClient;


    public AuthController(AuthenticationManager authManager, JwtUtils jwt, UserRepository userRepo, PasswordEncoder passwordEncoder, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper, RedisCacheClient redisCacheClient) {
        this.authManager = authManager;
        this.jwt = jwt;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.redisCacheClient = redisCacheClient;
    }



    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest req, HttpServletRequest request) {
        String userIp = resolveClientIp(request);
        String cacheKey = "stats:fail:ip:" + userIp + ":code:" + req.getUsername();
        String blockKey = "stats:block:ip:" + userIp + ":code:" + req.getUsername();

        if (redisCacheClient.exists(blockKey)) {
            return ResponseEntity.status(401).body(Map.of("message", "Too many failed attempts, wait for a bit"));
        }

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


            int keyCount = redisCacheClient.incrementAndGet(cacheKey, Duration.ofMinutes(4));
            if (keyCount == 5) {

                OutboxPayloadDto outboxPayloadDto = new OutboxPayloadDto(
                        req.getUsername(),
                        "Someone is trying to sign in",
                        "5 or more failed signin attempts to: " + req.getUsername()
                );

                JsonNode jsonPayload = objectMapper.valueToTree(outboxPayloadDto);

                outboxEventRepository.save(new OutboxEvent(
                        "SIGNIN_FAIL",
                        jsonPayload,
                        OutboxEvent.Status.NEW,
                        null,
                        Instant.now()
                ));
            }

            if (keyCount >= 6) {
                redisCacheClient.set(blockKey, "1", Duration.ofMinutes(10));
            }

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




    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

}
