package com.maasteria.agent.api.controller;

import com.maasteria.agent.application.port.in.AskAgentUseCase;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
import com.maasteria.agent.application.port.out.RagPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
@Tag(name = "Agente", description = "Ingesta documental y consultas al agente local")
public class AgentController {

    private final AskAgentUseCase useCase;
    private final RagPort ragPort;

    public AgentController(AskAgentUseCase useCase, RagPort ragPort) {
        this.useCase = useCase;
        this.ragPort = ragPort;
    }

    @PostMapping("/documents")
    @Operation(
            operationId = "ingestDocument",
            summary = "Ingerir un documento",
            description = "Fragmenta e indexa contenido textual para su recuperación posterior mediante RAG.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Documento aceptado para indexación"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)))
    })
    public ResponseEntity<Void> ingest(@Valid @RequestBody DocumentRequest request) {
        ragPort.ingestDocument(request.content(), request.sourceName());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/chat")
    @Operation(
            operationId = "askAgent",
            summary = "Consultar al agente",
            description = "Ejecuta guardrails, recuperación RAG, memoria, tool calling, salida estructurada y evaluación.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Respuesta estructurada del agente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AgentAnswer.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "answer": "El patrón outbox persiste el evento junto al cambio de negocio.",
                                      "sources": [{"name": "arquitectura.md", "reference": "chunk-1"}],
                                      "toolsUsed": [],
                                      "grounded": true,
                                      "confidence": 0.91,
                                      "warnings": []
                                    }
                                    """))),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))),
            @ApiResponse(
                    responseCode = "422",
                    description = "Solicitud bloqueada por guardrails",
                    content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiErrorResponse.class)))
    })
    public ResponseEntity<AgentAnswer> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(useCase.ask(new AgentQuestion(request.conversationId(), request.question())));
    }

    @Schema(name = "ChatRequest", description = "Solicitud de conversación con el agente")
    public record ChatRequest(
            @Schema(description = "Identificador que aísla la memoria conversacional", example = "conv-001")
            @NotBlank @Size(max = 100) String conversationId,
            @Schema(description = "Pregunta del usuario", example = "¿Cómo funciona el patrón outbox?")
            @NotBlank @Size(max = 12_000) String question) {
    }

    @Schema(name = "DocumentRequest", description = "Documento textual que será incorporado al RAG")
    public record DocumentRequest(
            @Schema(description = "Nombre lógico de la fuente", example = "arquitectura.md")
            @NotBlank @Size(max = 200) String sourceName,
            @Schema(description = "Contenido textual del documento", example = "El patrón outbox...")
            @NotBlank @Size(max = 100_000) String content) {
    }
}
