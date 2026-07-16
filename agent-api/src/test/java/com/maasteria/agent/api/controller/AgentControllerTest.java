package com.maasteria.agent.api.controller;

import com.maasteria.agent.application.port.in.AskAgentUseCase;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import com.maasteria.agent.application.port.out.RagPort;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentControllerTest {

    @Test
    void convierteLaSolicitudALaRespuestaEsperada() {
        AskAgentUseCase useCase = mock(AskAgentUseCase.class);
        RagPort ragPort = mock(RagPort.class);
        AgentController controller = new AgentController(useCase, ragPort);
        AgentController.ChatRequest request = new AgentController.ChatRequest("conv-1", "¿Qué es Java?");
        AgentAnswer expected = new AgentAnswer("Respuesta", List.of(), List.of(), true, 0.9, List.of());

        when(useCase.ask(new AgentQuestion("conv-1", "¿Qué es Java?"))).thenReturn(expected);

        ResponseEntity<AgentAnswer> response = controller.chat(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(expected, response.getBody());
        verify(useCase).ask(new AgentQuestion("conv-1", "¿Qué es Java?"));
    }
}
