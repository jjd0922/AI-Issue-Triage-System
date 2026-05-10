package com.example.aiissuetriage.issue.presentation;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import com.example.aiissuetriage.issue.presentation.request.CreateIssueRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
    @DisplayName("POST /issues 는 이슈를 등록한다")
    void createIssue_whenRequestIsValid_thenReturnCreatedIssue() throws Exception {
        when(issueCommandService.createIssue(any(CreateIssueCommand.class)))
                .thenReturn(new CreateIssueResult(1L, IssueStatus.ANALYSIS_REQUESTED, NOW));

        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIssueRequest(
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
    @DisplayName("POST /issues 는 요청 본문을 검증한다")
    void createIssue_whenRequestIsInvalid_thenReturnValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIssueRequest(
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
    @DisplayName("GET /issues/{issueId} 는 이슈를 단건 조회한다")
    void getIssue_whenIssueExists_thenReturnIssue() throws Exception {
        when(issueQueryService.getIssue(1L))
                .thenReturn(new IssueDetailResult(
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
    @DisplayName("GET /issues 는 이슈 목록을 조회한다")
    void getIssues_whenSearchConditionExists_thenReturnIssuePage() throws Exception {
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        when(issueQueryService.getIssues(
                eq(new IssueSearchCommand(IssueStatus.ANALYZED, IssueSource.CUSTOMER_SERVICE)),
                eq(pageable)
        )).thenReturn(new PageImpl<>(
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
    @DisplayName("GET /issues 는 page size 최대값을 100으로 제한한다")
    void getIssues_whenPageSizeExceedsMax_thenReturnValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/issues")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("getIssues.size"));
    }

    @Test
    @DisplayName("GET /issues 는 허용된 sort field 만 받는다")
    void getIssues_whenSortFieldIsInvalid_thenReturnValidationError() throws Exception {
        mockMvc.perform(get("/api/v1/issues")
                        .param("sort", "id,desc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Unsupported sort field: id"));
    }

    @Test
    @DisplayName("GET /issues/{issueId}/analysis 는 분석 결과를 조회한다")
    void getAnalysis_whenAnalysisExists_thenReturnAnalysis() throws Exception {
        when(issueQueryService.getAnalysis(1L))
                .thenReturn(new IssueAnalysisResult(
                        1L,
                        10L,
                        IssueCategory.PAYMENT,
                        IssuePriority.CRITICAL,
                        "결제 및 주문 생성 실패",
                        "결제 이벤트와 주문 트랜잭션 로그를 확인합니다.",
                        0.85,
                        "mock-ai-analysis",
                        List.of(new KnowledgeSearchResult(100L, "결제 역할 가이드", 0.75)),
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
    @DisplayName("POST /issues/{issueId}/analysis/retry 는 분석을 재시도한다")
    void retryAnalysis_whenRetryConditionIsValid_thenReturnAccepted() throws Exception {
        when(issueCommandService.retryAnalysis(1L))
                .thenReturn(new RetryIssueAnalysisResult(1L, IssueStatus.ANALYSIS_REQUESTED, NOW.plusMinutes(5)));

        mockMvc.perform(post("/api/v1/issues/{issueId}/analysis/retry", 1L))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.issueId").value(1))
                .andExpect(jsonPath("$.status").value("ANALYSIS_REQUESTED"))
                .andExpect(jsonPath("$.requestedAt").value("2026-05-09T10:05:00"));
    }

    @Test
    @DisplayName("GET /issues/{issueId} 는 이슈가 없으면 404를 반환한다")
    void getIssue_whenIssueDoesNotExist_thenReturnNotFound() throws Exception {
        when(issueQueryService.getIssue(1L))
                .thenThrow(new IssueNotFoundException(1L));

        mockMvc.perform(get("/api/v1/issues/{issueId}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ISSUE_NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/v1/issues/1"));
    }

    @Test
    @DisplayName("GET /issues/{issueId}/analysis 는 분석 결과가 없으면 404를 반환한다")
    void getAnalysis_whenAnalysisDoesNotExist_thenReturnNotFound() throws Exception {
        when(issueQueryService.getAnalysis(1L))
                .thenThrow(new IssueAnalysisNotFoundException(1L));

        mockMvc.perform(get("/api/v1/issues/{issueId}/analysis", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ISSUE_ANALYSIS_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /issues/{issueId}/analysis/retry 는 재시도 조건이 맞지 않으면 400을 반환한다")
    void retryAnalysis_whenRetryConditionIsInvalid_thenReturnBadRequest() throws Exception {
        when(issueCommandService.retryAnalysis(1L))
                .thenThrow(new InvalidRetryConditionException(1L, IssueStatus.ANALYZED));

        mockMvc.perform(post("/api/v1/issues/{issueId}/analysis/retry", 1L))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_RETRY_CONDITION"));
    }

    @Test
    @DisplayName("POST /issues 는 enum 값이 잘못되면 400을 반환한다")
    void createIssue_whenEnumValueIsInvalid_thenReturnValidationError() throws Exception {
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
    @DisplayName("POST /issues 는 요청 DTO 를 command 로 변환해 전달한다")
    void createIssue_whenRequestIsValid_thenPassCommandToService() throws Exception {
        when(issueCommandService.createIssue(any(CreateIssueCommand.class)))
                .thenReturn(new CreateIssueResult(1L, IssueStatus.ANALYSIS_REQUESTED, NOW));

        mockMvc.perform(post("/api/v1/issues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateIssueRequest(
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
}
