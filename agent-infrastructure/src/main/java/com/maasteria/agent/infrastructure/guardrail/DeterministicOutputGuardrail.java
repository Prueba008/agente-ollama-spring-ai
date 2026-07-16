package com.maasteria.agent.infrastructure.guardrail;

import com.maasteria.agent.application.port.out.OutputGuardrailPort;
import com.maasteria.agent.domain.exception.GuardrailViolationException;
import com.maasteria.agent.domain.model.AgentAnswer;

import java.util.Locale;
import java.util.ArrayList;
import java.util.List;

public final class DeterministicOutputGuardrail implements OutputGuardrailPort {

    @Override
    public AgentAnswer validate(AgentAnswer answer) {
        if (answer == null || answer.answer() == null || answer.answer().isBlank()) {
            throw new GuardrailViolationException("El modelo devolvió una respuesta vacía");
        }
        String normalized = answer.answer().toLowerCase(Locale.ROOT);
        if (normalized.contains("spring.ai.ollama") && normalized.contains("password")
                || normalized.contains("api_key=") || normalized.contains("authorization: bearer ")) {
            throw new GuardrailViolationException("La respuesta podría exponer configuración interna");
        }
        if (answer.grounded() && answer.sources().isEmpty()) {
            List<String> warnings = new ArrayList<>(answer.warnings());
            warnings.add("No se informaron fuentes verificables");
            return new AgentAnswer(answer.answer(), List.of(), answer.toolsUsed(), false,
                    Math.min(answer.confidence(), 0.49), warnings);
        }
        return answer;
    }
}
