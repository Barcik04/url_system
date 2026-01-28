CREATE TABLE idempotency_keys (
    id BIGSERIAL PRIMARY KEY,

    operation VARCHAR(64) NOT NULL,

    idempotency_key VARCHAR(128) NOT NULL,

    idempotency_status VARCHAR(32) NOT NULL,

    created_url_id BIGINT,

    expires_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_idempotency_op_key UNIQUE (operation, idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_idempotency_expires_at
ON idempotency_keys (expires_at)

