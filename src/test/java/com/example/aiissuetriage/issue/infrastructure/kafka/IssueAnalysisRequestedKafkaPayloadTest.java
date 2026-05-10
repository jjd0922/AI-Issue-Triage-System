package com.example.aiissuetriage.issue.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class IssueAnalysisRequestedKafkaPayloadTest {

    @Test
    void 애플리케이션_이벤트를_Kafka_payload로_변환한다() {
        LocalDateTime requestedAt = LocalDateTime.of(2026, 5, 9, 10, 0);
        IssueAnalysisRequestedEvent event = new IssueAnalysisRequestedEvent(
                "evt-1",
                1L,
                "결제 오류",
                requestedAt
        );

        IssueAnalysisRequestedKafkaPayload payload = IssueAnalysisRequestedKafkaPayload.from(event);

        assertThat(payload.eventId()).isEqualTo("evt-1");
        assertThat(payload.issueId()).isEqualTo(1L);
        assertThat(payload.title()).isEqualTo("결제 오류");
        assertThat(payload.requestedAt()).isEqualTo(requestedAt);
    }
}
