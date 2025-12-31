package com.sleekydz86.core.security.encryption.dto;

import lombok.Getter;

@Getter
public class DecryptResponse {

    private String decryptedText;

    private DecryptResponse(String decryptedText) {
        this.decryptedText = decryptedText;
    }

    public static DecryptResponse of(String decryptedText) {
        return new DecryptResponse(decryptedText);
    }
}

