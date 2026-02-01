ALTER TABLE outbox_events
    ALTER COLUMN next_attempt_at DROP NOT NULL;
