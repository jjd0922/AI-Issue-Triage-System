package com.example.aiissuetriage.issue.infrastructure.vector;

import com.example.aiissuetriage.issue.application.port.KnowledgeSearchPort;
import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import com.example.aiissuetriage.issue.infrastructure.persistence.KnowledgeDocumentJpaRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseKnowledgeSearchAdapter implements KnowledgeSearchPort {

    private final KnowledgeDocumentJpaRepository knowledgeDocumentJpaRepository;

    @Override
    public List<KnowledgeSearchResult> search(String query, int limit) {
        if (query == null || query.isBlank() || limit <= 0) {
            return List.of();
        }
        return knowledgeDocumentJpaRepository.searchByTitleOrContent(query.trim(), PageRequest.of(0, limit))
                .stream()
                .map(document -> new KnowledgeSearchResult(
                        document.getId(),
                        document.getTitle(),
                        1.0
                ))
                .toList();
    }
}
