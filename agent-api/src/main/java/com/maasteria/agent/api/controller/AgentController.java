package com.maasteria.agent.api.controller;

import com.maasteria.agent.application.port.in.AskAgentUseCase;
import com.maasteria.agent.domain.model.AgentAnswer;
import com.maasteria.agent.domain.model.AgentQuestion;
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
public class AgentController {

    private final AskAgentUseCase useCase;

    public AgentController(AskAgentUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentAnswer> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(useCase.ask(new AgentQuestion(request.conversationId(), request.question())));
    }

    public record ChatRequest(
            @NotBlank @Size(max = 100) String conversationId,
            @NotBlank @Size(max = 12_000) String question) {
    }
}
