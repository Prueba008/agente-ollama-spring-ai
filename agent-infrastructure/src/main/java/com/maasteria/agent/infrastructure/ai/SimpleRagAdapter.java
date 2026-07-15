package com.maasteria.agent.infrastructure.ai;

import com.maasteria.agent.application.port.out.RagPort;
import com.maasteria.agent.domain.model.AgentQuestion;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.core.env.Environment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleRagAdapter implements RagPort {

    private final VectorStore vectorStore;
    private final Environment environment;

    public SimpleRagAdapter(VectorStore vectorStore, Environment environment) {
        this.vectorStore = vectorStore;
        this.environment = environment;
    }

    @Override
    public List<String> retrieveRelevantContext(AgentQuestion question) {
        int topK = environment.getProperty("agent.rag.top-k", Integer.class, 4);
        double minSimilarity = environment.getProperty("agent.rag.min-similarity", Double.class, 0.65);

        SearchRequest request = SearchRequest.query(question.question())
                .withTopK(topK)
                .withSimilarityThreshold(minSimilarity);

        return vectorStore.similaritySearch(request).stream()
                .map(Document::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public void ingestDocument(String content, String sourceName) {
        Document document = new Document(content, Map.of("source", sourceName));
        vectorStore.add(List.of(document));
    }
}
