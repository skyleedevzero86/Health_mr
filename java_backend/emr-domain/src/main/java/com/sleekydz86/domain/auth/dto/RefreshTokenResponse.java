package com.sleekydz86.domain.auth.dto;

public record RefreshTokenResponse(
        String accessToken,
        String refreshToken
) {
    public static RefreshTokenResponse of(String accessToken, String refreshToken) {
        return new RefreshTokenResponse(accessToken, refreshToken);
    }
}

