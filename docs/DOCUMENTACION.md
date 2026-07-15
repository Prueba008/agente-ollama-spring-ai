# Documentación del Sistema - Agente IA con Spring AI y Ollama

## 1. Descripción General

El sistema es un agente de IA corporativo que opera localmente utilizando Java 25, Spring Boot 4.0.x, Spring AI 2.0.x y Ollama para modelos de chat y embeddings. Implementa una arquitectura hexagonal con separación clara de responsabilidades a través de módulos Maven.

## 2. Arquitectura de Módulos

### 2.1 agent-domain
**Responsabilidad:** Contiene los modelos de dominio, reglas de negocio y excepciones independientes del framework.

**Componentes principales:**
- `AgentQuestion`: Record que representa la pregunta del usuario con conversationId
- `AgentAnswer`: Record que representa la respuesta estructurada del agente
- `SourceReference`: Record que referencia fuentes documentales
- `GuardrailViolationException`: Excepción para violaciones de guardrails

**Características:**
- Sin dependencias de Spring
- Validaciones de negocio en los records
- Contratos de dominio puros

### 2.2 agent-application
**Responsabilidad:** Define los casos de uso y puertos de entrada/salida (arquitectura hexagonal).

**Componentes principales:**
- `AskAgentUseCase`: Puerto de entrada para consultar al agente
- `AskAgentService`: Implementación del caso de uso principal
- `AgentEnginePort`: Puerto de salida para ejecución del motor de IA
- `InputGuardrailPort`: Puerto de salida para validación de entrada
- `OutputGuardrailPort`: Puerto de salida para validación de salida
- `RagPort`: Puerto de salida para recuperación de información
- `EvaluatorPort`: Puerto de salida para evaluación de calidad

**Flujo principal:**
1. Validación de entrada con guardrails
2. Recuperación de contexto relevante con RAG
3. Ejecución del motor del agente
4. Validación de salida con guardrails
5. Evaluación de calidad de la respuesta
6. Logging de métricas y tiempos

### 2.3 agent-infrastructure
**Responsabilidad:** Implementa los adaptadores de infraestructura usando Spring AI y Ollama.

**Componentes principales:**
- `SpringAiAgentEngine`: Adaptador que implementa AgentEnginePort usando ChatClient de Spring AI
- `SimpleRagAdapter`: Implementación de RAG usando VectorStore
- `DeterministicInputGuardrail`: Validación determinista de entrada
- `DeterministicOutputGuardrail`: Validación determinista de salida
- `SimpleEvaluator`: Evaluador de calidad de respuestas
- `SystemTools`: Herramientas del sistema (ej: obtener hora)
- `AgentInfrastructureConfiguration`: Configuración de beans Spring

**Características:**
- Integración con Ollama para chat y embeddings
- Memoria conversacional con InMemoryChatMemory
- VectorStore simple para desarrollo
- Configuración externalizada

### 2.4 agent-api
**Responsabilidad:** Expone la API REST y ensambla la aplicación Spring Boot.

**Componentes principales:**
- `AgentApplication`: Clase principal de Spring Boot
- `AgentController`: Controlador REST con endpoint /api/v1/agent/chat
- `ApiExceptionHandler`: Manejo global de excepciones
- `application.yml`: Configuración de la aplicación

**Endpoints:**
- `POST /api/v1/agent/chat`: Consulta al agente
- Actuator endpoints: /actuator/health, /actuator/info, /actuator/metrics

## 3. Procesos del Sistema

### 3.1 Proceso Principal: Consulta al Agente

**Descripción:** Flujo completo desde que llega una pregunta hasta que se devuelve la respuesta.

**Pasos:**
1. **Recepción de la solicitud**
   - Controller recibe `ChatRequest` con conversationId y question
   - Validación de Jakarta Validation (@NotBlank, @Size)
   
