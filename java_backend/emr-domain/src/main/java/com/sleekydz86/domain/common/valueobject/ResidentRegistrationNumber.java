package com.sleekydz86.domain.common.valueobject;

import com.sleekydz86.core.security.encryption.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.regex.Pattern;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class ResidentRegistrationNumber {

    private static final Pattern RRN_PATTERN = Pattern.compile("^\\d{6}-\\d{7}$");

    @Column(name = "rrn", length = 500, unique = true)
    @Convert(converter = EncryptedStringConverter.class)
    private String value;

    private ResidentRegistrationNumber(String value) {
        validate(value);
        this.value = normalize(value);
    }

    public static ResidentRegistrationNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("주민등록번호는 필수입니다.");
        }
        return new ResidentRegistrationNumber(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("주민등록번호는 필수입니다.");
        }
        String normalized = normalize(value);
        if (!RRN_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("주민등록번호 형식이 올바르지 않습니다. 형식: YYMMDD-GXXXXXX");
        }
    }

    private String normalize(String value) {
        return value.replaceAll("[^0-9-]", "").replaceAll("(\\d{6})(\\d{7})", "$1-$2");
    }

    public String getValue() {
        return value;
    }

    public String getMaskedValue() {
        if (value == null || value.length() < 8) {
            return value;
        }
        return value.substring(0, 8) + "******";
    }

    @Override
    public String toString() {
        return getMaskedValue();
    }
}

