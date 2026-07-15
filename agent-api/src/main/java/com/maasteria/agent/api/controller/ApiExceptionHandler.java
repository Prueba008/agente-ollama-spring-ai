package com.maasteria.agent.api.controller;

import com.maasteria.agent.domain.exception.GuardrailViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(GuardrailViolationException.class)
    ResponseEntity<Map<String, Object>> handleGuardrail(GuardrailViolationException exception) {
        return ResponseEntity.status(422).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", 422,
                "error", "GUARDRAIL_VIOLATION",
                "message", exception.getMessage()
        ));
    }
}
