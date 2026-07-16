package com.maasteria.agent.infrastructure.guardrail;

import com.maasteria.agent.domain.exception.GuardrailViolationException;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.SourceReference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeterministicOutputGuardrailTest {

    private final DeterministicOutputGuardrail guardrail = new DeterministicOutputGuardrail();

    @Test
    void aceptaUnaRespuestaValida() {
        AgentAnswer answer = new AgentAnswer("Respuesta válida",
                List.of(new SourceReference("documento.md", "chunk-1")), List.of(), true, 0.9, List.of());

        AgentAnswer result = assertDoesNotThrow(() -> guardrail.validate(answer));

        assertEquals(answer, result);
    }

    @Test
    void rechazaUnaRespuestaVacia() {
        assertThrows(GuardrailViolationException.class, () -> guardrail.validate(new AgentAnswer("   ", List.of(), List.of(), true, 0.8, List.of())));
    }

    @Test
    void rechazaUnaRespuestaQueExponeConfiguracionInterna() {
        AgentAnswer answer = new AgentAnswer("spring.ai.ollama password=secret", List.of(), List.of(), true, 0.8, List.of());

        assertThrows(GuardrailViolationException.class, () -> guardrail.validate(answer));
    }
}
