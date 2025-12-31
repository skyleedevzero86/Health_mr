package com.sleekydz86.core.security.encryption.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class DecryptRequest {

    @NotBlank(message = "암호화된 텍스트는 필수입니다.")
    private String encryptedText;
}
