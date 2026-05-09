package com.example.aiissuetriage.issue.application.exception;

import com.example.aiissuetriage.issue.domain.IssueStatus;

public class InvalidRetryConditionException extends RuntimeException {

    public InvalidRetryConditionException(Long issueId, IssueStatus status) {
        super("Cannot retry issue analysis. issueId=" + issueId + ", status=" + status);
    }
}
