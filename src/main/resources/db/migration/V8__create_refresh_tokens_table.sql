CREATE TABLE refresh_tokens (
                                id          BIGSERIAL PRIMARY KEY,
                                user_id     BIGINT NOT NULL,
                                token_hash  VARCHAR(255) NOT NULL,
                                expires_at  TIMESTAMPTZ NOT NULL,
                                revoked_at  TIMESTAMPTZ NULL,
                                created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

                                CONSTRAINT user_token_id_fk
                                    FOREIGN KEY (user_id)
                                        REFERENCES users (id)
                                        ON DELETE CASCADE,

                                CONSTRAINT uq_refresh_tokens_token_hash
                                    UNIQUE (token_hash)
);

