package com.sleekydz86.emrclinical.reservation.controller;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.emrclinical.reservation.dto.*;
import com.sleekydz86.emrclinical.reservation.entity.ReservationEntity;
import com.sleekydz86.emrclinical.reservation.service.ReservationService;
import com.sleekydz86.emrclinical.types.ReservationStatus;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ExcelExportService excelExportService;

    @PostMapping("/register")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.CREATE)
    public ResponseEntity<ReservationResponse> registerReservation(
            @Valid @RequestBody ReservationCreateRequest request,
            @AuthUser UserEntity user) {
        ReservationEntity reservation = reservationService.createReservation(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(reservation));
    }

    @GetMapping
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<Page<ReservationResponse>> getAllReservations(
            @PageableDefault(size = 20, sort = "reservationDate", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(required = false) Long patientNo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Page<ReservationEntity> reservations;

        if (patientNo != null) {
            reservations = reservationService.getReservationsByPatientNo(patientNo, pageable);
        } else if (status != null) {
            reservations = reservationService.getReservationsByStatus(status, pageable);
        } else {
            reservations = reservationService.getAllReservations(pageable);
        }

        Page<ReservationResponse> response = reservations.map(ReservationResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reservationId}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<ReservationDetailResponse> getReservationDetail(@PathVariable Long reservationId) {
        ReservationEntity reservation = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(ReservationDetailResponse.from(reservation));
    }

    @GetMapping("/patient/{patientNo}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<Page<ReservationResponse>> getReservationsByPatient(
            @PathVariable Long patientNo,
            @PageableDefault(size = 20, sort = "reservationDate", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ReservationEntity> reservations = reservationService.getReservationsByPatientNo(patientNo, pageable);
        Page<ReservationResponse> response = reservations.map(ReservationResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date/{date}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<List<ReservationResponse>> getReservationsByDate(@PathVariable LocalDate date) {
        List<ReservationEntity> reservations = reservationService.getReservationsByDate(date);
        List<ReservationResponse> response = reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<List<ReservationResponse>> getReservationsByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        List<ReservationEntity> reservations = reservationService.getReservationsByDateRange(start, end);
        List<ReservationResponse> response = reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<List<ReservationResponse>> getTodayReservations() {
        List<ReservationEntity> reservations = reservationService.getTodayReservations();
        List<ReservationResponse> response = reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<Page<ReservationResponse>> getReservationsByStatus(
            @PathVariable ReservationStatus status,
            @PageableDefault(size = 20, sort = "reservationDate", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ReservationEntity> reservations = reservationService.getReservationsByStatus(status, pageable);
        Page<ReservationResponse> response = reservations.map(ReservationResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorId}")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<List<ReservationResponse>> getReservationsByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(required = false) LocalDate date) {
        List<ReservationEntity> reservations = reservationService.getReservationsByDoctor(doctorId, date);
        List<ReservationResponse> response = reservations.stream()
                .map(ReservationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{reservationId}")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationUpdateRequest request) {
        ReservationEntity reservation = reservationService.updateReservation(reservationId, request);
        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    @PostMapping("/{reservationId}/cancel")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationCancelRequest request) {
        reservationService.cancelReservation(reservationId, request.getCancelReason());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reservationId}/confirm")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<Void> confirmReservation(@PathVariable Long reservationId) {
        reservationService.confirmReservation(reservationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{reservationId}/complete")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<Void> completeReservation(@PathVariable Long reservationId) {
        reservationService.completeReservation(reservationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public void exportReservationsToExcel(
            @RequestParam(required = false) Long patientNo,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        List<ReservationEntity> reservations;
        if (patientNo != null) {
            reservations = reservationService.getReservationsByPatientNo(patientNo, org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else if (startDate != null && endDate != null) {
            reservations = reservationService.getReservationsByDateRange(startDate, endDate);
        } else {
            reservations = reservationService.getAllReservations(org.springframework.data.domain.Pageable.unpaged()).getContent();
        }

        List<String> headers = List.of("예약 ID", "환자 번호", "환자 이름", "예약 일시", "예약 상태", "담당 직원");
        List<Map<String, Object>> data = reservations.stream().map(r -> Map.of(
                "예약 ID", r.getReservationId(),
                "환자 번호", r.getPatientEntity().getPatientNo(),
                "환자 이름", r.getPatientEntity().getPatientName(),
                "예약 일시", r.getReservationDate(),
                "예약 상태", r.getReservationStatus().name(),
                "담당 직원", r.getUserEntity() != null ? r.getUserEntity().getName() : ""
        )).collect(Collectors.toList());

        excelExportService.exportToExcel(headers, data, "reservations.xlsx", response);
    }
}

