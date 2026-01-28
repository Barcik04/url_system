package com.example.url_system.repositories;

import com.example.url_system.models.IdempotencyKeys;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeys, Long> {
    Optional<IdempotencyKeys> findByOperationAndIdempotencyKey(String operation, String idempotencyKey);
}
