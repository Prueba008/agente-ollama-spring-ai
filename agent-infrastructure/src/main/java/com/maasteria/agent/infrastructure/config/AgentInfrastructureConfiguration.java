package com.maasteria.agent.infrastructure.config;

import com.maasteria.agent.application.port.in.AskAgentUseCase;
import com.maasteria.agent.application.port.out.AgentEnginePort;
import com.maasteria.agent.application.port.out.InputGuardrailPort;
import com.maasteria.agent.application.port.out.OutputGuardrailPort;
import com.maasteria.agent.application.service.AskAgentService;
import com.maasteria.agent.infrastructure.ai.SpringAiAgentEngine;
import com.maasteria.agent.infrastructure.guardrail.DeterministicInputGuardrail;
import com.maasteria.agent.infrastructure.guardrail.DeterministicOutputGuardrail;
import com.maasteria.agent.infrastructure.ai.SimpleRagAdapter;
import com.maasteria.agent.infrastructure.evaluator.SimpleEvaluator;
import com.maasteria.agent.infrastructure.tool.SystemTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Clock;

@Configuration
public class AgentInfrastructureConfiguration {

    @Bean
    Clock agentClock() {
        return Clock.systemUTC();
    }

    @Bean
    ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    @Bean
    ChatClient.Builder chatClientBuilder(org.springframework.ai.chat.model.ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    @Bean
    SystemTools systemTools(Clock agentClock) {
        return new SystemTools(agentClock);
    }

    @Bean
    VectorStore vectorStore(org.springframework.ai.embedding.EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }

    @Bean
    com.maasteria.agent.application.port.out.RagPort ragPort(VectorStore vectorStore, Environment environment) {
        return new SimpleRagAdapter(vectorStore, environment);
    }

    @Bean
    com.maasteria.agent.application.port.out.EvaluatorPort evaluatorPort(ChatClient chatClient, Environment environment) {
        return new SimpleEvaluator(chatClient, environment);
    }

    @Bean
    InputGuardrailPort inputGuardrail(Environment environment) {
        int maxChars = environment.getProperty("agent.guardrails.max-input-chars", Integer.class, 12_000);
        return new DeterministicInputGuardrail(maxChars);
    }

    @Bean
    OutputGuardrailPort outputGuardrail() {
        return new DeterministicOutputGuardrail();
    }

    @Bean
    AgentEnginePort agentEngine(ChatClient.Builder builder, ChatMemory chatMemory, SystemTools systemTools) {
        return new SpringAiAgentEngine(builder, chatMemory, systemTools);
    }

    @Bean
    AskAgentUseCase askAgentUseCase(AgentEnginePort engine,
                                    InputGuardrailPort inputGuardrail,
                                    OutputGuardrailPort outputGuardrail,
                                    com.maasteria.agent.application.port.out.RagPort ragPort,
                                    com.maasteria.agent.application.port.out.EvaluatorPort evaluatorPort) {
        return new AskAgentService(engine, inputGuardrail, outputGuardrail, ragPort, evaluatorPort);
    }
}
