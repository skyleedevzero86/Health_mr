package com.sleekydz86.emrclinical.treatment.controller;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.emrclinical.treatment.dto.*;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.service.TreatmentService;
import com.sleekydz86.emrclinical.treatment.statistics.TreatmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.TreatmentStatisticsService;
import com.sleekydz86.emrclinical.types.TreatmentStatus;
import com.sleekydz86.emrclinical.types.TreatmentType;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/treatment")
@RequiredArgsConstructor
public class TreatmentController {

    private final TreatmentService treatmentService;
    private final TreatmentStatisticsService treatmentStatisticsService;
    private final ExcelExportService excelExportService;

    @PostMapping
    @AuthRole({ "DOCTOR", "ADMIN" })
    @AuditLog(action = AuditLog.ActionType.CREATE)
    public ResponseEntity<TreatmentResponse> createTreatment(
            @Valid @RequestBody TreatmentCreateRequest request) {
        TreatmentEntity treatment = treatmentService.createTreatment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TreatmentResponse.from(treatment));
    }

    @GetMapping("/{treatmentId}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<TreatmentDetailResponse> getTreatmentDetail(@PathVariable Long treatmentId) {
        TreatmentEntity treatment = treatmentService.getTreatmentById(treatmentId);
        return ResponseEntity.ok(TreatmentDetailResponse.from(treatment));
    }

    @GetMapping
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Page<TreatmentResponse>> getAllTreatments(
            @PageableDefault(size = 20, sort = "treatmentDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long patientNo,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) TreatmentType treatmentType,
            @RequestParam(required = false) TreatmentStatus treatmentStatus,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Page<TreatmentEntity> treatments;

        if (patientNo != null) {
            treatments = treatmentService.getTreatmentsByPatientNo(patientNo, pageable);
        } else if (doctorId != null) {
            treatments = treatmentService.getTreatmentsByDoctor(doctorId, pageable);
        } else if (treatmentType != null) {
            treatments = treatmentService.getTreatmentsByType(treatmentType, pageable);
        } else if (treatmentStatus != null) {
            treatments = treatmentService.getTreatmentsByStatus(treatmentStatus, pageable);
        } else {
            treatments = treatmentService.getAllTreatments(pageable);
        }

        Page<TreatmentResponse> response = treatments.map(TreatmentResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patient/{patientNo}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Page<TreatmentResponse>> getTreatmentsByPatient(
            @PathVariable Long patientNo,
            @PageableDefault(size = 20, sort = "treatmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TreatmentEntity> treatments = treatmentService.getTreatmentsByPatientNo(patientNo, pageable);
        Page<TreatmentResponse> response = treatments.map(TreatmentResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorId}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Page<TreatmentResponse>> getTreatmentsByDoctor(
            @PathVariable Long doctorId,
            @PageableDefault(size = 20, sort = "treatmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TreatmentEntity> treatments = treatmentService.getTreatmentsByDoctor(doctorId, pageable);
        Page<TreatmentResponse> response = treatments.map(TreatmentResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Page<TreatmentResponse>> getTreatmentsByType(
            @PathVariable TreatmentType type,
            @PageableDefault(size = 20, sort = "treatmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TreatmentEntity> treatments = treatmentService.getTreatmentsByType(type, pageable);
        Page<TreatmentResponse> response = treatments.map(TreatmentResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Page<TreatmentResponse>> getTreatmentsByStatus(
            @PathVariable TreatmentStatus status,
            @PageableDefault(size = 20, sort = "treatmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TreatmentEntity> treatments = treatmentService.getTreatmentsByStatus(status, pageable);
        Page<TreatmentResponse> response = treatments.map(TreatmentResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<List<TreatmentResponse>> getTreatmentsByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        List<TreatmentEntity> treatments = treatmentService.getTreatmentsByDateRange(start, end);
        List<TreatmentResponse> response = treatments.stream()
                .map(TreatmentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<List<TreatmentResponse>> getTodayTreatments() {
        List<TreatmentEntity> treatments = treatmentService.getTodayTreatments();
        List<TreatmentResponse> response = treatments.stream()
                .map(TreatmentResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkin/{checkInId}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<TreatmentResponse> getTreatmentByCheckIn(@PathVariable Long checkInId) {
        TreatmentEntity treatment = treatmentService.getTreatmentByCheckInId(checkInId);
        return ResponseEntity.ok(TreatmentResponse.from(treatment));
    }

    @PutMapping("/{treatmentId}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public ResponseEntity<TreatmentResponse> updateTreatment(
            @PathVariable Long treatmentId,
            @Valid @RequestBody TreatmentUpdateRequest request) {
        TreatmentEntity treatment = treatmentService.updateTreatment(treatmentId, request);
        return ResponseEntity.ok(TreatmentResponse.from(treatment));
    }

    @PostMapping("/{treatmentId}/start")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Void> startTreatment(@PathVariable Long treatmentId) {
        treatmentService.startTreatment(treatmentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{treatmentId}/complete")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Void> completeTreatment(
            @PathVariable Long treatmentId,
            @Valid @RequestBody TreatmentCompleteRequest request) {
        treatmentService.completeTreatment(treatmentId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{treatmentId}/cancel")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Void> cancelTreatment(
            @PathVariable Long treatmentId,
            @RequestParam(required = false) String cancelReason) {
        treatmentService.cancelTreatment(treatmentId, cancelReason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics/daily")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<TreatmentStatisticsResponse> getDailyStatistics(
            @RequestParam LocalDate date) {
        TreatmentStatisticsResponse statistics = treatmentStatisticsService.getDailyStatistics(date);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/period")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<TreatmentStatisticsResponse> getPeriodStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        TreatmentStatisticsResponse statistics = treatmentStatisticsService.getPeriodStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/doctor/{doctorId}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<TreatmentStatisticsResponse> getDoctorStatistics(
            @PathVariable Long doctorId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        TreatmentStatisticsResponse statistics = treatmentStatisticsService.getDoctorStatistics(doctorId, startDate,
                endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/export")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public void exportTreatmentsToExcel(
            @RequestParam(required = false) Long patientNo,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        List<TreatmentEntity> treatments;
        if (patientNo != null) {
            treatments = treatmentService
                    .getTreatmentsByPatientNo(patientNo, org.springframework.data.domain.Pageable.unpaged())
                    .getContent();
        } else if (doctorId != null) {
            treatments = treatmentService
                    .getTreatmentsByDoctor(doctorId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else if (startDate != null && endDate != null) {
            treatments = treatmentService.getTreatmentsByDateRange(startDate, endDate);
        } else {
            treatments = treatmentService.getAllTreatments(org.springframework.data.domain.Pageable.unpaged())
                    .getContent();
        }

        List<String> headers = List.of("진료 ID", "환자 번호", "환자 이름", "진료 의사", "진료 일시", "진료 상태", "진료 유형", "진료과");
        List<Map<String, Object>> data = treatments.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("진료 ID", t.getTreatmentId());
            map.put("환자 번호", t.getCheckInEntity() != null ? t.getCheckInEntity().getPatientEntity().getPatientNoValue() : "");
            map.put("환자 이름", t.getCheckInEntity() != null ? t.getCheckInEntity().getPatientEntity().getPatientName() : "");
            map.put("진료 의사", t.getTreatmentDoc().getName());
            map.put("진료 일시", t.getTreatmentDate());
            map.put("진료 상태", t.getTreatmentStatus().name());
            map.put("진료 유형", t.getTreatmentType().name());
            map.put("진료과", t.getDepartmentEntity() != null ? t.getDepartmentEntity().getName() : "");
            return map;
        }).collect(Collectors.toList());

        excelExportService.exportToExcel(headers, data, "treatments.xlsx", response);
    }
}