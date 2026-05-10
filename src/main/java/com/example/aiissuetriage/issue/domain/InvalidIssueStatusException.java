package com.example.aiissuetriage.issue.domain;

public class InvalidIssueStatusException extends RuntimeException {

    public InvalidIssueStatusException(String message) {
        super(message);
    }
}
