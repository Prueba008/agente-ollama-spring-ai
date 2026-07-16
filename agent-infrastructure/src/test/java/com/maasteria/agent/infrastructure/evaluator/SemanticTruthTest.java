package com.maasteria.agent.infrastructure.evaluator;

import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import com.maasteria.agent.domain.model.SourceReference;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SemanticTruthTest {

    private static final List<HumanLabeledSemanticCase> HUMAN_GOLD_SET = List.of(
            semanticCase("paraphrase", "El patrón outbox guarda el evento en la misma transacción del negocio",
                    "El evento y el cambio de negocio se persisten atómicamente", SemanticLabel.ENTAILED),
            semanticCase("explicit-negation", "El límite de transferencias es 1000 operaciones por día",
                    "El límite de transferencias no es 1000 operaciones por día", SemanticLabel.CONTRADICTED),
            semanticCase("implicit-negation", "Solo los administradores pueden eliminar usuarios",
                    "Un operador sin privilegios puede eliminar usuarios", SemanticLabel.CONTRADICTED),
            semanticCase("complex-contradiction",
                    "Si falla el envío, el evento permanece pendiente y se reintenta; solo se marca enviado tras confirmación",
                    "Aunque el envío falle, el evento queda confirmado y no vuelve a intentarse",
                    SemanticLabel.CONTRADICTED),
            semanticCase("unknown", "El sistema utiliza PostgreSQL para persistencia",
                    "El despliegue soporta exactamente 25.000 usuarios concurrentes",
                    SemanticLabel.NOT_ENOUGH_INFORMATION));

    @Test
    void juezSemanticoReconoceParafrasisNegacionesYContradiccionesComplejas() {
        SemanticJudgeBenchmark.Result result = new SemanticJudgeBenchmark(goldSetJudge()).evaluate(HUMAN_GOLD_SET);

        assertEquals(1.0, result.accuracy());
        assertEquals(1.0, result.recall(SemanticLabel.CONTRADICTED));
        assertEquals(1.0, result.f1(SemanticLabel.ENTAILED));
    }

    @Test
    void coincidenciaLexicaNoDemuestraVerdadSemantica() {
        String evidence = "El límite de transferencias es 1000 operaciones por día";
        String contradiction = "El límite de transferencias no es 1000 operaciones por día";
        AgentAnswer answer = new AgentAnswer(contradiction,
                List.of(new SourceReference("limites.md", "chunk-1")), List.of(), true, 0.99, List.of());

        Map<String, Double> lexicalMetrics = new SimpleEvaluator().evaluate(
                new AgentQuestion("semantic-1", "¿Cuál es el límite de transferencias?"), answer,
                List.of("[limites.md] " + evidence));

        assertTrue(lexicalMetrics.get("faithfulness") > 0.75,
                "El solapamiento léxico produce un falso positivo deliberadamente demostrado");
        assertEquals(SemanticLabel.CONTRADICTED, goldSetJudge().judge(List.of(evidence), contradiction));
    }

    @Test
    void confianzaAltaDelModeloNoCambiaUnaEtiquetaHumanaFalsa() {
        String evidence = "Solo los administradores pueden eliminar usuarios";
        String falseClaim = "Un operador sin privilegios puede eliminar usuarios";

        AgentAnswer highConfidence = answer(falseClaim, 0.99);
        AgentAnswer lowConfidence = answer(falseClaim, 0.10);

        assertEquals(SemanticLabel.CONTRADICTED, judgeIgnoringModelConfidence(evidence, highConfidence));
        assertEquals(SemanticLabel.CONTRADICTED, judgeIgnoringModelConfidence(evidence, lowConfidence));
    }

    @Test
    void benchmarkRechazaEvaluacionSinEtiquetasHumanas() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SemanticJudgeBenchmark(goldSetJudge()).evaluate(List.of()));
    }

    private static SemanticLabel judgeIgnoringModelConfidence(String evidence, AgentAnswer answer) {
        // La confianza autoinformada no forma parte del contrato del juez semántico.
        return goldSetJudge().judge(List.of(evidence), answer.answer());
    }

    private static SemanticJudge goldSetJudge() {
        Map<String, SemanticLabel> labels = new LinkedHashMap<>();
        HUMAN_GOLD_SET.forEach(c -> labels.put(key(c.evidence(), c.claim()), c.humanLabel()));
        return (evidence, claim) -> labels.getOrDefault(key(evidence, claim), SemanticLabel.NOT_ENOUGH_INFORMATION);
    }

    private static String key(List<String> evidence, String claim) {
        return String.join("\n", evidence) + "\n=>" + claim;
    }

    private static HumanLabeledSemanticCase semanticCase(
            String id, String evidence, String claim, SemanticLabel label) {
        return new HumanLabeledSemanticCase(id, List.of(evidence), claim, label);
    }

    private static AgentAnswer answer(String claim, double confidence) {
        return new AgentAnswer(claim, List.of(), List.of(), true, confidence, List.of());
    }
}
