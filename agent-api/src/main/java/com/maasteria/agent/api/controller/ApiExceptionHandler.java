package com.maasteria.agent.api.controller;

import com.maasteria.agent.domain.exception.GuardrailViolationException;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(GuardrailViolationException.class)
    ResponseEntity<ApiErrorResponse> handleGuardrail(GuardrailViolationException exception) {
        return ResponseEntity.status(422).body(new ApiErrorResponse(
                Instant.now().toString(),
                422,
                "GUARDRAIL_VIOLATION",
                exception.getMessage()));
    }

    @Schema(name = "ApiError", description = "Error normalizado de la API")
    public record ApiErrorResponse(
            @Schema(example = "2026-07-16T12:00:00Z") String timestamp,
            @Schema(example = "422") int status,
            @Schema(example = "GUARDRAIL_VIOLATION") String error,
            @Schema(example = "La solicitud fue bloqueada por el guardrail de entrada") String message) {
    }
}
