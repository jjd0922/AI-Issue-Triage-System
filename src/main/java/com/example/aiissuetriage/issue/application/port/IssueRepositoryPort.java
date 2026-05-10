package com.example.aiissuetriage.issue.application.port;

import com.example.aiissuetriage.issue.application.command.IssueSearchCommand;
import com.example.aiissuetriage.issue.domain.Issue;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IssueRepositoryPort {

    Issue save(Issue issue);

    Optional<Issue> findById(Long issueId);

    Page<Issue> findAll(IssueSearchCommand command, Pageable pageable);
}
