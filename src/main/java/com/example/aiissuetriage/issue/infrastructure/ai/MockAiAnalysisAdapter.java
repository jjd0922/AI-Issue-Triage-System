package com.example.aiissuetriage.issue.infrastructure.ai;

import com.example.aiissuetriage.issue.application.command.AnalyzeIssueCommand;
import com.example.aiissuetriage.issue.application.port.AiAnalysisPort;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!llm")
public class MockAiAnalysisAdapter implements AiAnalysisPort {

    private static final String MODEL_NAME = "mock-ai-analysis";

    @Override
    public IssueAnalysisResult analyze(AnalyzeIssueCommand command) {
        RuleResult ruleResult = classify(command.title() + "\n" + command.content());
        return new IssueAnalysisResult(
                command.issueId(),
                null,
                ruleResult.category(),
                ruleResult.priority(),
                summary(ruleResult.category()),
                recommendation(ruleResult.category()),
                0.85,
                MODEL_NAME,
                command.references(),
                LocalDateTime.now()
        );
    }

    private RuleResult classify(String text) {
        if (containsAny(text, "결제", "주문 미생성")) {
            return new RuleResult(IssueCategory.PAYMENT, IssuePriority.CRITICAL);
        }
        if (containsAny(text, "교환", "재고")) {
            return new RuleResult(IssueCategory.EXCHANGE, IssuePriority.HIGH);
        }
        if (containsAny(text, "알림톡")) {
            return new RuleResult(IssueCategory.NOTIFICATION, IssuePriority.MEDIUM);
        }
        if (containsAny(text, "쿠폰", "Duplicate")) {
            return new RuleResult(IssueCategory.COUPON, IssuePriority.HIGH);
        }
        if (containsAny(text, "정산")) {
            return new RuleResult(IssueCategory.SETTLEMENT, IssuePriority.HIGH);
        }
        if (containsAny(text, "외부 API", "스펙")) {
            return new RuleResult(IssueCategory.EXTERNAL_API, IssuePriority.MEDIUM);
        }
        if (containsAny(text, "캐시", "Redis")) {
            return new RuleResult(IssueCategory.CACHE, IssuePriority.HIGH);
        }
        return new RuleResult(IssueCategory.UNKNOWN, IssuePriority.LOW);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String summary(IssueCategory category) {
        return switch (category) {
            case PAYMENT -> "결제 또는 주문 생성 흐름에서 발생한 이슈입니다.";
            case EXCHANGE -> "교환 또는 재고 처리 흐름에서 발생한 이슈입니다.";
            case NOTIFICATION -> "알림톡 발송 흐름에서 발생한 이슈입니다.";
            case COUPON -> "쿠폰 처리 또는 중복 처리와 관련된 이슈입니다.";
            case SETTLEMENT -> "정산 처리와 관련된 이슈입니다.";
            case EXTERNAL_API -> "외부 API 연동 또는 스펙 차이와 관련된 이슈입니다.";
            case CACHE -> "캐시 또는 Redis 데이터 정합성과 관련된 이슈입니다.";
            case UNKNOWN -> "명확한 분류 규칙에 매칭되지 않은 이슈입니다.";
        };
    }

    private String recommendation(IssueCategory category) {
        return switch (category) {
            case PAYMENT -> "결제 승인 이벤트와 주문 생성 트랜잭션 로그를 우선 확인하세요.";
            case EXCHANGE -> "교환 요청 상태와 재고 차감/복원 이력을 함께 확인하세요.";
            case NOTIFICATION -> "알림톡 발송 요청, 템플릿 코드, 외부 응답 코드를 확인하세요.";
            case COUPON -> "쿠폰 발급/사용 중복 키와 멱등성 처리를 확인하세요.";
            case SETTLEMENT -> "정산 기준 데이터와 집계 배치 실행 이력을 확인하세요.";
            case EXTERNAL_API -> "외부 API 요청/응답 전문과 최근 스펙 변경 여부를 확인하세요.";
            case CACHE -> "Redis key TTL, 캐시 무효화, 원본 DB와의 정합성을 확인하세요.";
            case UNKNOWN -> "재현 조건, 로그, 영향 범위를 추가로 수집한 뒤 수동 분류하세요.";
        };
    }

    private record RuleResult(
            IssueCategory category,
            IssuePriority priority
    ) {
    }
}
