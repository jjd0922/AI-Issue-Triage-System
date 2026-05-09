package com.example.aiissuetriage.issue.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.aiissuetriage.issue.application.command.AnalyzeIssueCommand;
import com.example.aiissuetriage.issue.application.port.AiAnalysisPort;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.port.KnowledgeSearchPort;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import com.example.aiissuetriage.issue.domain.Issue;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class IssueAnalysisServiceTest {

    private final InMemoryIssueRepository issueRepository = new InMemoryIssueRepository();
    private final InMemoryIssueAnalysisRepository analysisRepository = new InMemoryIssueAnalysisRepository();
    private final StubKnowledgeSearchPort knowledgeSearchPort = new StubKnowledgeSearchPort();
    private final StubAiAnalysisPort aiAnalysisPort = new StubAiAnalysisPort();
    private final RecordingAnalysisCache analysisCache = new RecordingAnalysisCache();
    private final IssueAnalysisService service = new IssueAnalysisService(
            issueRepository,
            analysisRepository,
            knowledgeSearchPort,
            aiAnalysisPort,
            analysisCache
    );

    @Test
    void 분석_요청_상태의_이슈를_분석하고_완료_상태로_변경한다() {
        Issue issue = issueRepository.save(TestIssueFactory.issue(IssueStatus.ANALYSIS_REQUESTED));

        IssueAnalysisResult result = service.processAnalysis(issue.getId());

        Issue savedIssue = issueRepository.findById(issue.getId()).orElseThrow();
        assertThat(savedIssue.getStatus()).isEqualTo(IssueStatus.ANALYZED);
        assertThat(result.issueId()).isEqualTo(issue.getId());
        assertThat(result.category()).isEqualTo(IssueCategory.PAYMENT);
        assertThat(result.references()).hasSize(1);
        assertThat(analysisCache.putIssueId).isEqualTo(issue.getId());
    }

    @Test
    void AI_분석이_실패하면_이슈를_분석_실패_상태로_변경한다() {
        Issue issue = issueRepository.save(TestIssueFactory.issue(IssueStatus.ANALYSIS_REQUESTED));
        aiAnalysisPort.failure = new IllegalStateException("AI failed");

        assertThatThrownBy(() -> service.processAnalysis(issue.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("AI failed");

        Issue savedIssue = issueRepository.findById(issue.getId()).orElseThrow();
        assertThat(savedIssue.getStatus()).isEqualTo(IssueStatus.ANALYSIS_FAILED);
        assertThat(savedIssue.getFailureReason()).isEqualTo("AI failed");
    }

    private static class StubKnowledgeSearchPort implements KnowledgeSearchPort {

        @Override
        public List<KnowledgeSearchResult> search(String query, int limit) {
            return List.of(new KnowledgeSearchResult(100L, "결제 장애 대응 가이드", 0.8));
        }
    }

    private static class StubAiAnalysisPort implements AiAnalysisPort {

        private RuntimeException failure;

        @Override
        public IssueAnalysisResult analyze(AnalyzeIssueCommand command) {
            if (failure != null) {
                throw failure;
            }
            return new IssueAnalysisResult(
                    command.issueId(),
                    null,
                    IssueCategory.PAYMENT,
                    IssuePriority.CRITICAL,
                    "결제 후 주문 생성 실패",
                    "결제 이벤트와 주문 트랜잭션 로그를 확인합니다.",
                    0.9,
                    "mock-ai-analysis",
                    command.references(),
                    LocalDateTime.now()
            );
        }
    }

    private static class RecordingAnalysisCache implements AnalysisCachePort {

        private Long putIssueId;

        @Override
        public Optional<IssueAnalysisResult> get(Long issueId) {
            return Optional.empty();
        }

        @Override
        public void put(Long issueId, IssueAnalysisResult result) {
            this.putIssueId = issueId;
        }

        @Override
        public void evict(Long issueId) {
        }
    }
}
