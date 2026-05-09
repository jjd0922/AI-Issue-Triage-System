package com.example.aiissuetriage.issue.presentation.response;

public record FieldErrorResponse(
        String field,
        String message,
        Object rejectedValue
) {
}
