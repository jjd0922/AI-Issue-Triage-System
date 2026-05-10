package com.example.aiissuetriage.issue.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.aiissuetriage.issue.application.command.IssueSearchCommand;
import com.example.aiissuetriage.issue.application.event.IssueAnalysisRequestedEvent;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.port.IssueAnalysisRepositoryPort;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.application.port.KnowledgeSearchPort;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import com.example.aiissuetriage.issue.application.service.IssueAnalysisService;
import com.example.aiissuetriage.issue.application.service.IssueAnalysisFailureService;
import com.example.aiissuetriage.issue.application.service.IssueCommandService;
import com.example.aiissuetriage.issue.application.service.IssueQueryService;
import com.example.aiissuetriage.issue.domain.Issue;
import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import com.example.aiissuetriage.issue.infrastructure.ai.MockAiAnalysisAdapter;
import com.example.aiissuetriage.issue.infrastructure.kafka.IssueAnalysisRequestedKafkaConsumer;
import com.example.aiissuetriage.issue.infrastructure.kafka.IssueAnalysisRequestedKafkaPayload;
import com.example.aiissuetriage.issue.presentation.IssueController;
import com.example.aiissuetriage.issue.presentation.request.CreateIssueRequest;
import com.example.aiissuetriage.issue.presentation.response.CreateIssueResponse;
import com.example.aiissuetriage.issue.presentation.response.IssueAnalysisResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class IssueTriageFlowIntegrationTest {

    @Test
    @DisplayName("이슈 등록부터 분석 결과 조회까지 통합 흐름이 동작한다")
    void issueTriageFlow_whenIssueCreatedAndConsumed_thenReturnAnalysisResult() {
        InMemoryIssueRepository issueRepository = new InMemoryIssueRepository();
        InMemoryIssueAnalysisRepository analysisRepository = new InMemoryIssueAnalysisRepository();
        InMemoryAnalysisCache analysisCache = new InMemoryAnalysisCache();
        RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        KnowledgeSearchPort knowledgeSearchPort = (query, limit) -> List.of(
                new KnowledgeSearchResult(100L, "결제 역할 가이드", 1.0)
        );

        IssueCommandService commandService = new IssueCommandService(
                issueRepository,
                analysisCache,
                eventPublisher
        );
        IssueAnalysisService analysisService = new IssueAnalysisService(
                issueRepository,
                analysisRepository,
                knowledgeSearchPort,
                new MockAiAnalysisAdapter(),
                analysisCache,
                new IssueAnalysisFailureService(issueRepository)
        );
        IssueQueryService queryService = new IssueQueryService(
                issueRepository,
                analysisRepository,
                analysisCache
        );
        IssueController controller = new IssueController(commandService, queryService);
        IssueAnalysisRequestedKafkaConsumer consumer = new IssueAnalysisRequestedKafkaConsumer(analysisService);

        CreateIssueResponse created = controller.createIssue(new CreateIssueRequest(
                "결제가 완료됐는데 주문이 생성되지 않았습니다.",
                "고객은 결제 성공 문자를 받았지만 주문 내역이 비어 있습니다.",
                IssueSource.CUSTOMER_SERVICE
        )).getBody();

        assertThat(created).isNotNull();
        assertThat(created.issueId()).isEqualTo(1L);
        assertThat(created.status()).isEqualTo(IssueStatus.ANALYSIS_REQUESTED);
        assertThat(eventPublisher.events).hasSize(1);

        IssueAnalysisRequestedEvent event = eventPublisher.events.get(0);
        consumer.consume(IssueAnalysisRequestedKafkaPayload.from(event));

        Issue savedIssue = issueRepository.findById(1L).orElseThrow();
        assertThat(savedIssue.getStatus()).isEqualTo(IssueStatus.ANALYZED);

        IssueAnalysisResponse analysis = controller.getAnalysis(1L);

        assertThat(analysis.issueId()).isEqualTo(1L);
        assertThat(analysis.category()).isEqualTo(IssueCategory.PAYMENT);
        assertThat(analysis.priority().name()).isEqualTo("CRITICAL");
        assertThat(analysis.references()).hasSize(1);
        assertThat(analysisCache.values).containsKey(1L);
    }

    private static class RecordingEventPublisher implements ApplicationEventPublisher {

        private final List<IssueAnalysisRequestedEvent> events = new ArrayList<>();

        @Override
        public void publishEvent(Object event) {
            if (event instanceof IssueAnalysisRequestedEvent issueAnalysisRequestedEvent) {
                events.add(issueAnalysisRequestedEvent);
            }
        }
    }

    private static class InMemoryIssueRepository implements IssueRepositoryPort {

        private final AtomicLong sequence = new AtomicLong(1);
        private final Map<Long, Issue> issues = new LinkedHashMap<>();

        @Override
        public Issue save(Issue issue) {
            Issue savedIssue = issue.getId() == null ? withId(issue, sequence.getAndIncrement()) : issue;
            issues.put(savedIssue.getId(), savedIssue);
            return savedIssue;
        }

        @Override
        public Optional<Issue> findById(Long issueId) {
            return Optional.ofNullable(issues.get(issueId));
        }

        @Override
        public Page<Issue> findAll(IssueSearchCommand command, Pageable pageable) {
            return new PageImpl<>(new ArrayList<>(issues.values()), pageable, issues.size());
        }

        private Issue withId(Issue issue, Long id) {
            return Issue.restore(
                    id,
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
    }

    private static class InMemoryIssueAnalysisRepository implements IssueAnalysisRepositoryPort {

        private final AtomicLong sequence = new AtomicLong(1);
        private final Map<Long, IssueAnalysis> analyses = new LinkedHashMap<>();
        private final Map<Long, List<KnowledgeSearchResult>> references = new LinkedHashMap<>();

        @Override
        public IssueAnalysis save(IssueAnalysis analysis) {
            IssueAnalysis savedAnalysis = analysis.getId() == null
                    ? withId(analysis, sequence.getAndIncrement())
                    : analysis;
            analyses.put(savedAnalysis.getId(), savedAnalysis);
            return savedAnalysis;
        }

        @Override
        public void saveReferences(Long analysisId, List<KnowledgeSearchResult> references) {
            this.references.put(analysisId, List.copyOf(references));
        }

        @Override
        public Optional<IssueAnalysis> findLatestByIssueId(Long issueId) {
            return analyses.values()
                    .stream()
                    .filter(analysis -> analysis.getIssueId().equals(issueId))
                    .findFirst();
        }

        @Override
        public List<KnowledgeSearchResult> findReferencesByAnalysisId(Long analysisId) {
            return references.getOrDefault(analysisId, List.of());
        }

        private IssueAnalysis withId(IssueAnalysis analysis, Long id) {
            return IssueAnalysis.restore(
                    id,
                    analysis.getIssueId(),
                    analysis.getCategory(),
                    analysis.getPriority(),
                    analysis.getSummary(),
                    analysis.getRecommendation(),
                    analysis.getConfidence(),
                    analysis.getModelName(),
                    analysis.getRawResponse(),
                    analysis.getCreatedAt()
            );
        }
    }

    private static class InMemoryAnalysisCache implements AnalysisCachePort {

        private final Map<Long, IssueAnalysisResult> values = new LinkedHashMap<>();

        @Override
        public Optional<IssueAnalysisResult> get(Long issueId) {
            return Optional.ofNullable(values.get(issueId));
        }

        @Override
        public void put(Long issueId, IssueAnalysisResult result) {
            values.put(issueId, result);
        }

        @Override
        public void evict(Long issueId) {
            values.remove(issueId);
        }
    }
}
