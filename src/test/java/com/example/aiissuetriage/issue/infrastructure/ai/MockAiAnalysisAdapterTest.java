package com.example.aiissuetriage.issue.infrastructure.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiissuetriage.issue.application.command.AnalyzeIssueCommand;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MockAiAnalysisAdapterTest {

    private final MockAiAnalysisAdapter adapter = new MockAiAnalysisAdapter();

    @Test
    @DisplayName("analyze 는 결제 키워드를 PAYMENT/CRITICAL 로 분석한다")
    void analyze_whenPaymentKeywordExists_thenReturnPaymentCritical() {
        IssueAnalysisResult result = analyze("결제가 완료됐는데 주문이 생성되지 않았습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.PAYMENT);
        assertThat(result.priority()).isEqualTo(IssuePriority.CRITICAL);
        assertThat(result.modelName()).isEqualTo("mock-ai-analysis");
        assertThat(result.summary()).contains("결제");
    }

    @Test
    @DisplayName("analyze 는 교환 또는 재고 키워드를 EXCHANGE/HIGH 로 분석한다")
    void analyze_whenExchangeKeywordExists_thenReturnExchangeHigh() {
        IssueAnalysisResult result = analyze("교환 요청 중 재고가 맞지 않습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.EXCHANGE);
        assertThat(result.priority()).isEqualTo(IssuePriority.HIGH);
    }

    @Test
    @DisplayName("analyze 는 알림 키워드를 NOTIFICATION/MEDIUM 으로 분석한다")
    void analyze_whenNotificationKeywordExists_thenReturnNotificationMedium() {
        IssueAnalysisResult result = analyze("알림톡 발송이 실패했습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.NOTIFICATION);
        assertThat(result.priority()).isEqualTo(IssuePriority.MEDIUM);
    }

    @Test
    @DisplayName("analyze 는 쿠폰 또는 Duplicate 키워드를 COUPON/HIGH 로 분석한다")
    void analyze_whenCouponKeywordExists_thenReturnCouponHigh() {
        IssueAnalysisResult result = analyze("쿠폰 Duplicate 오류가 발생했습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.COUPON);
        assertThat(result.priority()).isEqualTo(IssuePriority.HIGH);
    }

    @Test
    @DisplayName("analyze 는 정산 키워드를 SETTLEMENT/HIGH 로 분석한다")
    void analyze_whenSettlementKeywordExists_thenReturnSettlementHigh() {
        IssueAnalysisResult result = analyze("정산 금액이 맞지 않습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.SETTLEMENT);
        assertThat(result.priority()).isEqualTo(IssuePriority.HIGH);
    }

    @Test
    @DisplayName("analyze 는 외부 API 또는 스펙 키워드를 EXTERNAL_API/MEDIUM 으로 분석한다")
    void analyze_whenExternalApiKeywordExists_thenReturnExternalApiMedium() {
        IssueAnalysisResult result = analyze("외부 API 스펙 변경으로 실패합니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.EXTERNAL_API);
        assertThat(result.priority()).isEqualTo(IssuePriority.MEDIUM);
    }

    @Test
    @DisplayName("analyze 는 캐시 또는 Redis 키워드를 CACHE/HIGH 로 분석한다")
    void analyze_whenCacheKeywordExists_thenReturnCacheHigh() {
        IssueAnalysisResult result = analyze("Redis 캐시 데이터가 오래되었습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.CACHE);
        assertThat(result.priority()).isEqualTo(IssuePriority.HIGH);
    }

    @Test
    @DisplayName("analyze 는 매칭되는 키워드가 없으면 UNKNOWN/LOW 로 분석한다")
    void analyze_whenNoKeywordMatches_thenReturnUnknownLow() {
        IssueAnalysisResult result = analyze("관리자 메모 확인이 필요합니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.UNKNOWN);
        assertThat(result.priority()).isEqualTo(IssuePriority.LOW);
    }

    private IssueAnalysisResult analyze(String text) {
        return adapter.analyze(new AnalyzeIssueCommand(
                1L,
                text,
                "상세 내용",
                List.of()
        ));
    }
}
