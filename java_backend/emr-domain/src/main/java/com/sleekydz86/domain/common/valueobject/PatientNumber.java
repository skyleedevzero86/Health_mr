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
public class PatientNumber {

    private static final Pattern PATIENT_NUMBER_PATTERN = Pattern.compile("^N\\d{7}$");
    private static final String PREFIX = "N";

    @Column(name = "patient_no", length = 8, unique = true)
    private Long value;

    private PatientNumber(Long value) {
        validate(value);
        this.value = value;
    }

    public static PatientNumber of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("환자 번호는 필수입니다.");
        }
        return new PatientNumber(value);
    }

    public static PatientNumber of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("환자 번호는 필수입니다.");
        }
        if (!PATIENT_NUMBER_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("환자 번호 형식이 올바르지 않습니다. 형식: N0000001");
        }
        try {
            Long numericValue = Long.parseLong(value.substring(1));
            return new PatientNumber(numericValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("환자 번호 형식이 올바르지 않습니다: " + value);
        }
    }

    private void validate(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("환자 번호는 필수입니다.");
        }
        String stringValue = PREFIX + String.format("%07d", value);
        if (!PATIENT_NUMBER_PATTERN.matcher(stringValue).matches()) {
            throw new IllegalArgumentException("환자 번호 형식이 올바르지 않습니다. 형식: N0000001");
        }
    }

    public String getFormattedValue() {
        return PREFIX + String.format("%07d", value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getFormattedValue();
    }
}

