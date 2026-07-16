package com.maasteria.agent.infrastructure.evaluator;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SemanticJudgeBenchmarkTest {

    @Test
    void calculaMatrizAccuracyPrecisionRecallYF1ContraEtiquetasHumanas() {
        Map<String, SemanticLabel> predictions = Map.of(
                "paráfrasis válida", SemanticLabel.ENTAILED,
                "paráfrasis mal clasificada", SemanticLabel.CONTRADICTED,
                "negación detectada", SemanticLabel.CONTRADICTED,
                "contradicción omitida", SemanticLabel.NOT_ENOUGH_INFORMATION,
                "información ausente", SemanticLabel.NOT_ENOUGH_INFORMATION,
                "ausencia mal clasificada", SemanticLabel.CONTRADICTED);
        SemanticJudge judge = (evidence, claim) -> predictions.get(claim);

        SemanticJudgeBenchmark.Result result = new SemanticJudgeBenchmark(judge).evaluate(List.of(
                testCase("1", "paráfrasis válida", SemanticLabel.ENTAILED),
                testCase("2", "paráfrasis mal clasificada", SemanticLabel.ENTAILED),
                testCase("3", "negación detectada", SemanticLabel.CONTRADICTED),
                testCase("4", "contradicción omitida", SemanticLabel.CONTRADICTED),
                testCase("5", "información ausente", SemanticLabel.NOT_ENOUGH_INFORMATION),
                testCase("6", "ausencia mal clasificada", SemanticLabel.NOT_ENOUGH_INFORMATION)));

        assertEquals(3, result.correct());
        assertEquals(6, result.total());
        assertEquals(0.5, result.accuracy());
        assertEquals(1, result.confusionMatrix().get(SemanticLabel.ENTAILED)
                .get(SemanticLabel.ENTAILED));
        assertEquals(1, result.confusionMatrix().get(SemanticLabel.ENTAILED)
                .get(SemanticLabel.CONTRADICTED));
        assertEquals(1.0 / 3.0, result.precision(SemanticLabel.CONTRADICTED), 0.000_001);
        assertEquals(0.5, result.recall(SemanticLabel.CONTRADICTED));
        assertEquals(0.4, result.f1(SemanticLabel.CONTRADICTED), 0.000_001);
    }

    @Test
    void devuelveCeroCuandoUnaEtiquetaNoTienePrediccionesNiCasosHumanos() {
        SemanticJudgeBenchmark.Result result = new SemanticJudgeBenchmark(
                (evidence, claim) -> SemanticLabel.ENTAILED)
                .evaluate(List.of(testCase("1", "afirmación respaldada", SemanticLabel.ENTAILED)));

        assertEquals(0.0, result.precision(SemanticLabel.CONTRADICTED));
        assertEquals(0.0, result.recall(SemanticLabel.CONTRADICTED));
        assertEquals(0.0, result.f1(SemanticLabel.CONTRADICTED));
    }

    @Test
    void rechazaJuezNuloCorpusNuloOVacioYEtiquetaPredichaNula() {
        assertThrows(NullPointerException.class, () -> new SemanticJudgeBenchmark(null));

        SemanticJudgeBenchmark benchmark = new SemanticJudgeBenchmark(
                (evidence, claim) -> SemanticLabel.ENTAILED);
        assertThrows(IllegalArgumentException.class, () -> benchmark.evaluate(null));
        assertThrows(IllegalArgumentException.class, () -> benchmark.evaluate(List.of()));

        SemanticJudgeBenchmark nullLabelBenchmark = new SemanticJudgeBenchmark((evidence, claim) -> null);
        assertThrows(NullPointerException.class,
                () -> nullLabelBenchmark.evaluate(List.of(
                        testCase("1", "afirmación", SemanticLabel.ENTAILED))));
    }

    @Test
    void matrizExpuestaEsInmutable() {
        SemanticJudgeBenchmark.Result result = new SemanticJudgeBenchmark(
                (evidence, claim) -> SemanticLabel.ENTAILED)
                .evaluate(List.of(testCase("1", "afirmación", SemanticLabel.ENTAILED)));

        assertThrows(UnsupportedOperationException.class,
                () -> result.confusionMatrix().put(SemanticLabel.CONTRADICTED, Map.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> result.confusionMatrix().get(SemanticLabel.ENTAILED)
                        .put(SemanticLabel.CONTRADICTED, 1));
    }

    private static HumanLabeledSemanticCase testCase(
            String id, String claim, SemanticLabel humanLabel) {
        return new HumanLabeledSemanticCase(id, List.of("evidencia controlada"), claim, humanLabel);
    }
}
