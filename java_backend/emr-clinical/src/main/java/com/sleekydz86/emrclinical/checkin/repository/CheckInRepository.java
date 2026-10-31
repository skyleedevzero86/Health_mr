package com.sleekydz86.emrclinical.checkin.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.emrclinical.checkin.entity.CheckInEntity;
import com.sleekydz86.emrclinical.types.CheckInStatus;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CheckInRepository extends BaseRepository<CheckInEntity, Long>, CheckInRepositoryCustom {

    @Query("SELECT c FROM CheckIn c WHERE c.patientEntity.patientNo.value = :patientNo")
    List<CheckInEntity> findByPatientEntity_PatientNo(@Param("patientNo") Long patientNo);
    List<CheckInEntity> findByCheckInDateBetween(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    List<CheckInEntity> findByCheckInStatus(CheckInStatus status);

    Optional<CheckInEntity> findByCheckInId(Long checkInId);

    @Query("SELECT c FROM CheckIn c LEFT JOIN FETCH c.patientEntity LEFT JOIN FETCH c.userEntity LEFT JOIN FETCH c.departmentEntity WHERE c.checkInId = :checkInId")
    Optional<CheckInEntity> findDetailById(@Param("checkInId") Long checkInId);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CheckIn c "
            + "WHERE c.patientEntity.patientNo.value = :patientNo "
            + "AND c.checkinDay = :day "
            + "AND c.departmentEntity.id = :departmentId "
            + "AND c.dupSlot = 1")
    boolean existsActiveByPatientDayAndDepartment(
            @Param("patientNo") Long patientNo,
            @Param("day") LocalDate day,
            @Param("departmentId") Long departmentId);

    @Query("SELECT c FROM CheckIn c WHERE c.checkInId = :checkInId")
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<CheckInEntity> findByCheckInIdForUpdate(@Param("checkInId") Long checkInId);
}

