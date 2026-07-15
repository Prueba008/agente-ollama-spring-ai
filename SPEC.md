# SPEC — Agente local con Java 25, Spring Boot, Spring AI y Ollama

## 1. Propósito

Construir una implementación de referencia, ejecutable localmente y sin dependencia obligatoria de proveedores externos, para un agente de IA robusto basado en:

- Java 25.
- Spring Boot 4.0.x.
- Spring AI 2.0.x.
- Ollama local.
- Maven multimódulo.
- Arquitectura hexagonal.

La solución debe demostrar integración entre `ChatClient`, tool calling, Structured Outputs, RAG, memoria conversacional, guardrails, evaluadores y observabilidad.

## 2. Objetivo funcional

El agente debe responder consultas técnicas utilizando contexto documental, conservar información por conversación, invocar únicamente herramientas autorizadas y devolver una respuesta estructurada, validada y auditable.

Una ejecución se considera exitosa cuando:

1. La entrada supera las validaciones y guardrails.
2. Se recupera contexto documental cuando corresponde.
3. Las tools se ejecutan con parámetros válidos y dentro de una lista blanca.
4. La respuesta cumple el contrato `AgentAnswer`.
5. La salida supera las validaciones determinísticas.
6. La ejecución registra latencia, tools utilizadas y resultado de evaluaciones.

## 3. Alcance

### Incluido

- API REST síncrona.
- Conversaciones identificadas mediante `conversationId`.
- Inferencia local con Ollama.
- Tool calling con métodos Java declarativos.
- RAG básico sobre documentos locales.
- Memoria conversacional en memoria.
- Structured Outputs mediante records Java.
- Guardrails de entrada y salida.
- Evaluación de relevancia y fidelidad.
- Actuator y observabilidad básica.
- Tests unitarios y de integración desacoplados.
- Integración continua con GitHub Actions y JDK 25.

### Fuera de alcance inicial

- Multiagente.
- Ejecución distribuida.
- Persistencia productiva de memoria.
- OAuth2 completo.
- Vector database externa.
- Interfaz web.
- MCP.
- Ejecución de comandos arbitrarios del sistema operativo.

## 4. Arquitectura

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
   ├── RAG
   ├── ChatMemory
   ├── Tools
   ├── Guardrails
   └── Evaluadores
```

### Regla de dependencias

- `agent-domain` no depende de Spring.
- `agent-application` depende únicamente del dominio.
- `agent-infrastructure` implementa los puertos de aplicación.
- `agent-api` expone REST y ensambla los componentes.

## 5. Módulos Maven

| Módulo | Responsabilidad |
|---|---|
| `agent-domain` | Modelos, reglas y excepciones independientes del framework. |
| `agent-application` | Casos de uso y puertos de entrada/salida. |
| `agent-infrastructure` | Adaptadores Spring AI, Ollama, RAG, memoria, tools y guardrails. |
| `agent-api` | Aplicación Spring Boot, controladores, configuración y Actuator. |

## 6. Requisitos de plataforma

- JDK 25.
- Maven 3.9 o superior.
- Ollama instalado localmente.
- Modelo de chat configurable mediante variable de entorno.

El `pom.xml` padre debe:

- establecer `java.version=25`;
- establecer `maven.compiler.release=25`;
- ejecutar Maven Enforcer para exigir Java `[25,26)`;
- exigir Maven `3.9+`;
- activar warnings de compilación;
- configurar Surefire para Java 25.

## 7. Casos de uso

### CU-01 — Consultar al agente

**Entrada:** `conversationId`, pregunta y metadatos opcionales.

**Flujo:**

1. Validar la solicitud.
2. Ejecutar guardrails de entrada.
3. Recuperar memoria conversacional.
4. Recuperar documentos relevantes.
5. Construir el contexto del modelo.
6. Habilitar únicamente tools autorizadas.
7. Ejecutar el ciclo del agente.
8. Mapear la salida a `AgentAnswer`.
9. Ejecutar guardrails de salida.
10. Evaluar relevancia y fidelidad.
11. Actualizar memoria y auditoría.
12. Responder al cliente.

### CU-02 — Ingerir documentación

**Entrada:** texto, nombre de fuente y metadatos.

**Flujo inicial:**

1. Validar contenido y fuente.
2. Normalizar el texto.
3. Asociar metadatos.
4. Almacenar el documento en el repositorio RAG local.

### CU-03 — Consultar estado

Exponer health, readiness y métricas mediante Spring Boot Actuator.

## 8. Contratos

### Solicitud

```json
{
  "conversationId": "conv-001",
  "question": "¿Cómo se aplica el patrón outbox?"
}
```

### Respuesta estructurada

```json
{
  "answer": "...",
  "sources": [
    {
      "name": "arquitectura.md",
      "reference": "chunk-17"
    }
  ],
  "toolsUsed": [],
  "grounded": true,
  "confidence": 0.87,
  "warnings": []
}
```

### Reglas de `AgentAnswer`

- `answer` es obligatorio y no puede estar vacío.
- `confidence` debe estar entre `0` y `1`.
- `sources`, `toolsUsed` y `warnings` nunca son nulos.
- Las colecciones expuestas deben ser inmutables.
- `grounded=true` requiere evidencia identificable.
- La salida se valida en Java aunque el modelo genere JSON estructurado.

## 9. Configuración de Ollama

Modelos iniciales:

- Chat y tool calling: `qwen3:8b`.
- Embeddings: `nomic-embed-text`.
- Evaluador: reutilización del modelo de chat en la POC.

Preparación:

```bash
ollama pull qwen3:8b
ollama pull nomic-embed-text
ollama serve
```

Variables principales:

| Variable | Default |
|---|---|
| `OLLAMA_BASE_URL` | `http://localhost:11434` |
| `OLLAMA_CHAT_MODEL` | `qwen3:8b` |
| `AGENT_MAX_INPUT_CHARS` | `12000` |
| `AGENT_RAG_TOP_K` | `4` |

