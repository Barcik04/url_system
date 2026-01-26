CREATE TABLE
    urls (
                      id BIGSERIAL PRIMARY KEY,

                      code VARCHAR(16) NOT NULL,
                      long_url VARCHAR(2048) NOT NULL,

                      created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                      expires_at TIMESTAMPTZ NULL,

                      clicks BIGINT NOT NULL DEFAULT 0,

                      CONSTRAINT uk_urls_code UNIQUE (code)
);