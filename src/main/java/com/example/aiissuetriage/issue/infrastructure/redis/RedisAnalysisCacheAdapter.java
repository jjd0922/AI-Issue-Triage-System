package com.example.aiissuetriage.issue.infrastructure.redis;

import com.example.aiissuetriage.issue.application.port.AnalysisCachePort;
import com.example.aiissuetriage.issue.application.result.IssueAnalysisResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(StringRedisTemplate.class)
public class RedisAnalysisCacheAdapter implements AnalysisCachePort {

    static final Duration TTL = Duration.ofHours(1);

    private static final String KEY_PREFIX = "issue:analysis:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<IssueAnalysisResult> get(Long issueId) {
        String value = redisTemplate.opsForValue().get(key(issueId));
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(value, IssueAnalysisResult.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize issue analysis cache. issueId=" + issueId, e);
        }
    }

    @Override
    public void put(Long issueId, IssueAnalysisResult result) {
        try {
            redisTemplate.opsForValue().set(key(issueId), objectMapper.writeValueAsString(result), TTL);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize issue analysis cache. issueId=" + issueId, e);
        }
    }

    @Override
    public void evict(Long issueId) {
        redisTemplate.delete(key(issueId));
    }

    private String key(Long issueId) {
        return KEY_PREFIX + issueId;
    }
}
