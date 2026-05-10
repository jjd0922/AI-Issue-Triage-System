package com.example.aiissuetriage.issue.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.example.aiissuetriage.issue.application.service.IssueAnalysisService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueAnalysisRequestedKafkaConsumerTest {

    @Mock
    private IssueAnalysisService issueAnalysisService;

    @InjectMocks
    private IssueAnalysisRequestedKafkaConsumer consumer;

    @Test
    @DisplayName("consume 은 분석 요청 이벤트를 수신하면 분석 처리를 호출한다")
    void consume_whenAnalysisRequestedPayloadReceived_thenProcessAnalysis() {
        IssueAnalysisRequestedKafkaPayload payload = payload();

        consumer.consume(payload);

        verify(issueAnalysisService).processAnalysis(1L);
    }

    @Test
    @DisplayName("consume 은 분석 처리가 실패하면 예외를 다시 던진다")
    void consume_whenAnalysisProcessingFails_thenRethrowException() {
        IssueAnalysisRequestedKafkaPayload payload = payload();
        doThrow(new IllegalStateException("analysis failed"))
                .when(issueAnalysisService)
                .processAnalysis(1L);

        assertThatThrownBy(() -> consumer.consume(payload))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("analysis failed");
    }

    private IssueAnalysisRequestedKafkaPayload payload() {
        return new IssueAnalysisRequestedKafkaPayload(
                "evt-1",
                1L,
                "결제 오류",
                LocalDateTime.of(2026, 5, 9, 10, 0)
        );
    }
}
