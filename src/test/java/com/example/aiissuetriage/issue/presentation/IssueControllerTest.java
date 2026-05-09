package com.example.aiissuetriage.issue.presentation;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.aiissuetriage.issue.application.command.CreateIssueCommand;
import com.example.aiissuetriage.issue.application.command.IssueSearchCommand;
import com.example.aiissuetriage.issue.application.exception.InvalidRetryConditionException;
import com.example.aiissuetriage.issue.application.exception.IssueAnalysisNotFoundException;
import com.example.aiissuetriage.issue.application.exception.IssueNotFoundException;
import com.example.aiissuetriage.issue.application.result.CreateIssueResult;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.application.result.IssueDetailResult;
import com.example.aiissuetriage.issue.application.result.IssueSummaryResult;
import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import com.example.aiissuetriage.issue.application.result.RetryIssueAnalysisResult;
import com.example.aiissuetriage.issue.application.service.IssueCommandService;
import com.example.aiissuetriage.issue.application.service.IssueQueryService;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(IssueController.class)
class IssueControllerTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 9, 10, 0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IssueCommandService issueCommandService;

    @MockitoBean
    private IssueQueryService issueQueryService;

    @Test
    void 이슈를_등록한다() throws Exception {
        given(issueCommandService.createIssue(any(CreateIssueCommand.class)))
                .willReturn(new CreateIssueResult(1L, IssueStatus.ANALYSIS_REQUESTED, NOW));

        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIssueApiRequest(
                                "결제 오류",
                                "주문이 생성되지 않습니다.",
                                IssueSource.CUSTOMER_SERVICE
                        ))))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/issues/1"))
                .andExpect(jsonPath("$.issueId").value(1))
                .andExpect(jsonPath("$.status").value("ANALYSIS_REQUESTED"))
                .andExpect(jsonPath("$.createdAt").value("2026-05-09T10:00:00"));
    }

    @Test
    void 이슈_등록_요청을_검증한다() throws Exception {
        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIssueApiRequest(
                                "",
                                "",
                                null
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/v1/issues"))
                .andExpect(jsonPath("$.errors", hasSize(3)));
    }

    @Test
    void 이슈를_단건_조회한다() throws Exception {
        given(issueQueryService.getIssue(1L))
                .willReturn(new IssueDetailResult(
                        1L,
                        "결제 오류",
                        "주문이 생성되지 않습니다.",
                        IssueSource.CUSTOMER_SERVICE,
                        IssueStatus.ANALYZED,
                        null,
                        NOW,
                        NOW.plusSeconds(3)
                ));

        mockMvc.perform(get("/api/v1/issues/{issueId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issueId").value(1))
                .andExpect(jsonPath("$.title").value("결제 오류"))
                .andExpect(jsonPath("$.source").value("CUSTOMER_SERVICE"))
                .andExpect(jsonPath("$.status").value("ANALYZED"));
    }

    @Test
    void 이슈_목록을_조회한다() throws Exception {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        given(issueQueryService.getIssues(
                eq(new IssueSearchCommand(IssueStatus.ANALYZED, IssueSource.CUSTOMER_SERVICE)),
                eq(pageable)
        )).willReturn(new PageImpl<>(
                List.of(new IssueSummaryResult(
                        1L,
                        "결제 오류",
                        IssueSource.CUSTOMER_SERVICE,
                        IssueStatus.ANALYZED,
                        NOW,
                        NOW.plusSeconds(3)
                )),
                pageable,
                1
        ));

        mockMvc.perform(get("/api/v1/issues")
                        .param("page", "0")
                        .param("size", "20")
                        .param("status", "ANALYZED")
                        .param("source", "CUSTOMER_SERVICE")
                        .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].issueId").value(1))
                .andExpect(jsonPath("$.content[0].status").value("ANALYZED"))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void 목록_조회_page_size는_최대_100으로_제한한다() throws Exception {
        mockMvc.perform(get("/api/v1/issues")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("getIssues.size"));
    }

    @Test
    void 목록_조회_sort_field를_검증한다() throws Exception {
        mockMvc.perform(get("/api/v1/issues")
                        .param("sort", "id,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Unsupported sort field: id"));
    }

    @Test
    void 분석_결과를_조회한다() throws Exception {
        given(issueQueryService.getAnalysis(1L))
                .willReturn(new IssueAnalysisResult(
                        1L,
                        10L,
                        IssueCategory.PAYMENT,
                        IssuePriority.CRITICAL,
                        "결제 후 주문 생성 실패",
                        "결제 이벤트와 주문 트랜잭션 로그를 확인합니다.",
                        0.85,
                        "mock-ai-analysis",
                        List.of(new KnowledgeSearchResult(100L, "결제 장애 대응 가이드", 0.75)),
                        NOW.plusSeconds(3)
                ));

        mockMvc.perform(get("/api/v1/issues/{issueId}/analysis", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.issueId").value(1))
                .andExpect(jsonPath("$.analysisId").value(10))
                .andExpect(jsonPath("$.category").value("PAYMENT"))
                .andExpect(jsonPath("$.priority").value("CRITICAL"))
                .andExpect(jsonPath("$.references[0].knowledgeDocumentId").value(100));
    }

    @Test
    void 분석을_재시도한다() throws Exception {
        given(issueCommandService.retryAnalysis(1L))
                .willReturn(new RetryIssueAnalysisResult(1L, IssueStatus.ANALYSIS_REQUESTED, NOW.plusMinutes(5)));

        mockMvc.perform(post("/api/v1/issues/{issueId}/analysis/retry", 1L))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.issueId").value(1))
                .andExpect(jsonPath("$.status").value("ANALYSIS_REQUESTED"))
                .andExpect(jsonPath("$.requestedAt").value("2026-05-09T10:05:00"));
    }

    @Test
    void 이슈가_없으면_404를_반환한다() throws Exception {
        given(issueQueryService.getIssue(1L))
                .willThrow(new IssueNotFoundException(1L));

        mockMvc.perform(get("/api/v1/issues/{issueId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ISSUE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/issues/1"));
    }

    @Test
    void 분석_결과가_없으면_404를_반환한다() throws Exception {
        given(issueQueryService.getAnalysis(1L))
                .willThrow(new IssueAnalysisNotFoundException(1L));

        mockMvc.perform(get("/api/v1/issues/{issueId}/analysis", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ISSUE_ANALYSIS_NOT_FOUND"));
    }

    @Test
    void 재시도_조건이_맞지_않으면_400을_반환한다() throws Exception {
        given(issueCommandService.retryAnalysis(1L))
                .willThrow(new InvalidRetryConditionException(1L, IssueStatus.ANALYZED));

        mockMvc.perform(post("/api/v1/issues/{issueId}/analysis/retry", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_RETRY_CONDITION"));
    }

    @Test
    void enum_값이_잘못되면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "결제 오류",
                                  "content": "주문이 생성되지 않습니다.",
                                  "source": "INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Invalid request value for field: source"));
    }

    @Test
    void 이슈_등록_요청은_command로_변환된다() throws Exception {
        given(issueCommandService.createIssue(any(CreateIssueCommand.class)))
                .willReturn(new CreateIssueResult(1L, IssueStatus.ANALYSIS_REQUESTED, NOW));

        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIssueApiRequest(
                                "결제 오류",
                                "주문이 생성되지 않습니다.",
                                IssueSource.CUSTOMER_SERVICE
                        ))))
                .andExpect(status().isCreated());

        verify(issueCommandService).createIssue(new CreateIssueCommand(
                "결제 오류",
                "주문이 생성되지 않습니다.",
                IssueSource.CUSTOMER_SERVICE
        ));
    }

    private record CreateIssueApiRequest(
            String title,
            String content,
            IssueSource source
    ) {
    }
}
