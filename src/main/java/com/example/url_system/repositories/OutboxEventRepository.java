package com.example.url_system.repositories;

import com.example.url_system.models.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Query(value = """
        SELECT * FROM outbox_events
        WHERE status IN ('NEW','FAILED')
          AND next_attempt_at <= :now
        ORDER BY next_attempt_at
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
        """, nativeQuery = true)
    List<OutboxEvent> findDueForUpdateSkipLocked(@Param("now") Instant now,
                                                 @Param("limit") int limit);
}

