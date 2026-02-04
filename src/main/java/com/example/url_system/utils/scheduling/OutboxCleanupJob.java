package com.example.url_system.utils.scheduling;

import com.example.url_system.services.OutboxEventService;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Component
public class OutboxCleanupJob {

    private final OutboxEventService outboxEventService;

    private static final Duration TTL = Duration.ofDays(3);

    public OutboxCleanupJob(OutboxEventService outboxEventService) {
        this.outboxEventService = outboxEventService;
    }


    @Retry(name = "baseService")
    @Scheduled(cron = "0 2 3 * * *", zone = "Europe/Warsaw")
    @Transactional
    public void run() {
        outboxEventService.deleteDoneOutboxEvents();
    }
}
