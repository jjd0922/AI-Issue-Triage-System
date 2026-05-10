package com.example.aiissuetriage.issue.application.service;

import com.example.aiissuetriage.issue.application.command.CreateIssueCommand;
import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;
import com.example.aiissuetriage.issue.application.exception.InvalidRetryConditionException;
import com.example.aiissuetriage.issue.application.exception.IssueNotFoundException;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.application.result.CreateIssueResult;
import com.example.aiissuetriage.issue.application.result.RetryIssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.Issue;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueCommandService {

    private static final Logger log = LoggerFactory.getLogger(IssueCommandService.class);

    private final IssueRepositoryPort issueRepositoryPort;
    private final AnalysisCachePort analysisCachePort;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public CreateIssueResult createIssue(CreateIssueCommand command) {
        Issue issue = Issue.create(command.title(), command.content(), command.source());
        Issue savedIssue = issueRepositoryPort.save(issue);

        savedIssue.requestAnalysis();
        Issue analysisRequestedIssue = issueRepositoryPort.save(savedIssue);

        publishAnalysisRequestedEvent(analysisRequestedIssue);

        return IssueResultMapper.toCreateResult(analysisRequestedIssue);
    }

    @Transactional
    public RetryIssueAnalysisResult retryAnalysis(Long issueId) {
        Issue issue = issueRepositoryPort.findById(issueId)
                .orElseThrow(() -> new IssueNotFoundException(issueId));

        if (issue.getStatus() != IssueStatus.ANALYSIS_FAILED) {
            throw new InvalidRetryConditionException(issueId, issue.getStatus());
        }

        issue.requestAnalysis();
        Issue savedIssue = issueRepositoryPort.save(issue);
        evictAnalysisCache(savedIssue.getId());
        publishAnalysisRequestedEvent(savedIssue);

        return new RetryIssueAnalysisResult(
                savedIssue.getId(),
                savedIssue.getStatus(),
                savedIssue.getAnalysisRequestedAt()
        );
    }

    private void publishAnalysisRequestedEvent(Issue issue) {
        LocalDateTime requestedAt = issue.getAnalysisRequestedAt();
        applicationEventPublisher.publishEvent(new IssueAnalysisRequestedEvent(
                "evt-" + UUID.randomUUID(),
                issue.getId(),
                issue.getTitle(),
                requestedAt
        ));
    }

    private void evictAnalysisCache(Long issueId) {
        try {
            analysisCachePort.evict(issueId);
        } catch (RuntimeException e) {
            log.warn("Failed to evict issue analysis cache. issueId={}", issueId, e);
        }
    }
}
