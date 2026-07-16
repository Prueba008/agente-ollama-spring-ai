package com.maasteria.agent.infrastructure.ai;

import com.maasteria.agent.application.port.out.RagPort;
import com.maasteria.agent.domain.model.AgentQuestion;
import org.springframework.core.env.Environment;

import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class SimpleRagAdapter implements RagPort {

    private final List<String> documents = new CopyOnWriteArrayList<>();
    private final Environment environment;

    public SimpleRagAdapter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public List<String> retrieveRelevantContext(AgentQuestion question) {
        int topK = environment.getProperty("agent.rag.top-k", Integer.class, 4);
        Set<String> terms = tokens(question.question());
        return documents.stream()
                .map(document -> new ScoredDocument(document, score(document, terms)))
                .filter(candidate -> candidate.score() > 0 || terms.isEmpty())
                .sorted(java.util.Comparator.comparingInt(ScoredDocument::score).reversed())
                .limit(topK)
                .map(ScoredDocument::content)
                .toList();
    }

    @Override
    public void ingestDocument(String content, String sourceName) {
        if (content == null || content.isBlank() || sourceName == null || sourceName.isBlank()) {
            throw new IllegalArgumentException("content y sourceName son obligatorios");
        }
        documents.add("[%s] %s".formatted(sourceName, content));
    }

    private static int score(String document, Set<String> terms) {
        Set<String> documentTerms = tokens(document);
        return (int) terms.stream().filter(documentTerms::contains).count();
    }

    private static Set<String> tokens(String value) {
        return java.util.Arrays.stream(value.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(token -> token.length() > 2)
                .collect(java.util.stream.Collectors.toSet());
    }

    private record ScoredDocument(String content, int score) { }
}
