package com.maasteria.agent.infrastructure.evaluator;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Contrasta obligatoriamente las decisiones del juez con etiquetas humanas. */
public final class SemanticJudgeBenchmark {

    private final SemanticJudge judge;

    public SemanticJudgeBenchmark(SemanticJudge judge) {
        this.judge = Objects.requireNonNull(judge, "judge");
    }

    public Result evaluate(List<HumanLabeledSemanticCase> corpus) {
        if (corpus == null || corpus.isEmpty()) {
            throw new IllegalArgumentException("El corpus humano etiquetado no puede estar vacío");
        }

        Map<SemanticLabel, Map<SemanticLabel, Integer>> matrix = new EnumMap<>(SemanticLabel.class);
        for (SemanticLabel expected : SemanticLabel.values()) {
            matrix.put(expected, new EnumMap<>(SemanticLabel.class));
        }

        int correct = 0;
        for (HumanLabeledSemanticCase testCase : corpus) {
            SemanticLabel predicted = Objects.requireNonNull(
                    judge.judge(testCase.evidence(), testCase.claim()), "El juez devolvió una etiqueta nula");
            matrix.get(testCase.humanLabel()).merge(predicted, 1, Integer::sum);
            if (predicted == testCase.humanLabel()) correct++;
        }
        return new Result(correct, corpus.size(), matrix);
    }

    public record Result(
            int correct,
            int total,
            Map<SemanticLabel, Map<SemanticLabel, Integer>> confusionMatrix) {

        public Result {
            Map<SemanticLabel, Map<SemanticLabel, Integer>> copy = new EnumMap<>(SemanticLabel.class);
            confusionMatrix.forEach((label, row) -> copy.put(label, Map.copyOf(row)));
            confusionMatrix = Map.copyOf(copy);
        }

        public double accuracy() {
            return (double) correct / total;
        }

        public double precision(SemanticLabel label) {
            int truePositive = count(label, label);
            int predictedPositive = confusionMatrix.values().stream()
                    .mapToInt(row -> row.getOrDefault(label, 0)).sum();
            return ratio(truePositive, predictedPositive);
        }

        public double recall(SemanticLabel label) {
            int truePositive = count(label, label);
            int actualPositive = confusionMatrix.getOrDefault(label, Map.of()).values().stream()
                    .mapToInt(Integer::intValue).sum();
            return ratio(truePositive, actualPositive);
        }

        public double f1(SemanticLabel label) {
            double precision = precision(label);
            double recall = recall(label);
            return precision + recall == 0.0 ? 0.0 : 2.0 * precision * recall / (precision + recall);
        }

        private int count(SemanticLabel expected, SemanticLabel predicted) {
            return confusionMatrix.getOrDefault(expected, Map.of()).getOrDefault(predicted, 0);
        }

        private static double ratio(int numerator, int denominator) {
            return denominator == 0 ? 0.0 : (double) numerator / denominator;
        }
    }
}
