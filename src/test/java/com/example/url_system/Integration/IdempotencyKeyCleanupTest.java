package com.example.url_system.Integration;

import com.example.url_system.models.IdempotencyKeys;
import com.example.url_system.repositories.IdempotencyKeyRepository;
import com.example.url_system.services.AvatarService;
import com.example.url_system.services.IdempotencyKeyService;
import com.example.url_system.utils.emailSender.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.mockito.Mockito.when;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class IdempotencyKeyCleanupTest {
    private static final Logger log = LoggerFactory.getLogger(IdempotencyKeyCleanupTest.class);
    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    IdempotencyKeyService idempotencyKeyService;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private EmailSender emailSender;

    @MockitoBean
    private AvatarService avatarService;

    @BeforeEach
    void cleanDb() {
        jdbc.execute("""
        TRUNCATE TABLE
            users,
            urls,
            idempotency_keys
        RESTART IDENTITY CASCADE
    """);
    }


    @Test
    void shouldDeleteExpiredIdempotencyKeysScheduled() {
        Instant fixedInstant = LocalDate.of(2082, 12, 13)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        idempotencyKeyRepository.save(new IdempotencyKeys("OP_CREATE", "123"));
        idempotencyKeyRepository.save(new IdempotencyKeys("OP_CREATE", "1234"));


        idempotencyKeyService.cleanupOlderThan(Duration.ofDays(7));

    }
}
