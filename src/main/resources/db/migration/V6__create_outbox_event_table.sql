CREATE TABLE outbox_events (
                               id BIGSERIAL PRIMARY KEY,

                               event_type VARCHAR(100) NOT NULL,

                               payload JSONB NOT NULL,

                               status VARCHAR(20) NOT NULL,

                               attempts INTEGER NOT NULL DEFAULT 0,

                               next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT '1970-01-01T00:00:00Z',

                               last_error TEXT,

                               created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
