package com.maasteria.agent.infrastructure.evaluator;

import com.maasteria.agent.application.port.out.EvaluatorPort;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleEvaluator implements EvaluatorPort {

    private final ChatClient chatClient;
    private final Environment environment;

    public SimpleEvaluator(ChatClient chatClient, Environment environment) {
        this.chatClient = chatClient;
        this.environment = environment;
    }

    @Override
    public Map<String, Double> evaluate(AgentQuestion question, AgentAnswer answer, List<String> retrievedContext) {
        Map<String, Double> metrics = new HashMap<>();
        
        // Relevancia: la respuesta debe ser pertinente a la pregunta
        double relevance = evaluateRelevance(question, answer);
        metrics.put("relevance", relevance);
        
        // Fidelidad: la respuesta debe estar basada en el contexto recuperado
        double faithfulness = evaluateFaithfulness(answer, retrievedContext);
        metrics.put("faithfulness", faithfulness);
        
        // Formato válido: la respuesta debe cumplir el esquema
        double formatValid = answer.answer() != null && !answer.answer().isBlank() ? 1.0 : 0.0;
        metrics.put("format_valid", formatValid);
        
        // Confianza reportada por el modelo
        metrics.put("confidence", answer.confidence());
        
        return metrics;
    }

    private double evaluateRelevance(AgentQuestion question, AgentAnswer answer) {
        if (answer.answer() == null || answer.answer().isBlank()) {
            return 0.0;
        }
        
        String prompt = String.format("""
            Evalúa la relevancia de la siguiente respuesta para la pregunta dada.
            Pregunta: %s
            Respuesta: %s
            Devuelve únicamente un número decimal entre 0.0 y 1.0.
            """, question.question(), answer.answer());
        
        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            return parseScore(response);
        } catch (Exception e) {
            return 0.5; // Valor por defecto si falla la evaluación
        }
    }

    private double evaluateFaithfulness(AgentAnswer answer, List<String> retrievedContext) {
        if (retrievedContext == null || retrievedContext.isEmpty()) {
            return answer.grounded() ? 0.8 : 0.3;
        }
        
        String context = String.join("\n", retrievedContext);
        String prompt = String.format("""
            Evalúa si la siguiente respuesta está fielmente basada en el contexto proporcionado.
            Contexto: %s
            Respuesta: %s
            Devuelve únicamente un número decimal entre 0.0 y 1.0.
            """, context, answer.answer());
        
        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            return parseScore(response);
        } catch (Exception e) {
            return answer.grounded() ? 0.7 : 0.4;
        }
    }

    private double parseScore(String response) {
        try {
            String cleaned = response.replaceAll("[^0-9.]", "").trim();
            if (cleaned.isEmpty()) return 0.5;
            double score = Double.parseDouble(cleaned);
            return Math.max(0.0, Math.min(1.0, score));
        } catch (Exception e) {
            return 0.5;
        }
    }
}
