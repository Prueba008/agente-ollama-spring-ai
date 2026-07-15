package com.maasteria.agent.application.port.out;

import com.maasteria.agent.domain.model.AgentQuestion;

public interface InputGuardrailPort {
    void validate(AgentQuestion question);
}
