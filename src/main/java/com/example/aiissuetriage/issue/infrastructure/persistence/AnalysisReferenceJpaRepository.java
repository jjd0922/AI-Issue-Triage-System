package com.example.aiissuetriage.issue.infrastructure.persistence;

import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisReferenceJpaRepository extends JpaRepository<AnalysisReferenceEntity, Long> {

    @Query("""
            select new com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult(
                reference.knowledgeDocumentId,
                document.title,
                reference.score
            )
            from AnalysisReferenceEntity reference
            join KnowledgeDocumentEntity document on document.id = reference.knowledgeDocumentId
            where reference.issueAnalysisId = :analysisId
            order by reference.score desc, reference.id asc
            """)
    List<KnowledgeSearchResult> findKnowledgeResultsByAnalysisId(@Param("analysisId") Long analysisId);
}
