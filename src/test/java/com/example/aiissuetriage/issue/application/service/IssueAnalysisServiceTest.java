package com.example.aiissuetriage.issue.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiissuetriage.issue.application.command.AnalyzeIssueCommand;
import com.example.aiissuetriage.issue.application.port.AiAnalysisPort;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.port.IssueAnalysisRepositoryPort;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.application.port.KnowledgeSearchPort;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IssueAnalysisServiceTest {

    @Mock
    private IssueRepositoryPort issueRepositoryPort;

    @Mock
    private IssueAnalysisRepositoryPort issueAnalysisRepositoryPort;

    @Mock
    private KnowledgeSearchPort knowledgeSearchPort;

    @Mock
    private AiAnalysisPort aiAnalysisPort;

    @Mock
    private AnalysisCachePort analysisCachePort;

    @Mock
    private IssueAnalysisFailureService issueAnalysisFailureService;

    @InjectMocks
    private IssueAnalysisService issueAnalysisService;

    @Test
    @DisplayName("processAnalysis 는 분석 요청 상태의 이슈를 분석하고 완료 상태로 변경한다")
    void processAnalysis_whenIssueAnalysisRequested_thenAnalyzeAndCompleteIssue() {
        Issue issue = issue(IssueStatus.ANALYSIS_REQUESTED);
        List<KnowledgeSearchResult> references = List.of(
                new KnowledgeSearchResult(100L, "결제 역할 가이드", 0.8)
        );
        IssueAnalysisResult aiResult = analysisResult(issue.getId(), references);
        IssueAnalysis savedAnalysis = analysis(issue.getId(), 1L);

        when(issueRepositoryPort.findById(1L)).thenReturn(Optional.of(issue));
        when(knowledgeSearchPort.search("결제 오류", 5)).thenReturn(references);
        when(aiAnalysisPort.analyze(any(AnalyzeIssueCommand.class))).thenReturn(aiResult);
        when(issueAnalysisRepositoryPort.save(any(IssueAnalysis.class))).thenReturn(savedAnalysis);

        IssueAnalysisResult result = issueAnalysisService.processAnalysis(1L);

        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(issueRepositoryPort, times(2)).save(issueCaptor.capture());
        verify(analysisCachePort).put(1L, result);
        assertThat(issueCaptor.getAllValues().get(1).getStatus()).isEqualTo(IssueStatus.ANALYZED);
        assertThat(result.issueId()).isEqualTo(1L);
        assertThat(result.category()).isEqualTo(IssueCategory.PAYMENT);
        assertThat(result.references()).hasSize(1);
    }

    @Test
    @DisplayName("processAnalysis 는 AI 분석이 실패하면 별도 트랜잭션으로 실패 상태를 기록한다")
    void processAnalysis_whenAiAnalysisFails_thenMarkIssueAsFailedInNewTransaction() {
        Issue issue = issue(IssueStatus.ANALYSIS_REQUESTED);
        when(issueRepositoryPort.findById(1L)).thenReturn(Optional.of(issue));
        when(knowledgeSearchPort.search("결제 오류", 5)).thenReturn(List.of());
        when(aiAnalysisPort.analyze(any(AnalyzeIssueCommand.class)))
                .thenThrow(new IllegalStateException("AI failed"));

        assertThatThrownBy(() -> issueAnalysisService.processAnalysis(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI failed");

        verify(issueAnalysisFailureService).markAnalysisFailed(1L, "AI failed");
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
                status == IssueStatus.ANALYZED ? now : null,
                null
        );
    }

    private IssueAnalysisResult analysisResult(Long issueId, List<KnowledgeSearchResult> references) {
        return new IssueAnalysisResult(
                issueId,
                null,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "결제 및 주문 생성 실패",
                "결제 이벤트와 주문 트랜잭션 로그를 확인합니다.",
                0.9,
                "mock-ai-analysis",
                references,
                LocalDateTime.now()
        );
    }

    private IssueAnalysis analysis(Long issueId, Long analysisId) {
        return IssueAnalysis.restore(
                analysisId,
                issueId,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "결제 및 주문 생성 실패",
                "결제 이벤트와 주문 트랜잭션 로그를 확인합니다.",
                0.9,
                "mock-ai-analysis",
                "{}",
                LocalDateTime.now()
        );
    }
}
