package com.sleekydz86.emrclinical.reservation.repository;

import com.sleekydz86.emrclinical.reservation.entity.ReservationEntity;
import com.sleekydz86.emrclinical.types.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepositoryCustom {
    List<ReservationEntity> findByConditions(
            Long patientNo,
            Long userId,
            ReservationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
    Page<ReservationEntity> findByConditionsWithPaging(
            Long patientNo,
            Long userId,
            ReservationStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
    List<ReservationEntity> findTodayReservations(LocalDate date);
    Long countByDateRange(LocalDateTime start, LocalDateTime end);
}
