package com.example.aiissuetriage.issue.infrastructure.persistence;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KnowledgeDocumentJpaRepository extends JpaRepository<KnowledgeDocumentEntity, Long> {

    @Query("""
            select document
            from KnowledgeDocumentEntity document
            where lower(document.title) like lower(concat('%', :query, '%'))
               or lower(document.content) like lower(concat('%', :query, '%'))
            order by document.updatedAt desc, document.id desc
            """)
    List<KnowledgeDocumentEntity> searchByTitleOrContent(
            @Param("query") String query,
            Pageable pageable
    );
}
