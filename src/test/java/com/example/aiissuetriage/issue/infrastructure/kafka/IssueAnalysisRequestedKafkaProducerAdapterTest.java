package com.example.aiissuetriage.issue.infrastructure.kafka;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("unchecked")
class IssueAnalysisRequestedKafkaProducerAdapterTest {

    private final KafkaTemplate<String, IssueAnalysisRequestedKafkaPayload> kafkaTemplate =
            Mockito.mock(KafkaTemplate.class);
    private final IssueAnalysisRequestedKafkaProducerAdapter adapter =
            new IssueAnalysisRequestedKafkaProducerAdapter(kafkaTemplate);

    @Test
    void 분석_요청_이벤트를_Kafka로_발행한다() {
        ReflectionTestUtils.setField(adapter, "topic", "issue-analysis-requested");
        IssueAnalysisRequestedEvent event = new IssueAnalysisRequestedEvent(
                "evt-1",
                1L,
                "결제 오류",
                LocalDateTime.of(2026, 5, 9, 10, 0)
        );

        adapter.publish(event);

        ArgumentCaptor<IssueAnalysisRequestedKafkaPayload> payloadCaptor =
                ArgumentCaptor.forClass(IssueAnalysisRequestedKafkaPayload.class);
        verify(kafkaTemplate).send(eq("issue-analysis-requested"), eq("1"), payloadCaptor.capture());
        IssueAnalysisRequestedKafkaPayload payload = payloadCaptor.getValue();
        org.assertj.core.api.Assertions.assertThat(payload.eventId()).isEqualTo("evt-1");
        org.assertj.core.api.Assertions.assertThat(payload.issueId()).isEqualTo(1L);
    }
}
