package com.example.aiissuetriage.issue.infrastructure.persistence;

import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "issue")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IssueSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IssueStatus status;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime analysisRequestedAt;

    private LocalDateTime analysisStartedAt;

    private LocalDateTime analysisCompletedAt;

    private LocalDateTime closedAt;

    IssueEntity(
            Long id,
            String title,
            String content,
            IssueSource source,
            IssueStatus status,
            String failureReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime analysisRequestedAt,
            LocalDateTime analysisStartedAt,
            LocalDateTime analysisCompletedAt,
            LocalDateTime closedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.source = source;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.analysisRequestedAt = analysisRequestedAt;
        this.analysisStartedAt = analysisStartedAt;
        this.analysisCompletedAt = analysisCompletedAt;
        this.closedAt = closedAt;
    }

    void update(
            String title,
            String content,
            IssueSource source,
            IssueStatus status,
            String failureReason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime analysisRequestedAt,
            LocalDateTime analysisStartedAt,
            LocalDateTime analysisCompletedAt,
            LocalDateTime closedAt
    ) {
        this.title = title;
        this.content = content;
        this.source = source;
        this.status = status;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.analysisRequestedAt = analysisRequestedAt;
        this.analysisStartedAt = analysisStartedAt;
        this.analysisCompletedAt = analysisCompletedAt;
        this.closedAt = closedAt;
    }
}
