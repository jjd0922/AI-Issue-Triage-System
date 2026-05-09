package com.example.aiissuetriage.issue.application.service;

import com.example.aiissuetriage.issue.application.command.AnalyzeIssueCommand;
import com.example.aiissuetriage.issue.application.exception.IssueNotFoundException;
import com.example.aiissuetriage.issue.application.port.AiAnalysisPort;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.port.IssueAnalysisRepositoryPort;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.application.port.KnowledgeSearchPort;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import com.example.aiissuetriage.issue.domain.Issue;
import com.example.aiissuetriage.issue.domain.IssueAnalysis;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IssueAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(IssueAnalysisService.class);

    private static final int KNOWLEDGE_SEARCH_LIMIT = 5;

    private final IssueRepositoryPort issueRepositoryPort;
    private final IssueAnalysisRepositoryPort issueAnalysisRepositoryPort;
    private final KnowledgeSearchPort knowledgeSearchPort;
    private final AiAnalysisPort aiAnalysisPort;
    private final AnalysisCachePort analysisCachePort;

    @Transactional
    public IssueAnalysisResult processAnalysis(Long issueId) {
        Issue issue = issueRepositoryPort.findById(issueId)
                .orElseThrow(() -> new IssueNotFoundException(issueId));

        issue.startAnalysis();
        issueRepositoryPort.save(issue);

        try {
            List<KnowledgeSearchResult> references = knowledgeSearchPort.search(
                    issue.getTitle(),
                    KNOWLEDGE_SEARCH_LIMIT
            );
            IssueAnalysisResult aiResult = aiAnalysisPort.analyze(new AnalyzeIssueCommand(
                    issue.getId(),
                    issue.getTitle(),
                    issue.getContent(),
                    references
            ));

            IssueAnalysis savedAnalysis = issueAnalysisRepositoryPort.save(IssueAnalysis.create(
                    issue.getId(),
                    aiResult.category(),
                    aiResult.priority(),
                    aiResult.summary(),
                    aiResult.recommendation(),
                    aiResult.confidence(),
                    aiResult.modelName(),
                    null
            ));

            issue.completeAnalysis();
            issueRepositoryPort.save(issue);

            IssueAnalysisResult result = IssueResultMapper.toAnalysisResult(savedAnalysis, references);
            putAnalysisCache(issue.getId(), result);
            return result;
        } catch (RuntimeException e) {
            issue.failAnalysis(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            issueRepositoryPort.save(issue);
            throw e;
        }
    }

    private void putAnalysisCache(Long issueId, IssueAnalysisResult result) {
        try {
            analysisCachePort.put(issueId, result);
        } catch (RuntimeException e) {
            log.warn("Failed to put issue analysis cache. issueId={}", issueId, e);
        }
    }
}
