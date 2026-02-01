package com.example.url_system.services;

import com.example.url_system.repositories.IdempotencyKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class IdempotencyKeyService {
    private static final Logger log = LoggerFactory.getLogger(IdempotencyKeyService.class);

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final Clock clock;

    public IdempotencyKeyService(IdempotencyKeyRepository idempotencyKeyRepository, Clock clock) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.clock = clock;
    }

    /**
     * Method for deleting expired idempotency keys
     *
     * @param ttl redis ttl time
     */
    @Transactional
    public void cleanupOlderThan(Duration ttl) {
        Instant cutoff = Instant.now(clock).minus(ttl);
        long start = System.currentTimeMillis();

        idempotencyKeyRepository.deleteExpired(cutoff);

        long tookMs = System.currentTimeMillis() - start;
        log.info("IdempotencyKey cleanup done. cutoff={}, tookMs={}", cutoff, tookMs);

    }
}
