package com.example.aiissuetriage.issue.application.result;

import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;

public record IssueSummaryResult(
        Long issueId,
        String title,
        IssueSource source,
        IssueStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
