package com.example.aiissuetriage.issue.presentation.response;

import com.example.aiissuetriage.issue.application.result.IssueDetailResult;
import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;

public record IssueDetailResponse(
        Long issueId,
        String title,
        String content,
        IssueSource source,
        IssueStatus status,
        String failureReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static IssueDetailResponse from(IssueDetailResult result) {
        return new IssueDetailResponse(
                result.issueId(),
                result.title(),
                result.content(),
                result.source(),
                result.status(),
                result.failureReason(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
