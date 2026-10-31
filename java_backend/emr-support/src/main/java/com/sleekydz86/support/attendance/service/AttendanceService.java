package com.sleekydz86.support.attendance.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.core.tenant.TenantContext;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.support.attendance.domain.service.AttendanceDomainService;
import com.sleekydz86.support.attendance.dto.AttendanceRegisterRequest;
import com.sleekydz86.support.attendance.dto.AttendanceResponse;
import com.sleekydz86.support.attendance.dto.LeaveRegisterRequest;
import com.sleekydz86.support.attendance.dto.LeaveResponse;
import com.sleekydz86.support.attendance.entity.AttendanceEntity;
import com.sleekydz86.support.attendance.entity.LeaveEntity;
import com.sleekydz86.support.attendance.factory.AttendanceFactory;
import com.sleekydz86.support.attendance.repository.AttendanceRepository;
import com.sleekydz86.support.attendance.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService implements BaseService<AttendanceEntity, Long> {

    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;
    private final AttendanceFactory attendanceFactory;
    private final AttendanceDomainService attendanceDomainService;
    private final EventPublisher eventPublisher;

    @Transactional
    public AttendanceResponse registerAttendance(Long userId, AttendanceRegisterRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        attendanceDomainService.validateAttendanceTime(
                request.getAttendanceType(),
                request.getAttendanceTime(),
                request.getEndTime()
        );

        List<AttendanceEntity> recentAttendances = attendanceRepository
                .findByUserEntity_IdAndAttendanceTime_ValueBetween(
                        userId,
                        request.getAttendanceTime().minusDays(1),
                        request.getAttendanceTime().plusDays(1)
                );

        if (!recentAttendances.isEmpty()) {
            AttendanceEntity lastAttendance = recentAttendances.get(recentAttendances.size() - 1);
            if (!attendanceDomainService.canRegisterAttendance(lastAttendance, request.getAttendanceType())) {
                throw new IllegalArgumentException("근태 등록이 불가능한 상태입니다.");
            }
        }

        AttendanceEntity attendance = attendanceFactory.create(
                user,
                request.getAttendanceType(),
                request.getAttendanceTime(),
                request.getEndTime(),
                request.getLocation(),
                request.getRemarks()
        );

        AttendanceEntity saved = attendanceRepository.save(attendance);
        
        eventPublisher.publish(new com.sleekydz86.core.event.domain.AttendanceRegisteredEvent(
                saved.getAttendanceId(),
                saved.getUserEntity().getId(),
                saved.getUserEntity().getName(),
                saved.getAttendanceType().name(),
                saved.getAttendanceTime().getValue()
        ));

        return AttendanceResponse.from(saved);
    }

    public AttendanceResponse getAttendance(Long attendanceId, Long userId) {
        AttendanceEntity attendance = attendanceRepository.findByAttendanceId(attendanceId)
                .orElseThrow(() -> new NotFoundException("근태 기록을 찾을 수 없습니다."));

        if (!TenantContext.isAdmin() && !attendance.getUserEntity().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 근태 기록만 조회할 수 있습니다.");
        }

        return AttendanceResponse.from(attendance);
    }

    public List<AttendanceResponse> getMyAttendances(Long userId) {
        List<AttendanceEntity> attendances = attendanceRepository.findByUserEntity_Id(userId);
        return attendances.stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAllAttendances(Long userId) {
        if (!TenantContext.isAdmin()) {
            return getMyAttendances(userId);
        }

        List<AttendanceEntity> attendances = attendanceRepository.findAll();
        return attendances.stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getAttendancesByDate(Long userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        if (!TenantContext.isAdmin()) {
            List<AttendanceEntity> attendances = attendanceRepository
                    .findByUserEntity_IdAndAttendanceTime_ValueBetween(userId, start, end);
            return attendances.stream()
                    .map(AttendanceResponse::from)
                    .collect(Collectors.toList());
        }

        List<AttendanceEntity> attendances = attendanceRepository
                .findByAttendanceTime_ValueBetween(start, end);
        return attendances.stream()
                .map(AttendanceResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttendanceResponse updateAttendance(Long attendanceId, Long userId, AttendanceRegisterRequest request) {
        AttendanceEntity attendance = attendanceRepository.findByAttendanceId(attendanceId)
                .orElseThrow(() -> new NotFoundException("근태 기록을 찾을 수 없습니다."));

        if (!TenantContext.isAdmin() && !attendance.getUserEntity().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 근태 기록만 수정할 수 있습니다.");
        }

        attendanceDomainService.validateAttendanceTime(
                request.getAttendanceType(),
                request.getAttendanceTime(),
                request.getEndTime()
        );

        attendance.updateEndTime(request.getEndTime());
        attendance.updateLocation(request.getLocation());
        attendance.updateRemarks(request.getRemarks());

        return AttendanceResponse.from(attendance);
    }

    @Transactional
    public LeaveResponse registerLeave(Long userId, LeaveRegisterRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("종료 날짜는 시작 날짜 이후여야 합니다.");
        }

        LeaveEntity leave = LeaveEntity.builder()
                .userEntity(user)
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status("PENDING")
                .build();

        LeaveEntity saved = leaveRepository.save(leave);
        return LeaveResponse.from(saved);
    }

    public List<LeaveResponse> getMyLeaves(Long userId) {
        List<LeaveEntity> leaves = leaveRepository.findByUserEntity_Id(userId);
        return leaves.stream()
                .map(LeaveResponse::from)
                .collect(Collectors.toList());
    }

    public List<LeaveResponse> getAllLeaves(Long userId) {
        if (!TenantContext.isAdmin()) {
            return getMyLeaves(userId);
        }

        List<LeaveEntity> leaves = leaveRepository.findAll();
        return leaves.stream()
                .map(LeaveResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveResponse approveLeave(Long leaveId) {
        if (!TenantContext.isAdmin()) {
            throw new IllegalArgumentException("관리자만 휴가를 승인할 수 있습니다.");
        }

        LeaveEntity leave = leaveRepository.findByLeaveId(leaveId)
                .orElseThrow(() -> new NotFoundException("휴가 신청을 찾을 수 없습니다."));

        leave.approve();
        return LeaveResponse.from(leave);
    }

    @Transactional
    public LeaveResponse rejectLeave(Long leaveId) {
        if (!TenantContext.isAdmin()) {
            throw new IllegalArgumentException("관리자만 휴가를 거절할 수 있습니다.");
        }

        LeaveEntity leave = leaveRepository.findByLeaveId(leaveId)
                .orElseThrow(() -> new NotFoundException("휴가 신청을 찾을 수 없습니다."));

        leave.reject();
        return LeaveResponse.from(leave);
    }
}

