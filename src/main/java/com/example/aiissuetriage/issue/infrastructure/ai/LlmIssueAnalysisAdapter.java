package com.example.aiissuetriage.issue.infrastructure.ai;

import com.example.aiissuetriage.issue.application.command.AnalyzeIssueCommand;
import com.example.aiissuetriage.issue.application.port.AiAnalysisPort;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("llm")
public class LlmIssueAnalysisAdapter implements AiAnalysisPort {

    @Override
    public IssueAnalysisResult analyze(AnalyzeIssueCommand command) {
        throw new UnsupportedOperationException("LLM issue analysis adapter is not implemented yet");
    }
}
