#!/usr/bin/env bash
set -euo pipefail

API_URL="${API_URL:-http://localhost:8080/api/v1/agent/documents}"
DOCUMENT_DIR="${1:-docs/rag}"

find "$DOCUMENT_DIR" -type f \( -name '*.md' -o -name '*.txt' \) -print0 |
while IFS= read -r -d '' file; do
  payload=$(jq -n --arg source "$(basename "$file")" --rawfile content "$file" \
    '{sourceName: $source, content: $content}')
  curl --fail --silent --show-error -X POST "$API_URL" \
    -H 'Content-Type: application/json' -d "$payload"
  printf 'Indexado: %s\n' "$file"
done
