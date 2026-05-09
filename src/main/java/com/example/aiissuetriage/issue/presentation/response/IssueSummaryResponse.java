package com.example.aiissuetriage.issue.presentation.response;

import com.example.aiissuetriage.issue.application.result.IssueSummaryResult;
import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;

public record IssueSummaryResponse(
        Long issueId,
        String title,
        IssueSource source,
        IssueStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static IssueSummaryResponse from(IssueSummaryResult result) {
        return new IssueSummaryResponse(
                result.issueId(),
                result.title(),
                result.source(),
                result.status(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
