package com.maasteria.agent.domain.model;

import java.util.Objects;

public record AgentQuestion(String conversationId, String question) {
    public AgentQuestion {
        Objects.requireNonNull(conversationId, "conversationId es obligatorio");
        Objects.requireNonNull(question, "question es obligatoria");
    }
}
