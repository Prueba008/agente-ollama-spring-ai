# Migración del RAG en memoria a Ollama + PostgreSQL/pgvector

## Arquitectura resultante

El proceso queda dividido en cuatro responsabilidades:

1. `OverlappingTextChunker`: divide cada documento en fragmentos configurables y solapados.
2. `VectorRagService`: orquesta ingesta y recuperación sin depender de Spring AI ni PostgreSQL.
3. `PgVectorIndexAdapter`: traduce los chunks al contrato `VectorStore` y ejecuta búsquedas por similitud.
4. Ollama + `nomic-embed-text`: convierte cada chunk y cada consulta en un vector de 768 dimensiones.

PostgreSQL no genera embeddings por sí mismo. Se convierte en base vectorial al instalar la extensión `vector`, crear una columna `vector(768)` y un índice HNSW. Ollama genera los valores; pgvector los almacena y calcula vecinos por distancia coseno.

## Flujo de ingesta

```text
Documento → validación → chunking con overlap → Ollama embedding
          → INSERT en vector_store → índice HNSW
```

Cada chunk guarda `source`, número de chunk e identificador estable en metadata. El solapamiento evita perder relaciones que cruzan el límite entre fragmentos.

## Flujo de consulta

```text
Pregunta → Ollama embedding → búsqueda coseno HNSW → top-k + umbral
         → contexto recuperado → ChatClient → evaluadores/guardrails
```

## Puesta en marcha

```bash
docker compose up -d postgres ollama
ollama pull qwen3:8b
ollama pull nomic-embed-text
mvn -pl agent-api -am spring-boot:run
```

Los scripts de `scripts/` son ejecutados automáticamente por la imagen de PostgreSQL solamente al crear un volumen vacío. Para una base existente:

```bash
psql postgresql://agent:agent@localhost:5432/agentdb -f scripts/00-enable-extensions.sql
psql postgresql://agent:agent@localhost:5432/agentdb -f scripts/01-create-vector-store.sql
psql postgresql://agent:agent@localhost:5432/agentdb -f scripts/02-create-vector-index.sql
psql postgresql://agent:agent@localhost:5432/agentdb -f scripts/03-verify-vector-store.sql
```

## Migración desde la lista en memoria

La lista anterior era volátil y no contenía un historial durable exportable. La migración correcta consiste en reingerir las fuentes originales:

1. Respaldar los documentos `.md`/`.txt` originales.
2. Inicializar pgvector con los scripts.
3. Confirmar que `OLLAMA_EMBEDDING_MODEL=nomic-embed-text` y `EMBEDDING_DIMENSIONS=768` coinciden.
4. Iniciar la API.
5. Ejecutar `scripts/ingest-directory.sh <directorio>` o invocar `POST /api/v1/agent/documents`.
6. Comparar cantidad de chunks, consultas de control y fuentes recuperadas.
7. Recién entonces retirar el adaptador en memoria.

La reingesta es obligatoria al cambiar de modelo o dimensión: vectores producidos por modelos diferentes no son comparables. El script `04-recreate-for-new-dimensions.sql` recrea la tabla, pero es destructivo.

## Conversión de PostgreSQL a base vectorial

- `CREATE EXTENSION vector` registra el tipo `vector` y operadores de distancia.
- `embedding vector(768)` impone una dimensión uniforme.
- `vector_cosine_ops` define distancia coseno.
- HNSW acelera búsqueda aproximada y cambia velocidad por recall.
- El índice GIN permite filtrar metadata antes de combinarla con similitud.

Validar siempre dimensión, extensión, índices y cantidad de chunks con `03-verify-vector-store.sql`.

## Configuración

| Variable | Default |
|---|---|
| `POSTGRES_URL` | `jdbc:postgresql://localhost:5432/agentdb` |
| `POSTGRES_USER` | `agent` |
| `POSTGRES_PASSWORD` | `agent` |
| `OLLAMA_EMBEDDING_MODEL` | `nomic-embed-text` |
| `EMBEDDING_DIMENSIONS` | `768` |
| `AGENT_RAG_CHUNK_SIZE` | `1000` caracteres |
| `AGENT_RAG_CHUNK_OVERLAP` | `150` caracteres |
| `AGENT_RAG_TOP_K` | `4` |
| `AGENT_RAG_SIMILARITY_THRESHOLD` | `0.70` |

## Riesgos y validación

- Un chunk demasiado pequeño pierde contexto; uno demasiado grande diluye similitud.
- Un umbral alto produce falsos negativos; uno bajo inyecta ruido.
- HNSW es aproximado: medir recall contra búsqueda exacta antes de producción.
- Nunca mezclar embeddings de modelos o dimensiones diferentes.
- PostgreSQL persiste documentos RAG; la memoria conversacional sigue siendo un componente diferente y continúa en ventana local.
