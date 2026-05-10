package com.example.aiissuetriage.issue.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiissuetriage.issue.domain.Issue;
import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IssuePersistenceMapperTest {

    @Test
    @DisplayName("Issue 를 Entity 로 변환하고 다시 도메인으로 복원한다")
    void toEntityAndToDomain_whenIssueExists_thenRestoreIssue() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 9, 10, 0);
        Issue issue = Issue.restore(
                1L,
                "결제 오류",
                "주문이 생성되지 않습니다.",
                IssueSource.CUSTOMER_SERVICE,
                IssueStatus.ANALYSIS_REQUESTED,
                null,
                now,
                now,
                now,
                null,
                null,
                null
        );

        IssueEntity entity = IssuePersistenceMapper.toNewEntity(issue);
        Issue restoredIssue = IssuePersistenceMapper.toDomain(new IssueEntity(
                1L,
                entity.getTitle(),
                entity.getContent(),
                entity.getSource(),
                entity.getStatus(),
                entity.getFailureReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getAnalysisRequestedAt(),
                entity.getAnalysisStartedAt(),
                entity.getAnalysisCompletedAt(),
                entity.getClosedAt()
        ));

        assertThat(restoredIssue.getId()).isEqualTo(1L);
        assertThat(restoredIssue.getTitle()).isEqualTo(issue.getTitle());
        assertThat(restoredIssue.getContent()).isEqualTo(issue.getContent());
        assertThat(restoredIssue.getSource()).isEqualTo(issue.getSource());
        assertThat(restoredIssue.getStatus()).isEqualTo(issue.getStatus());
        assertThat(restoredIssue.getAnalysisRequestedAt()).isEqualTo(issue.getAnalysisRequestedAt());
    }
}
