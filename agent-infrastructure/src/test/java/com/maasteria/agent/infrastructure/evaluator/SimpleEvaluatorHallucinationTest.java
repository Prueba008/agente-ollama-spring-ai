package com.maasteria.agent.infrastructure.evaluator;

import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import com.maasteria.agent.domain.model.SourceReference;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleEvaluatorHallucinationTest {

    private final SimpleEvaluator evaluator = new SimpleEvaluator();

    @Test
    void respuestaTotalmenteRespaldadaTieneRiesgoBajoYCertezaAlta() {
        Map<String, Double> metrics = evaluate(
                "¿Cómo funciona el patrón outbox?",
                answer("El patrón outbox persiste el evento con la transacción de negocio", true, 0.90,
                        "arquitectura.md"),
                List.of("[arquitectura.md] El patrón outbox persiste el evento con la transacción de negocio"));

        assertTrue(metrics.get("faithfulness") >= 0.95);
        assertTrue(metrics.get("hallucination_risk") <= 0.05);
        assertTrue(metrics.get("certainty") >= 0.85);
    }

    @Test
    void afirmacionInventadaTieneRiesgoAlto() {
        Map<String, Double> metrics = evaluate(
                "¿Cómo funciona el patrón outbox?",
                answer("La solución utiliza blockchain cuántica y satélites privados", true, 0.98,
                        "arquitectura.md"),
                List.of("[arquitectura.md] El patrón outbox persiste eventos en la misma transacción"));

        assertTrue(metrics.get("faithfulness") < 0.25);
        assertTrue(metrics.get("hallucination_risk") > 0.75);
    }

    @Test
    void declararseFundamentadaSinEvidenciaEsAlucinacionCritica() {
        Map<String, Double> metrics = evaluate(
                "¿Cuál es el límite?",
                answer("El límite exacto es 5000 operaciones", true, 0.99, "limites.md"),
                List.of());

        assertEquals(0.0, metrics.get("faithfulness"));
        assertEquals(1.0, metrics.get("hallucination_risk"));
    }

    @Test
    void reconocerAusenciaDeEvidenciaNoSeClasificaComoAlucinacion() {
        Map<String, Double> metrics = evaluate(
                "¿Cuál es el límite?",
                new AgentAnswer("No existe evidencia suficiente para determinar el límite",
                        List.of(), List.of(), false, 0.20, List.of("Información insuficiente")),
                List.of());

        assertEquals(1.0, metrics.get("faithfulness"));
        assertEquals(0.0, metrics.get("hallucination_risk"));
    }

    @Test
    void fuenteQueNoFueRecuperadaReduceLaCerteza() {
        Map<String, Double> metrics = evaluate(
                "¿Cómo funciona el patrón outbox?",
                answer("El patrón outbox persiste el evento con la transacción", true, 0.90,
                        "fuente-inventada.md"),
                List.of("[arquitectura.md] El patrón outbox persiste el evento con la transacción"));

        assertEquals(0.0, metrics.get("source_coherence"));
        assertTrue(metrics.get("certainty") < 0.90);
    }

    private Map<String, Double> evaluate(String question, AgentAnswer answer, List<String> context) {
        return evaluator.evaluate(new AgentQuestion("conv-hallucination", question), answer, context);
    }

    private static AgentAnswer answer(String text, boolean grounded, double confidence, String source) {
        return new AgentAnswer(text, List.of(new SourceReference(source, "chunk-1")),
                List.of(), grounded, confidence, List.of());
    }
}
