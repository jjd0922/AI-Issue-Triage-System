package com.example.aiissuetriage.issue.application.result;

import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import java.time.LocalDateTime;
import java.util.List;

public record IssueAnalysisResult(
        Long issueId,
        Long analysisId,
        IssueCategory category,
        IssuePriority priority,
        String summary,
        String recommendation,
        double confidence,
        String modelName,
        List<KnowledgeSearchResult> references,
        LocalDateTime createdAt
) {
}
