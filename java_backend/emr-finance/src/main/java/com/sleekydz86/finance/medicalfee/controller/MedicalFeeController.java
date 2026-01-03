package com.sleekydz86.finance.medicalfee.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.finance.medicalfee.dto.DailyMedicalFeeStatistics;
import com.sleekydz86.finance.medicalfee.dto.MedicalFeeDetailResponse;
import com.sleekydz86.finance.medicalfee.dto.MedicalFeeRequest;
import com.sleekydz86.finance.medicalfee.dto.MedicalFeeResponse;
import com.sleekydz86.finance.medicalfee.dto.MedicalFeeUpdateRequest;
import com.sleekydz86.finance.medicalfee.dto.PeriodMedicalFeeStatistics;
import com.sleekydz86.finance.medicalfee.entity.MedicalFeeEntity;
import com.sleekydz86.finance.medicalfee.repository.MedicalFeeRepository;
import com.sleekydz86.finance.medicalfee.service.MedicalFeeService;
import com.sleekydz86.finance.medicalfee.service.MedicalFeeStatisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medicalfee")
@RequiredArgsConstructor
public class MedicalFeeController {

    private final MedicalFeeService medicalFeeService;
    private final MedicalFeeStatisticsService medicalFeeStatisticsService;
    private final ExcelExportService excelExportService;
    private final MedicalFeeRepository medicalFeeRepository;

