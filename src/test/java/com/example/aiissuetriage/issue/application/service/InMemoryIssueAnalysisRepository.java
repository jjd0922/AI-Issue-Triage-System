package com.example.aiissuetriage.issue.application.service;

import com.example.aiissuetriage.issue.application.port.IssueAnalysisRepositoryPort;
import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

class InMemoryIssueAnalysisRepository implements IssueAnalysisRepositoryPort {

    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, IssueAnalysis> analyses = new LinkedHashMap<>();
    int findLatestCallCount;

    @Override
    public IssueAnalysis save(IssueAnalysis analysis) {
        IssueAnalysis savedAnalysis = analysis.getId() == null
                ? withId(analysis, sequence.getAndIncrement())
                : analysis;
        analyses.put(savedAnalysis.getId(), savedAnalysis);
        return savedAnalysis;
    }

    @Override
    public Optional<IssueAnalysis> findLatestByIssueId(Long issueId) {
        findLatestCallCount++;
        return analyses.values().stream()
                .filter(analysis -> analysis.getIssueId().equals(issueId))
                .max(Comparator.comparing(IssueAnalysis::getCreatedAt));
    }

    private IssueAnalysis withId(IssueAnalysis analysis, Long id) {
        return IssueAnalysis.restore(
                id,
                analysis.getIssueId(),
                analysis.getCategory(),
                analysis.getPriority(),
                analysis.getSummary(),
                analysis.getRecommendation(),
                analysis.getConfidence(),
                analysis.getModelName(),
                analysis.getRawResponse(),
                analysis.getCreatedAt()
        );
    }
}
