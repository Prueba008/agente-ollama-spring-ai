# SPEC — Agente local con Java 25, Spring Boot, Spring AI y Ollama

## 1. Propósito

Construir una implementación de referencia, ejecutable de forma local y sin proveedores externos, para un agente de IA robusto basado en:

- Java 25.
- Spring Boot 4.0.x.
- Spring AI 2.0.x.
- Ollama local para chat y embeddings.
- Maven multimódulo.
- Arquitectura hexagonal.

La solución debe demostrar `ChatClient`, tool calling, Structured Outputs, RAG, memoria conversacional, guardrails y evaluaciones automatizadas.

## 2. Objetivo funcional

El agente responde consultas técnicas usando documentación local, conserva contexto por conversación, puede invocar herramientas autorizadas y devuelve una respuesta estructurada y auditable.

### Criterio de éxito

Una ejecución se considera exitosa cuando:

1. La solicitud supera los guardrails de entrada.
2. Se recupera contexto documental relevante cuando corresponde.
3. Las herramientas se ejecutan únicamente si están autorizadas.
4. La respuesta cumple el contrato JSON.
5. La respuesta supera las validaciones de salida.
6. Se registran trazas, latencia y metadatos de ejecución.
7. Los evaluadores alcanzan los umbrales configurados.

## 3. Alcance

### Incluido

- API REST síncrona.
- Conversaciones identificadas mediante `conversationId`.
- Modelo local de chat en Ollama.
- Modelo local de embeddings en Ollama.
- Tool calling con herramientas Java declarativas.
- RAG sobre documentos locales.
- Memoria de ventana por conversación.
- Salida tipada mediante records Java.
- Guardrails determinísticos de entrada y salida.
- Evaluación de relevancia y fidelidad.
- Actuator y observabilidad básica.
- Pruebas unitarias, de arquitectura y de integración opcionales.

### Fuera de alcance inicial

- Multiagente.
- Ejecución distribuida.
- Persistencia productiva de memoria.
- Autorización OAuth2 completa.
- Vector database externa.
- UI web.
- MCP.

## 4. Casos de uso

### CU-01 — Consultar al agente

**Entrada:** `conversationId`, pregunta y metadatos opcionales.

**Flujo:**

1. Validar entrada.
2. Aplicar guardrails.
3. Recuperar memoria.
4. Recuperar documentos relevantes.
5. Construir prompt.
6. Permitir tools autorizadas.
7. Ejecutar el agent loop.
8. Convertir la salida al esquema requerido.
9. Evaluar relevancia y fidelidad.
10. Persistir memoria y auditoría.
11. Responder.

### CU-02 — Ingerir documentación

**Entrada:** texto, nombre de fuente y metadatos.

**Flujo:** dividir, enriquecer, generar embeddings y almacenar en el `VectorStore`.

### CU-03 — Consultar estado

Exponer health, readiness y métricas técnicas mediante Actuator.

## 5. Contratos

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
    {"name": "arquitectura.md", "reference": "chunk-17"}
  ],
  "toolsUsed": [],
  "grounded": true,
  "confidence": 0.87,
  "warnings": []
}
```

## 6. Arquitectura

```text
agent-api
   │
   ▼
agent-application ─────► agent-domain
   ▲
   │
agent-infrastructure
   ├── Spring AI ChatClient
   ├── Ollama chat/embeddings
   ├── VectorStore / RAG
   ├── ChatMemory
   ├── Tools
   ├── Guardrails
   └── Evaluators
