package com.sleekydz86.emrclinical.treatment.controller;

import com.sleekydz86.core.audit.service.AuditService;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseDailyTreatmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentDepartmentStatisticsResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.dto.ClickHouseTreatmentSummaryResponse;
import com.sleekydz86.emrclinical.treatment.statistics.clickhouse.service.ClickHouseTreatmentStatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
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

@Profile("clickhouse")
@RestController
@RequestMapping("/api/treatment/statistics/clickhouse")
@RequiredArgsConstructor
public class TreatmentClickHouseStatisticsController {

    private final ClickHouseTreatmentStatisticsService clickHouseTreatmentStatisticsService;
    private final ExcelExportService excelExportService;
    private final AuditService auditService;

    @GetMapping("/summary")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getSummary(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = clickHouseTreatmentStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = clickHouseTreatmentStatisticsService.resolveEndDate(startDate, endDate);
        ClickHouseTreatmentSummaryResponse response =
                clickHouseTreatmentStatisticsService.getSummary(startDate, endDate);

        auditService.logAudit(
                userId,
                "CLICKHOUSE_USAGE",
                "CLICKHOUSE",
                "TREATMENT_SUMMARY",
                buildRequestAudit("analytics_treatment_daily", resolvedStartDate, resolvedEndDate),
                buildSummaryAudit(response),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", "ClickHouse 진료 요약 통계를 조회했습니다.",
                "data", response));
    }

    @GetMapping("/daily")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = clickHouseTreatmentStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = clickHouseTreatmentStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseDailyTreatmentStatisticsResponse> response =
                clickHouseTreatmentStatisticsService.getDailyStatistics(startDate, endDate);

        auditService.logAudit(
                userId,
                "CLICKHOUSE_USAGE",
                "CLICKHOUSE",
                "TREATMENT_DAILY",
                buildRequestAudit("analytics_treatment_daily", resolvedStartDate, resolvedEndDate),
                buildListAudit(response.size()),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", "ClickHouse 일별 진료 통계를 조회했습니다.",
                "data", response));
    }

    @GetMapping("/departments")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getDepartmentStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = clickHouseTreatmentStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = clickHouseTreatmentStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseTreatmentDepartmentStatisticsResponse> response =
                clickHouseTreatmentStatisticsService.getDepartmentStatistics(startDate, endDate);

        auditService.logAudit(
                userId,
                "CLICKHOUSE_USAGE",
                "CLICKHOUSE",
                "TREATMENT_DEPARTMENTS",
                buildRequestAudit("analytics_treatment_daily", resolvedStartDate, resolvedEndDate),
                buildListAudit(response.size()),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", "ClickHouse 진료과별 통계를 조회했습니다.",
                "data", response));
    }

    @GetMapping("/export/daily")
    @AuthRole({ "STAFF", "DOCTOR", "ADMIN" })
    public void exportDailyStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        LocalDate resolvedStartDate = clickHouseTreatmentStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = clickHouseTreatmentStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseDailyTreatmentStatisticsResponse> statistics =
                clickHouseTreatmentStatisticsService.getDailyStatistics(startDate, endDate);

        List<String> headers = List.of("일자", "환자수", "진료건수", "총진료비");
        List<Map<String, Object>> data = new ArrayList<>();
        for (ClickHouseDailyTreatmentStatisticsResponse statistic : statistics) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("일자", statistic.getMetricDate());
            row.put("환자수", statistic.getPatientCount());
            row.put("진료건수", statistic.getTreatmentCount());
            row.put("총진료비", statistic.getTotalMedicalFee());
            data.add(row);
        }

        String fileName = buildFilename("treatment_clickhouse_daily", resolvedStartDate, resolvedEndDate);
        excelExportService.exportToExcel(headers, data, fileName, response);

        auditService.logAudit(
                userId,
                "CLICKHOUSE_USAGE",
                "CLICKHOUSE",
                "TREATMENT_DAILY_EXPORT",
                buildRequestAudit("analytics_treatment_daily", resolvedStartDate, resolvedEndDate),
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
        LocalDate resolvedStartDate = clickHouseTreatmentStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = clickHouseTreatmentStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseTreatmentDepartmentStatisticsResponse> statistics =
                clickHouseTreatmentStatisticsService.getDepartmentStatistics(startDate, endDate);

        List<String> headers = List.of("진료과", "환자수", "진료건수", "총진료비");
        List<Map<String, Object>> data = new ArrayList<>();
        for (ClickHouseTreatmentDepartmentStatisticsResponse statistic : statistics) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("진료과", statistic.getDepartmentName());
            row.put("환자수", statistic.getPatientCount());
            row.put("진료건수", statistic.getTreatmentCount());
            row.put("총진료비", statistic.getTotalMedicalFee());
            data.add(row);
        }

        String fileName = buildFilename("treatment_clickhouse_departments", resolvedStartDate, resolvedEndDate);
        excelExportService.exportToExcel(headers, data, fileName, response);

        auditService.logAudit(
                userId,
                "CLICKHOUSE_USAGE",
                "CLICKHOUSE",
                "TREATMENT_DEPARTMENT_EXPORT",
                buildRequestAudit("analytics_treatment_daily", resolvedStartDate, resolvedEndDate),
                buildExportAudit(fileName, statistics.size()),
                null,
                null);
    }

    private Map<String, Object> buildRequestAudit(String sourceTable, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("sourceDatabase", "CLICKHOUSE");
        request.put("sourceTable", sourceTable);
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
