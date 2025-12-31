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
public class RefreshToken {

    public static final long EXPIRE_TIME_MS = 1000 * 60 * 60 * 24 * 7; // 7일

    private String value;
    private Long userId;
    private Instant issuedAt;
    private Instant expiresAt;

    private RefreshToken(String value, Long userId, Instant issuedAt, Instant expiresAt) {
        validate(value, userId, issuedAt, expiresAt);
        this.value = value;
        this.userId = userId;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public static RefreshToken of(String tokenValue, Long userId, Date issuedAt) {
        Instant issuedAtInstant = issuedAt.toInstant();
        Instant expiresAtInstant = issuedAtInstant.plusMillis(EXPIRE_TIME_MS);
        return new RefreshToken(tokenValue, userId, issuedAtInstant, expiresAtInstant);
    }

    public static RefreshToken of(String tokenValue, Long userId) {
        Instant now = Instant.now();
        return of(tokenValue, userId, Date.from(now));
    }

    private void validate(String value, Long userId, Instant issuedAt, Instant expiresAt) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("토큰 값은 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
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

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "RefreshToken{userId=" + userId + ", expiresAt=" + expiresAt + "}";
    }
}