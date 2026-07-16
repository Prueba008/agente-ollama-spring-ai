package com.maasteria.agent.application.service;

import com.maasteria.agent.application.port.in.AskAgentUseCase;
import com.maasteria.agent.application.port.out.AgentEnginePort;
import com.maasteria.agent.application.port.out.EvaluatorPort;
import com.maasteria.agent.application.port.out.InputGuardrailPort;
import com.maasteria.agent.application.port.out.OutputGuardrailPort;
import com.maasteria.agent.application.port.out.RagPort;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public final class AskAgentService implements AskAgentUseCase {

    private static final Logger log = LoggerFactory.getLogger(AskAgentService.class);

    private final AgentEnginePort engine;
    private final InputGuardrailPort inputGuardrail;
    private final OutputGuardrailPort outputGuardrail;
    private final RagPort ragPort;
    private final EvaluatorPort evaluatorPort;

    public AskAgentService(AgentEnginePort engine,
                           InputGuardrailPort inputGuardrail,
                           OutputGuardrailPort outputGuardrail,
                           RagPort ragPort,
                           EvaluatorPort evaluatorPort) {
        this.engine = engine;
        this.inputGuardrail = inputGuardrail;
        this.outputGuardrail = outputGuardrail;
        this.ragPort = ragPort;
        this.evaluatorPort = evaluatorPort;
    }

    @Override
    public AgentAnswer ask(AgentQuestion question) {
        long startTime = System.currentTimeMillis();
        
        // 1. Validar entrada con guardrails
        inputGuardrail.validate(question);
        
        // 2. Recuperar contexto relevante con RAG
        List<String> retrievedContext = ragPort.retrieveRelevantContext(question);
        log.info("Recuperados {} documentos relevantes para conversationId: {}", 
                retrievedContext.size(), question.conversationId());
        
        // 3. Ejecutar el motor del agente
        AgentAnswer answer = engine.execute(question, retrievedContext);
        
        // 4. Validar salida con guardrails
        answer = outputGuardrail.validate(answer);
        
        // 5. Evaluar calidad de la respuesta
        Map<String, Double> metrics = evaluatorPort.evaluate(question, answer, retrievedContext);
        log.info("Métricas de evaluación: {} para conversationId: {}", 
                metrics, question.conversationId());
        
        // 6. Agregar warnings si las métricas están por debajo del umbral
        if (metrics.getOrDefault("relevance", 1.0) < 0.70) {
            log.warn("Baja relevancia detectada: {}", metrics.get("relevance"));
        }
        if (metrics.getOrDefault("faithfulness", 1.0) < 0.75) {
            log.warn("Baja fidelidad detectada: {}", metrics.get("faithfulness"));
        }
        
        long duration = System.currentTimeMillis() - startTime;
        log.info("Tiempo total de procesamiento: {}ms", duration);
        
        List<String> warnings = new java.util.ArrayList<>(answer.warnings());
        if (metrics.getOrDefault("relevance", 1.0) < 0.70) {
            warnings.add("La respuesta no alcanzó el umbral de relevancia");
        }
        if (metrics.getOrDefault("faithfulness", 1.0) < 0.75) {
            warnings.add("La respuesta no alcanzó el umbral de fidelidad");
        }
        return new AgentAnswer(answer.answer(), answer.sources(), answer.toolsUsed(),
                answer.grounded(), answer.confidence(), warnings);
    }
}
