package com.sleekydz86.core.security.jwt;

import com.sleekydz86.core.security.jwt.valueobject.AccessToken;
import com.sleekydz86.core.security.jwt.valueobject.RefreshToken;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;


@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    @PostConstruct
    public void init() {
        validateSecret();
        byte[] bytes = Base64.getDecoder().decode(secret);
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public AccessToken generateAccessToken(Long userId, String role, String inttCd) {
        Date now = new Date();
        String tokenValue = generateAccessTokenValue(userId.toString(), role, inttCd, now);
        return AccessToken.of(tokenValue, userId, role, inttCd, now);
    }

    public AccessToken generateAccessToken(Long userId, String role) {
        return generateAccessToken(userId, role, null);
    }

    private String generateAccessTokenValue(String userId, String role, String inttCd, Date issuedAt) {
        var builder = Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + AccessToken.EXPIRE_TIME_MS));

        if (inttCd != null) {
            builder.claim("inttCd", inttCd);
        }

        return builder.signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public RefreshToken generateRefreshToken(Long userId) {
        Date now = new Date();
        String tokenValue = generateRefreshTokenValue(userId.toString(), now);
        return RefreshToken.of(tokenValue, userId, now);
    }

    private String generateRefreshTokenValue(String userId, Date issuedAt) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(issuedAt)
                .setExpiration(new Date(issuedAt.getTime() + RefreshToken.EXPIRE_TIME_MS))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public TokenPair generateTokenPair(Long userId, String role, String inttCd) {
        AccessToken accessToken = generateAccessToken(userId, role, inttCd);
        RefreshToken refreshToken = generateRefreshToken(userId);
        return TokenPair.of(accessToken, refreshToken);
    }

    public TokenPair generateTokenPair(Long userId, String role) {
        return generateTokenPair(userId, role, null);
    }

    public TokenPair generateTokens(Object userEntity, String primaryInstitutionCode) {
        try {
            // 리플렉션을 사용하여 userId, role 추출
            Long userId = extractUserId(userEntity);
            String role = extractRole(userEntity);

            return generateTokenPair(userId, role, primaryInstitutionCode);
        } catch (Exception e) {
            log.error("토큰 생성 실패", e);
            throw new CustomAuthenticationException("토큰 생성에 실패했습니다.");
        }
    }

    public TokenPair generateTokens(Object userEntity) {
        return generateTokens(userEntity, null);
    }

    private Long extractUserId(Object userEntity) {
        try {
            if (userEntity.getClass().getMethod("getId") != null) {
                Object id = userEntity.getClass().getMethod("getId").invoke(userEntity);
                return id instanceof Long ? (Long) id : Long.valueOf(id.toString());
            }
            throw new IllegalArgumentException("UserEntity에 getId() 메서드가 없습니다.");
        } catch (Exception e) {
            log.error("userId 추출 실패", e);
            throw new IllegalArgumentException("userId를 추출할 수 없습니다.", e);
        }
    }

    private String extractRole(Object userEntity) {
        try {
            Object role = userEntity.getClass().getMethod("getRole").invoke(userEntity);
            if (role instanceof Enum) {
                return ((Enum<?>) role).name();
            }
            return role.toString();
        } catch (Exception e) {
            log.error("role 추출 실패", e);
            throw new IllegalArgumentException("role을 추출할 수 없습니다.", e);
        }
    }

    private String extractInttCd(Object userEntity) {
        try {

            try {
                Object inttCd = userEntity.getClass().getMethod("getInttCd").invoke(userEntity);
                return inttCd != null ? inttCd.toString() : null;
            } catch (NoSuchMethodException e) {

                try {
                    Object inttCd = userEntity.getClass().getMethod("getInttCdValue").invoke(userEntity);
                    return inttCd != null ? inttCd.toString() : null;
                } catch (NoSuchMethodException ex) {

                    return null;
                }
            }
        } catch (Exception e) {
            log.warn("inttCd 추출 실패 (하위 호환성): {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw new CustomAuthenticationException("토큰이 비어있습니다.");
        }

        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (UnsupportedJwtException exception) {
            log.error("지원하지 않는 JWT 형식입니다: {}", token);
            throw new CustomAuthenticationException("지원하지 않는 JWT 형식입니다.");
        } catch (MalformedJwtException exception) {
            log.error("잘못된 JWT 형식입니다: {}", token);
            throw new CustomAuthenticationException("잘못된 JWT 형식입니다.");
        } catch (ExpiredJwtException exception) {
            log.warn("JWT가 만료되었습니다: {}", token);
            throw new CustomAuthenticationException("JWT가 만료되었습니다.");
        } catch (IllegalArgumentException exception) {
            log.error("JWT가 null이거나 비어있습니다.");
            throw new CustomAuthenticationException("JWT가 비어있거나 잘못되었습니다.");
        } catch (JwtException exception) {
            log.error("JWT 검증에 실패했습니다: {}", exception.getMessage());
            throw new CustomAuthenticationException("JWT 검증에 실패했습니다.");
        }
    }

    public AccessToken parseAccessToken(String tokenValue) {
        validateToken(tokenValue);
        Claims claims = extractClaims(tokenValue);
        Long userId = Long.valueOf(claims.getSubject());
        String role = claims.get("role", String.class);
        String inttCd = claims.get("inttCd", String.class);
        Date issuedAt = claims.getIssuedAt();
        return AccessToken.of(tokenValue, userId, role, inttCd, issuedAt);
    }

    public RefreshToken parseRefreshToken(String tokenValue) {
        validateToken(tokenValue);
        Claims claims = extractClaims(tokenValue);
        Long userId = Long.valueOf(claims.getSubject());
        Date issuedAt = claims.getIssuedAt();
        return RefreshToken.of(tokenValue, userId, issuedAt);
    }

    public Authentication getAuthentication(AccessToken accessToken) {
        if (!accessToken.isValid()) {
            throw new CustomAuthenticationException("만료된 토큰입니다.");
        }

        String role = accessToken.getRole();
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

        return new UsernamePasswordAuthenticationToken(
                accessToken.getUserId().toString(),
                null,
                authorities
        );
    }

    public Authentication getAuthentication(String token) {
        AccessToken accessToken = parseAccessToken(token);
        return getAuthentication(accessToken);
    }

    public String resolveToken(String bearerToken) {
        return AccessToken.extractTokenValue(bearerToken);
    }

    public Long getUserIdFromToken(String token) {
        AccessToken accessToken = parseAccessToken(token);
        return accessToken.getUserId();
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret이 설정되지 않았습니다.");
        }
        try {
            Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT secret이 유효한 Base64 형식이 아닙니다.", e);
        }
    }

    public Key getSigningKey() {
        return key;
    }

    public static class TokenPair {
        private final AccessToken accessToken;
        private final RefreshToken refreshToken;

        private TokenPair(AccessToken accessToken, RefreshToken refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

        public static TokenPair of(AccessToken accessToken, RefreshToken refreshToken) {
            return new TokenPair(accessToken, refreshToken);
        }

        public AccessToken getAccessToken() {
            return accessToken;
        }

        public RefreshToken getRefreshToken() {
            return refreshToken;
        }
    }
}

