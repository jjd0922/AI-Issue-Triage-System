package com.example.aiissuetriage;

import com.example.aiissuetriage.issue.application.port.AiAnalysisPort;
import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.port.IssueAnalysisRepositoryPort;
import com.example.aiissuetriage.issue.application.port.IssueAnalysisRequestedEventPublisher;
import com.example.aiissuetriage.issue.application.port.IssueRepositoryPort;
import com.example.aiissuetriage.issue.application.port.KnowledgeSearchPort;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "spring.docker.compose.enabled=false"
})
class AiIssueTriageSystemApplicationTests {

    @MockitoBean
    private IssueRepositoryPort issueRepositoryPort;

    @MockitoBean
    private IssueAnalysisRepositoryPort issueAnalysisRepositoryPort;

    @MockitoBean
    private IssueAnalysisRequestedEventPublisher issueAnalysisRequestedEventPublisher;

    @MockitoBean
    private AiAnalysisPort aiAnalysisPort;

    @MockitoBean
    private KnowledgeSearchPort knowledgeSearchPort;

    @MockitoBean
    private AnalysisCachePort analysisCachePort;

    @Test
    void contextLoads() {
    }
}
