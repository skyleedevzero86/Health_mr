package com.sleekydz86.support.board.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Hashtag {

    @Column(name = "tag", nullable = false, length = 50)
    private String value;

    private Hashtag(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("해시태그는 필수입니다.");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("해시태그는 50자를 초과할 수 없습니다.");
        }
        if (!value.startsWith("#")) {
            throw new IllegalArgumentException("해시태그는 #으로 시작해야 합니다.");
        }
        this.value = value;
    }

    public static Hashtag of(String value) {
        return new Hashtag(value);
    }
}

