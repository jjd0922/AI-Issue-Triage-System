package com.example.aiissuetriage.issue.infrastructure.persistence;

import com.example.aiissuetriage.issue.domain.IssueAnalysis;

final class IssueAnalysisPersistenceMapper {

    private IssueAnalysisPersistenceMapper() {
    }

    static IssueAnalysisEntity toEntity(IssueAnalysis analysis) {
        return new IssueAnalysisEntity(
                analysis.getId(),
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

    static IssueAnalysis toDomain(IssueAnalysisEntity entity) {
        return IssueAnalysis.restore(
                entity.getId(),
                entity.getIssueId(),
                entity.getCategory(),
                entity.getPriority(),
                entity.getSummary(),
                entity.getRecommendation(),
                entity.getConfidence(),
                entity.getModelName(),
                entity.getRawResponse(),
                entity.getCreatedAt()
        );
    }
}
