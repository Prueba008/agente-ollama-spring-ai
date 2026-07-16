-- DESTRUCTIVO: usar sólo después de exportar/reingestar los documentos.
-- Cambiar 768 por la dimensión exacta del nuevo modelo de embeddings.
BEGIN;
DROP TABLE IF EXISTS vector_store;
CREATE TABLE vector_store (
    id uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
    content text NOT NULL,
    metadata json,
    embedding vector(768) NOT NULL
);
CREATE INDEX vector_store_embedding_hnsw_idx
    ON vector_store USING hnsw (embedding vector_cosine_ops);
CREATE INDEX vector_store_metadata_gin_idx ON vector_store USING gin (metadata);
COMMIT;
