ALTER TABLE urls
    ADD COLUMN user_id BIGINT;

ALTER TABLE urls
    ADD CONSTRAINT url_user_id_fk
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE SET NULL;
