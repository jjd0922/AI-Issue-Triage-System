package com.example.aiissuetriage.issue.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class IssueTest {

    @Test
    void 신규_이슈는_REGISTERED_상태로_생성된다() {
        Issue issue = Issue.create("결제 오류", "주문이 생성되지 않습니다.", IssueSource.CUSTOMER_SERVICE);

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.REGISTERED);
        assertThat(issue.getTitle()).isEqualTo("결제 오류");
        assertThat(issue.getContent()).isEqualTo("주문이 생성되지 않습니다.");
        assertThat(issue.getSource()).isEqualTo(IssueSource.CUSTOMER_SERVICE);
        assertThat(issue.getCreatedAt()).isNotNull();
        assertThat(issue.getUpdatedAt()).isNotNull();
    }

    @Test
    void 등록된_이슈는_분석_요청_상태로_변경할_수_있다() {
        Issue issue = newIssue(IssueStatus.REGISTERED);

        issue.requestAnalysis();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYSIS_REQUESTED);
        assertThat(issue.getAnalysisRequestedAt()).isNotNull();
        assertThat(issue.getFailureReason()).isNull();
    }

    @Test
    void 분석_실패_상태의_이슈는_분석_요청_상태로_재요청할_수_있다() {
        Issue issue = newIssue(IssueStatus.ANALYSIS_FAILED, "temporary failure");

        issue.requestAnalysis();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYSIS_REQUESTED);
        assertThat(issue.getFailureReason()).isNull();
    }

    @Test
    void 분석_요청_상태의_이슈는_분석_중_상태로_변경할_수_있다() {
        Issue issue = newIssue(IssueStatus.ANALYSIS_REQUESTED);

        issue.startAnalysis();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYZING);
        assertThat(issue.getAnalysisStartedAt()).isNotNull();
    }

    @Test
    void 분석_중_상태의_이슈는_분석_완료_상태로_변경할_수_있다() {
        Issue issue = newIssue(IssueStatus.ANALYZING);

        issue.completeAnalysis();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYZED);
        assertThat(issue.getAnalysisCompletedAt()).isNotNull();
        assertThat(issue.getFailureReason()).isNull();
    }

    @Test
    void 분석_요청_상태의_이슈는_분석_실패_상태로_변경할_수_있다() {
        Issue issue = newIssue(IssueStatus.ANALYSIS_REQUESTED);

        issue.failAnalysis("AI adapter timeout");

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYSIS_FAILED);
        assertThat(issue.getFailureReason()).isEqualTo("AI adapter timeout");
    }

    @Test
    void 분석_중_상태의_이슈는_분석_실패_상태로_변경할_수_있다() {
        Issue issue = newIssue(IssueStatus.ANALYZING);

        issue.failAnalysis("AI adapter timeout");

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.ANALYSIS_FAILED);
        assertThat(issue.getFailureReason()).isEqualTo("AI adapter timeout");
    }

    @Test
    void 분석_완료_상태의_이슈는_종료할_수_있다() {
        Issue issue = newIssue(IssueStatus.ANALYZED);

        issue.close();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.CLOSED);
        assertThat(issue.getClosedAt()).isNotNull();
    }

    @Test
    void 분석_실패_상태의_이슈는_종료할_수_있다() {
        Issue issue = newIssue(IssueStatus.ANALYSIS_FAILED);

        issue.close();

        assertThat(issue.getStatus()).isEqualTo(IssueStatus.CLOSED);
        assertThat(issue.getClosedAt()).isNotNull();
    }

    @Test
    void 허용되지_않은_상태에서_분석_요청하면_예외가_발생한다() {
        Issue issue = newIssue(IssueStatus.ANALYZED);

        assertThatThrownBy(issue::requestAnalysis)
                .isInstanceOf(InvalidIssueStatusException.class)
                .hasMessageContaining("Cannot requestAnalysis issue");
    }

    @Test
    void 허용되지_않은_상태에서_분석을_시작하면_예외가_발생한다() {
        Issue issue = newIssue(IssueStatus.REGISTERED);

        assertThatThrownBy(issue::startAnalysis)
                .isInstanceOf(InvalidIssueStatusException.class)
                .hasMessageContaining("Cannot startAnalysis issue");
    }

    @Test
    void 허용되지_않은_상태에서_분석을_완료하면_예외가_발생한다() {
        Issue issue = newIssue(IssueStatus.ANALYSIS_REQUESTED);

        assertThatThrownBy(issue::completeAnalysis)
                .isInstanceOf(InvalidIssueStatusException.class)
                .hasMessageContaining("Cannot completeAnalysis issue");
    }

    @Test
    void 허용되지_않은_상태에서_분석_실패로_변경하면_예외가_발생한다() {
        Issue issue = newIssue(IssueStatus.ANALYZED);

        assertThatThrownBy(() -> issue.failAnalysis("failed"))
                .isInstanceOf(InvalidIssueStatusException.class)
                .hasMessageContaining("Cannot failAnalysis issue");
    }

    @Test
    void 허용되지_않은_상태에서_종료하면_예외가_발생한다() {
        Issue issue = newIssue(IssueStatus.ANALYZING);

        assertThatThrownBy(issue::close)
                .isInstanceOf(InvalidIssueStatusException.class)
                .hasMessageContaining("Cannot close issue");
    }

    @Test
    void 실패_사유는_빈_값일_수_없다() {
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
