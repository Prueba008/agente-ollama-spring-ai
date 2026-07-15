package com.maasteria.agent.application.port.out;

import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import java.util.List;
import java.util.Map;

public interface EvaluatorPort {
    Map<String, Double> evaluate(AgentQuestion question, AgentAnswer answer, List<String> retrievedContext);
}
