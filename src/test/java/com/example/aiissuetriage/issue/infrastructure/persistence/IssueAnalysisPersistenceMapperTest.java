package com.example.aiissuetriage.issue.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class IssueAnalysisPersistenceMapperTest {

    @Test
    void IssueAnalysis를_Entity로_변환하고_다시_도메인으로_복원한다() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 9, 10, 0);
        IssueAnalysis analysis = IssueAnalysis.restore(
                10L,
                1L,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "결제 후 주문 생성 실패",
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
