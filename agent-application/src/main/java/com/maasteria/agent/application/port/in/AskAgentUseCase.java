package com.maasteria.agent.application.port.in;

import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;

public interface AskAgentUseCase {
    AgentAnswer ask(AgentQuestion question);
}
