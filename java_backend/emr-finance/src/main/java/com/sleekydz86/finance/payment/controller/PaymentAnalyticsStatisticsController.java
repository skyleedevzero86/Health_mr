package com.sleekydz86.finance.payment.controller;

import com.sleekydz86.core.audit.service.AuditService;
import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.finance.payment.statistics.analytics.service.PaymentAnalyticsStatisticsService;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHouseDailyPaymentStatisticsResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHousePaymentStatusStatisticsResponse;
import com.sleekydz86.finance.payment.statistics.clickhouse.dto.ClickHousePaymentSummaryResponse;
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
@RequestMapping({ "/api/payment/analytics", "/api/payment/statistics/clickhouse" })
@RequiredArgsConstructor
public class PaymentAnalyticsStatisticsController {

    private static final String SOURCE_TABLE = "analytics_payment_daily";
    private static final String MESSAGE_SUMMARY = "\uACB0\uC81C \uC694\uC57D \uD1B5\uACC4\uB97C \uC870\uD68C\uD588\uC2B5\uB2C8\uB2E4.";
    private static final String MESSAGE_DAILY = "\uC77C\uBCC4 \uACB0\uC81C \uD1B5\uACC4\uB97C \uC870\uD68C\uD588\uC2B5\uB2C8\uB2E4.";
    private static final String MESSAGE_STATUS = "\uACB0\uC81C \uC0C1\uD0DC\uBCC4 \uD1B5\uACC4\uB97C \uC870\uD68C\uD588\uC2B5\uB2C8\uB2E4.";
    private static final String HEADER_DATE = "\uC77C\uC790";
    private static final String HEADER_PAYMENT_COUNT = "\uACB0\uC81C\uAC74\uC218";
    private static final String HEADER_TOTAL_AMOUNT = "\uCD1D\uACB0\uC81C\uAE08\uC561";
    private static final String HEADER_UNPAID_AMOUNT = "\uBBF8\uC218\uAE08\uC561";
    private static final String HEADER_PAYMENT_STATUS = "\uACB0\uC81C\uC0C1\uD0DC";

    private final PaymentAnalyticsStatisticsService paymentAnalyticsStatisticsService;
    private final ExcelExportService excelExportService;
    private final AuditService auditService;

