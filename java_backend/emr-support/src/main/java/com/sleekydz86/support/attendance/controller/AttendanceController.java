package com.sleekydz86.support.attendance.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.support.attendance.dto.AttendanceRegisterRequest;
import com.sleekydz86.support.attendance.dto.AttendanceResponse;
import com.sleekydz86.support.attendance.dto.LeaveRegisterRequest;
import com.sleekydz86.support.attendance.dto.LeaveResponse;
import com.sleekydz86.support.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    @AuthRole
    public ResponseEntity<Map<String, Object>> registerAttendance(
            @AuthUser Long userId,
            @Valid @RequestBody AttendanceRegisterRequest request) {
        try {
            AttendanceResponse response = attendanceService.registerAttendance(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "근태 등록 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "근태 등록 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{attendanceId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getAttendance(
            @AuthUser Long userId,
            @PathVariable Long attendanceId) {
        try {
            AttendanceResponse response = attendanceService.getAttendance(attendanceId, userId);
            return ResponseEntity.ok(Map.of(
                    "message", "근태 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "근태 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/my")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getMyAttendances(@AuthUser Long userId) {
        try {
            List<AttendanceResponse> responses = attendanceService.getMyAttendances(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "내 근태 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "근태 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/all")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> getAllAttendances(@AuthUser Long userId) {
        try {
            List<AttendanceResponse> responses = attendanceService.getAllAttendances(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "전체 근태 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "근태 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/date")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getAttendancesByDate(
            @AuthUser Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            List<AttendanceResponse> responses = attendanceService.getAttendancesByDate(userId, date);
            return ResponseEntity.ok(Map.of(
                    "message", "날짜별 근태 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "근태 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{attendanceId}")
    @AuthRole
    public ResponseEntity<Map<String, Object>> updateAttendance(
            @AuthUser Long userId,
            @PathVariable Long attendanceId,
            @Valid @RequestBody AttendanceRegisterRequest request) {
        try {
            AttendanceResponse response = attendanceService.updateAttendance(attendanceId, userId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "근태 수정 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "근태 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/leave")
    @AuthRole
    public ResponseEntity<Map<String, Object>> registerLeave(
            @AuthUser Long userId,
            @Valid @RequestBody LeaveRegisterRequest request) {
        try {
            LeaveResponse response = attendanceService.registerLeave(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "휴가 신청 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴가 신청 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/leave/my")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getMyLeaves(@AuthUser Long userId) {
        try {
            List<LeaveResponse> responses = attendanceService.getMyLeaves(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "내 휴가 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴가 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/leave/all")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> getAllLeaves(@AuthUser Long userId) {
        try {
            List<LeaveResponse> responses = attendanceService.getAllLeaves(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "전체 휴가 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴가 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/leave/{leaveId}/approve")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> approveLeave(@PathVariable Long leaveId) {
        try {
            LeaveResponse response = attendanceService.approveLeave(leaveId);
            return ResponseEntity.ok(Map.of(
                    "message", "휴가 승인 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴가 승인 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/leave/{leaveId}/reject")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> rejectLeave(@PathVariable Long leaveId) {
        try {
            LeaveResponse response = attendanceService.rejectLeave(leaveId);
            return ResponseEntity.ok(Map.of(
                    "message", "휴가 거절 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴가 거절 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

