CREATE INDEX idx_status_next_attempt_at
ON outbox_events (status, next_attempt_at)