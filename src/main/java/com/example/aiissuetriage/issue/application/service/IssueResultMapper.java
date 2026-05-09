package com.example.aiissuetriage.issue.application.service;

import com.example.aiissuetriage.issue.application.result.CreateIssueResult;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.application.result.IssueDetailResult;
import com.example.aiissuetriage.issue.application.result.IssueSummaryResult;
import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import com.example.aiissuetriage.issue.domain.Issue;
import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import java.util.List;

final class IssueResultMapper {

    private IssueResultMapper() {
    }

    static CreateIssueResult toCreateResult(Issue issue) {
        return new CreateIssueResult(
                issue.getId(),
                issue.getStatus(),
                issue.getCreatedAt()
        );
    }

    static IssueDetailResult toDetailResult(Issue issue) {
        return new IssueDetailResult(
                issue.getId(),
                issue.getTitle(),
                issue.getContent(),
                issue.getSource(),
                issue.getStatus(),
                issue.getFailureReason(),
                issue.getCreatedAt(),
                issue.getUpdatedAt()
        );
    }

    static IssueSummaryResult toSummaryResult(Issue issue) {
        return new IssueSummaryResult(
                issue.getId(),
                issue.getTitle(),
                issue.getSource(),
                issue.getStatus(),
                issue.getCreatedAt(),
                issue.getUpdatedAt()
        );
    }

    static IssueAnalysisResult toAnalysisResult(IssueAnalysis analysis) {
        return toAnalysisResult(analysis, List.of());
    }

    static IssueAnalysisResult toAnalysisResult(
            IssueAnalysis analysis,
            List<KnowledgeSearchResult> references
    ) {
        return new IssueAnalysisResult(
                analysis.getIssueId(),
                analysis.getId(),
                analysis.getCategory(),
                analysis.getPriority(),
                analysis.getSummary(),
                analysis.getRecommendation(),
                analysis.getConfidence(),
                analysis.getModelName(),
                references,
                analysis.getCreatedAt()
        );
    }
}
