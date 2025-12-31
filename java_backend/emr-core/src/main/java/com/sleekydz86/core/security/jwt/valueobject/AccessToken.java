package com.sleekydz86.core.security.jwt.valueobject;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Date;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class AccessToken {

    public static final long EXPIRE_TIME_MS = 1000 * 60 * 15; // 15분
    private static final String BEARER_PREFIX = "Bearer ";

    private String value;
    private Long userId;
    private String role;
    private String inttCd; // 기관 코드 (멀티테넌트)
    private Instant issuedAt;
    private Instant expiresAt;

    private AccessToken(String value, Long userId, String role, String inttCd, Instant issuedAt, Instant expiresAt) {
        validate(value, userId, role, inttCd, issuedAt, expiresAt);
        this.value = value;
        this.userId = userId;
        this.role = role;
        this.inttCd = inttCd;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public static AccessToken of(String tokenValue, Long userId, String role, String inttCd, Date issuedAt) {
        Instant issuedAtInstant = issuedAt.toInstant();
        Instant expiresAtInstant = issuedAtInstant.plusMillis(EXPIRE_TIME_MS);
        return new AccessToken(tokenValue, userId, role, inttCd, issuedAtInstant, expiresAtInstant);
    }

    public static AccessToken of(String tokenValue, Long userId, String role, String inttCd) {
        Instant now = Instant.now();
        return of(tokenValue, userId, role, inttCd, Date.from(now));
    }

    public static AccessToken of(String tokenValue, Long userId, String role, Date issuedAt) {
        return of(tokenValue, userId, role, null, issuedAt);
    }

    public static AccessToken of(String tokenValue, Long userId, String role) {
        return of(tokenValue, userId, role, null);
    }

    private void validate(String value, Long userId, String role, String inttCd, Instant issuedAt, Instant expiresAt) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("토큰 값은 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("역할은 필수입니다.");
        }
        if (issuedAt == null) {
            throw new IllegalArgumentException("발행 시간은 필수입니다.");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("만료 시간은 필수입니다.");
        }
        if (expiresAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("만료 시간은 발행 시간 이후여야 합니다.");
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isExpired();
    }

    public String toBearerToken() {
        return BEARER_PREFIX + value;
    }

    public static String extractTokenValue(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken;
        }
        return bearerToken.substring(BEARER_PREFIX.length());
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "AccessToken{userId=" + userId + ", role=" + role + ", expiresAt=" + expiresAt + "}";
    }
}

