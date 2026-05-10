package com.example.aiissuetriage.issue.application.service;

import com.example.aiissuetriage.issue.application.command.IssueSearchCommand;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.domain.Issue;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class InMemoryIssueRepository implements IssueRepositoryPort {

    private final AtomicLong sequence = new AtomicLong(1);
    private final Map<Long, Issue> issues = new LinkedHashMap<>();

    @Override
    public Issue save(Issue issue) {
        Issue savedIssue = issue.getId() == null ? withId(issue, sequence.getAndIncrement()) : issue;
        issues.put(savedIssue.getId(), savedIssue);
        return savedIssue;
    }

    @Override
    public Optional<Issue> findById(Long issueId) {
        return Optional.ofNullable(issues.get(issueId));
    }

    @Override
    public Page<Issue> findAll(IssueSearchCommand command, Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(issues.values()), pageable, issues.size());
    }

    private Issue withId(Issue issue, Long id) {
        return Issue.restore(
                id,
                issue.getTitle(),
                issue.getContent(),
                issue.getSource(),
                issue.getStatus(),
                issue.getFailureReason(),
                issue.getCreatedAt(),
                issue.getUpdatedAt(),
                issue.getAnalysisRequestedAt(),
                issue.getAnalysisStartedAt(),
                issue.getAnalysisCompletedAt(),
                issue.getClosedAt()
        );
    }
}
