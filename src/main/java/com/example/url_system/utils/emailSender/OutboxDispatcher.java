//package com.example.url_system.utils.emailSender;
//
//import com.example.url_system.models.OutboxEvent;
//import com.example.url_system.repositories.OutboxEventRepository;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.Clock;
//import java.time.Instant;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class OutboxDispatcher {
//
//    private final OutboxEventRepository repo;
//    private final Clock clock;
//    private final Map<String, OutboxHandler> handlersByType;
//
//    public OutboxDispatcher(OutboxEventRepository repo, Clock clock, List<OutboxHandler> handlers) {
//        this.repo = repo;
//        this.clock = clock;
//        this.handlersByType = handlers.stream()
//                .collect(java.util.stream.Collectors.toMap(OutboxHandler::eventType, h -> h));
//    }
//
//    @Scheduled(fixedDelayString = "PT2S") // every 2s
//    public void tick() {
//        List<Long> ids = claimBatch(50);
//        for (Long id : ids) {
//            OutboxEvent e = repo.findById(id).orElseThrow();
//            OutboxHandler h = handlersByType.get(e.getEventType());
//
//            if (h == null) {
//                markDead(id, "No handler for eventType=" + e.getEventType());
//                continue;
//            }
//
//            h.handle(id);
//        }
//    }
//
//    @Transactional
//    public List<Long> claimBatch(int limit) {
//        Instant now = Instant.now(clock);
//
//        List<OutboxEvent> due = repo.findDueForUpdateSkipLocked(now, limit);
//        for (OutboxEvent e : due) {
//            e.setStatus(OutboxEvent.Status.PROCESSING);
//            e.setAttempts(e.getAttempts() + 1);
//        }
//        return due.stream().map(OutboxEvent::getId).toList();
//    }
//
//    @Transactional
//    public void markDead(Long id, String error) {
//        OutboxEvent e = repo.findById(id).orElseThrow();
//        e.setStatus(OutboxEvent.Status.DEAD);
//        e.setLastError(error);
//    }
//}
//