## 10. Agent loop

El ciclo de orquestación debe imponer:

- máximo de 8 iteraciones;
- timeout total configurable;
- lista blanca de tools;
- registro de cada invocación;
- finalización ante respuesta válida;
- cancelación ante error no recuperable;
- ausencia de acciones destructivas en la POC.

## 11. Tool calling

Tool de referencia:

- `getSystemTime`: devuelve fecha y hora para una zona horaria solicitada.

Reglas:

- parámetros validados;
- salida serializable;
- implementación testeable mediante `Clock` inyectable;
- rechazo de zonas horarias inválidas;
- sin acceso arbitrario al sistema operativo;
- sin ejecución de comandos aportados por el usuario.

## 12. RAG

Pipeline inicial:

1. Ingesta de texto.
2. Normalización.
3. Asociación de fuente y metadatos.
4. Almacenamiento local.
5. Recuperación limitada por `top-k`.
6. Inyección del contexto mediante Spring AI.
7. Inclusión de referencias en la respuesta.

Evolución productiva:

- PGVector u otro `VectorStore` persistente;
- búsqueda semántica e híbrida;
- reranking;
- versionado de fuentes;
- filtros por metadatos;
- evaluación de recuperación.

## 13. Memoria

- Clave: `conversationId`.
- Estrategia inicial: ventana de mensajes en memoria.
- No almacenar secretos ni credenciales.
- La memoria no reemplaza la auditoría.
- La memoria debe poder sustituirse por JDBC o Redis sin modificar el dominio.

## 14. Guardrails

### Entrada

Rechazar o marcar:

- pregunta vacía;
- longitud superior al máximo configurado;
- prompt injection evidente;
- solicitudes de revelar system prompts;
- intentos de ejecutar comandos arbitrarios.

### Salida

Validar:

- contrato estructurado;
- respuesta no vacía;
- confianza válida;
- ausencia de secretos y configuración interna;
- coherencia entre fuentes y `grounded`;
- advertencias cuando exista evidencia insuficiente.

Las reglas críticas deben implementarse en Java y no depender únicamente del prompt.

## 15. Evaluadores

Métricas mínimas:

- relevancia;
- fidelidad respecto del contexto;
- cumplimiento del formato;
- uso correcto de tools;
- latencia total.

Umbrales iniciales:

- relevancia `>= 0.70`;
- fidelidad `>= 0.75`;
- formato válido `100%`.

Los evaluadores basados en LLM complementan, pero no sustituyen, las validaciones determinísticas.

## 16. Observabilidad

Registrar sin exponer información sensible:

- identificador de conversación anonimizado;
- modelo utilizado;
- duración total;
- tools invocadas;
- cantidad de documentos recuperados;
- resultados de evaluaciones;
- errores y reintentos.

No registrar prompts completos en producción por defecto.

## 17. Pruebas

La estrategia mínima incluye:

- tests del dominio;
- tests del contrato `AgentAnswer`;
- tests de colecciones inmutables;
- tests de confianza fuera de rango;
- tests de guardrails de entrada y salida;
- tests del orden de ejecución del caso de uso;
- tests determinísticos de tools mediante `Clock`;
- tests de zonas horarias inválidas;
- tests de arquitectura;
- tests de integración con Ollama separados de la suite unitaria.

Comando obligatorio:

```bash
mvn clean verify
```

## 18. Integración continua

GitHub Actions debe:

- usar Temurin JDK 25;
- cachear dependencias Maven;
- ejecutar `mvn clean verify`;
- activarse en push y pull request hacia ramas principales;
- no requerir Ollama para la suite unitaria.

Los tests de integración con Ollama se ejecutarán en un job separado cuando el runner disponga del modelo local.

## 19. Criterios de aceptación

- El reactor Maven compila con Java 25.
- Java inferior a 25 es rechazado por Maven Enforcer.
- `mvn clean verify` ejecuta todos los tests unitarios.
- La API inicia sin claves de proveedores externos.
- `/actuator/health` expone el estado de la aplicación.
- `/api/v1/agent/chat` devuelve un `AgentAnswer` válido.
- La misma conversación conserva contexto.
- Las consultas documentales pueden incluir fuentes.
- Una entrada con prompt injection básica es rechazada.
- Una salida inválida no se entrega al cliente.
- Las tools rechazan parámetros inválidos.

## 20. Evolución

1. Incorporar un `VectorStore` real.
2. Añadir búsqueda híbrida y reranking.
3. Persistir memoria con JDBC o Redis.
4. Añadir autenticación y autorización por tool.
5. Incorporar retry, circuit breaker y rate limiting.
6. Versionar datasets de evaluación.
7. Añadir trazabilidad distribuida con OpenTelemetry.
8. Incorporar MCP únicamente cuando existan integraciones reutilizables.
