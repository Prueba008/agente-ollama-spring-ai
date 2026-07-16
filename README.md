# Agente Ollama + Spring AI

Proyecto Maven multimódulo de referencia para Java 21 LTS, Spring Boot 4 y Spring AI 2 con inferencia local en Ollama.

## Requisitos

- JDK 21
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

Las pruebas determinísticas se ejecutan siempre. Las pruebas que requieren Ollama sólo se habilitan al definir `OLLAMA_BASE_URL`.

La especificación completa está en [SPEC.md](SPEC.md).
