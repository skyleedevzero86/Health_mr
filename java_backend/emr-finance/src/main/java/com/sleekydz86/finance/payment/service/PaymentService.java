package com.sleekydz86.finance.payment.service;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.exception.custom.DuplicateException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.service.TreatmentService;
import com.sleekydz86.finance.common.valueobject.Money;
import com.sleekydz86.finance.payment.dto.PaymentCalculationResult;
import com.sleekydz86.finance.payment.dto.*;
import com.sleekydz86.finance.payment.entity.PaymentEntity;
import com.sleekydz86.finance.payment.event.PaymentCancelledEvent;
import com.sleekydz86.finance.payment.event.PaymentCompletedEvent;
import com.sleekydz86.finance.payment.event.PaymentCreatedEvent;
import com.sleekydz86.finance.payment.event.PaymentRefundedEvent;
import com.sleekydz86.finance.payment.repository.PaymentRepository;
import com.sleekydz86.finance.type.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentService implements BaseService<PaymentEntity, Long> {

    private final PaymentRepository paymentRepository;
    private final TreatmentService treatmentService;
    private final PatientService patientService;
    private final PaymentCalculationService paymentCalculationService;
    private final PaymentValidationService paymentValidationService;
    private final EventPublisher eventPublisher;
    private final PaymentNotificationService paymentNotificationService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_PREFIX_PAYMENT = "payment:";
    private static final String CACHE_PREFIX_PAYMENT_PATIENT = "payment:patient:";
    private static final String CACHE_PREFIX_PAYMENT_TREATMENT = "payment:treatment:";
    private static final long CACHE_TTL_HOURS = 1;

    @Transactional
    @AuditLog(action = AuditLog.ActionType.CREATE)
    public PaymentResponse registerPayment(PaymentRegisterRequest request) {

        TreatmentEntity treatment = treatmentService.getTreatmentById(request.getTreatmentId());

        paymentValidationService.validateTreatmentStatus(treatment);

        if (paymentRepository.findByTreatmentEntity_TreatmentId(request.getTreatmentId()).isPresent()) {
            throw new DuplicateException("이미 결제 정보가 등록된 진료입니다.");
        }

        PatientEntity patient = null;
        if (treatment.getCheckInEntity() != null && treatment.getCheckInEntity().getPatientEntity() != null) {
            patient = treatment.getCheckInEntity().getPatientEntity();
        }

        if (patient == null) {
            throw new NotFoundException("환자 정보를 찾을 수 없습니다.");
        }

        PaymentCalculationResult calculationResult = paymentCalculationService
                .calculatePaymentAmount(treatment, patient);

        PaymentEntity payment = PaymentEntity.builder()
                .treatmentEntity(treatment)
                .patientEntity(patient)
                .paymentStatus(PaymentStatus.UNPAID)
                .paymentTotalAmount(Money.of(calculationResult.getTotalAmount()))
                .paymentSelfPay(Money.of(calculationResult.getSelfPay()))
                .paymentInsuranceMoney(Money.of(calculationResult.getInsuranceMoney()))
                .paymentCurrentMoney(Money.zero())
                .paymentAmount(Money.zero())
                .paymentRemainMoney(Money.of(calculationResult.getSelfPay()))
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(LocalDateTime.now())
                .build();

        PaymentEntity saved = paymentRepository.save(payment);

        eventPublisher.publish(new PaymentCreatedEvent(
                saved.getPaymentId(),
                saved.getTreatmentEntity().getTreatmentId(),
                patient.getPatientNoValue()));

        return PaymentResponse.from(saved);
    }

    public PaymentResponse getPaymentById(Long paymentId) {
        String cacheKey = CACHE_PREFIX_PAYMENT + paymentId;

        PaymentResponse cached = (PaymentResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("결제 정보 캐시 히트: PaymentId={}", paymentId);
            return cached;
        }

        PaymentEntity payment = validateExists(paymentRepository, paymentId,
                "결제를 찾을 수 없습니다. ID: " + paymentId);
        PaymentResponse response = PaymentResponse.from(payment);

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_HOURS, TimeUnit.HOURS);
        log.debug("결제 정보 캐시 저장: PaymentId={}", paymentId);

        return response;
    }

    public PaymentDetailResponse getPaymentDetailById(Long paymentId) {
        PaymentEntity payment = validateExists(paymentRepository, paymentId,
                "결제를 찾을 수 없습니다. ID: " + paymentId);
        return PaymentDetailResponse.from(payment);
    }

    public PaymentResponse getPaymentByTreatmentId(Long treatmentId) {
        String cacheKey = CACHE_PREFIX_PAYMENT_TREATMENT + treatmentId;

        PaymentResponse cached = (PaymentResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("진료별 결제 정보 캐시 히트: TreatmentId={}", treatmentId);
            return cached;
        }

        PaymentEntity payment = paymentRepository.findByTreatmentEntity_TreatmentId(treatmentId)
                .orElseThrow(() -> new NotFoundException("해당 진료의 결제 정보를 찾을 수 없습니다. TreatmentId: " + treatmentId));
        PaymentResponse response = PaymentResponse.from(payment);

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_HOURS, TimeUnit.HOURS);
        log.debug("진료별 결제 정보 캐시 저장: TreatmentId={}", treatmentId);

        return response;
    }

    public Page<PaymentResponse> getPaymentsByPatientNo(Long patientNo, Pageable pageable) {
        patientService.getPatientByNo(patientNo);

        if (pageable.getPageNumber() == 0 && pageable.getPageSize() <= 20) {
            String cacheKey = CACHE_PREFIX_PAYMENT_PATIENT + patientNo + ":page:0";

            @SuppressWarnings("unchecked")
            Page<PaymentResponse> cached = (Page<PaymentResponse>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("환자별 결제 목록 캐시 히트: PatientNo={}", patientNo);
                return cached;
            }
        }

        Page<PaymentEntity> payments = paymentRepository.findByPatientEntity_PatientNo(patientNo, pageable);
        Page<PaymentResponse> response = payments.map(PaymentResponse::from);

        if (pageable.getPageNumber() == 0 && pageable.getPageSize() <= 20) {
            String cacheKey = CACHE_PREFIX_PAYMENT_PATIENT + patientNo + ":page:0";
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.debug("환자별 결제 목록 캐시 저장: PatientNo={}", patientNo);
        }

        return response;
    }

    public Page<PaymentResponse> getPaymentsByStatus(PaymentStatus status, Pageable pageable) {
        Page<PaymentEntity> payments = paymentRepository.findByPaymentStatus(status, pageable);
        return payments.map(PaymentResponse::from);
    }

    public List<PaymentResponse> getPaymentsByDateRange(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        List<PaymentEntity> payments = paymentRepository.findByPaymentDateBetween(startDateTime, endDateTime);
        return payments.stream().map(PaymentResponse::from).collect(Collectors.toList());
    }

    public List<PaymentResponse> getTodayPayments() {
        List<PaymentEntity> payments = paymentRepository.findTodayPayments();
        return payments.stream().map(PaymentResponse::from).collect(Collectors.toList());
    }

    public List<PaymentResponse> getUnpaidPaymentsByPatientNo(Long patientNo) {
        patientService.getPatientByNo(patientNo);
        List<PaymentEntity> payments = paymentRepository.findByPatientEntity_PatientNo(patientNo);
        return payments.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.UNPAID ||
                        p.getPaymentStatus() == PaymentStatus.PARTIAL)
                .map(PaymentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public PaymentResponse updatePayment(Long paymentId, PaymentUpdateRequest request) {
        PaymentEntity payment = validateExists(paymentRepository, paymentId,
                "결제를 찾을 수 없습니다. ID: " + paymentId);

        paymentValidationService.validateNotPaid(payment);

        paymentValidationService.validatePaymentAmount(request.getPaymentAmount(),
                payment.getPaymentTotalAmountValue());

        payment.partialPay(Money.of(request.getPaymentAmount()));
        if (request.getPaymentMethod() != null) {
            payment.updatePaymentMethod(request.getPaymentMethod());
        }

        PaymentEntity saved = paymentRepository.save(payment);
        invalidatePaymentCache(saved);
        if (saved.getPaymentStatus() == PaymentStatus.PAID) {
            eventPublisher.publish(new PaymentCompletedEvent(
                    saved.getPaymentId(),
                    saved.getTreatmentEntity().getTreatmentId(),
                    saved.getPatientEntity() != null ? saved.getPatientEntity().getPatientNoValue() : null));
            paymentNotificationService.sendPaymentCompletedNotification(saved);
        }

        return PaymentResponse.from(saved);
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public PaymentResponse completePayment(Long paymentId, PaymentCompleteRequest request) {
        PaymentEntity payment = validateExists(paymentRepository, paymentId,
                "결제를 찾을 수 없습니다. ID: " + paymentId);

        payment.complete(request.getPaymentMethod(), request.getApprovalNumber(), request.getCardCompany());

        PaymentEntity saved = paymentRepository.save(payment);

        eventPublisher.publish(new PaymentCompletedEvent(
                saved.getPaymentId(),
                saved.getTreatmentEntity().getTreatmentId(),
                saved.getPatientEntity() != null ? saved.getPatientEntity().getPatientNoValue() : null));

        paymentNotificationService.sendPaymentCompletedNotification(saved);

        return PaymentResponse.from(saved);
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.DELETE)
    public PaymentResponse cancelPayment(Long paymentId, PaymentCancelRequest request) {
        PaymentEntity payment = validateExists(paymentRepository, paymentId,
                "결제를 찾을 수 없습니다. ID: " + paymentId);

        payment.cancel(request.getCancelReason());

        PaymentEntity saved = paymentRepository.save(payment);

        eventPublisher.publish(new PaymentCancelledEvent(
                saved.getPaymentId(),
                saved.getTreatmentEntity().getTreatmentId(),
                request.getCancelReason()));

        return PaymentResponse.from(saved);
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public PaymentResponse refundPayment(Long paymentId, PaymentRefundRequest request) {
        PaymentEntity payment = validateExists(paymentRepository, paymentId,
                "결제를 찾을 수 없습니다. ID: " + paymentId);

        paymentValidationService.validateCanRefund(payment);

        paymentValidationService.validateRefundAmount(request.getRefundAmount(),
                payment.getPaymentCurrentMoneyValue());

        payment.refund(Money.of(request.getRefundAmount()), request.getRefundMethod());

        PaymentEntity saved = paymentRepository.save(payment);

        eventPublisher.publish(new PaymentRefundedEvent(
                saved.getPaymentId(),
                saved.getTreatmentEntity().getTreatmentId(),
                request.getRefundAmount()));

        return PaymentResponse.from(saved);
    }

    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        Page<PaymentEntity> payments = paymentRepository.findAll(pageable);
        return payments.map(PaymentResponse::from);
    }

    public PaymentCalculationResult calculatePaymentAmount(
            Long treatmentId, Long patientNo) {
        TreatmentEntity treatment = treatmentService.getTreatmentById(treatmentId);
        PatientEntity patient = patientService.getPatientByNo(patientNo);
        return paymentCalculationService.calculatePaymentAmount(treatment, patient);
    }

    private void invalidatePaymentCache(PaymentEntity payment) {
        try {

            redisTemplate.delete(CACHE_PREFIX_PAYMENT + payment.getPaymentId());

            if (payment.getTreatmentEntity() != null) {
                redisTemplate.delete(CACHE_PREFIX_PAYMENT_TREATMENT + payment.getTreatmentEntity().getTreatmentId());
            }

            if (payment.getPatientEntity() != null) {
                String pattern = CACHE_PREFIX_PAYMENT_PATIENT + payment.getPatientEntity().getPatientNo() + ":*";
                redisTemplate.delete(redisTemplate.keys(pattern));
            }

            log.debug("결제 캐시 무효화 완료: PaymentId={}", payment.getPaymentId());
        } catch (Exception e) {
            log.warn("결제 캐시 무효화 실패: PaymentId={}", payment.getPaymentId(), e);
        }
    }
}
