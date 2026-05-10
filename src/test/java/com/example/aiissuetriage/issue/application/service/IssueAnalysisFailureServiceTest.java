package com.example.aiissuetriage.issue.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(MockitoExtension.class)
class IssueAnalysisFailureServiceTest {

    @Mock
    private IssueRepositoryPort issueRepositoryPort;

    @InjectMocks
    private IssueAnalysisFailureService issueAnalysisFailureService;

    @Test
    @DisplayName("markAnalysisFailed 는 이슈를 분석 실패 상태로 저장한다")
    void markAnalysisFailed_whenIssueExists_thenSaveAnalysisFailedIssue() {
        Issue issue = issue(IssueStatus.ANALYSIS_REQUESTED);
        when(issueRepositoryPort.findById(1L)).thenReturn(Optional.of(issue));
        when(issueRepositoryPort.save(any(Issue.class))).thenAnswer(invocation -> invocation.getArgument(0));

        issueAnalysisFailureService.markAnalysisFailed(1L, "AI failed");

        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(issueRepositoryPort).save(issueCaptor.capture());
        assertThat(issueCaptor.getValue().getStatus()).isEqualTo(IssueStatus.ANALYSIS_FAILED);
        assertThat(issueCaptor.getValue().getFailureReason()).isEqualTo("AI failed");
    }

    @Test
    @DisplayName("markAnalysisFailed 는 REQUIRES_NEW 트랜잭션으로 실행된다")
    void markAnalysisFailed_whenCalled_thenUseRequiresNewTransaction() throws NoSuchMethodException {
        Transactional transactional = IssueAnalysisFailureService.class
                .getMethod("markAnalysisFailed", Long.class, String.class)
                .getAnnotation(Transactional.class);

        assertThat(transactional.propagation()).isEqualTo(Propagation.REQUIRES_NEW);
    }

    private Issue issue(IssueStatus status) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 9, 10, 0);
        return Issue.restore(
                1L,
                "결제 오류",
                "주문이 생성되지 않습니다.",
                IssueSource.CUSTOMER_SERVICE,
                status,
                null,
                now,
                now,
                status == IssueStatus.ANALYSIS_REQUESTED ? now : null,
                status == IssueStatus.ANALYZING ? now : null,
                null,
                null
        );
    }
}
