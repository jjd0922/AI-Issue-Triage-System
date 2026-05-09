package com.example.aiissuetriage.issue.presentation.request;

import com.example.aiissuetriage.issue.application.command.CreateIssueCommand;
import com.example.aiissuetriage.issue.domain.IssueSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateIssueRequest(
        @NotBlank
        @Size(max = 200)
        String title,

        @NotBlank
        @Size(max = 5000)
        String content,

        @NotNull
        IssueSource source
) {

    public CreateIssueCommand toCommand() {
        return new CreateIssueCommand(title, content, source);
    }
}
