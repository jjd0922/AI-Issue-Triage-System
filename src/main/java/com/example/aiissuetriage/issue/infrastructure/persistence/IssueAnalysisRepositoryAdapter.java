package com.example.aiissuetriage.issue.infrastructure.persistence;

import com.example.aiissuetriage.issue.application.port.IssueAnalysisRepositoryPort;
import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnBean(IssueAnalysisJpaRepository.class)
public class IssueAnalysisRepositoryAdapter implements IssueAnalysisRepositoryPort {

    private final IssueAnalysisJpaRepository issueAnalysisJpaRepository;

    @Override
    public IssueAnalysis save(IssueAnalysis analysis) {
        IssueAnalysisEntity savedEntity = issueAnalysisJpaRepository.save(
                IssueAnalysisPersistenceMapper.toEntity(analysis)
        );
        return IssueAnalysisPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<IssueAnalysis> findLatestByIssueId(Long issueId) {
        return issueAnalysisJpaRepository.findFirstByIssueIdOrderByCreatedAtDesc(issueId)
                .map(IssueAnalysisPersistenceMapper::toDomain);
    }
}
