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
public class Email {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Column(name = "email", length = 500)
    @Convert(converter = EncryptedStringConverter.class)
    private String value;

    private Email(String value) {
        validate(value);
        this.value = value;
    }

    public static Email of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        return new Email(value.trim().toLowerCase());
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다: " + value);
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("이메일은 100자를 초과할 수 없습니다.");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
