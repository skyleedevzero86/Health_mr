package com.sleekydz86.core.security.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class BlindIndexService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static BlindIndexService instance;

    private final byte[] hmacKey;

    public BlindIndexService(@Value("${encryption.hmac-key:}") String hmacKeyValue) {
        if (hmacKeyValue == null || hmacKeyValue.isBlank()) {
            throw new IllegalStateException("블라인드 인덱스 키가 설정되지 않았습니다.");
        }
        this.hmacKey = hmacKeyValue.getBytes(StandardCharsets.UTF_8);
        instance = this;
    }

    public static BlindIndexService current() {
        if (instance == null) {
            throw new IllegalStateException("블라인드 인덱스 서비스가 초기화되지 않았습니다.");
        }
        return instance;
    }

    public String hmacRrn(String rrn) {
        return hmac(digitsOnly(rrn));
    }

    public String hmacPhone(String tel) {
        return hmac(digitsOnly(tel));
    }

    public String hmacEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            return null;
        }
        return hmac(normalized);
    }

    public String hmacPassport(String passport) {
        if (passport == null) {
            return null;
        }
        String normalized = passport.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return hmac(normalized);
    }

    private String digitsOnly(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : digits;
    }

    private String hmac(String normalized) {
        if (normalized == null || normalized.isEmpty()) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(hmacKey, HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("블라인드 인덱스 생성에 실패했습니다.", exception);
        }
    }
}