2. **Validación de entrada (Guardrails)**
   - DeterministicInputGuardrail valida:
     - Pregunta no vacía
     - Longitud máxima (configurable, default 12,000 caracteres)
     - Detección de patrones bloqueados (prompt injection)
   
3. **Recuperación de contexto (RAG)**
   - SimpleRagAdapter recupera documentos relevantes
   - Configuración: top-k (default 4), min-similarity (default 0.65)
   - Usa VectorStore con embeddings de Ollama
   
4. **Ejecución del motor de IA**
   - SpringAiAgentEngine usa ChatClient de Spring AI
   - System prompt configurado para comportamiento corporativo
   - ChatMemory mantiene contexto por conversationId
   - SystemTools disponible para tool calling
   
5. **Validación de salida (Guardrails)**
   - DeterministicOutputGuardrail valida:
     - Respuesta no vacía
     - No exposición de configuración interna
     - Cumplimiento del esquema AgentAnswer
   
6. **Evaluación de calidad**
   - SimpleEvaluator calcula métricas:
     - Relevancia (target >= 0.70)
     - Fidelidad (target >= 0.75)
     - Formato válido (target 100%)
     - Confianza del modelo
   
7. **Respuesta al cliente**
   - AgentAnswer con estructura validada
   - Logging de métricas y tiempos
   - Manejo de excepciones global

### 3.2 Proceso de Ingesta de Documentación

**Descripción:** Proceso para agregar documentación al sistema RAG.

**Pasos:**
1. Recibir contenido del documento y nombre de fuente
2. Dividir en chunks con solapamiento
3. Generar embeddings usando Ollama
4. Almacenar en VectorStore con metadatos
5. Indexar para búsqueda semántica

**Nota:** Este proceso está preparado en la arquitectura pero requiere endpoint específico.

### 3.3 Proceso de Tool Calling

**Descripción:** El agente puede invocar herramientas cuando es necesario.

**Herramientas disponibles:**
- `getSystemTime`: Devuelve fecha/hora en zona horaria especificada

**Reglas:**
- Parámetros validados
- Resultados serializables
- Sin acceso arbitrario al sistema operativo
- Declaración explícita de propósito y alcance

## 4. Configuración

### 4.1 Variables de Entorno

| Variable | Default | Descripción |
|----------|---------|-------------|
| `OLLAMA_BASE_URL` | `http://localhost:11434` | URL del servidor Ollama |
| `OLLAMA_CHAT_MODEL` | `qwen3:8b` | Modelo para chat y tool calling |
| `OLLAMA_EMBEDDING_MODEL` | `nomic-embed-text` | Modelo para embeddings |
| `AGENT_MAX_INPUT_CHARS` | `12000` | Máximo caracteres de entrada |
| `AGENT_RAG_TOP_K` | `4` | Documentos a recuperar en RAG |
| `AGENT_MIN_SIMILARITY` | `0.65` | Similitud mínima para RAG |
| `AGENT_MIN_RELEVANCE` | `0.70` | Umbral mínimo de relevancia |
| `AGENT_MIN_FAITHFULNESS` | `0.75` | Umbral mínimo de fidelidad |

### 4.2 Configuración de Spring AI

**Chat Model:**
- Modelo: Ollama (configurable)
- Temperature: 0.2 (baja temperatura para respuestas más deterministas)

**Embedding Model:**
- Modelo: Ollama nomic-embed-text
- Usado para RAG y búsqueda semántica

**Chat Memory:**
- Implementación: InMemoryChatMemory
- Clave: conversationId
- Estrategia: ventana de mensajes

### 4.3 Observabilidad

**Métricas expuestas:**
- Health checks (Actuator)
- Métricas de Spring Boot
- Logging de:
  - conversationId anonimizado
  - Modelo utilizado
  - Duración de procesamiento
  - Tool calls
  - Cantidad de documentos recuperados
  - Resultado de evaluaciones
  - Errores y reintentos

**Nota:** No se registran prompts completos en producción por defecto.

## 5. Guardrails

### 5.1 Guardrails de Entrada

