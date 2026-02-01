package com.example.url_system.repositories;

import com.example.url_system.models.IdempotencyKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeys, Long> {
    Optional<IdempotencyKeys> findByOperationAndIdempotencyKey(String operation, String idempotencyKey);

    @Modifying
    @Transactional
    @Query("""
        delete FROM IdempotencyKeys u
        WHERE u.expiresAt is not null
              and u.expiresAt < :now
        """)
    void deleteExpired(Instant now);
}
