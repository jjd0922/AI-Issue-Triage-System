package com.example.aiissuetriage.issue.infrastructure.persistence;

import com.example.aiissuetriage.issue.application.port.IssueAnalysisRepositoryPort;
import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnBean(IssueAnalysisJpaRepository.class)
public class IssueAnalysisRepositoryAdapter implements IssueAnalysisRepositoryPort {

    private final IssueAnalysisJpaRepository issueAnalysisJpaRepository;
    private final AnalysisReferenceJpaRepository analysisReferenceJpaRepository;

    @Override
    public IssueAnalysis save(IssueAnalysis analysis) {
        IssueAnalysisEntity savedEntity = issueAnalysisJpaRepository.save(
                IssueAnalysisPersistenceMapper.toEntity(analysis)
        );
        return IssueAnalysisPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public void saveReferences(Long analysisId, List<KnowledgeSearchResult> references) {
        if (references == null || references.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        analysisReferenceJpaRepository.saveAll(references.stream()
                .map(reference -> new AnalysisReferenceEntity(
                        null,
                        analysisId,
                        reference.knowledgeDocumentId(),
                        reference.score(),
                        now
                ))
                .toList());
    }

    @Override
    public Optional<IssueAnalysis> findLatestByIssueId(Long issueId) {
        return issueAnalysisJpaRepository.findFirstByIssueIdOrderByCreatedAtDesc(issueId)
                .map(IssueAnalysisPersistenceMapper::toDomain);
    }

    @Override
    public List<KnowledgeSearchResult> findReferencesByAnalysisId(Long analysisId) {
        return analysisReferenceJpaRepository.findKnowledgeResultsByAnalysisId(analysisId);
    }
}
