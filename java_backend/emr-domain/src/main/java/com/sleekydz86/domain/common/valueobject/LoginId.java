package com.sleekydz86.domain.common.valueobject;

import jakarta.persistence.Column;
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
public class LoginId {

    private static final Pattern LOGIN_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{4,50}$");

    @Column(name = "login_id", length = 50, unique = true)
    private String value;

    private LoginId(String value) {
        validate(value);
        this.value = value.toLowerCase();
    }

    public static LoginId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("로그인 ID는 필수입니다.");
        }
        return new LoginId(value);
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("로그인 ID는 필수입니다.");
        }
        if (value.length() < 4 || value.length() > 50) {
            throw new IllegalArgumentException("로그인 ID는 4자 이상 50자 이하여야 합니다.");
        }
        if (!LOGIN_ID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("로그인 ID는 영문, 숫자, 언더스코어만 사용 가능합니다.");
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
