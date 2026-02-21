package com.example.url_system.utils.emailSender;

import com.example.url_system.models.OutboxEvent;
import com.example.url_system.repositories.OutboxEventRepository;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Profile({"prod", "!stage"})
@ConditionalOnProperty(name = "app.sqs.enabled", havingValue = "true")
public class OutboxDispatcherSQS {
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcherSQS.class);


    private final OutboxEventRepository repo;
    private final Clock clock;
    private final SqsTemplate sqsTemplate;
    private final String emailQueueNameOrUrl;

    public OutboxDispatcherSQS(
            OutboxEventRepository repo,
            Clock clock,
            SqsTemplate sqsTemplate,
            @Value("${app.sqs.emailQueue}") String emailQueueNameOrUrl
    ) {
        this.repo = repo;
        this.clock = clock;
        this.sqsTemplate = sqsTemplate;
        this.emailQueueNameOrUrl = emailQueueNameOrUrl;
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




    @Async(value = "emailExecutor")
    public void publishOne(Long id) {
        OutboxEvent e = repo.findById(id).orElseThrow();

        if (e.getStatus() != OutboxEvent.Status.PROCESSING) {
            return;
        }

        try {
            String payloadJson = e.getPayload().toString();

            sqsTemplate.send(to -> to
                    .queue(emailQueueNameOrUrl)
                    .payload(payloadJson)
                    .header("eventType", e.getEventType())
                    .header("outboxId", String.valueOf(e.getId()))
            );

            markDone(id);
            log.info("Outbox {} published to SQS queue={}", id, emailQueueNameOrUrl);

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
        e.setNextAttemptAt(dead ? null : nextRetryTime(e.getAttempts(), Instant.now(clock)));
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
