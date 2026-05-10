package com.example.aiissuetriage.issue.application.port;

import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import java.util.Optional;

public interface AnalysisCachePort {

    Optional<IssueAnalysisResult> get(Long issueId);

    void put(Long issueId, IssueAnalysisResult result);

    void evict(Long issueId);
}
