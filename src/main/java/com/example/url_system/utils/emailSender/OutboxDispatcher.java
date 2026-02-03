package com.example.url_system.utils.emailSender;

import com.example.url_system.models.OutboxEvent;
import com.example.url_system.repositories.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.*;

@Service
public class OutboxDispatcher {

    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);

    private static final String EMAIL_TOPIC = "email.send.requested";
    private static final Map<String, String> STATUSES = new HashMap(Map.of(
            "EMAIL_SEND_REQUESTED", "email.send.requested",
            "SIGNIN_FAIL", "signin.fail"
    ));

    private final OutboxEventRepository repo;
    private final Clock clock;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxDispatcher(OutboxEventRepository repo, Clock clock, KafkaTemplate<String, String> kafkaTemplate) {
        this.repo = repo;
        this.clock = clock;
        this.kafkaTemplate = kafkaTemplate;
    }



    @Scheduled(fixedDelayString = "PT10S")
    public void tick() {
        List<Long> ids = claimBatch(50);
        for (Long id : ids) {
            publishOne(id);
        }
    }



    @Transactional
    public List<Long> claimBatch(int limit) {
        Instant now = Instant.now(clock);

        List<OutboxEvent> due = repo.findDueForUpdateSkipLocked(now, limit);

        for (OutboxEvent e : due) {
            e.setStatus(OutboxEvent.Status.PROCESSING);
            e.setAttempts(e.getAttempts() + 1);
            repo.save(e);
        }

        return due.stream().map(OutboxEvent::getId).toList();
    }




    public void publishOne(Long id) {
        OutboxEvent e = repo.findById(id).orElseThrow();

        if (e.getStatus() != OutboxEvent.Status.PROCESSING) {
            return;
        }

        if (STATUSES.keySet().stream().noneMatch(a -> a.equals(e.getEventType()))) {
            markDead(id, "Unsupported eventType=" + e.getEventType());
            return;
        }

        String payloadJson = e.getPayload().toString();

        String eventTopic = e.getEventType();
        String topic = STATUSES.get(eventTopic);

        try {
            // Wait for kafka ACK
            kafkaTemplate.send(topic, String.valueOf(e.getId()), payloadJson).get();

            markDone(id);
            log.info("Outbox {} published to Kafka topic={}", id, topic);

        } catch (Exception ex) {
            markFailed(id, ex);
            log.warn("Outbox {} publish FAILED: {}", id, ex.toString());
        }
    }





    @Transactional
    public void markDone(Long id) {
        OutboxEvent e = repo.findById(id).orElseThrow();
        e.setStatus(OutboxEvent.Status.DONE);
        e.setLastError(null);
        e.setNextAttemptAt(null);
        repo.save(e);
    }





    @Transactional
    public void markFailed(Long id, Exception ex) {
        OutboxEvent e = repo.findById(id).orElseThrow();

        boolean dead = e.getAttempts() >= 10;
        e.setStatus(dead ? OutboxEvent.Status.DEAD : OutboxEvent.Status.FAILED);
        e.setLastError(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        e.setNextAttemptAt(nextRetryTime(e.getAttempts(), Instant.now(clock)));
        repo.save(e);

    }





    @Transactional
    public void markDead(Long id, String error) {
        OutboxEvent e = repo.findById(id).orElseThrow();
        e.setStatus(OutboxEvent.Status.DEAD);
        e.setLastError(error);
        e.setNextAttemptAt(null);
        repo.save(e);

    }




    private Instant nextRetryTime(int attempts, Instant now) {
        long delaySeconds = Math.min(10L * (1L << Math.max(0, attempts - 1)), 3600L);
        return now.plusSeconds(delaySeconds);
    }
}
