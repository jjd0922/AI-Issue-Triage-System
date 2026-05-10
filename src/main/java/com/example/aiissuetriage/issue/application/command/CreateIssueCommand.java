package com.example.aiissuetriage.issue.application.command;

import com.example.aiissuetriage.issue.domain.IssueSource;

public record CreateIssueCommand(
        String title,
        String content,
        IssueSource source
) {
}
