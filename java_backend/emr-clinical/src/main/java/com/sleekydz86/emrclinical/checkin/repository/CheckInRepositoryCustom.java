package com.sleekydz86.emrclinical.checkin.repository;

import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface CheckInRepositoryCustom {
    List<CheckInEntity> findByConditions(
            Long patientNo,
            Long userId,
            CheckInStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Page<CheckInEntity> findByConditionsWithPaging(
            Long patientNo,
            Long userId,
            CheckInStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
    List<CheckInEntity> findTodayCheckIns(LocalDate date);
    List<CheckInEntity> findByPatientNo(Long patientNo);
    List<CheckInEntity> findByCheckInDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<CheckInEntity> findByCheckInStatus(CheckInStatus status);
}
