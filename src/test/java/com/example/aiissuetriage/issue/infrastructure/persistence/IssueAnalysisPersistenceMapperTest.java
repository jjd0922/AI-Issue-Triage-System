package com.example.aiissuetriage.issue.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IssueAnalysisPersistenceMapperTest {

    @Test
    @DisplayName("IssueAnalysis 를 Entity 로 변환하고 다시 도메인으로 복원한다")
    void toEntityAndToDomain_whenIssueAnalysisExists_thenRestoreIssueAnalysis() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 9, 10, 0);
        IssueAnalysis analysis = IssueAnalysis.restore(
                10L,
                1L,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "결제 및 주문 생성 실패",
                "결제 이벤트와 주문 트랜잭션 로그를 확인합니다.",
                0.85,
                "mock-ai-analysis",
                "{}",
                now
        );

        IssueAnalysisEntity entity = IssueAnalysisPersistenceMapper.toEntity(analysis);
        IssueAnalysis restoredAnalysis = IssueAnalysisPersistenceMapper.toDomain(entity);

        assertThat(restoredAnalysis.getId()).isEqualTo(analysis.getId());
        assertThat(restoredAnalysis.getIssueId()).isEqualTo(analysis.getIssueId());
        assertThat(restoredAnalysis.getCategory()).isEqualTo(analysis.getCategory());
        assertThat(restoredAnalysis.getPriority()).isEqualTo(analysis.getPriority());
        assertThat(restoredAnalysis.getSummary()).isEqualTo(analysis.getSummary());
        assertThat(restoredAnalysis.getRecommendation()).isEqualTo(analysis.getRecommendation());
        assertThat(restoredAnalysis.getConfidence()).isEqualTo(analysis.getConfidence());
        assertThat(restoredAnalysis.getModelName()).isEqualTo(analysis.getModelName());
        assertThat(restoredAnalysis.getRawResponse()).isEqualTo(analysis.getRawResponse());
        assertThat(restoredAnalysis.getCreatedAt()).isEqualTo(analysis.getCreatedAt());
    }
}
