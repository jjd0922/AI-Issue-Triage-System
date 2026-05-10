package com.example.aiissuetriage.issue.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiissuetriage.issue.application.command.CreateIssueCommand;
import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;
import com.example.aiissuetriage.issue.application.exception.InvalidRetryConditionException;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.application.result.RetryIssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.Issue;
import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class IssueCommandServiceTest {

    @Mock
    private IssueRepositoryPort issueRepositoryPort;

    @Mock
    private AnalysisCachePort analysisCachePort;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private IssueCommandService issueCommandService;

    @Test
    @DisplayName("createIssue 는 이슈를 분석 요청 상태로 저장하고 분석 요청 이벤트를 발행한다")
    void createIssue_whenValidCommand_thenSaveAnalysisRequestedIssueAndPublishEvent() {
        when(issueRepositoryPort.save(any(Issue.class)))
                .thenAnswer(invocation -> withIdIfNew(invocation.getArgument(0)));

        var result = issueCommandService.createIssue(new CreateIssueCommand(
                "결제 오류",
                "주문이 생성되지 않습니다.",
                IssueSource.CUSTOMER_SERVICE
        ));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        IssueAnalysisRequestedEvent event = (IssueAnalysisRequestedEvent) eventCaptor.getValue();

        assertThat(result.issueId()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(IssueStatus.ANALYSIS_REQUESTED);
        assertThat(event.issueId()).isEqualTo(1L);
        assertThat(event.title()).isEqualTo("결제 오류");
        assertThat(event.requestedAt()).isNotNull();
    }

    @Test
    @DisplayName("retryAnalysis 는 분석 실패 상태이면 캐시를 삭제하고 분석 요청 이벤트를 발행한다")
    void retryAnalysis_whenIssueAnalysisFailed_thenEvictCacheAndPublishEvent() {
        Issue issue = restoredIssue(1L, IssueStatus.ANALYSIS_FAILED);
        when(issueRepositoryPort.findById(1L)).thenReturn(Optional.of(issue));
        when(issueRepositoryPort.save(any(Issue.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RetryIssueAnalysisResult result = issueCommandService.retryAnalysis(1L);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(analysisCachePort).evict(1L);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        IssueAnalysisRequestedEvent event = (IssueAnalysisRequestedEvent) eventCaptor.getValue();

        assertThat(result.status()).isEqualTo(IssueStatus.ANALYSIS_REQUESTED);
        assertThat(result.requestedAt()).isNotNull();
        assertThat(event.issueId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("retryAnalysis 는 분석 실패 상태가 아니면 예외를 던진다")
    void retryAnalysis_whenIssueIsNotAnalysisFailed_thenThrowException() {
        when(issueRepositoryPort.findById(1L))
                .thenReturn(Optional.of(restoredIssue(1L, IssueStatus.ANALYZED)));

        assertThatThrownBy(() -> issueCommandService.retryAnalysis(1L))
                .isInstanceOf(InvalidRetryConditionException.class)
                .hasMessageContaining("Cannot retry issue analysis");
    }

    private Issue withIdIfNew(Issue issue) {
        if (issue.getId() != null) {
            return issue;
        }
        return Issue.restore(
                1L,
                issue.getTitle(),
                issue.getContent(),
                issue.getSource(),
                issue.getStatus(),
                issue.getFailureReason(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getAnalysisRequestedAt(),
                issue.getAnalysisStartedAt(),
                issue.getAnalysisCompletedAt(),
                issue.getClosedAt()
        );
    }

    private Issue restoredIssue(Long id, IssueStatus status) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 9, 10, 0);
        return Issue.restore(
                id,
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
}
