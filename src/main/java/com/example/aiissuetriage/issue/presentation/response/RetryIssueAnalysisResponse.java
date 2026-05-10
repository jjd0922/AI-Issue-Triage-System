package com.example.aiissuetriage.issue.presentation.response;

import com.example.aiissuetriage.issue.application.result.RetryIssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;

public record RetryIssueAnalysisResponse(
        Long issueId,
        IssueStatus status,
        LocalDateTime requestedAt
) {

    public static RetryIssueAnalysisResponse from(RetryIssueAnalysisResult result) {
        return new RetryIssueAnalysisResponse(
                result.issueId(),
                result.status(),
                result.requestedAt()
        );
    }
}
