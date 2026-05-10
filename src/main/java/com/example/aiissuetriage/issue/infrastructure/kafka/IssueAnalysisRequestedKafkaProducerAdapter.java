package com.example.aiissuetriage.issue.infrastructure.kafka;

import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;
import com.example.aiissuetriage.issue.application.port.IssueAnalysisRequestedEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(KafkaTemplate.class)
public class IssueAnalysisRequestedKafkaProducerAdapter implements IssueAnalysisRequestedEventPublisher {

    private final KafkaTemplate<String, IssueAnalysisRequestedKafkaPayload> kafkaTemplate;

    @Value("${issue.kafka.topic.issue-analysis-requested:issue-analysis-requested}")
    private String topic;

    @Override
    public void publish(IssueAnalysisRequestedEvent event) {
        kafkaTemplate.send(
                topic,
                event.issueId().toString(),
                IssueAnalysisRequestedKafkaPayload.from(event)
        );
    }
}
