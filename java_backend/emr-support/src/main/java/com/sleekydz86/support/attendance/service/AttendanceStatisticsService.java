package com.sleekydz86.support.attendance.service;

import com.sleekydz86.core.tenant.TenantContext;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.support.attendance.dto.AttendanceStatisticsResponse;
import com.sleekydz86.support.attendance.entity.AttendanceEntity;
import com.sleekydz86.support.attendance.entity.LeaveEntity;
import com.sleekydz86.support.attendance.repository.AttendanceRepository;
import com.sleekydz86.support.attendance.repository.LeaveRepository;
import com.sleekydz86.support.attendance.type.AttendanceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceStatisticsService {

    private final AttendanceRepository attendanceRepository;
    private final LeaveRepository leaveRepository;
    private final UserRepository userRepository;

    public AttendanceStatisticsResponse getDailyStatistics(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        List<AttendanceEntity> attendances = attendanceRepository
                .findByAttendanceTime_ValueBetween(start, end);

        return buildStatisticsResponse(attendances, date, null, null, null);
    }

    public AttendanceStatisticsResponse getWeeklyStatistics(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<AttendanceEntity> attendances = attendanceRepository
                .findByAttendanceTime_ValueBetween(start, end);

        return buildStatisticsResponse(attendances, null, startDate, endDate, null);
    }

    public AttendanceStatisticsResponse getMonthlyStatistics(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<AttendanceEntity> attendances = attendanceRepository
                .findByAttendanceTime_ValueBetween(start, end);

        return buildStatisticsResponse(attendances, null, startDate, endDate, null);
    }

    public AttendanceStatisticsResponse getUserStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<AttendanceEntity> attendances = attendanceRepository
                .findByUserEntity_IdAndAttendanceTime_ValueBetween(userId, start, end);

        return buildStatisticsResponse(attendances, null,
                startDate != null ? startDate : start.toLocalDate(),
                endDate != null ? endDate : end.toLocalDate(),
                userId);
    }

    public AttendanceStatisticsResponse getAdminStatistics(LocalDate startDate, LocalDate endDate) {
        if (!TenantContext.isAdmin()) {
            throw new IllegalArgumentException("관리자만 전체 통계를 조회할 수 있습니다.");
        }

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime end = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();

        List<AttendanceEntity> attendances = attendanceRepository
                .findByAttendanceTime_ValueBetween(start, end);

        return buildStatisticsResponse(attendances, null,
                startDate != null ? startDate : start.toLocalDate(),
                endDate != null ? endDate : end.toLocalDate(),
                null);
    }

    private AttendanceStatisticsResponse buildStatisticsResponse(
            List<AttendanceEntity> attendances, LocalDate date, 
            LocalDate startDate, LocalDate endDate, Long userId) {

        long totalAttendances = attendances.size();
        long totalCheckIns = attendances.stream()
                .filter(a -> a.getAttendanceType() == AttendanceType.CHECK_IN)
                .count();
        long totalCheckOuts = attendances.stream()
                .filter(a -> a.getAttendanceType() == AttendanceType.CHECK_OUT)
                .count();

        List<AttendanceEntity> checkIns = attendances.stream()
                .filter(a -> a.getAttendanceType() == AttendanceType.CHECK_IN)
                .collect(Collectors.toList());

        List<AttendanceEntity> checkOuts = attendances.stream()
                .filter(a -> a.getAttendanceType() == AttendanceType.CHECK_OUT)
                .collect(Collectors.toList());

        long totalWorkingMinutes = 0;
        for (int i = 0; i < Math.min(checkIns.size(), checkOuts.size()); i++) {
            AttendanceEntity checkIn = checkIns.get(i);
            AttendanceEntity checkOut = checkOuts.get(i);
            if (checkIn.getEndTime() != null) {
                totalWorkingMinutes += java.time.Duration.between(
                        checkIn.getAttendanceTime().getValue(),
                        checkIn.getEndTime()
                ).toMinutes();
            }
        }

        Map<AttendanceType, Long> typeStats = attendances.stream()
                .collect(Collectors.groupingBy(AttendanceEntity::getAttendanceType, Collectors.counting()));

        LocalTime avgCheckInTime = null;
        if (!checkIns.isEmpty()) {
            long totalSeconds = checkIns.stream()
                    .mapToLong(a -> a.getAttendanceTime().getValue().toLocalTime().toSecondOfDay())
                    .sum();
            avgCheckInTime = LocalTime.ofSecondOfDay(totalSeconds / checkIns.size());
        }

        LocalTime avgCheckOutTime = null;
        if (!checkOuts.isEmpty()) {
            long totalSeconds = checkOuts.stream()
                    .mapToLong(a -> a.getAttendanceTime().getValue().toLocalTime().toSecondOfDay())
                    .sum();
            avgCheckOutTime = LocalTime.ofSecondOfDay(totalSeconds / checkOuts.size());
        }

        long avgWorkingMinutes = totalCheckIns > 0 ? totalWorkingMinutes / totalCheckIns : 0;

        LocalDate leaveStartDate = startDate != null ? startDate : (date != null ? date : LocalDate.now().minusMonths(1));
        LocalDate leaveEndDate = endDate != null ? endDate : (date != null ? date : LocalDate.now());

        List<LeaveEntity> leaves;
        if (userId != null) {
            leaves = leaveRepository.findByUserEntity_IdAndStartDateBetween(userId, leaveStartDate, leaveEndDate);
        } else {
            leaves = leaveRepository.findAll().stream()
                    .filter(leave -> !leave.getStartDate().isBefore(leaveStartDate) && 
                            !leave.getStartDate().isAfter(leaveEndDate))
                    .collect(Collectors.toList());
        }

        long totalLeaves = leaves.size();
        long approvedLeaves = leaves.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
        long pendingLeaves = leaves.stream().filter(l -> "PENDING".equals(l.getStatus())).count();
        long rejectedLeaves = leaves.stream().filter(l -> "REJECTED".equals(l.getStatus())).count();

        String userName = null;
        if (userId != null) {
            UserEntity user = userRepository.findById(userId).orElse(null);
            userName = user != null ? user.getName() : null;
        }

        return AttendanceStatisticsResponse.builder()
                .date(date)
                .startDate(startDate)
                .endDate(endDate)
                .userId(userId)
                .userName(userName)
                .totalAttendances(totalAttendances)
                .totalCheckIns(totalCheckIns)
                .totalCheckOuts(totalCheckOuts)
                .totalWorkingMinutes(totalWorkingMinutes)
                .attendanceTypeStatistics(typeStats)
                .averageCheckInTime(avgCheckInTime)
                .averageCheckOutTime(avgCheckOutTime)
                .averageWorkingMinutes(avgWorkingMinutes)
                .totalLeaves(totalLeaves)
                .approvedLeaves(approvedLeaves)
                .pendingLeaves(pendingLeaves)
                .rejectedLeaves(rejectedLeaves)
                .build();
    }
}

