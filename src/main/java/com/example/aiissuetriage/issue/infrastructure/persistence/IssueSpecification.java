package com.example.aiissuetriage.issue.infrastructure.persistence;

import com.example.aiissuetriage.issue.application.command.IssueSearchCommand;
import org.springframework.data.jpa.domain.Specification;

final class IssueSpecification {

    private IssueSpecification() {
    }

    static Specification<IssueEntity> from(IssueSearchCommand command) {
        if (command == null) {
            return Specification.unrestricted();
        }
        return Specification.allOf(
                statusEquals(command),
                sourceEquals(command)
        );
    }

    private static Specification<IssueEntity> statusEquals(IssueSearchCommand command) {
        return (root, query, criteriaBuilder) -> command.status() == null
                ? null
                : criteriaBuilder.equal(root.get("status"), command.status());
    }

    private static Specification<IssueEntity> sourceEquals(IssueSearchCommand command) {
        return (root, query, criteriaBuilder) -> command.source() == null
                ? null
                : criteriaBuilder.equal(root.get("source"), command.source());
    }
}
