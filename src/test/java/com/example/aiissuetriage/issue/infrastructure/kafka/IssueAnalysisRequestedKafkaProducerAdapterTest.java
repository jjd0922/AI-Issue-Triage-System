package com.example.aiissuetriage.issue.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class IssueAnalysisRequestedKafkaProducerAdapterTest {

    @Mock
    private KafkaTemplate<String, IssueAnalysisRequestedKafkaPayload> kafkaTemplate;

    @InjectMocks
    private IssueAnalysisRequestedKafkaProducerAdapter adapter;

    @Test
    @DisplayName("publish 는 분석 요청 이벤트를 Kafka 로 발행한다")
    void publish_whenAnalysisRequestedEvent_thenSendKafkaPayload() {
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
        assertThat(payload.eventId()).isEqualTo("evt-1");
        assertThat(payload.issueId()).isEqualTo(1L);
    }
}
