package com.example.aiissuetriage.issue.application.command;

import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import java.util.List;

public record AnalyzeIssueCommand(
        Long issueId,
        String title,
        String content,
        List<KnowledgeSearchResult> references
) {
}
