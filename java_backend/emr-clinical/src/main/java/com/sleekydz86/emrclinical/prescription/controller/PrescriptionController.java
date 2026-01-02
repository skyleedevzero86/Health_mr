package com.sleekydz86.emrclinical.prescription.controller;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.emrclinical.prescription.dto.*;
import com.sleekydz86.emrclinical.prescription.entity.PrescriptionEntity;
import com.sleekydz86.emrclinical.prescription.service.PrescriptionService;
import com.sleekydz86.emrclinical.prescription.statistics.PrescriptionStatisticsResponse;
import com.sleekydz86.emrclinical.prescription.statistics.PrescriptionStatisticsService;
import com.sleekydz86.emrclinical.types.PrescriptionStatus;
import com.sleekydz86.emrclinical.types.PrescriptionType;
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
@RequestMapping("/api/prescription")
@RequiredArgsConstructor
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionStatisticsService prescriptionStatisticsService;
    private final ExcelExportService excelExportService;

    @PostMapping("/register")
    @AuthRole({"DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.CREATE)
    public ResponseEntity<PrescriptionResponse> registerPrescription(
            @Valid @RequestBody PrescriptionCreateRequest request) {
        PrescriptionEntity prescription = prescriptionService.registerPrescription(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PrescriptionResponse.from(prescription));
    }

    @GetMapping("/{prescriptionId}")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<PrescriptionDetailResponse> getPrescriptionDetail(@PathVariable Long prescriptionId) {
        PrescriptionEntity prescription = prescriptionService.getPrescriptionById(prescriptionId);
        return ResponseEntity.ok(PrescriptionDetailResponse.from(prescription));
    }

    @GetMapping
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<Page<PrescriptionResponse>> getAllPrescriptions(
            @PageableDefault(size = 20, sort = "prescriptionDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long patientNo,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) PrescriptionStatus status,
            @RequestParam(required = false) PrescriptionType type,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Page<PrescriptionEntity> prescriptions;

        if (patientNo != null) {
            prescriptions = prescriptionService.getPrescriptionsByPatientNo(patientNo, pageable);
        } else if (doctorId != null) {
            prescriptions = prescriptionService.getPrescriptionsByDoctor(doctorId, pageable);
        } else if (status != null) {
            prescriptions = prescriptionService.getPrescriptionsByStatus(status, pageable);
        } else if (type != null) {
            prescriptions = prescriptionService.getPrescriptionsByType(type, pageable);
        } else {
            prescriptions = prescriptionService.getAllPrescriptions(pageable);
        }

        Page<PrescriptionResponse> response = prescriptions.map(PrescriptionResponse::from);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/treatment/{treatmentId}")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<PrescriptionResponse> getPrescriptionByTreatment(@PathVariable Long treatmentId) {
        PrescriptionEntity prescription = prescriptionService.getPrescriptionByTreatmentId(treatmentId);
        return ResponseEntity.ok(PrescriptionResponse.from(prescription));
    }

    @GetMapping("/patient/{patientNo}")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<Page<PrescriptionResponse>> getPrescriptionsByPatient(
            @PathVariable Long patientNo,
            @PageableDefault(size = 20, sort = "prescriptionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PrescriptionEntity> prescriptions = prescriptionService.getPrescriptionsByPatientNo(patientNo, pageable);
        Page<PrescriptionResponse> response = prescriptions.map(PrescriptionResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctor/{doctorId}")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<Page<PrescriptionResponse>> getPrescriptionsByDoctor(
            @PathVariable Long doctorId,
            @PageableDefault(size = 20, sort = "prescriptionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PrescriptionEntity> prescriptions = prescriptionService.getPrescriptionsByDoctor(doctorId, pageable);
        Page<PrescriptionResponse> response = prescriptions.map(PrescriptionResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<Page<PrescriptionResponse>> getPrescriptionsByStatus(
            @PathVariable PrescriptionStatus status,
            @PageableDefault(size = 20, sort = "prescriptionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PrescriptionEntity> prescriptions = prescriptionService.getPrescriptionsByStatus(status, pageable);
        Page<PrescriptionResponse> response = prescriptions.map(PrescriptionResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<Page<PrescriptionResponse>> getPrescriptionsByType(
            @PathVariable PrescriptionType type,
            @PageableDefault(size = 20, sort = "prescriptionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PrescriptionEntity> prescriptions = prescriptionService.getPrescriptionsByType(type, pageable);
        Page<PrescriptionResponse> response = prescriptions.map(PrescriptionResponse::from);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/date-range")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<List<PrescriptionResponse>> getPrescriptionsByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        List<PrescriptionEntity> prescriptions = prescriptionService.getPrescriptionsByDateRange(start, end);
        List<PrescriptionResponse> response = prescriptions.stream()
                .map(PrescriptionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<List<PrescriptionResponse>> getTodayPrescriptions() {
        List<PrescriptionEntity> prescriptions = prescriptionService.getTodayPrescriptions();
        List<PrescriptionResponse> response = prescriptions.stream()
                .map(PrescriptionResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{prescriptionId}")
    @AuthRole({"DOCTOR", "ADMIN"})
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public ResponseEntity<PrescriptionResponse> updatePrescription(
            @PathVariable Long prescriptionId,
            @Valid @RequestBody PrescriptionUpdateRequest request) {
        PrescriptionEntity prescription = prescriptionService.updatePrescription(prescriptionId, request);
        return ResponseEntity.ok(PrescriptionResponse.from(prescription));
    }

    @PostMapping("/{prescriptionId}/dispense")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<Void> dispensePrescription(@PathVariable Long prescriptionId) {
        prescriptionService.dispensePrescription(prescriptionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{prescriptionId}/cancel")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<Void> cancelPrescription(
            @PathVariable Long prescriptionId,
            @Valid @RequestBody PrescriptionCancelRequest request) {
        prescriptionService.cancelPrescription(prescriptionId, request.getCancelReason());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics/daily")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<PrescriptionStatisticsResponse> getDailyStatistics(
            @RequestParam LocalDate date) {
        PrescriptionStatisticsResponse statistics = prescriptionStatisticsService.getDailyStatistics(date);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/period")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public ResponseEntity<PrescriptionStatisticsResponse> getPeriodStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        PrescriptionStatisticsResponse statistics = prescriptionStatisticsService.getPeriodStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/statistics/doctor/{doctorId}")
    @AuthRole({"DOCTOR", "ADMIN"})
    public ResponseEntity<PrescriptionStatisticsResponse> getDoctorStatistics(
            @PathVariable Long doctorId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        PrescriptionStatisticsResponse statistics = prescriptionStatisticsService.getDoctorStatistics(doctorId, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/export")
    @AuthRole({"STAFF", "DOCTOR", "ADMIN"})
    public void exportPrescriptionsToExcel(
            @RequestParam(required = false) Long patientNo,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            HttpServletResponse response) throws IOException {

        List<PrescriptionEntity> prescriptions;
        if (patientNo != null) {
            prescriptions = prescriptionService.getPrescriptionsByPatientNo(patientNo, org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else if (doctorId != null) {
            prescriptions = prescriptionService.getPrescriptionsByDoctor(doctorId, org.springframework.data.domain.Pageable.unpaged()).getContent();
        } else if (startDate != null && endDate != null) {
            prescriptions = prescriptionService.getPrescriptionsByDateRange(startDate, endDate);
        } else {
            prescriptions = prescriptionService.getAllPrescriptions(org.springframework.data.domain.Pageable.unpaged()).getContent();
        }

        List<String> headers = List.of("처방 ID", "환자 번호", "환자 이름", "처방 의사", "처방 일시", "처방 상태", "처방 유형", "메모");
        List<Map<String, Object>> data = prescriptions.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("처방 ID", p.getPrescriptionId());
            map.put("환자 번호", p.getPatientEntity().getPatientNoValue());
            map.put("환자 이름", p.getPatientEntity().getPatientName());
            map.put("처방 의사", p.getPrescriptionDoc().getName());
            map.put("처방 일시", p.getPrescriptionDate());
            map.put("처방 상태", p.getPrescriptionStatus().name());
            map.put("처방 유형", p.getPrescriptionType().name());
            map.put("메모", p.getPrescriptionMemo() != null ? p.getPrescriptionMemo() : "");
            return map;
        }).collect(Collectors.toList());

        excelExportService.exportToExcel(headers, data, "prescriptions.xlsx", response);
    }
}


