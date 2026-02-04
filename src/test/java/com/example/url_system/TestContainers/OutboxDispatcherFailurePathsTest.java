package com.example.url_system.TestContainers;


import com.example.url_system.dtos.CreateUrlRequest;
import com.example.url_system.models.OutboxEvent;
import com.example.url_system.models.User;
import com.example.url_system.repositories.OutboxEventRepository;
import com.example.url_system.repositories.UserRepository;
import com.example.url_system.services.UrlService;
import com.example.url_system.utils.emailSender.EmailSender;
import com.example.url_system.utils.emailSender.OutboxDispatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
class OutboxDispatcherFailurePathsTest {

    @Autowired PasswordEncoder passwordEncoder;
    @Autowired UserRepository userRepository;
    @Autowired UrlService urlService;
    @Autowired OutboxEventRepository outboxEventRepository;
    @Autowired JdbcTemplate jdbc;
    @Autowired OutboxDispatcher outboxDispatcher;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean EmailSender emailSender;

    @MockitoBean KafkaTemplate<String, String> kafkaTemplate;

    @MockitoBean Clock clock;



    @BeforeEach
    void setup() {
        jdbc.execute("TRUNCATE TABLE urls, users, idempotency_keys, outbox_events RESTART IDENTITY CASCADE");
        userRepository.save(new User("igor.bb00@gmail.com", passwordEncoder.encode("12345678")));

        Instant fixedInstant = LocalDate.of(2082, 12, 13)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
    }




    @Test
    void shouldMarkFailedAndScheduleRetryWhenKafkaSendFails() {
        CompletableFuture failed = CompletableFuture.failedFuture(new RuntimeException("boom"));
        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(failed);

        User user = userRepository.findByUsername("igor.bb00@gmail.com").orElseThrow();

        urlService.create(
                new CreateUrlRequest("https://example.com", Instant.now().plusSeconds(120)),
                user.getId(),
                "123"
        );

        OutboxEvent event = outboxEventRepository.findAll().getFirst();
        assertEquals(OutboxEvent.Status.NEW, event.getStatus());

        List<Long> ids = outboxDispatcher.claimBatch(50);
        assertTrue(ids.contains(event.getId()));

        OutboxEvent processing = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertEquals(OutboxEvent.Status.PROCESSING, processing.getStatus());

        outboxDispatcher.publishOne(event.getId());

        await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(50))
                .untilAsserted(() -> {
                    OutboxEvent failedEvent = outboxEventRepository.findById(event.getId()).orElseThrow();
                    assertEquals(OutboxEvent.Status.FAILED, failedEvent.getStatus());
                });

        OutboxEvent failedEvent = outboxEventRepository.findById(event.getId()).orElseThrow();
        assertEquals(OutboxEvent.Status.FAILED, failedEvent.getStatus());
        assertNotNull(failedEvent.getLastError());
        assertNotNull(failedEvent.getNextAttemptAt());
        assertTrue(failedEvent.getAttempts() >= 1);
    }





    @Test
    void shouldMarkDeadWhenEventTypeIsUnsupported() {
        String str = "{\"hello\":\"world\"}";
        OutboxEvent e = new OutboxEvent();

        e.setEventType("SOMETHING_ELSE");
        e.setStatus(OutboxEvent.Status.PROCESSING);
        e.setAttempts(0);
        e.setPayload(objectMapper.valueToTree(str));
        e.setNextAttemptAt(Instant.now(clock));

        OutboxEvent saved = outboxEventRepository.save(e);

        outboxDispatcher.publishOne(saved.getId());


        await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(50))
                .untilAsserted(() -> {
                    OutboxEvent failedEvent = outboxEventRepository.findById(saved.getId()).orElseThrow();
                    assertEquals(OutboxEvent.Status.DEAD, failedEvent.getStatus());
                });

        OutboxEvent dead = outboxEventRepository.findById(saved.getId()).orElseThrow();
        assertEquals(OutboxEvent.Status.DEAD, dead.getStatus());
        assertNotNull(dead.getLastError());
        assertTrue(dead.getLastError().contains("Unsupported eventType="));
        assertNull(dead.getNextAttemptAt());
    }
}

