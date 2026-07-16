CREATE TABLE IF NOT EXISTS vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text NOT NULL,
    metadata json,
    embedding vector(768) NOT NULL
);

COMMENT ON TABLE vector_store IS 'Chunks documentales y embeddings generados por Ollama';
COMMENT ON COLUMN vector_store.embedding IS 'Vector de 768 dimensiones de nomic-embed-text';
