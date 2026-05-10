package com.example.aiissuetriage.issue.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IssueAnalysisTest {

    @Test
    @DisplayName("create 는 분석 결과를 생성한다")
    void create_whenValidInput_thenCreateIssueAnalysis() {
        IssueAnalysis analysis = IssueAnalysis.create(
                1L,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "결제 및 주문 생성 실패",
                "결제 이벤트와 주문 트랜잭션 로그를 확인합니다.",
                0.85,
                "mock-ai-analysis",
                "{}"
        );

        assertThat(analysis.getIssueId()).isEqualTo(1L);
        assertThat(analysis.getCategory()).isEqualTo(IssueCategory.PAYMENT);
        assertThat(analysis.getPriority()).isEqualTo(IssuePriority.CRITICAL);
        assertThat(analysis.getConfidence()).isEqualTo(0.85);
        assertThat(analysis.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("create 는 신뢰도가 0과 1 사이가 아니면 예외를 던진다")
    void create_whenConfidenceIsOutOfRange_thenThrowException() {
        assertThatThrownBy(() -> IssueAnalysis.create(
                1L,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "summary",
                "recommendation",
                1.1,
                "mock-ai-analysis",
                "{}"
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("confidence must be between 0.0 and 1.0");
    }
}
