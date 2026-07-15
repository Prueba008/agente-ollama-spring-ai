package com.maasteria.agent.infrastructure.ai;

import com.maasteria.agent.application.port.out.RagPort;
import com.maasteria.agent.domain.model.AgentQuestion;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

public class SimpleRagAdapter implements RagPort {

    private final List<String> documents = new ArrayList<>();
    private final Environment environment;

    public SimpleRagAdapter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public List<String> retrieveRelevantContext(AgentQuestion question) {
        int topK = environment.getProperty("agent.rag.top-k", Integer.class, 4);
        return documents.stream().limit(topK).toList();
    }

    @Override
    public void ingestDocument(String content, String sourceName) {
        documents.add("[%s] %s".formatted(sourceName, content));
    }
}
