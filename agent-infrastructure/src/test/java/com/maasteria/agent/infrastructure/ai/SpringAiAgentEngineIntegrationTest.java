package com.maasteria.agent.infrastructure.ai;

import com.maasteria.agent.application.port.out.AgentEnginePort;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@EnabledIfEnvironmentVariable(named = "OLLAMA_BASE_URL", matches = ".*")
@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.ollama.base-url=${OLLAMA_BASE_URL:http://localhost:11434}",
    "spring.ai.ollama.chat.options.model=${OLLAMA_CHAT_MODEL:qwen3:8b}"
})
class SpringAiAgentEngineIntegrationTest {

    @Autowired
    private AgentEnginePort agentEngine;

    @Test
    void ejecutaPreguntaSimpleConOllama() {
        AgentQuestion question = new AgentQuestion("conv-test-001", "¿Qué es Java?");
        
        AgentAnswer answer = agentEngine.execute(question);
        
        assertNotNull(answer);
        assertNotNull(answer.answer());
        assertFalse(answer.answer().isBlank());
        assertTrue(answer.confidence() >= 0.0 && answer.confidence() <= 1.0);
    }

    @Test
    void ejecutaPreguntaRequiereToolCalling() {
        AgentQuestion question = new AgentQuestion("conv-test-002", 
            "¿Qué hora es en Buenos Aires?");
        
        AgentAnswer answer = agentEngine.execute(question);
        
        assertNotNull(answer);
        assertNotNull(answer.answer());
        assertFalse(answer.toolsUsed().isEmpty(), 
            "Debería haber utilizado la herramienta de tiempo");
    }

    @Test
    void ejecutaPreguntaConRespuestaEstructurada() {
        AgentQuestion question = new AgentQuestion("conv-test-003", 
            "Expllica brevemente qué es Spring Boot");
        
        AgentAnswer answer = agentEngine.execute(question);
        
        assertNotNull(answer);
        assertNotNull(answer.answer());
        assertNotNull(answer.sources());
        assertNotNull(answer.toolsUsed());
        assertNotNull(answer.warnings());
        assertTrue(answer.confidence() >= 0.0 && answer.confidence() <= 1.0);
    }
}
