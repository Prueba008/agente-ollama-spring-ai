package com.maasteria.agent.application.port.out;

import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;

public interface AgentEnginePort {
    AgentAnswer execute(AgentQuestion question);
}
