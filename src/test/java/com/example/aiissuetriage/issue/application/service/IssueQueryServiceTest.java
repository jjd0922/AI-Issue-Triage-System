package com.example.aiissuetriage.issue.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiissuetriage.issue.application.command.IssueSearchCommand;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.port.IssueAnalysisRepositoryPort;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.Issue;
import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class IssueQueryServiceTest {

    @Mock
    private IssueRepositoryPort issueRepositoryPort;

    @Mock
    private IssueAnalysisRepositoryPort issueAnalysisRepositoryPort;

    @Mock
    private AnalysisCachePort analysisCachePort;

    @InjectMocks
    private IssueQueryService issueQueryService;

    @Test
    @DisplayName("getAnalysis 는 분석 결과가 캐시에 있으면 DB를 조회하지 않는다")
    void getAnalysis_whenCached_thenReturnCachedResultWithoutRepositoryLookup() {
        IssueAnalysisResult cached = analysisResult();
        when(analysisCachePort.get(1L)).thenReturn(Optional.of(cached));

        IssueAnalysisResult result = issueQueryService.getAnalysis(1L);

        assertThat(result).isSameAs(cached);
        verify(issueAnalysisRepositoryPort, never()).findLatestByIssueId(1L);
    }

    @Test
    @DisplayName("getAnalysis 는 캐시가 없으면 DB에서 조회하고 캐시에 저장한다")
    void getAnalysis_whenCacheMiss_thenFindFromRepositoryAndPutCache() {
        IssueAnalysis analysis = IssueAnalysis.create(
                1L,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "summary",
                "recommendation",
                0.9,
                "mock-ai-analysis",
                "{}"
        );
        when(analysisCachePort.get(1L)).thenReturn(Optional.empty());
        when(issueAnalysisRepositoryPort.findLatestByIssueId(1L)).thenReturn(Optional.of(analysis));

        IssueAnalysisResult result = issueQueryService.getAnalysis(1L);

        assertThat(result.issueId()).isEqualTo(1L);
        verify(analysisCachePort).put(1L, result);
    }

    @Test
    @DisplayName("getIssues 는 port 의 페이지 결과를 summary result 로 변환한다")
    void getIssues_whenRepositoryReturnsPage_thenMapToSummaryResult() {
        IssueSearchCommand command = new IssueSearchCommand(null, null);
        PageRequest pageable = PageRequest.of(0, 20);
        Issue issue = issue(1L);
        when(issueRepositoryPort.findAll(command, pageable))
                .thenReturn(new PageImpl<>(List.of(issue), pageable, 1));

        var page = issueQueryService.getIssues(command, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).title()).isEqualTo("결제 오류");
    }

    private IssueAnalysisResult analysisResult() {
        return new IssueAnalysisResult(
                1L,
                10L,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "summary",
                "recommendation",
                0.9,
                "mock-ai-analysis",
                List.of(),
                LocalDateTime.now()
        );
    }

    private Issue issue(Long id) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 9, 10, 0);
        return Issue.restore(
                id,
                "결제 오류",
                "주문이 생성되지 않습니다.",
                IssueSource.CUSTOMER_SERVICE,
                IssueStatus.REGISTERED,
                null,
                now,
                now,
                null,
                null,
                null,
                null
        );
    }
}
