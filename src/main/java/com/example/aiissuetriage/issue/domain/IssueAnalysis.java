package com.example.aiissuetriage.issue.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class IssueAnalysis {

    private final Long id;
    private final Long issueId;
    private final IssueCategory category;
    private final IssuePriority priority;
    private final String summary;
    private final String recommendation;
    private final double confidence;
    private final String modelName;
    private final String rawResponse;
    private final LocalDateTime createdAt;

    private IssueAnalysis(
            Long id,
            Long issueId,
            IssueCategory category,
            IssuePriority priority,
            String summary,
            String recommendation,
            double confidence,
            String modelName,
            String rawResponse,
            LocalDateTime createdAt
    ) {
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be between 0.0 and 1.0");
        }
        this.id = id;
        this.issueId = Objects.requireNonNull(issueId, "issueId must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.priority = Objects.requireNonNull(priority, "priority must not be null");
        this.summary = requireText(summary, "summary");
        this.recommendation = requireText(recommendation, "recommendation");
        this.confidence = confidence;
        this.modelName = requireText(modelName, "modelName");
        this.rawResponse = rawResponse;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    public static IssueAnalysis create(
            Long issueId,
            IssueCategory category,
            IssuePriority priority,
            String summary,
            String recommendation,
            double confidence,
            String modelName,
            String rawResponse
    ) {
        return new IssueAnalysis(
                null,
                issueId,
                category,
                priority,
                summary,
                recommendation,
                confidence,
                modelName,
                rawResponse,
                LocalDateTime.now()
        );
    }

    public static IssueAnalysis restore(
            Long id,
            Long issueId,
            IssueCategory category,
            IssuePriority priority,
            String summary,
            String recommendation,
            double confidence,
            String modelName,
            String rawResponse,
            LocalDateTime createdAt
    ) {
        return new IssueAnalysis(
                id,
                issueId,
                category,
                priority,
                summary,
                recommendation,
                confidence,
                modelName,
                rawResponse,
                createdAt
        );
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

    public Long getIssueId() {
        return issueId;
    }

    public IssueCategory getCategory() {
        return category;
    }

    public IssuePriority getPriority() {
        return priority;
    }

    public String getSummary() {
        return summary;
    }

    public String getRecommendation() {
        return recommendation;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getModelName() {
        return modelName;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
