package com.example.url_system.utils.emailSender;

import com.example.url_system.models.OutboxEvent;
import com.example.url_system.repositories.OutboxEventRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;

@Service
public class EmailOutboxHandler implements OutboxHandler {

    private final OutboxEventRepository repo;
    private final ObjectMapper objectMapper;
    private final EmailSender emailSender;
    private final Clock clock;

    public EmailOutboxHandler(OutboxEventRepository repo,
                              ObjectMapper objectMapper,
                              EmailSender emailSender,
                              Clock clock) {
        this.repo = repo;
        this.objectMapper = objectMapper;
        this.emailSender = emailSender;
        this.clock = clock;
    }

    @Override public String eventType() { return "EMAIL_SEND_REQUESTED"; }

    @Async("emailExecutor")
    @Transactional
    public void handle(Long outboxId) {
        OutboxEvent e = repo.findById(outboxId).orElseThrow();

        if (e.getStatus() != OutboxEvent.Status.PROCESSING) return;

        try {
            EmailPayload p = objectMapper.treeToValue(e.getPayload(), EmailPayload.class);

            emailSender.send(p.toEmail(), p.subject(), p.body());

            e.setStatus(OutboxEvent.Status.DONE);
            e.setLastError(null);

        } catch (Exception ex) {
            e.setStatus(e.getAttempts() >= 10 ? OutboxEvent.Status.DEAD : OutboxEvent.Status.FAILED);
            e.setLastError(ex.getClass().getSimpleName() + ": " + ex.getMessage());
            e.setNextAttemptAt(nextRetryTime(e.getAttempts(), Instant.now(clock)));
        }
    }

    private Instant nextRetryTime(int attempts, Instant now) {
        long delaySeconds = Math.min(10L * (1L << Math.max(0, attempts - 1)), 3600L); // cap at 1h
        return now.plusSeconds(delaySeconds);
    }
}


