package com.example.aiissuetriage.issue.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiissuetriage.issue.application.command.IssueSearchCommand;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;

class IssueQueryServiceTest {

    private final InMemoryIssueRepository issueRepository = new InMemoryIssueRepository();
    private final InMemoryIssueAnalysisRepository analysisRepository = new InMemoryIssueAnalysisRepository();
    private final RecordingAnalysisCache analysisCache = new RecordingAnalysisCache();
    private final IssueQueryService service = new IssueQueryService(
            issueRepository,
            analysisRepository,
            analysisCache
    );

    @Test
    void 분석_결과가_캐시에_있으면_DB를_조회하지_않는다() {
        IssueAnalysisResult cached = new IssueAnalysisResult(
                1L,
                10L,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "summary",
                "recommendation",
                0.9,
                "mock-ai-analysis",
                java.util.List.of(),
                java.time.LocalDateTime.now()
        );
        analysisCache.cached = cached;

        IssueAnalysisResult result = service.getAnalysis(1L);

        assertThat(result).isSameAs(cached);
        assertThat(analysisRepository.findLatestCallCount).isZero();
    }

    @Test
    void 캐시가_없으면_DB에서_분석_결과를_조회하고_캐시에_저장한다() {
        analysisRepository.save(IssueAnalysis.create(
                1L,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "summary",
                "recommendation",
                0.9,
                "mock-ai-analysis",
                "{}"
        ));

        IssueAnalysisResult result = service.getAnalysis(1L);

        assertThat(result.issueId()).isEqualTo(1L);
        assertThat(analysisCache.putIssueId).isEqualTo(1L);
    }

    @Test
    void 목록_조회는_포트의_페이지_결과를_Result로_변환한다() {
        issueRepository.save(TestIssueFactory.issue());

        var page = service.getIssues(
                new IssueSearchCommand(null, null),
                PageRequest.of(0, 20)
        );

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).title()).isEqualTo("결제 오류");
    }

    private static class RecordingAnalysisCache implements AnalysisCachePort {

        private IssueAnalysisResult cached;
        private Long putIssueId;

        @Override
        public Optional<IssueAnalysisResult> get(Long issueId) {
            return Optional.ofNullable(cached);
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
