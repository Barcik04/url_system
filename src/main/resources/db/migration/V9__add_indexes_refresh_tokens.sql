CREATE INDEX idx_revoked_at
ON refresh_tokens (revoked_at);

CREATE INDEX idx_expires_at
ON refresh_tokens (expires_at);

CREATE UNIQUE INDEX idx_token_hash
ON refresh_tokens (token_hash);

