package com.example.url_system.services;


import com.example.url_system.dtos.OutboxPayloadDto;
import com.example.url_system.models.EmailVerificationToken;
import com.example.url_system.models.OutboxEvent;
import com.example.url_system.models.User;
import com.example.url_system.repositories.EmailVerificationTokenRepository;
import com.example.url_system.repositories.OutboxEventRepository;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.utils.tokenhashing.TokenGenerator;
import com.example.url_system.utils.tokenhashing.TokenHashing;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private final EmailVerificationTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;


    private final Duration tokenTtl = Duration.ofHours(24);

    public EmailVerificationService(
            EmailVerificationTokenRepository tokenRepo,
            UserRepository userRepo,
            OutboxEventRepository outboxRepo,
            ObjectMapper objectMapper
    ) {
        this.tokenRepo = tokenRepo;
        this.userRepo = userRepo;
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
    }




    /**
     * Creates (or replaces) verification token and enqueues email via outbox.
     * Returns RAW token (you usually won't return it to client, only email it).
     */
    @Transactional
    public String createAndSendFor(User user, String verifyBaseUrl) {
        tokenRepo.deleteByUserId(user.getId());

        String raw = TokenGenerator.randomToken(48);
        String hash = TokenHashing.sha256Hex(raw);

        EmailVerificationToken evt = new EmailVerificationToken();
        evt.setUser(user);
        evt.setTokenHash(hash);
        evt.setExpiresAt(Instant.now().plus(tokenTtl));
        tokenRepo.save(evt);


        String verifyUrl = verifyBaseUrl + "?token=" + raw;

        OutboxPayloadDto outboxPayloadDto = new OutboxPayloadDto(
                user.getUsername(),
                "Verify your email",
                verifyUrl
        );

        JsonNode payload = objectMapper.valueToTree(outboxPayloadDto);


        outboxRepo.save(new OutboxEvent(
                "EMAIL_VERIFY",
                payload,
                OutboxEvent.Status.NEW,
                null,
                Instant.now()
        ));

        return raw;
    }



    @Transactional
    public void verifyToken(String rawToken) {
        String hash = TokenHashing.sha256Hex(rawToken);

        EmailVerificationToken t = tokenRepo.findByTokenHash(hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (t.getUsedAt() != null) throw new IllegalArgumentException("Token already used");
        if (t.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Token expired");

        User user = t.getUser();
        user.setEnabled(true);
        user.setEmailVerifiedAt(Instant.now());
        userRepo.save(user);

        t.setUsedAt(Instant.now());
        tokenRepo.save(t);
    }




    @Transactional
    public void deleteExpiredTokens() {
        Instant now = Instant.now();

        int count = tokenRepo.deleteExpiredTokens(now);
        log.info("deleted expired tokens {} times", count);
    }
}

