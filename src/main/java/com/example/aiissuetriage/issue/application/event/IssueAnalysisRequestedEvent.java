package com.example.aiissuetriage.issue.application.event;

import java.time.LocalDateTime;

public record IssueAnalysisRequestedEvent(
        String eventId,
        Long issueId,
        String title,
        LocalDateTime requestedAt
) {
}
