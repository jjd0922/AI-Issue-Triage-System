package com.example.aiissuetriage.issue.presentation.response;

import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;

public record KnowledgeReferenceResponse(
        Long knowledgeDocumentId,
        String title,
        double score
) {

    public static KnowledgeReferenceResponse from(KnowledgeSearchResult result) {
        return new KnowledgeReferenceResponse(
                result.knowledgeDocumentId(),
                result.title(),
                result.score()
        );
    }
}
