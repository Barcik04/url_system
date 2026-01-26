CREATE UNIQUE INDEX idx_urls_code
ON urls (code);

CREATE INDEX idx_urls_user_id
ON urls (user_id);

CREATE UNIQUE INDEX idx_users_username
ON users (username)