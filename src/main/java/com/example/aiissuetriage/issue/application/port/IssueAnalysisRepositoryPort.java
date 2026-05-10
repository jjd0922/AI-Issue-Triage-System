package com.example.aiissuetriage.issue.application.port;

import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import java.util.List;
import java.util.Optional;

public interface IssueAnalysisRepositoryPort {

    IssueAnalysis save(IssueAnalysis analysis);

    void saveReferences(Long analysisId, List<KnowledgeSearchResult> references);

    Optional<IssueAnalysis> findLatestByIssueId(Long issueId);

    List<KnowledgeSearchResult> findReferencesByAnalysisId(Long analysisId);
}
