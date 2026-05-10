package com.example.aiissuetriage.issue.application.exception;

public class IssueNotFoundException extends RuntimeException {

    public IssueNotFoundException(Long issueId) {
        super("Issue not found: " + issueId);
    }
}
