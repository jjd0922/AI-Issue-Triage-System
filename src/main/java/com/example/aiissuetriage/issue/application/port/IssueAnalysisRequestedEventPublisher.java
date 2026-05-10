package com.example.aiissuetriage.issue.application.port;

import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;

public interface IssueAnalysisRequestedEventPublisher {

    void publish(IssueAnalysisRequestedEvent event);
}
