package com.example.aiissuetriage.issue.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.aiissuetriage.issue.application.command.CreateIssueCommand;
import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;
import com.example.aiissuetriage.issue.application.exception.InvalidRetryConditionException;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.port.IssueAnalysisRequestedEventPublisher;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.application.result.RetryIssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.Issue;
import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class IssueCommandServiceTest {

    private final InMemoryIssueRepository issueRepository = new InMemoryIssueRepository();
    private final RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
    private final RecordingAnalysisCache analysisCache = new RecordingAnalysisCache();
    private final IssueCommandService service = new IssueCommandService(
            issueRepository,
            eventPublisher,
            analysisCache
    );

    @Test
    void 이슈를_등록하면_분석_요청_상태로_저장하고_이벤트를_발행한다() {
        var result = service.createIssue(new CreateIssueCommand(
                "결제 오류",
                "주문이 생성되지 않습니다.",
                IssueSource.CUSTOMER_SERVICE
        ));

        assertThat(result.issueId()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(IssueStatus.ANALYSIS_REQUESTED);
        assertThat(eventPublisher.events).hasSize(1);
        assertThat(eventPublisher.events.get(0).issueId()).isEqualTo(1L);
        assertThat(eventPublisher.events.get(0).title()).isEqualTo("결제 오류");
    }

    @Test
    void 분석_실패_상태의_이슈는_재시도할_수_있다() {
        Issue issue = issueRepository.save(restoredIssue(IssueStatus.ANALYSIS_FAILED));

        RetryIssueAnalysisResult result = service.retryAnalysis(issue.getId());

        assertThat(result.status()).isEqualTo(IssueStatus.ANALYSIS_REQUESTED);
        assertThat(result.requestedAt()).isNotNull();
        assertThat(eventPublisher.events).hasSize(1);
        assertThat(analysisCache.evictedIssueIds).containsExactly(issue.getId());
    }

    @Test
    void 분석_실패가_아닌_상태는_재시도할_수_없다() {
        Issue issue = issueRepository.save(restoredIssue(IssueStatus.ANALYZED));

        assertThatThrownBy(() -> service.retryAnalysis(issue.getId()))
                .isInstanceOf(InvalidRetryConditionException.class)
                .hasMessageContaining("Cannot retry issue analysis");
    }

    private Issue restoredIssue(IssueStatus status) {
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
                null,
                null,
                null,
                null
        );
    }

    private static class RecordingEventPublisher implements IssueAnalysisRequestedEventPublisher {

        private final List<IssueAnalysisRequestedEvent> events = new ArrayList<>();

        @Override
        public void publish(IssueAnalysisRequestedEvent event) {
            events.add(event);
        }
    }

    private static class RecordingAnalysisCache implements AnalysisCachePort {

        private final List<Long> evictedIssueIds = new ArrayList<>();

        @Override
        public Optional<IssueAnalysisResult> get(Long issueId) {
            return Optional.empty();
        }

        @Override
        public void put(Long issueId, IssueAnalysisResult result) {
        }

        @Override
        public void evict(Long issueId) {
            evictedIssueIds.add(issueId);
        }
    }
}
