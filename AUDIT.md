# Auditoría técnica y cumplimiento

## Resultado

La base original no cumplía el flujo definido por `SPEC.md`: recuperaba RAG sin inyectarlo al modelo, no configuraba memoria, el evaluador requería un bean inexistente, faltaba ingesta REST y una prueba usaba una firma eliminada. La migración adopta Java 25 como requisito vigente.

## Correcciones aplicadas

- Compilación, Enforcer y GitHub Actions alineados con Java 25.
- Contexto RAG inyectado explícitamente en el prompt; recuperación concurrente, limitada por `top-k` y ordenada por coincidencia léxica.
- Endpoint `POST /api/v1/agent/documents` con validación para la ingesta local.
- Memoria por `conversationId` con ventana configurable y `MessageChatMemoryAdvisor`.
- Structured Output mapeado a `AgentAnswer` y validado nuevamente en Java.
- Guardrails determinísticos de entrada y salida, incluyendo prompt injection, tamaño, salida vacía, secretos y falsa atribución de fuentes.
- Evaluación reproducible de relevancia, fidelidad, formato y tools, sin convertir Ollama en una dependencia de las pruebas de CI.
- Tests unitarios, de contrato, RAG, guardrails, tools y reglas ArchUnit; tests reales con Ollama etiquetados como integración opt-in.
- Actuator mantiene health, info, metrics y Prometheus expuestos según configuración.

## Riesgos residuales aceptados por la POC

- RAG en memoria y ranking léxico: no reemplaza embeddings ni una base vectorial.
- Memoria volátil: se pierde al reiniciar y no debe contener secretos.
- La calidad generativa y el tool calling dependen del modelo Ollama elegido.
- Los evaluadores determinísticos sirven como guardas de CI; una evaluación LLM-as-judge offline debe ejecutarse contra un corpus versionado antes de producción.

## Matriz resumida

| Requisito | Estado |
|---|---|
| Maven multimódulo / hexagonal | Cumple |
| Java 25 | Cumple |
| ChatClient / Ollama | Cumple |
| Tool calling autorizado | Cumple para `getSystemTime` |
| Structured Output | Cumple |
| RAG local e ingesta | Cumple |
| Memoria por conversación | Cumple |
| Guardrails Java | Cumple |
| Evaluadores y umbrales | Cumple, con warnings |
| CI sin Ollama | Pendiente de validar con Temurin 25 en esta migración |
| Integración real Ollama | Opt-in mediante `OLLAMA_BASE_URL` |
