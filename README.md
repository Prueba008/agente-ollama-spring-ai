# Agente Ollama + Spring AI

Implementación de referencia para construir un agente de IA local y robusto con **Java 25**, **Spring Boot 4**, **Spring AI 2** y **Ollama**, organizada como proyecto Maven multimódulo con arquitectura hexagonal.

La solución incluye `ChatClient`, tool calling, respuestas estructuradas, memoria conversacional, RAG básico, guardrails determinísticos, evaluaciones y observabilidad.

## Stack

- Java 25.
- Maven 3.9 o superior.
- Spring Boot 4.0.x.
- Spring AI 2.0.x.
- Ollama local.
- JUnit 5.
- Maven Enforcer y Surefire.
- GitHub Actions con Temurin JDK 25.

## Arquitectura

```text
agent-api
   │
   ▼
agent-application ─────► agent-domain
   ▲
   │
agent-infrastructure
   ├── Spring AI ChatClient
   ├── Ollama
   ├── Tools
   ├── Memoria
   ├── RAG
   ├── Guardrails
   └── Evaluadores
```

### Módulos

| Módulo | Responsabilidad |
|---|---|
| `agent-domain` | Modelos, contratos y reglas independientes de Spring. |
| `agent-application` | Casos de uso y puertos de entrada/salida. |
| `agent-infrastructure` | Integración con Spring AI, Ollama, tools, RAG, memoria y guardrails. |
| `agent-api` | Aplicación Spring Boot, API REST, configuración y Actuator. |

## Requisitos

Verificar las versiones instaladas:

```bash
java -version
mvn -version
ollama --version
```

El build exige Java 25 mediante Maven Enforcer.

## Preparar Ollama

```bash
ollama pull qwen3:8b
ollama pull nomic-embed-text
ollama serve
```

Variables de entorno principales:

```bash
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_CHAT_MODEL=qwen3:8b
AGENT_MAX_INPUT_CHARS=12000
AGENT_RAG_TOP_K=4
```

## Compilar y ejecutar tests

```bash
mvn clean verify
```

Este comando valida:

- versión de Java y Maven;
- compilación de todos los módulos;
- tests unitarios;
- contratos de salida;
- guardrails;
- servicios de aplicación;
- tools determinísticas;
- reglas de arquitectura configuradas.

Los tests que requieren un modelo Ollama activo deben mantenerse separados de los tests unitarios y ejecutarse como integración.

## Ejecutar la aplicación

```bash
mvn -pl agent-api -am spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/actuator/health
```

Consulta al agente:

```bash
curl -X POST http://localhost:8080/api/v1/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "demo-001",
    "question": "Explica el patrón outbox y cita la evidencia recuperada"
  }'
```

Respuesta esperada:

```json
{
  "answer": "...",
  "sources": [],
  "toolsUsed": [],
  "grounded": false,
  "confidence": 0.8,
  "warnings": []
}
```

## Tool calling

La herramienta de referencia `getSystemTime`:

- recibe una zona horaria;
- valida sus parámetros;
- utiliza `Clock` para permitir tests determinísticos;
- no ejecuta comandos del sistema operativo;
- devuelve una salida serializable.

## Guardrails

La capa de entrada rechaza, entre otros casos:

- preguntas vacías;
- entradas que superan el máximo configurado;
- intentos básicos de prompt injection;
- solicitudes para revelar instrucciones internas.

La capa de salida valida:

- respuesta no vacía;
- confianza entre `0` y `1`;
- colecciones no nulas;
- ausencia de secretos o configuración interna;
- coherencia entre `grounded` y las fuentes disponibles.

## RAG y memoria

La POC utiliza almacenamiento local simple para demostrar el flujo completo. La evolución recomendada para producción es:

- PGVector o un `VectorStore` equivalente;
- búsqueda híbrida;
- reranking;
- metadatos de fuente y versión;
- memoria persistida mediante JDBC o Redis;
- separación entre memoria conversacional y auditoría.

## Integración continua

El workflow de GitHub Actions ejecuta `mvn clean verify` con Temurin JDK 25 en cada push y pull request hacia las ramas principales configuradas.

## Especificación

La definición funcional, técnica, criterios de aceptación y evolución se encuentran en [SPEC.md](SPEC.md).
