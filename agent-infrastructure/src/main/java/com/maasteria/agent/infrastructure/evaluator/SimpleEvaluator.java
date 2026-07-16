package com.maasteria.agent.infrastructure.evaluator;

import com.maasteria.agent.application.port.out.EvaluatorPort;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Evaluador determinístico, reproducible y apto para CI sin un segundo llamado al LLM. */
public final class SimpleEvaluator implements EvaluatorPort {

    @Override
    public Map<String, Double> evaluate(AgentQuestion question, AgentAnswer answer, List<String> context) {
        double relevance = overlap(tokens(question.question()), tokens(answer.answer()));
        double faithfulness = context == null || context.isEmpty()
                ? (answer.grounded() ? 0.0 : 1.0)
                : overlap(tokens(answer.answer()), tokens(String.join(" ", context)));
        double toolUsage = answer.toolsUsed().stream().allMatch(tool -> tool.equals("getSystemTime")) ? 1.0 : 0.0;
        return Map.of(
                "relevance", relevance,
                "faithfulness", faithfulness,
                "format_valid", 1.0,
                "tool_usage", toolUsage,
                "confidence", answer.confidence());
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
}
