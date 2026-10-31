package com.sleekydz86.core.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private static final String USER_REVOCATION_PREFIX = "blacklist:user:revoked_at:";
    private static final long MAX_TOKEN_TTL_MS = 24 * 60 * 60 * 1000L; // 24시간

    public void blacklistToken(String token) {
        try {
            long expirationTime = getTokenExpirationTime(token);
            long currentTime = System.currentTimeMillis();
            long ttl = expirationTime - currentTime;

            if (ttl > 0) {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.MILLISECONDS);
                log.debug("토큰이 Blacklist에 추가되었습니다: {}", token.substring(0, Math.min(20, token.length())) + "...");
            }
        } catch (Exception e) {
            log.error("토큰 Blacklist 추가 실패", e);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_PREFIX + token;
            String value = redisTemplate.opsForValue().get(key);
            return value != null && value.equals("true");
        } catch (Exception e) {
            log.error("토큰 Blacklist 확인 실패", e);
            return false;
        }
    }

    /**
     * 사용자 단위의 모든 토큰 폐기 (퇴직, 계정 정지, 역할 변경, 비밀번호 변경 등)
     */
    public void revokeUserTokens(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            String key = USER_REVOCATION_PREFIX + userId;
            long now = System.currentTimeMillis();
            redisTemplate.opsForValue().set(key, String.valueOf(now), MAX_TOKEN_TTL_MS, TimeUnit.MILLISECONDS);
            log.info("사용자(ID: {})의 모든 발급 토큰이 폐기(Revoke) 처리되었습니다. 기준시각: {}", userId, now);
        } catch (Exception e) {
            log.error("사용자 토큰 폐기 처리 실패 (userId: {})", userId, e);
        }
    }

    /**
     * 토큰 발급 시점이 사용자 토큰 폐기 시점 이전인지 검사
     */
    public boolean isUserTokenRevoked(Long userId, java.time.Instant tokenIssuedAt) {
        if (userId == null || tokenIssuedAt == null) {
            return false;
        }
        try {
            String key = USER_REVOCATION_PREFIX + userId;
            String val = redisTemplate.opsForValue().get(key);
            if (val == null) {
                return false;
            }
            long revokedAtEpoch = Long.parseLong(val);
            return tokenIssuedAt.toEpochMilli() <= revokedAtEpoch;
        } catch (Exception e) {
            log.error("사용자 토큰 폐기 여부 확인 실패 (userId: {})", userId, e);
            return false;
        }
    }

    private long getTokenExpirationTime(String token) {
        try {

            String cleanToken = jwtUtil.resolveToken(token);
            if (cleanToken == null) {
                cleanToken = token;
            }

            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                    .setSigningKey(jwtUtil.getSigningKey())
                    .build()
                    .parseClaimsJws(cleanToken)
                    .getBody();

            return claims.getExpiration().getTime();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {

            return e.getClaims().getExpiration().getTime();
        } catch (Exception e) {
            log.error("토큰 만료 시간 추출 실패", e);

            return System.currentTimeMillis() + (15 * 60 * 1000);
        }
    }

    public void clearBlacklist() {
        try {

            Set<String> keys = redisTemplate.keys(BLACKLIST_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Blacklist가 초기화되었습니다. 삭제된 키 수: {}", keys.size());
            } else {
                log.info("Blacklist가 비어있습니다.");
            }
        } catch (Exception e) {
            log.error("Blacklist 초기화 실패", e);
        }
    }
}

