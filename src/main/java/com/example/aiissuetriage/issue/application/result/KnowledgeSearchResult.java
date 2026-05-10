package com.example.aiissuetriage.issue.application.result;

public record KnowledgeSearchResult(
        Long knowledgeDocumentId,
        String title,
        double score
) {
}
