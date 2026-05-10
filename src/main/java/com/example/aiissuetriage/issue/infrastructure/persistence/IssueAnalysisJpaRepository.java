package com.example.aiissuetriage.issue.infrastructure.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueAnalysisJpaRepository extends JpaRepository<IssueAnalysisEntity, Long> {

    Optional<IssueAnalysisEntity> findFirstByIssueIdOrderByCreatedAtDesc(Long issueId);
}
