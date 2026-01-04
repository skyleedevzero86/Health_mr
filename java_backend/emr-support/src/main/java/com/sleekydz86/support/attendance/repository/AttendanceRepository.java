package com.sleekydz86.support.attendance.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.attendance.entity.AttendanceEntity;
import com.sleekydz86.support.attendance.type.AttendanceType;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends BaseRepository<AttendanceEntity, Long> {

    Optional<AttendanceEntity> findByAttendanceId(Long attendanceId);

    List<AttendanceEntity> findByUserEntity_Id(Long userId);

    List<AttendanceEntity> findByUserEntity_IdAndAttendanceTime_ValueBetween(
            Long userId, LocalDateTime start, LocalDateTime end);

    List<AttendanceEntity> findByUserEntity_IdAndAttendanceTypeAndAttendanceTime_ValueBetween(
            Long userId, AttendanceType type, LocalDateTime start, LocalDateTime end);

    List<AttendanceEntity> findByAttendanceTime_ValueBetween(LocalDateTime start, LocalDateTime end);
}

