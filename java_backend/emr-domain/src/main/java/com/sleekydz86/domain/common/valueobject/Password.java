package com.sleekydz86.domain.common.valueobject;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.regex.Pattern;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Password {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    private String encryptedValue;

    private Password(String encryptedValue) {
        this.encryptedValue = encryptedValue;
    }

    public static Password fromPlainText(String plainPassword, PasswordEncoder passwordEncoder) {
        validate(plainPassword);
        String encrypted = passwordEncoder.encode(plainPassword);
        return new Password(encrypted);
    }

    public static Password fromEncrypted(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isBlank()) {
            throw new IllegalArgumentException("암호화된 비밀번호는 필수입니다.");
        }
        return new Password(encryptedValue);
    }

    private static void validate(String plainPassword) {
        if (plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        if (plainPassword.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 8자 이상이어야 합니다.");
        }
        if (!PASSWORD_PATTERN.matcher(plainPassword).matches()) {
            throw new IllegalArgumentException(
                    "비밀번호는 대문자, 소문자, 숫자, 특수문자를 포함한 8자 이상이어야 합니다."
            );
        }
    }

    public boolean matches(String plainPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(plainPassword, this.encryptedValue);
    }

    public String getEncryptedValue() {
        return encryptedValue;
    }
}

