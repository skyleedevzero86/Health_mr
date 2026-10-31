package com.sleekydz86.core.security.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    @DisplayName("사용자 단위 토큰 폐기 시 저장소에 폐기 시각이 정상 등록된다")
    void revokeUserTokensRecordsTimestampInRedis() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // when
        tokenBlacklistService.revokeUserTokens(100L);

        // then
        verify(valueOperations).set(eq("blacklist:user:revoked_at:100"), anyString(), eq(24 * 60 * 60 * 1000L), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("폐기 시각 이전에 발급된 토큰은 폐기된 것으로 판정된다")
    void tokenIssuedBeforeRevocationIsRevoked() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        long revocationEpoch = 1700000000000L;
        when(valueOperations.get("blacklist:user:revoked_at:100")).thenReturn(String.valueOf(revocationEpoch));
        Instant issuedBefore = Instant.ofEpochMilli(revocationEpoch - 10000L);

        // when
        boolean isRevoked = tokenBlacklistService.isUserTokenRevoked(100L, issuedBefore);

        // then
        assertThat(isRevoked).isTrue();
    }

    @Test
    @DisplayName("폐기 시각 이후에 새로 발급된 토큰은 유효한 것으로 판정된다")
    void tokenIssuedAfterRevocationIsValid() {
        // given
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        long revocationEpoch = 1700000000000L;
        when(valueOperations.get("blacklist:user:revoked_at:100")).thenReturn(String.valueOf(revocationEpoch));
        Instant issuedAfter = Instant.ofEpochMilli(revocationEpoch + 10000L);

        // when
        boolean isRevoked = tokenBlacklistService.isUserTokenRevoked(100L, issuedAfter);

        // then
        assertThat(isRevoked).isFalse();
    }
}
