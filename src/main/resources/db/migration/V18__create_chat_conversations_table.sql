CREATE TABLE chat_conversations (
                                    id BIGSERIAL PRIMARY KEY,
                                    title VARCHAR(255),
                                    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                    user_id BIGINT NOT NULL,

                                    CONSTRAINT fk_chat_conversations_user
                                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);