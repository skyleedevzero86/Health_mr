package com.sleekydz86.emrclinical.treatment.controller;

import com.sleekydz86.core.audit.service.AuditService;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.emrclinical.treatment.statistics.analytics.service.TreatmentAnalyticsStatisticsService;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseDailyTreatmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentDepartmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentSummaryResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({ "/api/treatment/analytics", "/api/treatment/statistics/clickhouse" })
@RequiredArgsConstructor
public class TreatmentAnalyticsStatisticsController {

    private static final String SOURCE_TABLE = "analytics_treatment_daily";
    private static final String MESSAGE_SUMMARY = "\uC9C4\uB8CC \uC694\uC57D \uD1B5\uACC4\uB97C \uC870\uD68C\uD588\uC2B5\uB2C8\uB2E4.";
    private static final String MESSAGE_DAILY = "\uC77C\uBCC4 \uC9C4\uB8CC \uD1B5\uACC4\uB97C \uC870\uD68C\uD588\uC2B5\uB2C8\uB2E4.";
    private static final String MESSAGE_DEPARTMENT = "\uC9C4\uB8CC\uACFC\uBCC4 \uD1B5\uACC4\uB97C \uC870\uD68C\uD588\uC2B5\uB2C8\uB2E4.";
    private static final String HEADER_DATE = "\uC77C\uC790";
    private static final String HEADER_PATIENT_COUNT = "\uD658\uC790\uC218";
    private static final String HEADER_TREATMENT_COUNT = "\uC9C4\uB8CC\uAC74\uC218";
    private static final String HEADER_TOTAL_MEDICAL_FEE = "\uCD1D\uC9C4\uB8CC\uBE44";
    private static final String HEADER_DEPARTMENT = "\uC9C4\uB8CC\uACFC";

    private final TreatmentAnalyticsStatisticsService treatmentAnalyticsStatisticsService;
    private final ExcelExportService excelExportService;
    private final AuditService auditService;

