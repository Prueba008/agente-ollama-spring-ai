package com.maasteria.agent.infrastructure.ai;

import com.maasteria.agent.application.port.out.AgentEnginePort;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import com.maasteria.agent.infrastructure.tool.SystemTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.List;

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

    public SpringAiAgentEngine(ChatClient.Builder builder, SystemTools systemTools, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.systemTools = systemTools;
    }

    @Override
    public AgentAnswer execute(AgentQuestion question, List<String> retrievedContext) {
        String evidence = retrievedContext.isEmpty()
                ? "No se recuperó evidencia documental. No declares la respuesta como fundamentada."
                : String.join("\n", retrievedContext);
        AgentAnswer answer = chatClient.prompt()
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, question.conversationId()))
                .user(user -> user.text("""
                        Pregunta:
                        {question}

                        Evidencia documental recuperada:
                        {evidence}

                        Usa exclusivamente nombres y referencias presentes en la evidencia. Si no alcanza,
                        responde con grounded=false y explica la incertidumbre en warnings.
                        """).param("question", question.question()).param("evidence", evidence))
                .tools(systemTools)
                .call()
                .entity(AgentAnswer.class);

        if (answer == null) {
            throw new IllegalStateException("No se pudo convertir la salida del modelo a AgentAnswer");
        }
        return answer;
    }
}
