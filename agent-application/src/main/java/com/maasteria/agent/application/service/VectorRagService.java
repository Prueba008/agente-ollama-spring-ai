package com.maasteria.agent.application.service;

import com.maasteria.agent.application.port.out.DocumentChunkerPort;
import com.maasteria.agent.application.port.out.RagPort;
import com.maasteria.agent.application.port.out.VectorIndexPort;
import com.maasteria.agent.domain.model.AgentQuestion;
import java.util.List;

public final class VectorRagService implements RagPort {
    private final DocumentChunkerPort chunker;
    private final VectorIndexPort index;
    private final int topK;
    private final double threshold;

    public VectorRagService(DocumentChunkerPort chunker, VectorIndexPort index, int topK, double threshold) {
        this.chunker = chunker;
        this.index = index;
        this.topK = topK;
        this.threshold = threshold;
    }

    @Override
    public List<String> retrieveRelevantContext(AgentQuestion question) {
        return index.search(question.question(), topK, threshold).stream()
                .map(chunk -> "[%s#%s] %s".formatted(chunk.metadata().get("source"), chunk.id(), chunk.content()))
                .toList();
    }

    @Override
    public void ingestDocument(String content, String sourceName) {
        index.index(chunker.split(content, sourceName));
    }
}
