# Agente Ollama + Spring AI

Proyecto Maven multimódulo de referencia para Java 25, Spring Boot 4 y Spring AI 2 con inferencia local en Ollama.

## Requisitos

- JDK 25
- Maven 3.9+
- Ollama

## Preparación

```bash
ollama pull qwen3:8b
ollama pull nomic-embed-text
ollama serve
```

## Compilar

```bash
mvn clean verify
```

## Ejecutar

```bash
mvn -pl agent-api -am spring-boot:run
```

## Probar

```bash
curl -X POST http://localhost:8080/api/v1/agent/chat \
  -H 'Content-Type: application/json' \
  -d '{"conversationId":"demo-1","question":"¿Qué hora tiene el servidor?"}'
```

Ingerir documentación local antes de consultar:

```bash
curl -X POST http://localhost:8080/api/v1/agent/documents \
  -H 'Content-Type: application/json' \
  -d '{"sourceName":"arquitectura.md","content":"El patrón outbox persiste el evento junto con la transacción de negocio."}'
```

Las pruebas determinísticas se ejecutan siempre. Las pruebas reales con Ollama se habilitan mediante el perfil Maven `ollama-it`.

## Pruebas de alucinación e integración

El CI determinístico ejecuta RAG, guardrails, validación de fuentes y métricas de fidelidad, riesgo de alucinación y certeza:

```bash
mvn clean verify
```

La integración real con Ollama se ejecuta de forma explícita mediante Maven Failsafe:

```bash
OLLAMA_BASE_URL=http://localhost:11434 OLLAMA_CHAT_MODEL=qwen3:8b \
  mvn -Pollama-it clean verify
```

La definición de métricas, bandas de riesgo, casos y limitaciones está en
[SPEC-HALLUCINATION-TESTS.md](SPEC-HALLUCINATION-TESTS.md).

## RAG vectorial persistente

El RAG productivo utiliza Ollama `nomic-embed-text` para embeddings y PostgreSQL con `pgvector`, índice HNSW y distancia coseno. El chunking, la indexación y la búsqueda están desacoplados mediante puertos de aplicación.

```bash
docker compose up -d postgres ollama
ollama pull nomic-embed-text
mvn -pl agent-api -am spring-boot:run
```

La creación de extensiones, tabla e índices está versionada en `scripts/`. La migración completa desde la implementación en memoria se detalla en [docs/MIGRACION-RAG-PGVECTOR.md](docs/MIGRACION-RAG-PGVECTOR.md).

Referencias oficiales: [Spring AI PGvector](https://docs.spring.io/spring-ai/reference/api/vectordbs/pgvector.html), [Ollama Embeddings](https://docs.spring.io/spring-ai/reference/api/embeddings/ollama-embeddings.html), [Spring AI ETL](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html) y [pgvector](https://github.com/pgvector/pgvector).

La especificación completa está en [SPEC.md](SPEC.md).

La ficha técnica y el estado del proyecto están disponibles en
[docs/Proyecto_Agente_Ollama_Spring_AI_Java25.docx](docs/Proyecto_Agente_Ollama_Spring_AI_Java25.docx).
