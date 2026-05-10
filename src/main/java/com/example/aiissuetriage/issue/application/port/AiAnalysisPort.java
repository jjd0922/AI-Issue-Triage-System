package com.example.aiissuetriage.issue.application.port;

import com.example.aiissuetriage.issue.application.command.AnalyzeIssueCommand;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;

public interface AiAnalysisPort {

    IssueAnalysisResult analyze(AnalyzeIssueCommand command);
}