**Validaciones:**
- Pregunta no vacía
- Longitud máxima configurable
- Detección de patrones maliciosos:
  - "ignore previous instructions"
  - "reveal the system prompt"
  - "muestra el system prompt"
  - "ignora las instrucciones anteriores"
  - "ejecuta este comando del sistema"

**Acción:** Lanza `GuardrailViolationException` con HTTP 422

### 5.2 Guardrails de Salida

**Validaciones:**
- Respuesta no vacía
- No exposición de configuración interna
- Cumplimiento del esquema AgentAnswer
- Validez de campos numéricos (confidence entre 0 y 1)

**Acción:** Lanza `GuardrailViolationException` con HTTP 422

## 6. Evaluación de Calidad

### 6.1 Métricas Calculadas

**Relevancia:**
- Evalúa si la respuesta es pertinente a la pregunta
- Implementación: evaluación mediante LLM auxiliar
- Umbral: >= 0.70

**Fidelidad:**
- Evalúa si la respuesta está basada en el contexto recuperado
- Implementación: evaluación mediante LLM auxiliar
- Umbral: >= 0.75

**Formato Válido:**
- Verifica cumplimiento del esquema
- Implementación: validación Java
- Umbral: 100%

**Confianza:**
- Valor reportado por el modelo en AgentAnswer
- Rango: 0.0 a 1.0

### 6.2 Acciones ante Bajas Métricas

- Logging de warning
- No se bloquea la respuesta (modo observación)
- Futuro: podría activar mecanismos de retry

## 7. Manejo de Errores

### 7.1 Excepciones de Negocio

**GuardrailViolationException:**
- Causa: Violación de reglas de guardrail
- HTTP Status: 422 Unprocessable Entity
- Response: JSON con timestamp, status, error type, message

### 7.2 Excepciones Técnicas

**IllegalStateException:**
- Causa: Error en conversión de salida del modelo
- HTTP Status: 500 Internal Server Error
- Logging: Error completo para debugging

## 8. Consideraciones de Seguridad

### 8.1 Seguridad de Entrada
- Validación estricta de longitud
- Detección de prompt injection
- Sanitización de patrones maliciosos

### 8.2 Seguridad de Salida
- No exposición de configuración interna
- Validación de esquema obligatoria
- No filtrado de secretos

### 8.3 Seguridad de Ejecución
- Tool calling con lista blanca
- Sin ejecución de comandos arbitrarios
- Sin acceso directo al sistema operativo

## 9. Rendimiento y Escalabilidad

### 9.1 Optimizaciones Actuales
- Memoria en memoria para desarrollo
- VectorStore simple (no persistente)
- ChatMemory con ventana de mensajes

### 9.2 Limitaciones Conocidas
- Sin persistencia productiva de memoria
- VectorStore no escalable (SimpleVectorStore)
- Sin mecanismos de cache distribuido

### 9.3 Evolución Sugerida
- Reemplazar SimpleVectorStore por PGVector
- Persistir memoria mediante JDBC
- Añadir cache distribuido (Redis)
- Implementar circuit breaker para llamadas a Ollama

## 10. Testing

### 10.1 Estrategia de Testing

**Unitarios:**
- Dominio: validaciones de records
- Guardrails: patrones de validación
- Servicios: lógica de negocio pura

**Integración:**
- Con Ollama local (requiere modelo)
- RAG con corpus controlado
- End-to-end con API real

**Arquitectura:**
- ArchUnit para validar dependencias entre capas
- Verificar regla de dependencias hexagonal

### 10.2 Tests Requeridos por SPEC
- `mvn clean verify` compila todos los módulos
- API inicia sin claves externas
- `/actuator/health` responde `UP` cuando Ollama está disponible
- `/api/v1/agent/chat` devuelve `AgentAnswer` válido
- Conversación conserva contexto
- Preguntas documentales recuperan fuentes
- Prompt injection es rechazado
- Salida inválida no se entrega sin validación
