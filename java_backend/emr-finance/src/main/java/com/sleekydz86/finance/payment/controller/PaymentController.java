package com.sleekydz86.finance.payment.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.finance.payment.dto.DailyPaymentStatistics;
import com.sleekydz86.finance.payment.dto.PaymentCalculationResult;
import com.sleekydz86.finance.payment.dto.PaymentCancelRequest;
import com.sleekydz86.finance.payment.dto.PaymentCompleteRequest;
import com.sleekydz86.finance.payment.dto.PaymentDetailResponse;
import com.sleekydz86.finance.payment.dto.PaymentRefundRequest;
import com.sleekydz86.finance.payment.dto.PaymentRegisterRequest;
import com.sleekydz86.finance.payment.dto.PaymentResponse;
import com.sleekydz86.finance.payment.dto.PaymentUpdateRequest;
import com.sleekydz86.finance.payment.dto.PeriodPaymentStatistics;
import com.sleekydz86.finance.payment.dto.UnpaidPaymentStatistics;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.payment.repository.PaymentRepository;
import com.sleekydz86.finance.payment.service.PaymentService;
import com.sleekydz86.finance.payment.service.PaymentStatisticsService;
import com.sleekydz86.finance.type.PaymentStatus;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentStatisticsService paymentStatisticsService;
    private final ExcelExportService excelExportService;
    private final PaymentRepository paymentRepository;

    @PostMapping("/register")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> registerPayment(
            @Valid @RequestBody PaymentRegisterRequest request,
            @AuthUser Long userId) {
        PaymentResponse response = paymentService.registerPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "결제 등록이 완료되었습니다.",
                "data", response));
    }

    @GetMapping("/{paymentId}")
    @AuthRole({ "STAFF", "ADMIN", "DOCTOR" })
    public ResponseEntity<Map<String, Object>> getPayment(@PathVariable Long paymentId) {
        PaymentDetailResponse response = paymentService.getPaymentDetailById(paymentId);
        return ResponseEntity.ok(Map.of(
                "message", "결제 상세 정보를 조회했습니다.",
                "data", response));
    }

    @GetMapping
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(
            @RequestParam(required = false) Long patientNo,
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        Page<PaymentResponse> payments;
        if (patientNo != null) {
            payments = paymentService.getPaymentsByPatientNo(patientNo, pageable);
        } else if (status != null) {
            payments = paymentService.getPaymentsByStatus(status, pageable);
        } else {
            payments = paymentService.getAllPayments(pageable);
        }

        return ResponseEntity.ok(payments);
    }

    @GetMapping("/treatment/{treatmentId}")
    @AuthRole({ "STAFF", "ADMIN", "DOCTOR" })
    public ResponseEntity<Map<String, Object>> getPaymentByTreatmentId(@PathVariable Long treatmentId) {
        PaymentResponse response = paymentService.getPaymentByTreatmentId(treatmentId);
        return ResponseEntity.ok(Map.of(
                "message", "진료 결제 정보를 조회했습니다.",
                "data", response));
    }

    @GetMapping("/patient/{patientNo}")
    @AuthRole({ "STAFF", "ADMIN", "DOCTOR" })
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByPatientNo(
            @PathVariable Long patientNo,
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPaymentsByPatientNo(patientNo, pageable));
    }

    @GetMapping("/status/{status}")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Page<PaymentResponse>> getPaymentsByStatus(
            @PathVariable PaymentStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status, pageable));
    }

    @GetMapping("/date-range")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        List<PaymentResponse> payments = paymentService.getPaymentsByDateRange(start, end);
        return ResponseEntity.ok(Map.of(
                "message", "기간별 결제 목록을 조회했습니다.",
                "data", payments));
    }

    @GetMapping("/today")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getTodayPayments() {
        List<PaymentResponse> payments = paymentService.getTodayPayments();
        return ResponseEntity.ok(Map.of(
                "message", "오늘 결제 목록을 조회했습니다.",
                "data", payments));
    }

    @GetMapping("/patient/{patientNo}/unpaid")
    @AuthRole({ "STAFF", "ADMIN", "DOCTOR" })
    public ResponseEntity<Map<String, Object>> getUnpaidPaymentsByPatientNo(@PathVariable Long patientNo) {
        List<PaymentResponse> payments = paymentService.getUnpaidPaymentsByPatientNo(patientNo);
        return ResponseEntity.ok(Map.of(
                "message", "미수 결제 목록을 조회했습니다.",
                "data", payments));
    }

    @PutMapping("/{paymentId}")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> updatePayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentUpdateRequest request) {
        PaymentResponse response = paymentService.updatePayment(paymentId, request);
        return ResponseEntity.ok(Map.of(
                "message", "결제 정보를 수정했습니다.",
                "data", response));
    }

    @PostMapping("/{paymentId}/complete")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> completePayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentCompleteRequest request) {
        PaymentResponse response = paymentService.completePayment(paymentId, request);
        return ResponseEntity.ok(Map.of(
                "message", "결제 완료 처리를 마쳤습니다.",
                "data", response));
    }

    @PostMapping("/{paymentId}/cancel")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> cancelPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentCancelRequest request) {
        PaymentResponse response = paymentService.cancelPayment(paymentId, request);
        return ResponseEntity.ok(Map.of(
                "message", "결제를 취소했습니다.",
                "data", response));
    }

    @PostMapping("/{paymentId}/refund")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> refundPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentRefundRequest request) {
        PaymentResponse response = paymentService.refundPayment(paymentId, request);
        return ResponseEntity.ok(Map.of(
                "message", "환불 처리를 완료했습니다.",
                "data", response));
    }

    @GetMapping("/calculate")
    @AuthRole({ "STAFF", "ADMIN", "DOCTOR" })
    public ResponseEntity<Map<String, Object>> calculatePaymentAmount(
            @RequestParam Long treatmentId,
            @RequestParam Long patientNo) {
        PaymentCalculationResult result = paymentService.calculatePaymentAmount(treatmentId, patientNo);
        return ResponseEntity.ok(Map.of(
                "message", "결제 금액을 계산했습니다.",
                "data", result));
    }

    @GetMapping("/statistics/daily")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        DailyPaymentStatistics statistics = paymentStatisticsService.getDailyStatistics(targetDate);
        return ResponseEntity.ok(Map.of(
                "message", "일별 결제 통계를 조회했습니다.",
                "data", statistics));
    }

    @GetMapping("/statistics/period")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getPeriodStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        PeriodPaymentStatistics statistics = paymentStatisticsService.getPeriodStatistics(startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "message", "기간별 결제 통계를 조회했습니다.",
                "data", statistics));
    }

    @GetMapping("/statistics/unpaid")
    @AuthRole({ "STAFF", "ADMIN" })
    public ResponseEntity<Map<String, Object>> getUnpaidStatistics() {
        UnpaidPaymentStatistics statistics = paymentStatisticsService.getUnpaidStatistics();
        return ResponseEntity.ok(Map.of(
                "message", "미수 결제 통계를 조회했습니다.",
                "data", statistics));
    }

    @GetMapping("/export")
    @AuthRole({ "STAFF", "ADMIN" })
    public void exportPayments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) PaymentStatus status,
            HttpServletResponse response) throws IOException {

        List<PaymentEntity> payments;
        if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            payments = paymentRepository.findByPaymentDateBetween(start, end);
            if (status != null) {
                payments = payments.stream()
                        .filter(payment -> payment.getPaymentStatus() == status)
                        .toList();
            }
        } else if (status != null) {
            payments = paymentRepository.findByPaymentStatus(status);
        } else {
            payments = paymentRepository.findAll();
        }

        List<String> headers = List.of(
                "결제ID",
                "환자번호",
                "환자명",
                "진료ID",
                "결제상태",
                "총금액",
                "본인부담금",
                "보험금",
                "현재수납금",
                "잔여수납금",
                "결제수단",
                "결제일시"
        );

        List<Map<String, Object>> data = payments.stream()
                .map(payment -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("결제ID", payment.getPaymentId());
                    row.put("환자번호", payment.getPatientEntity() != null ? payment.getPatientEntity().getPatientNo() : "");
                    row.put("환자명", payment.getPatientEntity() != null ? payment.getPatientEntity().getPatientName() : "");
                    row.put("진료ID", payment.getTreatmentEntity() != null ? payment.getTreatmentEntity().getTreatmentId() : "");
                    row.put("결제상태", payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : "");
                    row.put("총금액", payment.getPaymentTotalAmount() != null ? payment.getPaymentTotalAmount() : 0);
                    row.put("본인부담금", payment.getPaymentSelfPay() != null ? payment.getPaymentSelfPay() : 0);
                    row.put("보험금", payment.getPaymentInsuranceMoney() != null ? payment.getPaymentInsuranceMoney() : 0);
                    row.put("현재수납금", payment.getPaymentCurrentMoney() != null ? payment.getPaymentCurrentMoney() : 0);
                    row.put("잔여수납금", payment.getPaymentRemainMoney() != null ? payment.getPaymentRemainMoney() : 0);
                    row.put("결제수단", payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "");
                    row.put("결제일시", payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "");
                    return row;
                })
                .toList();

        String filename = "payments_" + LocalDate.now() + ".xlsx";
        excelExportService.exportToExcel(headers, data, filename, response);
    }
}
