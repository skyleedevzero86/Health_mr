package com.sleekydz86.emrclinical.checkin.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends BaseRepository<CheckInEntity, Long>, CheckInRepositoryCustom {

    List<CheckInEntity> findByPatientEntity_PatientNo(Long patientNo);
    List<CheckInEntity> findByCheckInDateBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    List<CheckInEntity> findByCheckInStatus(CheckInStatus status);

    Optional<CheckInEntity> findByCheckInId(Long checkInId);
}