    @PostMapping("/register")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> registerMedicalFee(
            @Valid @RequestBody MedicalFeeRequest request,
            @RequestParam Long treatmentId) {
        MedicalFeeResponse response = medicalFeeService.createMedicalFee(request, treatmentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "진료비 등록 성공",
                "data", response));
    }

    @GetMapping("/{medicalFeeId}")
    @AuthRole({ "STAFF", "ADMIN", "DOCTOR" })
    public ResponseEntity<Map<String, Object>> getMedicalFee(@PathVariable Long medicalFeeId) {
        MedicalFeeDetailResponse response = medicalFeeService.getMedicalFeeDetailById(medicalFeeId);
        return ResponseEntity.ok(Map.of(
                "message", "진료비 조회 성공",
                "data", response));
    }

    @GetMapping
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Page<MedicalFeeResponse>> getAllMedicalFees(Pageable pageable) {
        Page<MedicalFeeResponse> medicalFees = medicalFeeService.getAllMedicalFees(pageable);
        return ResponseEntity.ok(medicalFees);
    }

    @GetMapping("/treatment/{treatmentId}")
    @AuthRole({ "STAFF", "ADMIN", "DOCTOR" })
    public ResponseEntity<Map<String, Object>> getMedicalFeesByTreatmentId(@PathVariable Long treatmentId) {
        List<MedicalFeeResponse> medicalFees = medicalFeeService.getMedicalFeesByTreatmentId(treatmentId);
        return ResponseEntity.ok(Map.of(
                "message", "진료비 조회 성공",
                "data", medicalFees));
    }

    @GetMapping("/treatment/{treatmentId}/total")
    @AuthRole({ "STAFF", "ADMIN", "DOCTOR" })
    public ResponseEntity<Map<String, Object>> getTotalMedicalFeeByTreatmentId(@PathVariable Long treatmentId) {
        Long totalFee = medicalFeeService.getTotalMedicalFeeByTreatmentId(treatmentId);
        return ResponseEntity.ok(Map.of(
                "message", "총 진료비 조회 성공",
                "totalFee", totalFee));
    }

    @PutMapping("/{medicalFeeId}")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> updateMedicalFee(
            @PathVariable Long medicalFeeId,
            @Valid @RequestBody MedicalFeeUpdateRequest request) {
        MedicalFeeResponse response = medicalFeeService.updateMedicalFee(medicalFeeId, request);
        return ResponseEntity.ok(Map.of(
                "message", "진료비 수정 성공",
                "data", response));
    }

    @DeleteMapping("/{medicalFeeId}")
    @AuthRole({ "ADMIN" })
    public ResponseEntity<Map<String, Object>> deleteMedicalFee(@PathVariable Long medicalFeeId) {
        medicalFeeService.deleteMedicalFee(medicalFeeId);
        return ResponseEntity.ok(Map.of(
                "message", "진료비 삭제 성공"));
    }

    @GetMapping("/statistics/daily")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        DailyMedicalFeeStatistics statistics = medicalFeeStatisticsService
                .getDailyStatistics(date);
        return ResponseEntity.ok(Map.of(
                "message", "일일 진료비 통계 조회 성공",
                "data", statistics));
    }

    @GetMapping("/statistics/period")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getPeriodStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        PeriodMedicalFeeStatistics statistics = medicalFeeStatisticsService
                .getPeriodStatistics(startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "message", "기간별 진료비 통계 조회 성공",
                "data", statistics));
    }

    @GetMapping("/export")
    @AuthRole({ "STAFF", "ADMIN" })
    public void exportMedicalFees(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long medicalTypeId,
            HttpServletResponse response) throws IOException {

        List<MedicalFeeEntity> medicalFees;
        if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            medicalFees = medicalFeeRepository.findAll().stream()
                    .filter(mf -> mf.getCreatedAt() != null &&
                            mf.getCreatedAt().isAfter(start) &&
                            mf.getCreatedAt().isBefore(end))
                    .toList();
            if (medicalTypeId != null) {
                medicalFees = medicalFees.stream()
                        .filter(mf -> mf.getMedicalTypeEntity() != null &&
                                mf.getMedicalTypeEntity().getMedicalTypeId()
                                        .equals(medicalTypeId))
                        .toList();
            }
        } else if (medicalTypeId != null) {
            medicalFees = medicalFeeRepository.findByMedicalTypeEntity_MedicalTypeId(medicalTypeId);
        } else {
            medicalFees = medicalFeeRepository.findAll();
        }

        List<String> headers = List.of(
                "진료비ID", "진료ID", "진료유형ID", "진료유형명", "진료유형코드",
                "진료비금액", "수량", "총금액", "생성일시");

        List<Map<String, Object>> data = new ArrayList<>();
        for (MedicalFeeEntity medicalFee : medicalFees) {
            Map<String, Object> row = new HashMap<>();
            row.put("진료비ID", medicalFee.getMedicalFeeId());
            row.put("진료ID", medicalFee.getTreatmentEntity() != null
                    ? medicalFee.getTreatmentEntity().getTreatmentId()
                    : "");
            row.put("진료유형ID",
                    medicalFee.getMedicalTypeEntity() != null
                            ? medicalFee.getMedicalTypeEntity().getMedicalTypeId()
                            : "");
            row.put("진료유형명", medicalFee.getMedicalTypeEntity() != null
                    ? medicalFee.getMedicalTypeEntity().getMedicalTypeName()
                    : "");
            row.put("진료유형코드",
                    medicalFee.getMedicalTypeEntity() != null
                            ? medicalFee.getMedicalTypeEntity().getMedicalTypeCode()
                            : "");
            row.put("진료비금액", medicalFee.getMedicalFeeAmount() != null ? medicalFee.getMedicalFeeAmount()
                    : 0);
            row.put("수량", medicalFee.getQuantity() != null ? medicalFee.getQuantity() : 1);
            Long totalAmount = (medicalFee.getMedicalFeeAmount() != null ? medicalFee.getMedicalFeeAmount()
                    : 0L) *
                    (medicalFee.getQuantity() != null ? medicalFee.getQuantity() : 1);
            row.put("총금액", totalAmount);
            row.put("생성일시", medicalFee.getCreatedAt() != null ? medicalFee.getCreatedAt().toString() : "");
            data.add(row);
        }

        String filename = "진료비목록_" + java.time.LocalDate.now() + ".xlsx";

        excelExportService.exportToExcel(headers, data, filename, response);
    }
}
