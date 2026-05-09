package com.example.aiissuetriage.issue.application.event;

import static org.mockito.Mockito.verify;

import com.example.aiissuetriage.issue.application.port.IssueAnalysisRequestedEventPublisher;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IssueAnalysisRequestedAfterCommitListenerTest {

    private final IssueAnalysisRequestedEventPublisher eventPublisher =
            Mockito.mock(IssueAnalysisRequestedEventPublisher.class);
    private final IssueAnalysisRequestedAfterCommitListener listener =
            new IssueAnalysisRequestedAfterCommitListener(eventPublisher);

    @Test
    void 트랜잭션_커밋_이후_분석_요청_이벤트를_발행한다() {
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
