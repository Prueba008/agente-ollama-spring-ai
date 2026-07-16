package com.maasteria.agent.infrastructure.evaluator;

import java.util.List;

/**
 * Contrato para un evaluador semántico (NLI o LLM-as-judge).
 * La etiqueta debe inferirse exclusivamente de la evidencia y la afirmación.
 */
@FunctionalInterface
public interface SemanticJudge {

    SemanticLabel judge(List<String> evidence, String claim);
}
