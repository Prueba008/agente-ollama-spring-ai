package com.maasteria.agent.domain.model;

import java.util.List;

public record AgentAnswer(
        String answer,
        List<SourceReference> sources,
        List<String> toolsUsed,
        boolean grounded,
        double confidence,
        List<String> warnings) {

    public AgentAnswer {
        sources = sources == null ? List.of() : List.copyOf(sources);
        toolsUsed = toolsUsed == null ? List.of() : List.copyOf(toolsUsed);
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence debe estar entre 0 y 1");
        }
    }
}
