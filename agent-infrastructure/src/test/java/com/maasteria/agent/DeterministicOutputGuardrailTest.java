package com.maasteria.agent;

import com.maasteria.agent.domain.exception.GuardrailViolationException;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.infrastructure.guardrail.DeterministicOutputGuardrail;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeterministicOutputGuardrailTest {

    private final DeterministicOutputGuardrail guardrail = new DeterministicOutputGuardrail();

    @Test
    void aceptaUnaRespuestaValida() {
        AgentAnswer answer = new AgentAnswer(
                "Respuesta fundamentada",
                List.of(),
                List.of(),
                true,
                0.95,
                List.of());

        assertSame(answer, guardrail.validate(answer));
    }

    @Test
    void bloqueaRespuestaNula() {
        GuardrailViolationException exception = assertThrows(
                GuardrailViolationException.class,
                () -> guardrail.validate(null));

        assertEquals("El modelo devolvió una respuesta vacía", exception.getMessage());
    }

    @Test
    void bloqueaRespuestaEnBlanco() {
        AgentAnswer answer = new AgentAnswer("  ", null, null, false, 0.0, null);

        assertThrows(GuardrailViolationException.class, () -> guardrail.validate(answer));
    }

    @Test
    void bloqueaExposicionDeConfiguracionInterna() {
        AgentAnswer answer = new AgentAnswer(
                "spring.ai.ollama.password=secreto",
                List.of(),
                List.of(),
                false,
                0.1,
                List.of());

        GuardrailViolationException exception = assertThrows(
                GuardrailViolationException.class,
                () -> guardrail.validate(answer));

        assertEquals("La respuesta podría exponer configuración interna", exception.getMessage());
    }
}
