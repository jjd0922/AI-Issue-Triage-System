package com.example.aiissuetriage.issue.infrastructure.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiissuetriage.issue.application.command.AnalyzeIssueCommand;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import java.util.List;
import org.junit.jupiter.api.Test;

class MockAiAnalysisAdapterTest {

    private final MockAiAnalysisAdapter adapter = new MockAiAnalysisAdapter();

    @Test
    void 결제_키워드는_PAYMENT_CRITICAL로_분석한다() {
        IssueAnalysisResult result = analyze("결제는 완료됐는데 주문이 생성되지 않았습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.PAYMENT);
        assertThat(result.priority()).isEqualTo(IssuePriority.CRITICAL);
        assertThat(result.modelName()).isEqualTo("mock-ai-analysis");
        assertThat(result.summary()).contains("결제");
    }

    @Test
    void 교환_또는_재고_키워드는_EXCHANGE_HIGH로_분석한다() {
        IssueAnalysisResult result = analyze("교환 요청 후 재고가 맞지 않습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.EXCHANGE);
        assertThat(result.priority()).isEqualTo(IssuePriority.HIGH);
    }

    @Test
    void 알림톡_키워드는_NOTIFICATION_MEDIUM으로_분석한다() {
        IssueAnalysisResult result = analyze("알림톡 발송이 실패했습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.NOTIFICATION);
        assertThat(result.priority()).isEqualTo(IssuePriority.MEDIUM);
    }

    @Test
    void 쿠폰_또는_Duplicate_키워드는_COUPON_HIGH로_분석한다() {
        IssueAnalysisResult result = analyze("쿠폰 Duplicate 오류가 발생했습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.COUPON);
        assertThat(result.priority()).isEqualTo(IssuePriority.HIGH);
    }

    @Test
    void 정산_키워드는_SETTLEMENT_HIGH로_분석한다() {
        IssueAnalysisResult result = analyze("정산 금액이 맞지 않습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.SETTLEMENT);
        assertThat(result.priority()).isEqualTo(IssuePriority.HIGH);
    }

    @Test
    void 외부_API_또는_스펙_키워드는_EXTERNAL_API_MEDIUM으로_분석한다() {
        IssueAnalysisResult result = analyze("외부 API 스펙 변경으로 실패합니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.EXTERNAL_API);
        assertThat(result.priority()).isEqualTo(IssuePriority.MEDIUM);
    }

    @Test
    void 캐시_또는_Redis_키워드는_CACHE_HIGH로_분석한다() {
        IssueAnalysisResult result = analyze("Redis 캐시 데이터가 오래되었습니다.");

        assertThat(result.category()).isEqualTo(IssueCategory.CACHE);
        assertThat(result.priority()).isEqualTo(IssuePriority.HIGH);
    }

    @Test
    void 매칭되는_키워드가_없으면_UNKNOWN_LOW로_분석한다() {
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
