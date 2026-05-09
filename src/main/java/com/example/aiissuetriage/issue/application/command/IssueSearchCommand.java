package com.example.aiissuetriage.issue.application.command;

import com.example.aiissuetriage.issue.domain.IssueSource;
import com.example.aiissuetriage.issue.domain.IssueStatus;

public record IssueSearchCommand(
        IssueStatus status,
        IssueSource source
) {
}
