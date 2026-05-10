package com.example.aiissuetriage.issue.infrastructure.persistence;

import com.example.aiissuetriage.issue.domain.Issue;

final class IssuePersistenceMapper {

    private IssuePersistenceMapper() {
    }

    static IssueEntity toNewEntity(Issue issue) {
        return new IssueEntity(
                null,
                issue.getTitle(),
                issue.getContent(),
                issue.getSource(),
                issue.getStatus(),
                issue.getFailureReason(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getAnalysisRequestedAt(),
                issue.getAnalysisStartedAt(),
                issue.getAnalysisCompletedAt(),
                issue.getClosedAt()
        );
    }

    static void updateEntity(IssueEntity entity, Issue issue) {
        entity.update(
                issue.getTitle(),
                issue.getContent(),
                issue.getSource(),
                issue.getStatus(),
                issue.getFailureReason(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getAnalysisRequestedAt(),
                issue.getAnalysisStartedAt(),
                issue.getAnalysisCompletedAt(),
                issue.getClosedAt()
        );
    }

    static Issue toDomain(IssueEntity entity) {
        return Issue.restore(
                entity.getId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getSource(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getAnalysisRequestedAt(),
                entity.getAnalysisStartedAt(),
                entity.getAnalysisCompletedAt(),
                entity.getClosedAt()
        );
    }
}
