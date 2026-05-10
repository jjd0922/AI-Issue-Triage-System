package com.example.aiissuetriage.issue.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Issue {

    private final Long id;
    private final String title;
    private final String content;
    private final IssueSource source;
    private IssueStatus status;
    private String failureReason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime analysisRequestedAt;
    private LocalDateTime analysisStartedAt;
    private LocalDateTime analysisCompletedAt;
    private LocalDateTime closedAt;

    private Issue(
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
        this.title = requireText(title, "title");
        this.content = requireText(content, "content");
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.failureReason = failureReason;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        this.analysisRequestedAt = analysisRequestedAt;
        this.analysisStartedAt = analysisStartedAt;
        this.analysisCompletedAt = analysisCompletedAt;
        this.closedAt = closedAt;
    }

    public static Issue create(String title, String content, IssueSource source) {
        LocalDateTime now = LocalDateTime.now();
        return new Issue(
                null,
                title,
                content,
                source,
                IssueStatus.REGISTERED,
                null,
                now,
                now,
                null,
                null,
                null,
                null
        );
    }

    public static Issue restore(
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
        return new Issue(
                id,
                title,
                content,
                source,
                status,
                failureReason,
                createdAt,
                updatedAt,
                analysisRequestedAt,
                analysisStartedAt,
                analysisCompletedAt,
                closedAt
        );
    }

    public void requestAnalysis() {
        if (status != IssueStatus.REGISTERED && status != IssueStatus.ANALYSIS_FAILED) {
            throw invalidTransition("requestAnalysis", IssueStatus.ANALYSIS_REQUESTED);
        }

        status = IssueStatus.ANALYSIS_REQUESTED;
        failureReason = null;
        analysisRequestedAt = LocalDateTime.now();
        touch();
    }

    public void startAnalysis() {
        if (status != IssueStatus.ANALYSIS_REQUESTED) {
            throw invalidTransition("startAnalysis", IssueStatus.ANALYZING);
        }

        status = IssueStatus.ANALYZING;
        analysisStartedAt = LocalDateTime.now();
        touch();
    }

    public void completeAnalysis() {
        if (status != IssueStatus.ANALYZING) {
            throw invalidTransition("completeAnalysis", IssueStatus.ANALYZED);
        }

        status = IssueStatus.ANALYZED;
        failureReason = null;
        analysisCompletedAt = LocalDateTime.now();
        touch();
    }

    public void failAnalysis(String reason) {
        if (status != IssueStatus.ANALYSIS_REQUESTED && status != IssueStatus.ANALYZING) {
            throw invalidTransition("failAnalysis", IssueStatus.ANALYSIS_FAILED);
        }

        status = IssueStatus.ANALYSIS_FAILED;
        failureReason = requireText(reason, "reason");
        touch();
    }

    public void close() {
        if (status != IssueStatus.ANALYZED && status != IssueStatus.ANALYSIS_FAILED) {
            throw invalidTransition("close", IssueStatus.CLOSED);
        }

        status = IssueStatus.CLOSED;
        closedAt = LocalDateTime.now();
        touch();
    }

    private InvalidIssueStatusException invalidTransition(String action, IssueStatus targetStatus) {
        return new InvalidIssueStatusException(
                "Cannot " + action + " issue from " + status + " to " + targetStatus
        );
    }

    private void touch() {
        updatedAt = LocalDateTime.now();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public IssueSource getSource() {
        return source;
    }

    public IssueStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getAnalysisRequestedAt() {
        return analysisRequestedAt;
    }

    public LocalDateTime getAnalysisStartedAt() {
        return analysisStartedAt;
    }

    public LocalDateTime getAnalysisCompletedAt() {
        return analysisCompletedAt;
    }

    public LocalDateTime getClosedAt() {
        return closedAt;
    }
}
