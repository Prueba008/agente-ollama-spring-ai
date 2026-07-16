# Pruebas de API con Bruno

Colección para validar la API del agente local con Spring AI, Ollama y PGVector.

## Requisitos

- Aplicación iniciada en `http://localhost:8080`.
- Ollama disponible con los modelos configurados.
- PostgreSQL con la extensión `vector` y el esquema inicializado.
- Bruno Desktop o Bruno CLI.

## Ejecución

Desde la raíz del repositorio:

```bash
npx @usebruno/cli run bruno --env Local
```

Para ejecutar un grupo concreto:

```bash
npx @usebruno/cli run bruno/05-guardrails --env Local
```

## Orden de la colección

1. Health check.
2. OpenAPI y Swagger UI.
3. Ingesta y validaciones documentales.
4. Chat, RAG y tool calling.
5. Memoria conversacional.
6. Guardrails y Bean Validation.

Los escenarios de RAG, memoria y tool calling dependen del modelo y de los servicios locales. Las pruebas de contratos inválidos y prompt injection son determinísticas cuando la aplicación está disponible.

## Ejecución con data sets

Los datos de iteración están agrupados por test en [`../datas-sets`](../datas-sets/README.md). Ejemplo:

```bash
npx @usebruno/cli run 03-chat/01-chat-estructurado.bru \
  --env Local \
  --json-file-path ../datas-sets/03-chat/01-chat-estructurado.json
```
