package com.example.aiissuetriage.issue.infrastructure.ai;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.aiissuetriage.issue.application.command.AnalyzeIssueCommand;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LlmIssueAnalysisAdapterTest {

    private final LlmIssueAnalysisAdapter adapter = new LlmIssueAnalysisAdapter();

    @Test
    @DisplayName("analyze 는 LLM 어댑터가 아직 구현되지 않았으면 예외를 던진다")
    void analyze_whenLlmAdapterIsNotImplemented_thenThrowException() {
        assertThatThrownBy(() -> adapter.analyze(new AnalyzeIssueCommand(
                1L,
                "title",
                "content",
                List.of()
        ))).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("LLM issue analysis adapter is not implemented yet");
    }
}
