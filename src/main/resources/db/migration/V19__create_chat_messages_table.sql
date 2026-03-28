CREATE TABLE chat_messages (
                               id BIGSERIAL PRIMARY KEY,
                               sender_type VARCHAR(50) NOT NULL,
                               content TEXT NOT NULL,
                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                               conversation_id BIGINT NOT NULL,

                               CONSTRAINT fk_chat_messages_conversation
                                   FOREIGN KEY (conversation_id) REFERENCES chat_conversations(id) ON DELETE CASCADE
);