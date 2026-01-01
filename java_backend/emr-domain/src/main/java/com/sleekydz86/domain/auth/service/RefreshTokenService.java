package com.sleekydz86.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import com.sleekydz86.core.security.jwt.JwtUtil;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7일 (기본값)
    private long refreshTokenExpiration;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String USER_TOKEN_PREFIX = "user_token:";

    @Transactional
    public void saveRefreshToken(Long userId, String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userKey = USER_TOKEN_PREFIX + userId;


        redisTemplate.opsForValue().set(tokenKey, String.valueOf(userId),
                refreshTokenExpiration, TimeUnit.MILLISECONDS);


        redisTemplate.opsForValue().set(userKey, refreshToken,
                refreshTokenExpiration, TimeUnit.MILLISECONDS);
    }


    public boolean validateRefreshToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;

        if (!Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey))) {
            return false;
        }

        try {
            jwtUtil.validateToken(refreshToken);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Long getUserIdFromToken(String refreshToken) {
        String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
        String userIdStr = redisTemplate.opsForValue().get(tokenKey);

        if (userIdStr == null) {
            // Redis에 없으면 JWT에서 추출
            return jwtUtil.getUserIdFromToken(refreshToken);
        }

        return Long.parseLong(userIdStr);
    }

    @Transactional
    public void deleteRefreshToken(Long userId) {
        String userKey = USER_TOKEN_PREFIX + userId;
        String refreshToken = redisTemplate.opsForValue().get(userKey);

        if (refreshToken != null) {
            String tokenKey = REFRESH_TOKEN_PREFIX + refreshToken;
            redisTemplate.delete(tokenKey);
        }

        redisTemplate.delete(userKey);
    }

    @Transactional
    public void rotateRefreshToken(Long userId, String oldRefreshToken, String newRefreshToken) {

        deleteRefreshToken(userId);

        saveRefreshToken(userId, newRefreshToken);
    }

    public boolean isTokenBlacklisted(String token) {
        // emr-core의 TokenBlacklistService 활용 예정
        return false;
    }
}
