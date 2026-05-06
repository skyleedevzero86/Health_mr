package com.sleekydz86.emrclinical.treatment.controller;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentCompleteRequest;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentCreateRequest;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentDetailResponse;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentResponse;
import com.sleekydz86.emrclinical.treatment.dto.TreatmentUpdateRequest;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        return ResponseEntity.status(HttpStatus.CREATED).body(TreatmentResponse.from(treatment));
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

        return ResponseEntity.ok(treatments.map(TreatmentResponse::from));
    }

    @GetMapping("/patient/{patientNo}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Page<TreatmentResponse>> getTreatmentsByPatient(
            @PathVariable Long patientNo,
            @PageableDefault(size = 20, sort = "treatmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TreatmentEntity> treatments = treatmentService.getTreatmentsByPatientNo(patientNo, pageable);
        return ResponseEntity.ok(treatments.map(TreatmentResponse::from));
    }

    @GetMapping("/doctor/{doctorId}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Page<TreatmentResponse>> getTreatmentsByDoctor(
            @PathVariable Long doctorId,
            @PageableDefault(size = 20, sort = "treatmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TreatmentEntity> treatments = treatmentService.getTreatmentsByDoctor(doctorId, pageable);
        return ResponseEntity.ok(treatments.map(TreatmentResponse::from));
    }

    @GetMapping("/type/{type}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Page<TreatmentResponse>> getTreatmentsByType(
            @PathVariable TreatmentType type,
            @PageableDefault(size = 20, sort = "treatmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TreatmentEntity> treatments = treatmentService.getTreatmentsByType(type, pageable);
        return ResponseEntity.ok(treatments.map(TreatmentResponse::from));
    }

    @GetMapping("/status/{status}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<Page<TreatmentResponse>> getTreatmentsByStatus(
            @PathVariable TreatmentStatus status,
            @PageableDefault(size = 20, sort = "treatmentDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TreatmentEntity> treatments = treatmentService.getTreatmentsByStatus(status, pageable);
        return ResponseEntity.ok(treatments.map(TreatmentResponse::from));
    }

    @GetMapping("/date-range")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<List<TreatmentResponse>> getTreatmentsByDateRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        List<TreatmentEntity> treatments = treatmentService.getTreatmentsByDateRange(start, end);
        return ResponseEntity.ok(treatments.stream().map(TreatmentResponse::from).toList());
    }

    @GetMapping("/today")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<List<TreatmentResponse>> getTodayTreatments() {
        List<TreatmentEntity> treatments = treatmentService.getTodayTreatments();
        return ResponseEntity.ok(treatments.stream().map(TreatmentResponse::from).toList());
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
    public ResponseEntity<TreatmentStatisticsResponse> getDailyStatistics(@RequestParam LocalDate date) {
        return ResponseEntity.ok(treatmentStatisticsService.getDailyStatistics(date));
    }

    @GetMapping("/statistics/period")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<TreatmentStatisticsResponse> getPeriodStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(treatmentStatisticsService.getPeriodStatistics(startDate, endDate));
    }

    @GetMapping("/statistics/doctor/{doctorId}")
    @AuthRole({ "DOCTOR", "ADMIN" })
    public ResponseEntity<TreatmentStatisticsResponse> getDoctorStatistics(
            @PathVariable Long doctorId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(treatmentStatisticsService.getDoctorStatistics(doctorId, startDate, endDate));
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
                    .getTreatmentsByPatientNo(patientNo, Pageable.unpaged())
                    .getContent();
        } else if (doctorId != null) {
            treatments = treatmentService
                    .getTreatmentsByDoctor(doctorId, Pageable.unpaged())
                    .getContent();
        } else if (startDate != null && endDate != null) {
            treatments = treatmentService.getTreatmentsByDateRange(startDate, endDate);
        } else {
            treatments = treatmentService.getAllTreatments(Pageable.unpaged()).getContent();
        }

        List<String> headers = List.of(
                "진료ID",
                "환자번호",
                "환자명",
                "진료의사",
                "진료일시",
                "진료상태",
                "진료유형",
                "진료과"
        );

        List<Map<String, Object>> data = treatments.stream()
                .map(treatment -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("진료ID", treatment.getTreatmentId());
                    row.put("환자번호", treatment.getCheckInEntity() != null
                            ? treatment.getCheckInEntity().getPatientEntity().getPatientNoValue()
                            : "");
                    row.put("환자명", treatment.getCheckInEntity() != null
                            ? treatment.getCheckInEntity().getPatientEntity().getPatientName()
                            : "");
                    row.put("진료의사", treatment.getTreatmentDoc() != null ? treatment.getTreatmentDoc().getName() : "");
                    row.put("진료일시", treatment.getTreatmentDate());
                    row.put("진료상태", treatment.getTreatmentStatus() != null ? treatment.getTreatmentStatus().name() : "");
                    row.put("진료유형", treatment.getTreatmentType() != null ? treatment.getTreatmentType().name() : "");
                    row.put("진료과", treatment.getDepartmentEntity() != null ? treatment.getDepartmentEntity().getName() : "");
                    return row;
                })
                .toList();

        excelExportService.exportToExcel(headers, data, "treatments.xlsx", response);
    }
}
