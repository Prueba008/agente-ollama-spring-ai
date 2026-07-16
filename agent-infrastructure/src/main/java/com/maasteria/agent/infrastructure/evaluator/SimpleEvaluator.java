package com.maasteria.agent.infrastructure.evaluator;

import com.maasteria.agent.application.port.out.EvaluatorPort;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Evaluación determinística y reproducible para detectar afirmaciones no respaldadas. */
public final class SimpleEvaluator implements EvaluatorPort {

    @Override
    public Map<String, Double> evaluate(AgentQuestion question, AgentAnswer answer, List<String> context) {
        List<String> evidence = context == null ? List.of() : context;
        double relevance = overlap(tokens(question.question()), tokens(answer.answer()));
        double sourceCoherence = sourceCoherence(answer, evidence);
        double faithfulness = faithfulness(answer, evidence, sourceCoherence);
        double formatValid = validFormat(answer) ? 1.0 : 0.0;
        double toolUsage = answer.toolsUsed().stream().allMatch("getSystemTime"::equals) ? 1.0 : 0.0;
        double hallucinationRisk = clamp(1.0 - faithfulness);
        double certainty = clamp(0.40 * faithfulness
                + 0.25 * relevance
                + 0.20 * answer.confidence()
                + 0.10 * formatValid
                + 0.05 * sourceCoherence);

        return Map.of(
                "relevance", relevance,
                "faithfulness", faithfulness,
                "source_coherence", sourceCoherence,
                "hallucination_risk", hallucinationRisk,
                "certainty", certainty,
                "format_valid", formatValid,
                "tool_usage", toolUsage,
                "confidence", answer.confidence());
    }

    private static double faithfulness(AgentAnswer answer, List<String> context, double sourceCoherence) {
        if (context.isEmpty()) {
            return answer.grounded() ? 0.0 : 1.0;
        }
        if (!answer.grounded()) {
            return 1.0;
        }
        double evidenceCoverage = overlap(tokens(answer.answer()), tokens(String.join(" ", context)));
        return clamp(0.85 * evidenceCoverage + 0.15 * sourceCoherence);
    }

    private static double sourceCoherence(AgentAnswer answer, List<String> context) {
        if (answer.sources().isEmpty()) {
            return answer.grounded() ? 0.0 : 1.0;
        }
        String normalizedContext = String.join(" ", context).toLowerCase(Locale.ROOT);
        long coherent = answer.sources().stream()
                .filter(source -> source.name() != null && normalizedContext.contains(source.name().toLowerCase(Locale.ROOT)))
                .count();
        return (double) coherent / answer.sources().size();
    }

    private static boolean validFormat(AgentAnswer answer) {
        return answer.answer() != null && !answer.answer().isBlank()
                && answer.confidence() >= 0.0 && answer.confidence() <= 1.0;
    }

    private static double overlap(Set<String> expected, Set<String> actual) {
        if (expected.isEmpty()) return 1.0;
        Set<String> intersection = new HashSet<>(expected);
        intersection.retainAll(actual);
        return (double) intersection.size() / expected.size();
    }

    private static Set<String> tokens(String value) {
        if (value == null) return Set.of();
        return java.util.Arrays.stream(value.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(token -> token.length() > 3)
                .collect(java.util.stream.Collectors.toSet());
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
