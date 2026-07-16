package com.maasteria.agent.api.config;

import com.maasteria.agent.api.controller.AgentController;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OpenApiConfigurationTest {

    @Test
    void declaraMetadatosGeneralesDeLaApi() {
        OpenAPIDefinition definition = OpenApiConfiguration.class.getAnnotation(OpenAPIDefinition.class);

        assertNotNull(definition);
        assertEquals("Agente local con Ollama y Spring AI", definition.info().title());
        assertEquals("1.0.0", definition.info().version());
        assertEquals("http://localhost:8080", definition.servers()[0].url());
    }

    @Test
    void documentaElControladorYSusOperaciones() throws NoSuchMethodException {
        Tag tag = AgentController.class.getAnnotation(Tag.class);
        Operation ingest = AgentController.class
                .getMethod("ingest", AgentController.DocumentRequest.class)
                .getAnnotation(Operation.class);
        Operation chat = AgentController.class
                .getMethod("chat", AgentController.ChatRequest.class)
                .getAnnotation(Operation.class);

        assertNotNull(tag);
        assertEquals("Agente", tag.name());
        assertEquals("ingestDocument", ingest.operationId());
        assertEquals("askAgent", chat.operationId());
    }
}
