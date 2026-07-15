package com.maasteria.agent.domain.exception;

public class GuardrailViolationException extends RuntimeException {
    public GuardrailViolationException(String message) {
        super(message);
    }
}
