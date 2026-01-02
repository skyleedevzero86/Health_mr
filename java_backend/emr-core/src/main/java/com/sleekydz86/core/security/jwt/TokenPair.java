package com.sleekydz86.core.security.jwt;

import com.sleekydz86.core.security.jwt.valueobject.AccessToken;
import com.sleekydz86.core.security.jwt.valueobject.RefreshToken;

public class TokenPair {
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

