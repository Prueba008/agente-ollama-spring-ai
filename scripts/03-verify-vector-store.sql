SELECT extname, extversion FROM pg_extension WHERE extname IN ('vector', 'hstore', 'uuid-ossp');
SELECT format_type(a.atttypid, a.atttypmod) AS embedding_type
FROM pg_attribute a
WHERE a.attrelid = 'vector_store'::regclass AND a.attname = 'embedding';
SELECT indexname, indexdef FROM pg_indexes WHERE tablename = 'vector_store';
SELECT count(*) AS indexed_chunks FROM vector_store;
