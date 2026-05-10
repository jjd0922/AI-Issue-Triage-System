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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SuppressWarnings("unchecked")
@ExtendWith(MockitoExtension.class)
class RedisAnalysisCacheAdapterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @InjectMocks
    private RedisAnalysisCacheAdapter adapter;

    @Test
    @DisplayName("get 은 캐시에서 분석 결과를 조회한다")
    void get_whenCachedValueExists_thenReturnAnalysisResult() throws Exception {
        IssueAnalysisResult cachedResult = result();
        String cachedJson = objectMapper.writeValueAsString(cachedResult);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("issue:analysis:1")).thenReturn(cachedJson);

        Optional<IssueAnalysisResult> result = adapter.get(1L);

        assertThat(result).contains(cachedResult);
    }

    @Test
    @DisplayName("get 은 캐시가 없으면 empty 를 반환한다")
    void get_whenCacheMiss_thenReturnEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("issue:analysis:1")).thenReturn(null);

        Optional<IssueAnalysisResult> result = adapter.get(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("put 은 분석 결과를 1시간 TTL 로 저장한다")
    void put_whenAnalysisResultProvided_thenStoreWithOneHourTtl() throws Exception {
        IssueAnalysisResult result = result();
        String expectedJson = objectMapper.writeValueAsString(result);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        adapter.put(1L, result);

        verify(valueOperations).set(
                "issue:analysis:1",
                expectedJson,
                RedisAnalysisCacheAdapter.TTL
        );
    }

    @Test
    @DisplayName("evict 는 분석 결과 캐시를 삭제한다")
    void evict_whenIssueIdProvided_thenDeleteCacheKey() {
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
