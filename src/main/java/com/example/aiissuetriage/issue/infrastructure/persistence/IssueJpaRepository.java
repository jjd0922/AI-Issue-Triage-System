package com.example.aiissuetriage.issue.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IssueJpaRepository extends JpaRepository<IssueEntity, Long>, JpaSpecificationExecutor<IssueEntity> {
}
