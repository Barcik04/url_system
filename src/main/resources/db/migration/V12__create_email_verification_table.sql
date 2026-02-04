CREATE TABLE email_verification_tokens (
                                           id BIGSERIAL PRIMARY KEY,

                                           user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

                                           token_hash VARCHAR(64) NOT NULL, -- sha256 hex = 64 chars

                                           created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                           expires_at TIMESTAMPTZ NOT NULL,
                                           used_at TIMESTAMPTZ NULL
);
