package com.maasteria.agent.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Agente local con Ollama y Spring AI",
                version = "1.0.0",
                description = "API para ingesta documental RAG y conversaciones con un agente local, "
                        + "incluyendo memoria, tools, guardrails y respuestas estructuradas.",
                contact = @Contact(name = "Maaster.IA"),
                license = @License(name = "Uso interno / referencia técnica")),
        servers = {
                @Server(url = "http://localhost:8080", description = "Entorno local")
        })
public class OpenApiConfiguration {
}
