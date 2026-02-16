package com.example.url_system.jwt;

import com.example.url_system.dtos.CreateResponseUrlDto;
import com.example.url_system.dtos.OutboxPayloadDto;
import com.example.url_system.exceptions.ApiError;
import com.example.url_system.models.OutboxEvent;
import com.example.url_system.models.Role;
import com.example.url_system.models.User;
import com.example.url_system.repositories.OutboxEventRepository;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.services.EmailVerificationService;
import com.example.url_system.utils.redis.RedisCacheClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.GrantedAuthority;


import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Tag(
        name = "auth",
        description = "Endpoints for signing in, registering, logout and refreshing tokens"
)
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
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;




    public AuthController(AuthenticationManager authManager, JwtUtils jwt, UserRepository userRepo, PasswordEncoder passwordEncoder, OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper, RedisCacheClient redisCacheClient, RefreshTokenService refreshTokenService, EmailVerificationService emailVerificationService) {
        this.authManager = authManager;
        this.jwt = jwt;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
        this.redisCacheClient = redisCacheClient;
        this.refreshTokenService = refreshTokenService;
        this.emailVerificationService = emailVerificationService;
    }



    @Operation(
            summary = "Signin endpoint, returning JWT bearer token, creates refresh_token also",
            tags = {"Create"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token with username and roles",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad credentials"
            )
    })
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
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

            User dbUser = userRepo.findByUsername(user.getUsername())
                    .orElseThrow();

            if (dbUser.getEnabled().equals(false)) {
                return ResponseEntity.status(403).body(Map.of(
                        "message", "Email not verified"));
            }

            String rawRefresh = refreshTokenService.issueRefreshToken(dbUser);

            setRefreshCookie(response, rawRefresh, (int) Duration.ofDays(30).getSeconds());

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




    @Operation(
            summary = "Register endpoint for creating an account",
            tags = {"Create"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registered"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Username already taken"
            )
    })
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


        emailVerificationService.createAndSendFor(user, publicBaseUrl());

        return ResponseEntity.ok(Map.of(
                "message", "Registered. Please verify your email."
        ));
    }





    @Operation(summary = "Verify email using token", tags = {"Create"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            emailVerificationService.verifyToken(token);
            return ResponseEntity.ok(Map.of("message", "Email verified. You can sign in now."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }







    @Operation(
            summary = "Logout from account and revoke JWT refresh token",
            tags = {"Create"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Logged out"
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        String raw = getCookie(request, REFRESH_COOKIE);

        if (raw != null && !raw.isBlank()) {
            refreshTokenService.revokeByRawToken(raw);
        }

        clearRefreshCookie(response);
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }



    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }


    @Operation(
            summary = "Endpoint for refreshing refresh token",
            tags = {"Create"})
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Registered"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Username already taken"
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        String raw = getCookie(request, REFRESH_COOKIE);
        if (raw == null || raw.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("message", "Missing refresh token"));
        }

        return refreshTokenService.findValidByRawToken(raw)
                .map(rt -> {
                    String newAccess = jwt.generateTokenFromUsername(rt.getUser().getUsername());
                    return ResponseEntity.ok(Map.of("accessToken", newAccess));
                })
                .orElseGet(() -> ResponseEntity.status(401).body(Map.of("message", "Invalid refresh token")));
    }



    private static final String REFRESH_COOKIE = "refresh_token";

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }



    private void setRefreshCookie(HttpServletResponse response, String rawRefreshToken, int maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, rawRefreshToken)
                .httpOnly(true)
                .secure(true) // true in prod
                .path("/")
                .maxAge(maxAgeSeconds)
                .sameSite("Lax") // "None" if cross-site + Secure=true
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(true) // true in prod
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }


    @Value("${app.publicBaseUrl}")
    private String publicBaseUrl;

    private String publicBaseUrl() {
        return publicBaseUrl + "/auth/verify-email";
    }

}
