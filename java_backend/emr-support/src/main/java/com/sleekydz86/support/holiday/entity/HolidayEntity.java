package com.sleekydz86.support.holiday.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sleekydz86.domain.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity(name = "HolidayEntity")
@Table(name = "holiday")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HolidayEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "holiday_date", nullable = false, unique = true)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate holidayDate;

    @Column(name = "holiday_national", nullable = false)
    private Boolean holidayNational;

    @Column(name = "holiday_reason", length = 255)
    private String holidayReason;

    @Builder
    private HolidayEntity(
            Long id,
            LocalDate holidayDate,
            Boolean holidayNational,
            String holidayReason
    ) {
        validate(holidayDate, holidayNational);
        this.id = id;
        this.holidayDate = holidayDate;
        this.holidayNational = holidayNational;
        this.holidayReason = holidayReason;
    }

    private void validate(LocalDate holidayDate, Boolean holidayNational) {
        if (holidayDate == null) {
            throw new IllegalArgumentException("휴일 날짜는 필수입니다.");
        }
        if (holidayNational == null) {
            throw new IllegalArgumentException("국가 공휴일 여부는 필수입니다.");
        }
        if (holidayDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("과거 날짜는 휴일로 등록할 수 없습니다.");
        }
    }

    public void updateInfo(Boolean isNational, String reason) {
        if (isNational != null) {
            this.holidayNational = isNational;
        }
        if (reason != null) {
            this.holidayReason = reason;
        }
    }

    public void setNational() {
        this.holidayNational = true;
    }

    public void setHospitalHoliday() {
        this.holidayNational = false;
    }

    public boolean isNational() {
        return Boolean.TRUE.equals(holidayNational);
    }

    public boolean isHospitalHoliday() {
        return Boolean.FALSE.equals(holidayNational);
    }

    public boolean isHoliday(LocalDate date) {
        return this.holidayDate.equals(date);
    }

    public boolean isPast() {
        return this.holidayDate.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        return this.holidayDate.equals(LocalDate.now());
    }

    public boolean isFuture() {
        return this.holidayDate.isAfter(LocalDate.now());
    }
}