```

### Regla de dependencias

- `agent-domain`: no depende de Spring.
- `agent-application`: depende únicamente de dominio.
- `agent-infrastructure`: implementa puertos de aplicación.
- `agent-api`: expone REST y ensambla la aplicación.

## 7. Módulos Maven

| Módulo | Responsabilidad |
|---|---|
| `agent-domain` | Modelos, reglas y excepciones independientes del framework. |
| `agent-application` | Casos de uso y puertos de entrada/salida. |
| `agent-infrastructure` | Adaptadores Spring AI, Ollama, RAG, memoria, tools y guardrails. |
| `agent-api` | Aplicación Spring Boot, controladores, configuración y Actuator. |

## 8. Modelos Ollama

Valores iniciales configurables:

- Chat y tool calling: `qwen3:8b`.
- Embeddings: `nomic-embed-text`.
- Evaluador opcional: el mismo modelo de chat; en producción local se recomienda separar el juez.

Comandos:

```bash
ollama pull qwen3:8b
ollama pull nomic-embed-text
ollama serve
```

El modelo seleccionado debe soportar adecuadamente tool calling. El nombre se externaliza en variables de entorno.

## 9. Agent loop

El loop debe imponer:

- Máximo de 8 iteraciones.
- Timeout total configurable.
- Lista blanca de tools.
- Registro de cada invocación.
- Finalización ante respuesta válida o error no recuperable.
- Ausencia de acciones destructivas en la POC.

## 10. Tool calling

Herramienta inicial de ejemplo:

- `getSystemTime`: devuelve fecha y hora del servidor.

Reglas:

- Parámetros validados.
- Resultados serializables.
- Sin acceso arbitrario al sistema operativo.
- Sin ejecución de comandos suministrados por el usuario.
- Toda tool debe declarar propósito y alcance.

## 11. Structured Outputs

La respuesta final se mapea a `AgentAnswer`.

Validaciones:

- `answer` obligatorio y no vacío.
- `confidence` entre 0 y 1.
- `sources` sin valores nulos.
- `grounded=false` cuando no existe evidencia suficiente.
- `warnings` para incertidumbre o información incompleta.

La validación Java es obligatoria aunque el proveedor soporte JSON Schema nativo.

## 12. RAG

Pipeline inicial:

1. Ingesta de texto.
2. Chunking con solapamiento.
3. Metadatos de fuente.
4. Embeddings con Ollama.
5. Persistencia en `SimpleVectorStore` para desarrollo.
6. Recuperación top-k.
7. Inyección mediante Advisor.
8. Citas en la respuesta.

Evolución productiva sugerida: PostgreSQL + PGVector.

## 13. Memoria

- Estrategia inicial: ventana de mensajes.
- Clave: `conversationId`.
- Repositorio inicial: memoria local.
- No almacenar secretos ni datos sensibles.
- La memoria no reemplaza el historial de auditoría.

## 14. Guardrails

### Entrada

Bloquear o marcar:

- Prompt injection evidente.
- Solicitudes de revelar system prompts.
- Solicitudes de ejecutar comandos arbitrarios.
- Entrada vacía o excesivamente larga.

### Salida

Validar:

- Esquema.
- Ausencia de secretos/configuración.
- Referencias documentales coherentes.
- Respuesta no vacía.
- Umbral mínimo de confianza.

Las reglas críticas se implementan en Java, no sólo en prompts.

## 15. Evaluadores

Métricas mínimas:

- Relevancia de la respuesta.
- Fidelidad respecto del contexto recuperado.
- Cumplimiento del formato.
- Uso correcto de herramientas.
- Latencia total.

Umbrales iniciales:

- Relevancia: `>= 0.70`.
- Fidelidad: `>= 0.75`.
- Formato válido: `100%`.

Los tests que requieren Ollama se etiquetan como integración y pueden deshabilitarse en CI sin modelo local.

## 16. Observabilidad

Registrar sin exponer contenido sensible:

- `conversationId` anonimizado.
- Modelo.
- Duración.
- Tool calls.
- Cantidad de documentos recuperados.
- Resultado de evaluaciones.
- Errores y reintentos.

No registrar prompts completos en producción por defecto.

## 17. Configuración

Variables principales:

| Variable | Default |
|---|---|
| `OLLAMA_BASE_URL` | `http://localhost:11434` |
| `OLLAMA_CHAT_MODEL` | `qwen3:8b` |
| `OLLAMA_EMBEDDING_MODEL` | `nomic-embed-text` |
| `AGENT_MAX_INPUT_CHARS` | `12000` |
| `AGENT_RAG_TOP_K` | `4` |
| `AGENT_MIN_SIMILARITY` | `0.65` |

## 18. Pruebas

- Unitarias para dominio y guardrails.
- Contrato para Structured Outputs.
- Integración con Ollama local.
- RAG con corpus controlado.
- Evaluación de respuestas respaldadas/no respaldadas.
- ArchUnit para validar dependencias entre capas.

## 19. Criterios de aceptación

- `mvn clean verify` compila todos los módulos.
- La API inicia sin claves externas.
- `/actuator/health` responde `UP` cuando Ollama está disponible.
- `/api/v1/agent/chat` devuelve un `AgentAnswer` válido.
- La misma conversación conserva contexto.
- Las preguntas documentales recuperan fuentes.
- Una entrada con prompt injection es rechazada.
- La salida inválida no se entrega sin validación.

## 20. Evolución

1. Reemplazar almacenamiento local por PGVector.
2. Persistir memoria mediante JDBC.
3. Añadir autenticación y autorización por tool.
4. Incorporar retry, circuit breaker y rate limiting.
5. Añadir evaluación offline versionada.
6. Incorporar MCP sólo cuando existan integraciones reutilizables.
