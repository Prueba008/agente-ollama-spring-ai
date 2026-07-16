package com.maasteria.agent.domain.model;

import java.util.Map;

public record DocumentChunk(String id, String content, Map<String, Object> metadata) {
    public DocumentChunk {
        if (id == null || id.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("id y content son obligatorios");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
