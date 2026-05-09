package com.example.aiissuetriage.issue.application.event;

import com.example.aiissuetriage.issue.application.port.IssueAnalysisRequestedEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class IssueAnalysisRequestedAfterCommitListener {

    private final IssueAnalysisRequestedEventPublisher eventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(IssueAnalysisRequestedEvent event) {
        eventPublisher.publish(event);
    }
}
