package com.maasteria.agent;

import com.maasteria.agent.application.port.out.AgentEnginePort;
import com.maasteria.agent.application.port.out.InputGuardrailPort;
import com.maasteria.agent.application.port.out.OutputGuardrailPort;
import com.maasteria.agent.application.service.AskAgentService;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AskAgentServiceTest {

    @Test
    void ejecutaGuardrailEntradaMotorYGuardrailSalidaEnOrden() {
        List<String> invocations = new ArrayList<>();
        AgentQuestion question = new AgentQuestion("conversation-1", "Pregunta técnica");
        AgentAnswer generated = new AgentAnswer("respuesta", null, null, true, 0.9, null);
        AgentAnswer validated = new AgentAnswer("respuesta validada", null, null, true, 1.0, null);

        InputGuardrailPort inputGuardrail = value -> {
            assertSame(question, value);
            invocations.add("input");
        };
        AgentEnginePort engine = value -> {
            assertSame(question, value);
            invocations.add("engine");
            return generated;
        };
        OutputGuardrailPort outputGuardrail = value -> {
            assertSame(generated, value);
            invocations.add("output");
            return validated;
        };

        AskAgentService service = new AskAgentService(engine, inputGuardrail, outputGuardrail);

        AgentAnswer result = service.ask(question);

        assertSame(validated, result);
        assertEquals(List.of("input", "engine", "output"), invocations);
    }
}
