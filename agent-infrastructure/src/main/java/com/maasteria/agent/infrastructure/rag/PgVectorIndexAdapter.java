package com.maasteria.agent.infrastructure.rag;

import com.maasteria.agent.application.port.out.VectorIndexPort;
import com.maasteria.agent.domain.model.DocumentChunk;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import java.util.List;

public final class PgVectorIndexAdapter implements VectorIndexPort {
    private final VectorStore vectorStore;
    public PgVectorIndexAdapter(VectorStore vectorStore) { this.vectorStore = vectorStore; }

    @Override
    public void index(List<DocumentChunk> chunks) {
        vectorStore.add(chunks.stream().map(c -> new Document(c.id(), c.content(), c.metadata())).toList());
    }

    @Override
    public List<DocumentChunk> search(String query, int topK, double similarityThreshold) {
        return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(topK)
                .similarityThreshold(similarityThreshold).build()).stream()
                .map(d -> new DocumentChunk(d.getId(), d.getText(), d.getMetadata())).toList();
    }
}
