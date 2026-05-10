package com.example.aiissuetriage.issue.application.service;

import com.example.aiissuetriage.issue.domain.Issue;
import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;

final class TestIssueFactory {

    private TestIssueFactory() {
    }

    static Issue issue() {
        return issue(IssueStatus.REGISTERED);
    }

    static Issue issue(IssueStatus status) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 9, 10, 0);
        return Issue.restore(
                null,
                "결제 오류",
                "주문이 생성되지 않습니다.",
                IssueSource.CUSTOMER_SERVICE,
                status,
                status == IssueStatus.ANALYSIS_FAILED ? "failed" : null,
                now,
                now,
                status == IssueStatus.ANALYSIS_REQUESTED ? now : null,
                status == IssueStatus.ANALYZING ? now : null,
                status == IssueStatus.ANALYZED ? now : null,
                status == IssueStatus.CLOSED ? now : null
        );
    }
}
