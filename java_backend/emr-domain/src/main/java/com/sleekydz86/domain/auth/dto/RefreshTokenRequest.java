package com.sleekydz86.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenRequest {
    @NotBlank(message = "리프레시 토큰은 필수항목입니다.")
    private String refreshToken;
}

