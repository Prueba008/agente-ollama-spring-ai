package com.maasteria.agent.api.controller;

import com.maasteria.agent.application.port.in.AskAgentUseCase;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.ai.ollama.base-url=${OLLAMA_BASE_URL:http://localhost:11434}",
    "spring.ai.ollama.chat.options.model=${OLLAMA_CHAT_MODEL:qwen3:8b}"
})
class AgentControllerOllamaIT {

    @Autowired
    private AskAgentUseCase askAgentUseCase;

    @Test
    void chatEndpointRespondeConOllama() {
        AgentQuestion question = new AgentQuestion("conv-integration-test-001", "¿Qué es Java en una frase?");
        
        AgentAnswer answer = askAgentUseCase.ask(question);

        assertNotNull(answer);
        assertNotNull(answer.answer());
        assertFalse(answer.answer().isBlank());
        assertTrue(answer.confidence() >= 0.0 && answer.confidence() <= 1.0);
    }

    @Test
    void chatEndpointConToolCalling() {
        AgentQuestion question = new AgentQuestion("conv-integration-test-002", "¿Qué hora es en UTC?");
        
        AgentAnswer answer = askAgentUseCase.ask(question);

        assertNotNull(answer);
        assertNotNull(answer.answer());
        // The agent should use the system time tool
        assertNotNull(answer.toolsUsed());
    }

    @Test
    void chatEndpointMantieneContextoPorConversationId() {
        String conversationId = "conv-integration-test-003";

        // First question
        AgentQuestion question1 = new AgentQuestion(conversationId, "Mi nombre es TestUser");
        AgentAnswer answer1 = askAgentUseCase.ask(question1);
        assertNotNull(answer1);

        // Second question referencing the first
        AgentQuestion question2 = new AgentQuestion(conversationId, "¿Cómo me llamo?");
        AgentAnswer answer2 = askAgentUseCase.ask(question2);
        
        assertNotNull(answer2);
        assertNotNull(answer2.answer());
        // The answer should reference the name from the previous context
        assertTrue(answer2.answer().toLowerCase().contains("testuser") || 
                   answer2.answer().toLowerCase().contains("test"));
    }
}
