package com.example.aiissuetriage.issue.application.service;

import com.example.aiissuetriage.issue.application.exception.IssueNotFoundException;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.domain.Issue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueAnalysisFailureService {

    private final IssueRepositoryPort issueRepositoryPort;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAnalysisFailed(Long issueId, String reason) {
        Issue issue = issueRepositoryPort.findById(issueId)
                .orElseThrow(() -> new IssueNotFoundException(issueId));

        issue.failAnalysis(reason);
        issueRepositoryPort.save(issue);
    }
}
