package com.sleekydz86.emrclinical.reservation.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.emrclinical.reservation.entity.ReservationEntity;
import com.sleekydz86.emrclinical.types.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends BaseRepository<ReservationEntity, Long>, ReservationRepositoryCustom {

    List<ReservationEntity> findByPatientEntity_PatientNo(Long patientNo);

    Page<ReservationEntity> findByPatientEntity_PatientNo(Long patientNo, Pageable pageable);

    List<ReservationEntity> findByReservationDateBetween(LocalDateTime start, LocalDateTime end);

    Page<ReservationEntity> findByReservationDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<ReservationEntity> findByReservationStatus(ReservationStatus status);

    Page<ReservationEntity> findByReservationStatus(ReservationStatus status, Pageable pageable);

    List<ReservationEntity> findByReservationDate(LocalDateTime date);

    List<ReservationEntity> findByUserEntity_Id(Long userId);

    boolean existsByPatientEntity_PatientNoAndReservationDate(Long patientNo, LocalDateTime date);
    Page<ReservationEntity> findAll(Pageable pageable);
    List<ReservationEntity> findAllByOrderByReservationDateAsc();


    @Deprecated
    List<ReservationEntity> findByReservationYn(String reservationYn);
}

