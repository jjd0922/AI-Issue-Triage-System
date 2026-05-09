package com.example.aiissuetriage.issue.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeDocumentJpaRepository extends JpaRepository<KnowledgeDocumentEntity, Long> {
}
