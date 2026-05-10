package com.example.aiissuetriage.issue.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IssueAnalysisRequestedKafkaPayloadTest {

    @Test
    @DisplayName("from 은 애플리케이션 이벤트를 Kafka payload 로 변환한다")
    void from_whenEventExists_thenConvertToKafkaPayload() {
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
