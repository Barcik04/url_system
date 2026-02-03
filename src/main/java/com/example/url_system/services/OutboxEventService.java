package com.example.url_system.services;

import com.example.url_system.repositories.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class OutboxEventService {
    private static final Logger log = LoggerFactory.getLogger(OutboxEventService.class);
    private final OutboxEventRepository outboxEventRepository;
    private final Clock clock;


    public OutboxEventService(OutboxEventRepository outboxEventRepository, Clock clock) {
        this.outboxEventRepository = outboxEventRepository;
        this.clock = clock;
    }


    /*
    Method for deleting DONE outbox events
     */
    @Transactional
    public void deleteDoneOutboxEvents() {
        outboxEventRepository.deleteOnStatusDone();
        log.info("Deleted all DONE outbox events");
    }
}
