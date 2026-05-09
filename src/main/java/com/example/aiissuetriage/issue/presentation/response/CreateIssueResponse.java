package com.example.aiissuetriage.issue.presentation.response;

import com.example.aiissuetriage.issue.application.result.CreateIssueResult;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;

public record CreateIssueResponse(
        Long issueId,
        IssueStatus status,
        LocalDateTime createdAt
) {

    public static CreateIssueResponse from(CreateIssueResult result) {
        return new CreateIssueResponse(
                result.issueId(),
                result.status(),
                result.createdAt()
        );
    }
}
