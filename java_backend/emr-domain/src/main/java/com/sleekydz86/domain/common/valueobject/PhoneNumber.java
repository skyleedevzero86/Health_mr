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
public class PhoneNumber {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$|^0[2-9]-?[0-9]{3,4}-?[0-9]{4}$"
    );

    @Column(name = "phone_number", length = 500)
    @Convert(converter = EncryptedStringConverter.class)
    private String value;

    private PhoneNumber(String value) {
        validate(value);
        this.value = normalize(value);
    }

    public static PhoneNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        return new PhoneNumber(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("전화번호는 필수입니다.");
        }
        String normalized = normalize(value);
        if (!PHONE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("올바른 전화번호 형식이 아닙니다: " + value);
        }
        if (normalized.length() > 20) {
            throw new IllegalArgumentException("전화번호는 20자를 초과할 수 없습니다.");
        }
    }

    private String normalize(String value) {
        return value.replaceAll("[^0-9]", "");
    }

    public String getValue() {
        return value;
    }

    public String getFormattedValue() {
        if (value.length() == 11) {
            return value.substring(0, 3) + "-" + value.substring(3, 7) + "-" + value.substring(7);
        } else if (value.length() == 10) {
            return value.substring(0, 2) + "-" + value.substring(2, 6) + "-" + value.substring(6);
        }
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}