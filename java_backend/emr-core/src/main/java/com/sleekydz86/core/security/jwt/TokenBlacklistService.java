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

    public void blacklistToken(String token) {
        try {

            long expirationTime = getTokenExpirationTime(token);
            long currentTime = System.currentTimeMillis();
            long ttl = expirationTime - currentTime;

            if (ttl > 0) {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, "true", ttl, TimeUnit.MILLISECONDS);
                log.debug("토큰이 Blacklist에 추가되었습니다: {}", token.substring(0, 20) + "...");
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

