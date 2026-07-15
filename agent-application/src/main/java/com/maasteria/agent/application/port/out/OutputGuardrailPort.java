package com.maasteria.agent.application.port.out;

import com.maasteria.agent.domain.model.AgentAnswer;

public interface OutputGuardrailPort {
    AgentAnswer validate(AgentAnswer answer);
}
