package com.maasteria.agent.application.service;

import com.maasteria.agent.application.port.out.*;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AskAgentServiceTest {

    @Test
    void delegaLaValidacionYRetornaLaRespuestaFinal() {
        AgentEnginePort engine = mock(AgentEnginePort.class);
        InputGuardrailPort inputGuardrail = mock(InputGuardrailPort.class);
        OutputGuardrailPort outputGuardrail = mock(OutputGuardrailPort.class);
        RagPort ragPort = mock(RagPort.class);
        EvaluatorPort evaluatorPort = mock(EvaluatorPort.class);
        AskAgentService service = new AskAgentService(engine, inputGuardrail, outputGuardrail, ragPort, evaluatorPort);

        AgentQuestion question = new AgentQuestion("conv-1", "¿Qué es Java?");
        AgentAnswer expected = new AgentAnswer("Respuesta", List.of(), List.of(), true, 0.8, List.of());
        List<String> context = List.of("contexto");

        when(ragPort.retrieveRelevantContext(question)).thenReturn(context);
        when(engine.execute(question)).thenReturn(expected);
        when(outputGuardrail.validate(expected)).thenReturn(expected);
        when(evaluatorPort.evaluate(question, expected, context)).thenReturn(Map.of("relevance", 0.95, "faithfulness", 0.9));

        AgentAnswer result = service.ask(question);

        assertEquals(expected, result);
        verify(inputGuardrail).validate(question);
        verify(ragPort).retrieveRelevantContext(question);
        verify(engine).execute(question);
        verify(outputGuardrail).validate(expected);
        verify(evaluatorPort).evaluate(question, expected, context);
    }
}
