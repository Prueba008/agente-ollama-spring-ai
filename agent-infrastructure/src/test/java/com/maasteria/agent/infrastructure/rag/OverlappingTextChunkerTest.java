package com.maasteria.agent.infrastructure.rag;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OverlappingTextChunkerTest {
    @Test
    void divideConSolapamientoYMetadata() {
        var chunker = new OverlappingTextChunker(100, 20);
        String content = "arquitectura ".repeat(30);
        var chunks = chunker.split(content, "arquitectura.md");
        assertTrue(chunks.size() > 1);
        assertEquals("arquitectura.md", chunks.getFirst().metadata().get("source"));
        assertEquals(0, chunks.getFirst().metadata().get("chunk"));
        assertNotEquals(chunks.getFirst().id(), chunks.get(1).id());
    }

    @Test
    void generaIdsDeterministicosParaReingesta() {
        var chunker = new OverlappingTextChunker(100, 10);
        assertEquals(chunker.split("contenido estable", "doc.md").getFirst().id(),
                chunker.split("contenido estable", "doc.md").getFirst().id());
    }
}
