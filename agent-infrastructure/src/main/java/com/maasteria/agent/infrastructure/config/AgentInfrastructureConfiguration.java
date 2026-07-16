package com.maasteria.agent.infrastructure.config;

import com.maasteria.agent.application.port.in.AskAgentUseCase;
import com.maasteria.agent.application.port.out.AgentEnginePort;
import com.maasteria.agent.application.port.out.InputGuardrailPort;
import com.maasteria.agent.application.port.out.OutputGuardrailPort;
import com.maasteria.agent.application.service.AskAgentService;
import com.maasteria.agent.application.service.VectorRagService;
import com.maasteria.agent.infrastructure.ai.SpringAiAgentEngine;
import com.maasteria.agent.infrastructure.evaluator.SimpleEvaluator;
import com.maasteria.agent.infrastructure.guardrail.DeterministicInputGuardrail;
import com.maasteria.agent.infrastructure.guardrail.DeterministicOutputGuardrail;
import com.maasteria.agent.infrastructure.tool.SystemTools;
import com.maasteria.agent.infrastructure.rag.OverlappingTextChunker;
import com.maasteria.agent.infrastructure.rag.PgVectorIndexAdapter;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
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
    ChatClient.Builder chatClientBuilder(org.springframework.ai.chat.model.ChatModel chatModel) {
        return ChatClient.builder(chatModel);
    }

    @Bean
    ChatMemory chatMemory(Environment environment) {
        int maxMessages = environment.getProperty("agent.memory.max-messages", Integer.class, 20);
        return MessageWindowChatMemory.builder().maxMessages(maxMessages).build();
    }

    @Bean
    SystemTools systemTools(Clock agentClock) {
        return new SystemTools(agentClock);
    }

    @Bean
    com.maasteria.agent.application.port.out.DocumentChunkerPort documentChunker(Environment environment) {
        int size = environment.getProperty("agent.rag.chunk-size", Integer.class, 1000);
        int overlap = environment.getProperty("agent.rag.chunk-overlap", Integer.class, 150);
        return new OverlappingTextChunker(size, overlap);
    }

    @Bean
    com.maasteria.agent.application.port.out.VectorIndexPort vectorIndex(org.springframework.ai.vectorstore.VectorStore store) {
        return new PgVectorIndexAdapter(store);
    }

    @Bean
    com.maasteria.agent.application.port.out.RagPort ragPort(
            com.maasteria.agent.application.port.out.DocumentChunkerPort chunker,
            com.maasteria.agent.application.port.out.VectorIndexPort index,
            Environment environment) {
        int topK = environment.getProperty("agent.rag.top-k", Integer.class, 4);
        double threshold = environment.getProperty("agent.rag.similarity-threshold", Double.class, 0.70);
        return new VectorRagService(chunker, index, topK, threshold);
    }

    @Bean
    com.maasteria.agent.application.port.out.EvaluatorPort evaluatorPort() {
        return new SimpleEvaluator();
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
    AgentEnginePort agentEngine(ChatClient.Builder builder, SystemTools systemTools, ChatMemory chatMemory) {
        return new SpringAiAgentEngine(builder, systemTools, chatMemory);
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