    @GetMapping("/summary")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getSummary(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = paymentAnalyticsStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = paymentAnalyticsStatisticsService.resolveEndDate(startDate, endDate);
        ClickHousePaymentSummaryResponse response = paymentAnalyticsStatisticsService.getSummary(startDate, endDate);

        auditService.logAudit(
                userId,
                paymentAnalyticsStatisticsService.getAuditActionType(),
                paymentAnalyticsStatisticsService.getSourceDatabase(),
                "PAYMENT_SUMMARY",
                buildRequestAudit(resolvedStartDate, resolvedEndDate),
                buildSummaryAudit(response),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", MESSAGE_SUMMARY,
                "data", response));
    }

    @GetMapping("/daily")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = paymentAnalyticsStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = paymentAnalyticsStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseDailyPaymentStatisticsResponse> response =
                paymentAnalyticsStatisticsService.getDailyStatistics(startDate, endDate);

        auditService.logAudit(
                userId,
                paymentAnalyticsStatisticsService.getAuditActionType(),
                paymentAnalyticsStatisticsService.getSourceDatabase(),
                "PAYMENT_DAILY",
                buildRequestAudit(resolvedStartDate, resolvedEndDate),
                buildListAudit(response.size()),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", MESSAGE_DAILY,
                "data", response));
    }

    @GetMapping("/status")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getStatusStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        LocalDate resolvedStartDate = paymentAnalyticsStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = paymentAnalyticsStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHousePaymentStatusStatisticsResponse> response =
                paymentAnalyticsStatisticsService.getStatusStatistics(startDate, endDate);

        auditService.logAudit(
                userId,
                paymentAnalyticsStatisticsService.getAuditActionType(),
                paymentAnalyticsStatisticsService.getSourceDatabase(),
                "PAYMENT_STATUS",
                buildRequestAudit(resolvedStartDate, resolvedEndDate),
                buildListAudit(response.size()),
                null,
                null);

        return ResponseEntity.ok(Map.of(
                "message", MESSAGE_STATUS,
                "data", response));
    }

    @GetMapping("/export/daily")
    @AuthRole({ "STAFF", "ADMIN" })
    public void exportDailyStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletResponse response) throws IOException {
        LocalDate resolvedStartDate = paymentAnalyticsStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = paymentAnalyticsStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHouseDailyPaymentStatisticsResponse> statistics =
                paymentAnalyticsStatisticsService.getDailyStatistics(startDate, endDate);

        List<String> headers = List.of(HEADER_DATE, HEADER_PAYMENT_COUNT, HEADER_TOTAL_AMOUNT, HEADER_UNPAID_AMOUNT);
        List<Map<String, Object>> data = new ArrayList<>();
        for (ClickHouseDailyPaymentStatisticsResponse statistic : statistics) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(HEADER_DATE, statistic.getMetricDate());
            row.put(HEADER_PAYMENT_COUNT, statistic.getPaymentCount());
            row.put(HEADER_TOTAL_AMOUNT, statistic.getTotalAmount());
            row.put(HEADER_UNPAID_AMOUNT, statistic.getUnpaidAmount());
            data.add(row);
        }

        String fileName = buildFilename("payment_analytics_daily", resolvedStartDate, resolvedEndDate);
        excelExportService.exportToExcel(headers, data, fileName, response);

        auditService.logAudit(
                userId,
                paymentAnalyticsStatisticsService.getAuditActionType(),
                paymentAnalyticsStatisticsService.getSourceDatabase(),
                "PAYMENT_DAILY_EXPORT",
                buildRequestAudit(resolvedStartDate, resolvedEndDate),
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
        LocalDate resolvedStartDate = paymentAnalyticsStatisticsService.resolveStartDate(startDate, endDate);
        LocalDate resolvedEndDate = paymentAnalyticsStatisticsService.resolveEndDate(startDate, endDate);
        List<ClickHousePaymentStatusStatisticsResponse> statistics =
                paymentAnalyticsStatisticsService.getStatusStatistics(startDate, endDate);

        List<String> headers = List.of(HEADER_PAYMENT_STATUS, HEADER_PAYMENT_COUNT, HEADER_TOTAL_AMOUNT, HEADER_UNPAID_AMOUNT);
        List<Map<String, Object>> data = new ArrayList<>();
        for (ClickHousePaymentStatusStatisticsResponse statistic : statistics) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(HEADER_PAYMENT_STATUS, statistic.getPaymentStatus());
            row.put(HEADER_PAYMENT_COUNT, statistic.getPaymentCount());
            row.put(HEADER_TOTAL_AMOUNT, statistic.getTotalAmount());
            row.put(HEADER_UNPAID_AMOUNT, statistic.getUnpaidAmount());
            data.add(row);
        }

        String fileName = buildFilename("payment_analytics_status", resolvedStartDate, resolvedEndDate);
        excelExportService.exportToExcel(headers, data, fileName, response);

        auditService.logAudit(
                userId,
                paymentAnalyticsStatisticsService.getAuditActionType(),
                paymentAnalyticsStatisticsService.getSourceDatabase(),
                "PAYMENT_STATUS_EXPORT",
                buildRequestAudit(resolvedStartDate, resolvedEndDate),
                buildExportAudit(fileName, statistics.size()),
                null,
                null);
    }

    private Map<String, Object> buildRequestAudit(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("sourceDatabase", paymentAnalyticsStatisticsService.getSourceDatabase());
        request.put("sourceTable", SOURCE_TABLE);
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
