# Data sets de las pruebas Bruno

Los archivos están agrupados por suite y por request. Cada JSON contiene un arreglo de iteraciones compatible con `--json-file-path` de Bruno CLI.

Ejemplo desde la carpeta `bruno/`:

```bash
bru run 02-documents/01-ingestar-documento.bru \
  --env Local \
  --json-file-path ../datas-sets/02-documents/01-ingestar-documento.json
```

Para chat estructurado:

```bash
bru run 03-chat/01-chat-estructurado.bru \
  --env Local \
  --json-file-path ../datas-sets/03-chat/01-chat-estructurado.json
```

Los casos de memoria deben ejecutarse en orden y con el mismo `memoryConversationId`: primero `01-informar-nombre.bru` y después `02-recuperar-nombre.bru`.
