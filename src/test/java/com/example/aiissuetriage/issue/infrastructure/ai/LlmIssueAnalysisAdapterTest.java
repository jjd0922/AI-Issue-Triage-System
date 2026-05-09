package com.example.aiissuetriage.issue.infrastructure.ai;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.aiissuetriage.issue.application.command.AnalyzeIssueCommand;
import java.util.List;
import org.junit.jupiter.api.Test;

class LlmIssueAnalysisAdapterTest {

    private final LlmIssueAnalysisAdapter adapter = new LlmIssueAnalysisAdapter();

    @Test
    void LLM_어댑터는_아직_구현되지_않았다() {
        assertThatThrownBy(() -> adapter.analyze(new AnalyzeIssueCommand(
                1L,
                "title",
                "content",
                List.of()
        ))).isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("LLM issue analysis adapter is not implemented yet");
    }
}
