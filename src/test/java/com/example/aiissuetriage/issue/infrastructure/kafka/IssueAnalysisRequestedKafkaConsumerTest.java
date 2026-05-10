package com.example.aiissuetriage.issue.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.example.aiissuetriage.issue.application.service.IssueAnalysisService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IssueAnalysisRequestedKafkaConsumerTest {

    private final IssueAnalysisService issueAnalysisService = Mockito.mock(IssueAnalysisService.class);
    private final IssueAnalysisRequestedKafkaConsumer consumer =
            new IssueAnalysisRequestedKafkaConsumer(issueAnalysisService);

    @Test
    void 분석_요청_이벤트를_수신하면_분석_처리를_호출한다() {
        IssueAnalysisRequestedKafkaPayload payload = payload();

        consumer.consume(payload);

        verify(issueAnalysisService).processAnalysis(1L);
    }

    @Test
    void 분석_처리가_실패하면_예외를_다시_던진다() {
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
