package com.example.aiissuetriage.issue.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.example.aiissuetriage.issue.domain.IssueCategory;
import com.example.aiissuetriage.issue.domain.IssuePriority;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SuppressWarnings("unchecked")
class RedisAnalysisCacheAdapterTest {

    private final StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
    private final ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private final RedisAnalysisCacheAdapter adapter = new RedisAnalysisCacheAdapter(redisTemplate, objectMapper);

    @Test
    void 캐시에서_분석_결과를_조회한다() throws Exception {
        IssueAnalysisResult cachedResult = result();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("issue:analysis:1"))
                .thenReturn(objectMapper.writeValueAsString(cachedResult));

        Optional<IssueAnalysisResult> result = adapter.get(1L);

        assertThat(result).contains(cachedResult);
    }

    @Test
    void 캐시가_없으면_empty를_반환한다() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("issue:analysis:1")).thenReturn(null);

        Optional<IssueAnalysisResult> result = adapter.get(1L);

        assertThat(result).isEmpty();
    }

    @Test
    void 분석_결과를_1시간_TTL로_저장한다() throws Exception {
        IssueAnalysisResult result = result();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        adapter.put(1L, result);

        verify(valueOperations).set(
                "issue:analysis:1",
                objectMapper.writeValueAsString(result),
                RedisAnalysisCacheAdapter.TTL
        );
    }

    @Test
    void 분석_결과_캐시를_삭제한다() {
        adapter.evict(1L);

        verify(redisTemplate).delete("issue:analysis:1");
    }

    private IssueAnalysisResult result() {
        return new IssueAnalysisResult(
                1L,
                10L,
                IssueCategory.PAYMENT,
                IssuePriority.CRITICAL,
                "결제 후 주문 생성 실패",
                "결제 이벤트와 주문 트랜잭션 로그를 확인합니다.",
                0.85,
                "mock-ai-analysis",
                List.of(),
                LocalDateTime.of(2026, 5, 9, 10, 0)
        );
    }
}
