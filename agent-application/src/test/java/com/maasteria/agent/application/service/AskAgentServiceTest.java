package com.maasteria.agent.application.service;

import com.maasteria.agent.application.port.out.AgentEnginePort;
import com.maasteria.agent.application.port.out.InputGuardrailPort;
import com.maasteria.agent.application.port.out.OutputGuardrailPort;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AskAgentServiceTest {

    @Test
    void delegaLaValidacionYRetornaLaRespuestaFinal() {
        AgentEnginePort engine = mock(AgentEnginePort.class);
        InputGuardrailPort inputGuardrail = mock(InputGuardrailPort.class);
        OutputGuardrailPort outputGuardrail = mock(OutputGuardrailPort.class);
        AskAgentService service = new AskAgentService(engine, inputGuardrail, outputGuardrail);

        AgentQuestion question = new AgentQuestion("conv-1", "¿Qué es Java?");
        AgentAnswer expected = new AgentAnswer("Respuesta", List.of(), List.of(), true, 0.8, List.of());

        when(engine.execute(question)).thenReturn(expected);
        when(outputGuardrail.validate(expected)).thenReturn(expected);

        AgentAnswer result = service.ask(question);

        assertEquals(expected, result);
        verify(inputGuardrail).validate(question);
        verify(engine).execute(question);
        verify(outputGuardrail).validate(expected);
    }
}
