package com.maasteria.agent.infrastructure;

import com.maasteria.agent.application.service.AskAgentService;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import com.maasteria.agent.domain.model.SourceReference;
import com.maasteria.agent.infrastructure.ai.SimpleRagAdapter;
import com.maasteria.agent.infrastructure.evaluator.SimpleEvaluator;
import com.maasteria.agent.infrastructure.guardrail.DeterministicInputGuardrail;
import com.maasteria.agent.infrastructure.guardrail.DeterministicOutputGuardrail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentFlowIntegrationTest {

    private SimpleRagAdapter rag;

    @BeforeEach
    void setUp() {
        rag = new SimpleRagAdapter(new StandardEnvironment());
    }

    @Test
    void flujoCompletoAceptaRespuestaRespaldada() {
        rag.ingestDocument("El patrón outbox persiste el evento con la transacción de negocio", "arquitectura.md");
        AskAgentService service = serviceReturning(new AgentAnswer(
                "El patrón outbox persiste el evento con la transacción de negocio",
                List.of(new SourceReference("arquitectura.md", "chunk-1")),
                List.of(), true, 0.92, List.of()));

        AgentAnswer result = service.ask(new AgentQuestion("conv-supported", "Explica el patrón outbox"));

        assertTrue(result.grounded());
        assertFalse(result.warnings().contains("Se detectó riesgo de afirmaciones no respaldadas"));
    }

    @Test
    void flujoCompletoAdvierteRespuestaNoRespaldada() {
        rag.ingestDocument("El patrón outbox persiste eventos en una transacción", "arquitectura.md");
        AskAgentService service = serviceReturning(new AgentAnswer(
                "La plataforma utiliza blockchain cuántica y satélites privados",
                List.of(new SourceReference("arquitectura.md", "chunk-1")),
                List.of(), true, 0.97, List.of()));

        AgentAnswer result = service.ask(new AgentQuestion("conv-unsupported", "Explica el patrón outbox"));

        assertTrue(result.warnings().contains("La respuesta no alcanzó el umbral de fidelidad"));
        assertTrue(result.warnings().contains("Se detectó riesgo de afirmaciones no respaldadas"));
    }

    private AskAgentService serviceReturning(AgentAnswer answer) {
        return new AskAgentService(
                (question, context) -> answer,
                new DeterministicInputGuardrail(12_000),
                new DeterministicOutputGuardrail(),
                rag,
                new SimpleEvaluator());
    }
}
