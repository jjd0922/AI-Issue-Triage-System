package com.example.aiissuetriage.issue.presentation;

import com.example.aiissuetriage.issue.application.exception.InvalidRetryConditionException;
import com.example.aiissuetriage.issue.application.exception.IssueAnalysisNotFoundException;
import com.example.aiissuetriage.issue.application.exception.IssueNotFoundException;
import com.example.aiissuetriage.issue.domain.InvalidIssueStatusException;
import com.example.aiissuetriage.issue.presentation.response.ApiErrorResponse;
import com.example.aiissuetriage.issue.presentation.response.FieldErrorResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class IssueExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldErrorResponse)
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        "VALIDATION_ERROR",
                        "Request validation failed",
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestURI(),
                        errors
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<FieldErrorResponse> errors = exception.getConstraintViolations()
                .stream()
                .map(violation -> new FieldErrorResponse(
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getInvalidValue()
                ))
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        "VALIDATION_ERROR",
                        "Request validation failed",
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestURI(),
                        errors
                ));
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            InvalidPageRequestException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        "VALIDATION_ERROR",
                        resolveBadRequestMessage(exception),
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(InvalidIssueStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidIssueStatus(
            InvalidIssueStatusException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        "INVALID_ISSUE_STATUS",
                        exception.getMessage(),
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(InvalidRetryConditionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidRetryCondition(
            InvalidRetryConditionException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.badRequest()
                .body(ApiErrorResponse.of(
                        "INVALID_RETRY_CONDITION",
                        exception.getMessage(),
                        HttpStatus.BAD_REQUEST.value(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(IssueNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleIssueNotFound(
            IssueNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        "ISSUE_NOT_FOUND",
                        exception.getMessage(),
                        HttpStatus.NOT_FOUND.value(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(IssueAnalysisNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleIssueAnalysisNotFound(
            IssueAnalysisNotFoundException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiErrorResponse.of(
                        "ISSUE_ANALYSIS_NOT_FOUND",
                        exception.getMessage(),
                        HttpStatus.NOT_FOUND.value(),
                        request.getRequestURI()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception exception, HttpServletRequest request) {
        return ResponseEntity.internalServerError()
                .body(ApiErrorResponse.of(
                        "INTERNAL_SERVER_ERROR",
                        "Internal server error",
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        request.getRequestURI()
                ));
    }

    private FieldErrorResponse toFieldErrorResponse(FieldError fieldError) {
        return new FieldErrorResponse(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getRejectedValue()
        );
    }

    private String resolveBadRequestMessage(Exception exception) {
        if (exception instanceof HttpMessageNotReadableException notReadableException
                && notReadableException.getCause() instanceof InvalidFormatException invalidFormatException) {
            String field = invalidFormatException.getPath().isEmpty()
                    ? "request"
                    : invalidFormatException.getPath().get(0).getFieldName();
            return "Invalid request value for field: " + field;
        }
        return exception.getMessage() == null ? "Invalid request" : exception.getMessage();
    }
}
