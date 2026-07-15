package com.maasteria.agent.infrastructure.ai;

import com.maasteria.agent.application.port.out.AgentEnginePort;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import com.maasteria.agent.infrastructure.tool.SystemTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;

public final class SpringAiAgentEngine implements AgentEnginePort {

    private static final String SYSTEM_PROMPT = """
            Eres un agente técnico corporativo ejecutado localmente.
            Responde en español profesional y utiliza herramientas sólo cuando aporten datos verificables.
            No inventes fuentes. Cuando no exista evidencia suficiente, establece grounded=false,
            reduce confidence y agrega una advertencia.
            Devuelve exclusivamente la estructura solicitada.
            """;

    private final ChatClient chatClient;
    private final SystemTools systemTools;

    public SpringAiAgentEngine(ChatClient.Builder builder, ChatMemory chatMemory, SystemTools systemTools) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.systemTools = systemTools;
    }

    @Override
    public AgentAnswer execute(AgentQuestion question) {
        AgentAnswer answer = chatClient.prompt()
                .user(question.question())
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, question.conversationId()))
                .tools(systemTools)
                .call()
                .entity(AgentAnswer.class);

        if (answer == null) {
            throw new IllegalStateException("No se pudo convertir la salida del modelo a AgentAnswer");
        }
        return answer;
    }
}
