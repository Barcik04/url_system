package com.example.url_system.models;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.NEW;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private Instant nextAttemptAt = Instant.EPOCH;

    @Column(columnDefinition = "text")
    private String lastError;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();



    public enum Status { NEW, PROCESSING, DONE, FAILED, DEAD }

    public OutboxEvent() {
    }

    public OutboxEvent(String eventType, String payload, Status status, String lastError) {
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.lastError = lastError;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public void setNextAttemptAt(Instant nextAttemptAt) {
        this.nextAttemptAt = nextAttemptAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
