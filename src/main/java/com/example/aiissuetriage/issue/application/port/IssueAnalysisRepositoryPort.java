package com.example.aiissuetriage.issue.application.port;

import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import java.util.Optional;

public interface IssueAnalysisRepositoryPort {

    IssueAnalysis save(IssueAnalysis analysis);

    Optional<IssueAnalysis> findLatestByIssueId(Long issueId);
}
