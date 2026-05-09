package com.example.aiissuetriage.issue.application.result;

import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;

public record IssueDetailResult(
        Long issueId,
        String title,
        String content,
        IssueSource source,
        IssueStatus status,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
