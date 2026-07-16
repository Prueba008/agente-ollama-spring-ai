package com.maasteria.agent.infrastructure.evaluator;

import java.util.List;
import java.util.Objects;

/** Caso versionable cuya verdad de referencia fue asignada por una persona. */
public record HumanLabeledSemanticCase(
        String id,
        List<String> evidence,
        String claim,
        SemanticLabel humanLabel) {

    public HumanLabeledSemanticCase {
        Objects.requireNonNull(id, "id");
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        Objects.requireNonNull(claim, "claim");
        Objects.requireNonNull(humanLabel, "humanLabel");
    }
}
