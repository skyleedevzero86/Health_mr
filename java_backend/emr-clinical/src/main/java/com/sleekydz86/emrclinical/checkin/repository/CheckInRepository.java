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
public interface CheckInRepository extends BaseRepository<CheckInEntity, Long> {

    @Query("SELECT c FROM CheckIn c WHERE c.patientEntity.patientNo = :patientNo")
    List<CheckInEntity> findByPatientEntity_PatientNo(@Param("patientNo") Long patientNo);

    @Query("SELECT c FROM CheckIn c WHERE c.checkInDate >= :startDate AND c.checkInDate <= :endDate")
    List<CheckInEntity> findByCheckInDateBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                                  @Param("endDate") java.time.LocalDateTime endDate);

    @Query("SELECT c FROM CheckIn c WHERE DATE(c.checkInDate) = :date")
    List<CheckInEntity> findTodayCheckIns(@Param("date") LocalDate date);

    @Query("SELECT c FROM CheckIn c WHERE c.checkInStatus = :status")
    List<CheckInEntity> findByCheckInStatus(@Param("status") CheckInStatus status);

    Optional<CheckInEntity> findByCheckInId(Long checkInId);
}

