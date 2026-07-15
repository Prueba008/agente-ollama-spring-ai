package com.maasteria.agent.application.port.out;

import com.maasteria.agent.domain.model.AgentQuestion;
import java.util.List;

public interface RagPort {
    List<String> retrieveRelevantContext(AgentQuestion question);
    void ingestDocument(String content, String sourceName);
}
