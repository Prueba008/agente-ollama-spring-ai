package com.maasteria.agent.infrastructure.rag;

import com.maasteria.agent.application.port.out.DocumentChunkerPort;
import com.maasteria.agent.domain.model.DocumentChunk;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class OverlappingTextChunker implements DocumentChunkerPort {
    private final int chunkSize;
    private final int overlap;

    public OverlappingTextChunker(int chunkSize, int overlap) {
        if (chunkSize < 100 || overlap < 0 || overlap >= chunkSize) throw new IllegalArgumentException("Configuración de chunk inválida");
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    @Override
    public List<DocumentChunk> split(String content, String sourceName) {
        if (content == null || content.isBlank() || sourceName == null || sourceName.isBlank()) throw new IllegalArgumentException("content y sourceName son obligatorios");
        List<DocumentChunk> result = new ArrayList<>();
        int start = 0, sequence = 0;
        while (start < content.length()) {
            int end = Math.min(content.length(), start + chunkSize);
            if (end < content.length()) {
                int boundary = content.lastIndexOf(' ', end);
                if (boundary > start + chunkSize / 2) end = boundary;
            }
            String text = content.substring(start, end).trim();
            String id = UUID.nameUUIDFromBytes((sourceName + ":" + sequence + ":" + text).getBytes(StandardCharsets.UTF_8)).toString();
            result.add(new DocumentChunk(id, text, Map.of("source", sourceName, "chunk", sequence)));
            if (end == content.length()) break;
            start = Math.max(start + 1, end - overlap);
            sequence++;
        }
        return List.copyOf(result);
    }
}
