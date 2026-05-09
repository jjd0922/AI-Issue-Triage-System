package com.example.aiissuetriage.issue.presentation;

import com.example.aiissuetriage.issue.application.command.IssueSearchCommand;
import com.example.aiissuetriage.issue.application.service.IssueCommandService;
import com.example.aiissuetriage.issue.application.service.IssueQueryService;
import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;
import com.example.aiissuetriage.issue.presentation.request.CreateIssueRequest;
import com.example.aiissuetriage.issue.presentation.response.CreateIssueResponse;
import com.example.aiissuetriage.issue.presentation.response.IssueAnalysisResponse;
import com.example.aiissuetriage.issue.presentation.response.IssueDetailResponse;
import com.example.aiissuetriage.issue.presentation.response.IssueSummaryResponse;
import com.example.aiissuetriage.issue.presentation.response.PageResponse;
import com.example.aiissuetriage.issue.presentation.response.RetryIssueAnalysisResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/issues")
@RequiredArgsConstructor
public class IssueController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt",
            "updatedAt",
            "status",
            "source"
    );

    private final IssueCommandService issueCommandService;
    private final IssueQueryService issueQueryService;

    @PostMapping
    public ResponseEntity<CreateIssueResponse> createIssue(@Valid @RequestBody CreateIssueRequest request) {
        CreateIssueResponse response = CreateIssueResponse.from(
                issueCommandService.createIssue(request.toCommand())
        );
        return ResponseEntity
                .created(URI.create("/api/v1/issues/" + response.issueId()))
                .body(response);
    }

    @GetMapping("/{issueId}")
    public IssueDetailResponse getIssue(@PathVariable Long issueId) {
        return IssueDetailResponse.from(issueQueryService.getIssue(issueId));
    }

    @GetMapping
    public PageResponse<IssueSummaryResponse> getIssues(
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) IssueSource source,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        return PageResponse.from(
                issueQueryService.getIssues(
                        new IssueSearchCommand(status, source),
                        PageRequest.of(page, size, parseSort(sort))
                ),
                IssueSummaryResponse::from
        );
    }

    @GetMapping("/{issueId}/analysis")
    public IssueAnalysisResponse getAnalysis(@PathVariable Long issueId) {
        return IssueAnalysisResponse.from(issueQueryService.getAnalysis(issueId));
    }

    @PostMapping("/{issueId}/analysis/retry")
    public ResponseEntity<RetryIssueAnalysisResponse> retryAnalysis(@PathVariable Long issueId) {
        return ResponseEntity.accepted()
                .body(RetryIssueAnalysisResponse.from(issueCommandService.retryAnalysis(issueId)));
    }

    private Sort parseSort(String sort) {
        String[] tokens = sort.split(",");
        String field = tokens[0].trim();
        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new InvalidPageRequestException("Unsupported sort field: " + field);
        }
        Sort.Direction direction = tokens.length < 2
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(tokens[1].trim());
        return Sort.by(direction, field);
    }
}
