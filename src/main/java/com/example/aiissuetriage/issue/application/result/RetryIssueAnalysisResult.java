package com.example.aiissuetriage.issue.application.result;

import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;

public record RetryIssueAnalysisResult(
        Long issueId,
        IssueStatus status,
        LocalDateTime requestedAt
) {
}
