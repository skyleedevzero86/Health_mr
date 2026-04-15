package com.sleekydz86.finance.payment.controller;

import com.sleekydz86.core.audit.service.AuditService;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHouseDailyPaymentStatisticsResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHousePaymentStatusStatisticsResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHousePaymentSummaryResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.service.ClickHousePaymentStatisticsService;
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
@RequestMapping("/api/payment/statistics/clickhouse")
@RequiredArgsConstructor
public class PaymentClickHouseStatisticsController {

    private final ClickHousePaymentStatisticsService clickHousePaymentStatisticsService;
    private final ExcelExportService excelExportService;
    private final AuditService auditService;

    @GetMapping("/summary")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getSummary(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = clickHousePaymentStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = clickHousePaymentStatisticsService.resolveEndDate(startDate, endDate);
        ClickHousePaymentSummaryResponse response = clickHousePaymentStatisticsService.getSummary(startDate, endDate);

        auditService.logAudit(
                userId,
                "CLICKHOUSE_USAGE",
                "CLICKHOUSE",
                "PAYMENT_SUMMARY",
                buildRequestAudit("analytics_payment_daily", resolvedStartDate, resolvedEndDate),
                buildSummaryAudit(response),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", "ClickHouse 결제 요약 통계를 조회했습니다.",
                "data", response));
    }

    @GetMapping("/daily")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = clickHousePaymentStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = clickHousePaymentStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseDailyPaymentStatisticsResponse> response =
                clickHousePaymentStatisticsService.getDailyStatistics(startDate, endDate);

        auditService.logAudit(
                userId,
                "CLICKHOUSE_USAGE",
                "CLICKHOUSE",
                "PAYMENT_DAILY",
                buildRequestAudit("analytics_payment_daily", resolvedStartDate, resolvedEndDate),
                buildListAudit(response.size()),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", "ClickHouse 일별 결제 통계를 조회했습니다.",
                "data", response));
    }

    @GetMapping("/status")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getStatusStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = clickHousePaymentStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = clickHousePaymentStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHousePaymentStatusStatisticsResponse> response =
                clickHousePaymentStatisticsService.getStatusStatistics(startDate, endDate);

        auditService.logAudit(
                userId,
                "CLICKHOUSE_USAGE",
                "CLICKHOUSE",
                "PAYMENT_STATUS",
                buildRequestAudit("analytics_payment_daily", resolvedStartDate, resolvedEndDate),
                buildListAudit(response.size()),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", "ClickHouse 결제 상태별 통계를 조회했습니다.",
                "data", response));
    }

    @GetMapping("/export/daily")
    @AuthRole({ "STAFF", "ADMIN" })
    public void exportDailyStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        LocalDate resolvedStartDate = clickHousePaymentStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = clickHousePaymentStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseDailyPaymentStatisticsResponse> statistics =
                clickHousePaymentStatisticsService.getDailyStatistics(startDate, endDate);

        List<String> headers = List.of("일자", "결제건수", "총결제금액", "미수금액");
        List<Map<String, Object>> data = new ArrayList<>();
        for (ClickHouseDailyPaymentStatisticsResponse statistic : statistics) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("일자", statistic.getMetricDate());
            row.put("결제건수", statistic.getPaymentCount());
            row.put("총결제금액", statistic.getTotalAmount());
            row.put("미수금액", statistic.getUnpaidAmount());
            data.add(row);
        }

        String fileName = buildFilename("payment_clickhouse_daily", resolvedStartDate, resolvedEndDate);
        excelExportService.exportToExcel(headers, data, fileName, response);

        auditService.logAudit(
                userId,
                "CLICKHOUSE_USAGE",
                "CLICKHOUSE",
                "PAYMENT_DAILY_EXPORT",
                buildRequestAudit("analytics_payment_daily", resolvedStartDate, resolvedEndDate),
                buildExportAudit(fileName, statistics.size()),
                null,
                null);
    }

    @GetMapping("/export/status")
    @AuthRole({ "STAFF", "ADMIN" })
    public void exportStatusStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        LocalDate resolvedStartDate = clickHousePaymentStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = clickHousePaymentStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHousePaymentStatusStatisticsResponse> statistics =
                clickHousePaymentStatisticsService.getStatusStatistics(startDate, endDate);

        List<String> headers = List.of("결제상태", "결제건수", "총결제금액", "미수금액");
        List<Map<String, Object>> data = new ArrayList<>();
        for (ClickHousePaymentStatusStatisticsResponse statistic : statistics) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("결제상태", statistic.getPaymentStatus());
            row.put("결제건수", statistic.getPaymentCount());
            row.put("총결제금액", statistic.getTotalAmount());
            row.put("미수금액", statistic.getUnpaidAmount());
            data.add(row);
        }

        String fileName = buildFilename("payment_clickhouse_status", resolvedStartDate, resolvedEndDate);
        excelExportService.exportToExcel(headers, data, fileName, response);

        auditService.logAudit(
                userId,
                "CLICKHOUSE_USAGE",
                "CLICKHOUSE",
                "PAYMENT_STATUS_EXPORT",
                buildRequestAudit("analytics_payment_daily", resolvedStartDate, resolvedEndDate),
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

    private Map<String, Object> buildSummaryAudit(ClickHousePaymentSummaryResponse response) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("paymentCount", response.getPaymentCount());
        summary.put("totalAmount", response.getTotalAmount());
        summary.put("unpaidAmount", response.getUnpaidAmount());
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
