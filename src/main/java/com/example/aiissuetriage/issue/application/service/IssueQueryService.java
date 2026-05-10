package com.example.aiissuetriage.issue.application.service;

import com.example.aiissuetriage.issue.application.command.IssueSearchCommand;
import com.example.aiissuetriage.issue.application.exception.IssueAnalysisNotFoundException;
import com.example.aiissuetriage.issue.application.exception.IssueNotFoundException;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.port.IssueAnalysisRepositoryPort;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.application.result.IssueDetailResult;
import com.example.aiissuetriage.issue.application.result.IssueSummaryResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueQueryService {

    private static final Logger log = LoggerFactory.getLogger(IssueQueryService.class);

    private final IssueRepositoryPort issueRepositoryPort;
    private final IssueAnalysisRepositoryPort issueAnalysisRepositoryPort;
    private final AnalysisCachePort analysisCachePort;

    @Transactional(readOnly = true)
    public IssueDetailResult getIssue(Long issueId) {
        return issueRepositoryPort.findById(issueId)
                .map(IssueResultMapper::toDetailResult)
                .orElseThrow(() -> new IssueNotFoundException(issueId));
    }

    @Transactional(readOnly = true)
    public Page<IssueSummaryResult> getIssues(IssueSearchCommand command, Pageable pageable) {
        return issueRepositoryPort.findAll(command, pageable)
                .map(IssueResultMapper::toSummaryResult);
    }

    @Transactional(readOnly = true)
    public IssueAnalysisResult getAnalysis(Long issueId) {
        return getCachedAnalysis(issueId)
                .orElseGet(() -> findAnalysisFromRepository(issueId));
    }

    private java.util.Optional<IssueAnalysisResult> getCachedAnalysis(Long issueId) {
        try {
            return analysisCachePort.get(issueId);
        } catch (RuntimeException e) {
            log.warn("Failed to get issue analysis cache. issueId={}", issueId, e);
            return java.util.Optional.empty();
        }
    }

    private IssueAnalysisResult findAnalysisFromRepository(Long issueId) {
        IssueAnalysisResult result = issueAnalysisRepositoryPort.findLatestByIssueId(issueId)
                .map(IssueResultMapper::toAnalysisResult)
                .orElseThrow(() -> new IssueAnalysisNotFoundException(issueId));

        putCache(issueId, result);
        return result;
    }

    private void putCache(Long issueId, IssueAnalysisResult result) {
        try {
            analysisCachePort.put(issueId, result);
        } catch (RuntimeException e) {
            log.warn("Failed to put issue analysis cache. issueId={}", issueId, e);
        }
    }
}
