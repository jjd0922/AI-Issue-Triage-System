package com.example.aiissuetriage.issue.presentation.response;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        String code,
        String message,
        int status,
        String path,
        LocalDateTime timestamp,
        List<FieldErrorResponse> errors
) {

    public static ApiErrorResponse of(String code, String message, int status, String path) {
        return new ApiErrorResponse(
                code,
                message,
                status,
                path,
                LocalDateTime.now(),
                List.of()
        );
    }

    public static ApiErrorResponse of(
            String code,
            String message,
            int status,
            String path,
            List<FieldErrorResponse> errors
    ) {
        return new ApiErrorResponse(
                code,
                message,
                status,
                path,
                LocalDateTime.now(),
                errors
        );
    }
}
