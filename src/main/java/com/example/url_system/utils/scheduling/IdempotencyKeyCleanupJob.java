package com.example.url_system.utils.scheduling;

import com.example.url_system.services.IdempotencyKeyService;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class IdempotencyKeyCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyKeyCleanupJob.class);

    private final IdempotencyKeyService service;

    private static final Duration TTL = Duration.ofDays(7);

    public IdempotencyKeyCleanupJob(IdempotencyKeyService service) {
        this.service = service;
    }


    /**
     * Delete expired idempotency keys after 7 days
     *
     */
    @Retry(name = "baseService")
    @Scheduled(cron = "0 1 3 * * *", zone = "Europe/Warsaw")
    public void run() {
        service.cleanupOlderThan(TTL);
        log.info("Cleaned up old Idempotency Keys, {} ", Instant.now());
    }


}

