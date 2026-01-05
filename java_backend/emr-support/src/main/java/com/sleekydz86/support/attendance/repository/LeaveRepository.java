package com.sleekydz86.support.attendance.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.support.attendance.entity.LeaveEntity;
import com.sleekydz86.support.attendance.type.LeaveType;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRepository extends BaseRepository<LeaveEntity, Long> {

    Optional<LeaveEntity> findByLeaveId(Long leaveId);

    List<LeaveEntity> findByUserEntity_Id(Long userId);

    List<LeaveEntity> findByUserEntity_IdAndStartDateBetween(
            Long userId, LocalDate start, LocalDate end);

    List<LeaveEntity> findByLeaveType(LeaveType leaveType);

    List<LeaveEntity> findByStatus(String status);
}
