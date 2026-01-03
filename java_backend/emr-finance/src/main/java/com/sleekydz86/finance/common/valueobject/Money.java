package com.sleekydz86.finance.common.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class Money {

    private static final long MIN_AMOUNT = 0L;
    private static final int SCALE = 0; // 원 단위이므로 소수점 없음

    @Column(name = "amount", nullable = false)
    private Long value;

    private Money(Long value) {
        validate(value);
        this.value = value;
    }

    public static Money of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("금액은 필수입니다.");
        }
        return new Money(value);
    }

    public static Money zero() {
        return new Money(0L);
    }

    private void validate(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("금액은 필수입니다.");
        }
        if (value < MIN_AMOUNT) {
            throw new IllegalArgumentException("금액은 0 이상이어야 합니다: " + value);
        }
    }

    public Money add(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("더할 금액은 null일 수 없습니다.");
        }
        return Money.of(this.value + other.value);
    }

    public Money subtract(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("뺄 금액은 null일 수 없습니다.");
        }
        if (this.value < other.value) {
            throw new IllegalArgumentException("금액이 부족합니다. 현재: " + this.value + ", 차감: " + other.value);
        }
        return Money.of(this.value - other.value);
    }

    public Money multiply(double multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("곱셈 계수는 0 이상이어야 합니다: " + multiplier);
        }
        BigDecimal result = BigDecimal.valueOf(this.value)
                .multiply(BigDecimal.valueOf(multiplier))
                .setScale(SCALE, RoundingMode.HALF_UP);
        return Money.of(result.longValue());
    }

    public Money applyDiscount(long discountRate) {
        if (discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException("할인율은 0 이상 100 이하여야 합니다: " + discountRate);
        }
        double multiplier = (100.0 - discountRate) / 100.0;
        return multiply(multiplier);
    }

    public Money calculatePercentage(long percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("비율은 0 이상 100 이하여야 합니다: " + percentage);
        }
        double multiplier = percentage / 100.0;
        return multiply(multiplier);
    }

    public boolean isGreaterThanOrEqual(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("비교할 금액은 null일 수 없습니다.");
        }
        return this.value >= other.value;
    }

    public boolean isLessThanOrEqual(Money other) {
        if (other == null) {
            throw new IllegalArgumentException("비교할 금액은 null일 수 없습니다.");
        }
        return this.value <= other.value;
    }

    public boolean isZero() {
        return this.value == 0L;
    }

    public boolean isPositive() {
        return this.value > 0L;
    }

    public String getFormattedValue() {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        return formatter.format(this.value) + "원";
    }

    public Long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getFormattedValue();
    }
}
