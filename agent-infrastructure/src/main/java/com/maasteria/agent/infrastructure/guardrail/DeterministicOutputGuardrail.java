package com.maasteria.agent.infrastructure.guardrail;

import com.maasteria.agent.application.port.out.OutputGuardrailPort;
import com.maasteria.agent.domain.exception.GuardrailViolationException;
import com.maasteria.agent.domain.model.AgentAnswer;

import java.util.Locale;

public final class DeterministicOutputGuardrail implements OutputGuardrailPort {

    @Override
    public AgentAnswer validate(AgentAnswer answer) {
        if (answer == null || answer.answer() == null || answer.answer().isBlank()) {
            throw new GuardrailViolationException("El modelo devolvió una respuesta vacía");
        }
        String normalized = answer.answer().toLowerCase(Locale.ROOT);
        if (normalized.contains("spring.ai.ollama") && normalized.contains("password")) {
            throw new GuardrailViolationException("La respuesta podría exponer configuración interna");
        }
        return answer;
    }
}
