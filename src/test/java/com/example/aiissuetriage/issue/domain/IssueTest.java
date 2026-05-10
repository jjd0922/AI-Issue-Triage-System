package com.example.aiissuetriage.issue.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IssueTest {

    @Test
    @DisplayName("create 는 신규 이슈를 REGISTERED 상태로 생성한다")
    void create_whenValidInput_thenCreateRegisteredIssue() {
        Issue issue = Issue.create("결제 오류", "주문이 생성되지 않습니다.", IssueSource.CUSTOMER_SERVICE);

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.REGISTERED);
        assertThat(issue.getTitle()).isEqualTo("결제 오류");
        assertThat(issue.getContent()).isEqualTo("주문이 생성되지 않습니다.");
        assertThat(issue.getSource()).isEqualTo(IssueSource.CUSTOMER_SERVICE);
        assertThat(issue.getCreatedAt()).isNotNull();
        assertThat(issue.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("requestAnalysis 는 등록된 이슈를 분석 요청 상태로 변경한다")
    void requestAnalysis_whenIssueIsRegistered_thenChangeToAnalysisRequested() {
        Issue issue = newIssue(IssueStatus.REGISTERED);

        issue.requestAnalysis();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYSIS_REQUESTED);
        assertThat(issue.getAnalysisRequestedAt()).isNotNull();
        assertThat(issue.getFailureReason()).isNull();
    }

    @Test
    @DisplayName("requestAnalysis 는 분석 실패 이슈를 분석 요청 상태로 재요청한다")
    void requestAnalysis_whenIssueAnalysisFailed_thenChangeToAnalysisRequested() {
        Issue issue = newIssue(IssueStatus.ANALYSIS_FAILED, "temporary failure");

        issue.requestAnalysis();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYSIS_REQUESTED);
        assertThat(issue.getFailureReason()).isNull();
    }

    @Test
    @DisplayName("startAnalysis 는 분석 요청 이슈를 분석 중 상태로 변경한다")
    void startAnalysis_whenIssueAnalysisRequested_thenChangeToAnalyzing() {
        Issue issue = newIssue(IssueStatus.ANALYSIS_REQUESTED);

        issue.startAnalysis();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYZING);
        assertThat(issue.getAnalysisStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("completeAnalysis 는 분석 중 이슈를 분석 완료 상태로 변경한다")
    void completeAnalysis_whenIssueAnalyzing_thenChangeToAnalyzed() {
        Issue issue = newIssue(IssueStatus.ANALYZING);

        issue.completeAnalysis();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYZED);
        assertThat(issue.getAnalysisCompletedAt()).isNotNull();
        assertThat(issue.getFailureReason()).isNull();
    }

    @Test
    @DisplayName("failAnalysis 는 분석 요청 이슈를 분석 실패 상태로 변경한다")
    void failAnalysis_whenIssueAnalysisRequested_thenChangeToAnalysisFailed() {
        Issue issue = newIssue(IssueStatus.ANALYSIS_REQUESTED);

        issue.failAnalysis("AI adapter timeout");

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYSIS_FAILED);
        assertThat(issue.getFailureReason()).isEqualTo("AI adapter timeout");
    }

    @Test
    @DisplayName("failAnalysis 는 분석 중 이슈를 분석 실패 상태로 변경한다")
    void failAnalysis_whenIssueAnalyzing_thenChangeToAnalysisFailed() {
        Issue issue = newIssue(IssueStatus.ANALYZING);

        issue.failAnalysis("AI adapter timeout");

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYSIS_FAILED);
        assertThat(issue.getFailureReason()).isEqualTo("AI adapter timeout");
    }

    @Test
    @DisplayName("close 는 분석 완료 이슈를 종료 상태로 변경한다")
    void close_whenIssueAnalyzed_thenChangeToClosed() {
        Issue issue = newIssue(IssueStatus.ANALYZED);

        issue.close();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.CLOSED);
        assertThat(issue.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("close 는 분석 실패 이슈를 종료 상태로 변경한다")
    void close_whenIssueAnalysisFailed_thenChangeToClosed() {
        Issue issue = newIssue(IssueStatus.ANALYSIS_FAILED);

        issue.close();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.CLOSED);
        assertThat(issue.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("requestAnalysis 는 허용되지 않은 상태이면 예외를 던진다")
    void requestAnalysis_whenIssueStatusIsNotAllowed_thenThrowException() {
        Issue issue = newIssue(IssueStatus.ANALYZED);

        assertThatThrownBy(issue::requestAnalysis)
                .isInstanceOf(InvalidIssueStatusException.class)
                .hasMessageContaining("Cannot requestAnalysis issue");
    }

    @Test
    @DisplayName("startAnalysis 는 허용되지 않은 상태이면 예외를 던진다")
    void startAnalysis_whenIssueStatusIsNotAllowed_thenThrowException() {
        Issue issue = newIssue(IssueStatus.REGISTERED);

        assertThatThrownBy(issue::startAnalysis)
                .isInstanceOf(InvalidIssueStatusException.class)
                .hasMessageContaining("Cannot startAnalysis issue");
    }

    @Test
    @DisplayName("completeAnalysis 는 허용되지 않은 상태이면 예외를 던진다")
    void completeAnalysis_whenIssueStatusIsNotAllowed_thenThrowException() {
        Issue issue = newIssue(IssueStatus.ANALYSIS_REQUESTED);

        assertThatThrownBy(issue::completeAnalysis)
                .isInstanceOf(InvalidIssueStatusException.class)
                .hasMessageContaining("Cannot completeAnalysis issue");
    }

    @Test
    @DisplayName("failAnalysis 는 허용되지 않은 상태이면 예외를 던진다")
    void failAnalysis_whenIssueStatusIsNotAllowed_thenThrowException() {
        Issue issue = newIssue(IssueStatus.ANALYZED);

        assertThatThrownBy(() -> issue.failAnalysis("failed"))
                .isInstanceOf(InvalidIssueStatusException.class)
                .hasMessageContaining("Cannot failAnalysis issue");
    }

    @Test
    @DisplayName("close 는 허용되지 않은 상태이면 예외를 던진다")
    void close_whenIssueStatusIsNotAllowed_thenThrowException() {
        Issue issue = newIssue(IssueStatus.ANALYZING);

        assertThatThrownBy(issue::close)
                .isInstanceOf(InvalidIssueStatusException.class)
                .hasMessageContaining("Cannot close issue");
    }

    @Test
    @DisplayName("failAnalysis 는 실패 사유가 비어 있으면 예외를 던진다")
    void failAnalysis_whenReasonIsBlank_thenThrowException() {
        Issue issue = newIssue(IssueStatus.ANALYZING);

        assertThatThrownBy(() -> issue.failAnalysis(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reason must not be blank");
    }

    private Issue newIssue(IssueStatus status) {
        return newIssue(status, null);
    }

    private Issue newIssue(IssueStatus status, String failureReason) {
        LocalDateTime now = LocalDateTime.of(2026, 5, 9, 10, 0);
        return Issue.restore(
                1L,
                "결제 오류",
                "주문이 생성되지 않습니다.",
                IssueSource.CUSTOMER_SERVICE,
                status,
                failureReason,
                now,
                now,
                status == IssueStatus.ANALYSIS_REQUESTED ? now : null,
                status == IssueStatus.ANALYZING ? now : null,
                status == IssueStatus.ANALYZED ? now : null,
                status == IssueStatus.CLOSED ? now : null
        );
    }
}
