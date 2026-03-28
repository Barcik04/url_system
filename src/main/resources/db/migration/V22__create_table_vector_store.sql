CREATE TABLE IF NOT EXISTS vector_store (
                                            id UUID PRIMARY KEY,
                                            content TEXT,
                                            metadata JSONB,
                                            embedding VECTOR(4096)
);