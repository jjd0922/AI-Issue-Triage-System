package com.example.aiissuetriage.issue.infrastructure.kafka;

import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;
import java.time.LocalDateTime;

public record IssueAnalysisRequestedKafkaPayload(
        String eventId,
        Long issueId,
        String title,
        LocalDateTime requestedAt
) {

    public static IssueAnalysisRequestedKafkaPayload from(IssueAnalysisRequestedEvent event) {
        return new IssueAnalysisRequestedKafkaPayload(
                event.eventId(),
                event.issueId(),
                event.title(),
                event.requestedAt()
        );
    }
}
