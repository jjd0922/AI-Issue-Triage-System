package com.example.aiissuetriage.issue.application.event;

import static org.mockito.Mockito.verify;

import com.example.aiissuetriage.issue.application.port.IssueAnalysisRequestedEventPublisher;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueAnalysisRequestedAfterCommitListenerTest {

    @Mock
    private IssueAnalysisRequestedEventPublisher eventPublisher;

    @InjectMocks
    private IssueAnalysisRequestedAfterCommitListener listener;

    @Test
    @DisplayName("publish 는 트랜잭션 커밋 이후 분석 요청 이벤트를 발행한다")
    void publish_whenTransactionCommitted_thenPublishAnalysisRequestedEvent() {
        IssueAnalysisRequestedEvent event = new IssueAnalysisRequestedEvent(
                "evt-1",
                1L,
                "결제 오류",
                LocalDateTime.of(2026, 5, 9, 10, 0)
        );

        listener.publish(event);

        verify(eventPublisher).publish(event);
    }
}
