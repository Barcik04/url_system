package com.example.url_system.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKeys {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 64, nullable = false)
    private String operation;

    @Column(length = 128, nullable = false, name = "idempotency_key")
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "idempotency_status")
    private IdempotencyStatus idempotencyStatus = IdempotencyStatus.IN_PROGRESS;

    @Column(name = "created_url_id")
    private Long createdUrlId;

    @Future(message = "expiry date has to be in the future")
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;


    public IdempotencyKeys() {
    }

    public IdempotencyKeys(String operation, String idempotencyKey) {
        this.operation = operation;
        this.idempotencyKey = idempotencyKey;
        this.expiresAt = Instant.now().plus(24, ChronoUnit.HOURS);
    }


    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public IdempotencyStatus getIdempotencyStatus() {
        return idempotencyStatus;
    }

    public void setIdempotencyStatus(IdempotencyStatus idempotencyStatus) {
        this.idempotencyStatus = idempotencyStatus;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCreatedUrlId() {
        return createdUrlId;
    }

    public void setCreatedUrlId(Long createdUrlId) {
        this.createdUrlId = createdUrlId;
    }
}
