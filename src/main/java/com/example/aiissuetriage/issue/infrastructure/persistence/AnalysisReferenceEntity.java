package com.example.aiissuetriage.issue.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "analysis_reference")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalysisReferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long issueAnalysisId;

    @Column(nullable = false)
    private Long knowledgeDocumentId;

    @Column(nullable = false)
    private double score;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public AnalysisReferenceEntity(
            Long id,
            Long issueAnalysisId,
            Long knowledgeDocumentId,
            double score,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.issueAnalysisId = issueAnalysisId;
        this.knowledgeDocumentId = knowledgeDocumentId;
        this.score = score;
        this.createdAt = createdAt;
    }
}
