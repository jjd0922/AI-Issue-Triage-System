package com.example.aiissuetriage.issue.infrastructure.kafka;

import com.example.aiissuetriage.issue.application.service.IssueAnalysisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IssueAnalysisRequestedKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(IssueAnalysisRequestedKafkaConsumer.class);

    private final IssueAnalysisService issueAnalysisService;

    @KafkaListener(
            topics = "${issue.kafka.topic.issue-analysis-requested:issue-analysis-requested}",
            groupId = "${spring.kafka.consumer.group-id:ai-issue-triage}"
    )
    public void consume(IssueAnalysisRequestedKafkaPayload payload) {
        try {
            log.info(
                    "Consume issue analysis requested event. eventId={}, issueId={}",
                    payload.eventId(),
                    payload.issueId()
            );
            issueAnalysisService.processAnalysis(payload.issueId());
        } catch (RuntimeException e) {
            log.error(
                    "Failed to process issue analysis requested event. eventId={}, issueId={}",
                    payload.eventId(),
                    payload.issueId(),
                    e
            );
            throw e;
        }
    }
}
