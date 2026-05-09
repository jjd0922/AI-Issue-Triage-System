package com.example.aiissuetriage.issue.presentation.response;

import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import java.time.LocalDateTime;
import java.util.List;

public record IssueAnalysisResponse(
        Long issueId,
        Long analysisId,
        IssueCategory category,
        IssuePriority priority,
        String summary,
        String recommendation,
        double confidence,
        String modelName,
        List<KnowledgeReferenceResponse> references,
        LocalDateTime createdAt
) {

    public static IssueAnalysisResponse from(IssueAnalysisResult result) {
        return new IssueAnalysisResponse(
                result.issueId(),
                result.analysisId(),
                result.category(),
                result.priority(),
                result.summary(),
                result.recommendation(),
                result.confidence(),
                result.modelName(),
                result.references().stream()
                        .map(KnowledgeReferenceResponse::from)
                        .toList(),
                result.createdAt()
        );
    }
}
