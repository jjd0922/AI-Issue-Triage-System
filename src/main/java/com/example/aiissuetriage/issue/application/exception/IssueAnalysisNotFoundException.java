package com.example.aiissuetriage.issue.application.exception;

public class IssueAnalysisNotFoundException extends RuntimeException {

    public IssueAnalysisNotFoundException(Long issueId) {
        super("Issue analysis not found: " + issueId);
    }
}