    @GetMapping("/summary")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getSummary(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = treatmentAnalyticsStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = treatmentAnalyticsStatisticsService.resolveEndDate(startDate, endDate);
        ClickHouseTreatmentSummaryResponse response =
                treatmentAnalyticsStatisticsService.getSummary(startDate, endDate);

        auditService.logAudit(
                userId,
                treatmentAnalyticsStatisticsService.getAuditActionType(),
                treatmentAnalyticsStatisticsService.getSourceDatabase(),
                "TREATMENT_SUMMARY",
                buildRequestAudit(resolvedStartDate, resolvedEndDate),
                buildSummaryAudit(response),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", MESSAGE_SUMMARY,
                "data", response));
    }

    @GetMapping("/daily")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = treatmentAnalyticsStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = treatmentAnalyticsStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseDailyTreatmentStatisticsResponse> response =
                treatmentAnalyticsStatisticsService.getDailyStatistics(startDate, endDate);

        auditService.logAudit(
                userId,
                treatmentAnalyticsStatisticsService.getAuditActionType(),
                treatmentAnalyticsStatisticsService.getSourceDatabase(),
                "TREATMENT_DAILY",
                buildRequestAudit(resolvedStartDate, resolvedEndDate),
                buildListAudit(response.size()),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", MESSAGE_DAILY,
                "data", response));
    }

    @GetMapping("/departments")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getDepartmentStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = treatmentAnalyticsStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = treatmentAnalyticsStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseTreatmentDepartmentStatisticsResponse> response =
                treatmentAnalyticsStatisticsService.getDepartmentStatistics(startDate, endDate);

        auditService.logAudit(
                userId,
                treatmentAnalyticsStatisticsService.getAuditActionType(),
                treatmentAnalyticsStatisticsService.getSourceDatabase(),
                "TREATMENT_DEPARTMENTS",
                buildRequestAudit(resolvedStartDate, resolvedEndDate),
                buildListAudit(response.size()),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", MESSAGE_DEPARTMENT,
                "data", response));
    }

    @GetMapping("/export/daily")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public void exportDailyStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        LocalDate resolvedStartDate = treatmentAnalyticsStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = treatmentAnalyticsStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseDailyTreatmentStatisticsResponse> statistics =
                treatmentAnalyticsStatisticsService.getDailyStatistics(startDate, endDate);

        List<String> headers = List.of(HEADER_DATE, HEADER_PATIENT_COUNT, HEADER_TREATMENT_COUNT, HEADER_TOTAL_MEDICAL_FEE);
        List<Map<String, Object>> data = new ArrayList<>();
        for (ClickHouseDailyTreatmentStatisticsResponse statistic : statistics) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(HEADER_DATE, statistic.getMetricDate());
            row.put(HEADER_PATIENT_COUNT, statistic.getPatientCount());
            row.put(HEADER_TREATMENT_COUNT, statistic.getTreatmentCount());
            row.put(HEADER_TOTAL_MEDICAL_FEE, statistic.getTotalMedicalFee());
            data.add(row);
        }

        String fileName = buildFilename("treatment_analytics_daily", resolvedStartDate, resolvedEndDate);
        excelExportService.exportToExcel(headers, data, fileName, response);

        auditService.logAudit(
                userId,
                treatmentAnalyticsStatisticsService.getAuditActionType(),
                treatmentAnalyticsStatisticsService.getSourceDatabase(),
                "TREATMENT_DAILY_EXPORT",
                buildRequestAudit(resolvedStartDate, resolvedEndDate),
                buildExportAudit(fileName, statistics.size()),
                null,
                null);
    }

    @GetMapping("/export/departments")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public void exportDepartmentStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        LocalDate resolvedStartDate = treatmentAnalyticsStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = treatmentAnalyticsStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseTreatmentDepartmentStatisticsResponse> statistics =
                treatmentAnalyticsStatisticsService.getDepartmentStatistics(startDate, endDate);

        List<String> headers = List.of(HEADER_DEPARTMENT, HEADER_PATIENT_COUNT, HEADER_TREATMENT_COUNT, HEADER_TOTAL_MEDICAL_FEE);
        List<Map<String, Object>> data = new ArrayList<>();
        for (ClickHouseTreatmentDepartmentStatisticsResponse statistic : statistics) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(HEADER_DEPARTMENT, statistic.getDepartmentName());
            row.put(HEADER_PATIENT_COUNT, statistic.getPatientCount());
            row.put(HEADER_TREATMENT_COUNT, statistic.getTreatmentCount());
            row.put(HEADER_TOTAL_MEDICAL_FEE, statistic.getTotalMedicalFee());
            data.add(row);
        }

        String fileName = buildFilename("treatment_analytics_departments", resolvedStartDate, resolvedEndDate);
        excelExportService.exportToExcel(headers, data, fileName, response);

        auditService.logAudit(
                userId,
                treatmentAnalyticsStatisticsService.getAuditActionType(),
                treatmentAnalyticsStatisticsService.getSourceDatabase(),
                "TREATMENT_DEPARTMENT_EXPORT",
                buildRequestAudit(resolvedStartDate, resolvedEndDate),
                buildExportAudit(fileName, statistics.size()),
                null,
                null);
    }

    private Map<String, Object> buildRequestAudit(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("sourceDatabase", treatmentAnalyticsStatisticsService.getSourceDatabase());
        request.put("sourceTable", SOURCE_TABLE);
        request.put("startDate", startDate);
        request.put("endDate", endDate);
        return request;
    }

    private Map<String, Object> buildSummaryAudit(ClickHouseTreatmentSummaryResponse response) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("patientCount", response.getPatientCount());
        summary.put("treatmentCount", response.getTreatmentCount());
        summary.put("totalMedicalFee", response.getTotalMedicalFee());
        return summary;
    }

    private Map<String, Object> buildListAudit(int rowCount) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("rowCount", rowCount);
        return summary;
    }

    private Map<String, Object> buildExportAudit(String fileName, int rowCount) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("fileName", fileName);
        summary.put("rowCount", rowCount);
        return summary;
    }

    private String buildFilename(String prefix, LocalDate startDate, LocalDate endDate) {
        return prefix + "_" + startDate + "_" + endDate + ".xlsx";
    }
}
