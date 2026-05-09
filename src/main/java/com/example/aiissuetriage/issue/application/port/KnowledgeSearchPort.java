package com.example.aiissuetriage.issue.application.port;

import com.example.aiissuetriage.issue.application.result.KnowledgeSearchResult;
import java.util.List;

public interface KnowledgeSearchPort {

    List<KnowledgeSearchResult> search(String query, int limit);
}
