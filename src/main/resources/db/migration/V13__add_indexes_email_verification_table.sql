CREATE UNIQUE INDEX idx_token_hash
ON email_verification_tokens (token_hash);

CREATE INDEX idx_expires_at
ON email_verification_tokens (expires_at);

CREATE UNIQUE INDEX idx_user_id
ON email_verification_tokens (user_id)