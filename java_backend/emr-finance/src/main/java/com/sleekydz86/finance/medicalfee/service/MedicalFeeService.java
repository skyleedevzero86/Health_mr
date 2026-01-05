package com.sleekydz86.finance.medicalfee.service;

import com.sleekydz86.core.audit.annotation.AuditLog;
import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.common.exception.custom.DuplicateException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.finance.common.valueobject.Money;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.service.TreatmentService;
import com.sleekydz86.finance.medicalfee.dto.MedicalFeeDetailResponse;
import com.sleekydz86.finance.medicalfee.dto.MedicalFeeRequest;
import com.sleekydz86.finance.medicalfee.dto.MedicalFeeResponse;
import com.sleekydz86.finance.medicalfee.dto.MedicalFeeUpdateRequest;
import com.sleekydz86.finance.medicalfee.entity.MedicalTypeEntity;
import com.sleekydz86.finance.medicalfee.event.MedicalFeeCreatedEvent;
import com.sleekydz86.finance.medicalfee.event.MedicalFeeDeletedEvent;
import com.sleekydz86.finance.medicalfee.event.MedicalFeeUpdatedEvent;
import com.sleekydz86.finance.medicalfee.repository.MedicalFeeRepository;
import com.sleekydz86.finance.medicalfee.repository.MedicalTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.sleekydz86.finance.medicalfee.entity.MedicalFeeEntity;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MedicalFeeService implements BaseService<MedicalFeeEntity, Long> {

    private final MedicalFeeRepository medicalFeeRepository;
    private final MedicalTypeRepository medicalTypeRepository;
    private final TreatmentService treatmentService;
    private final EventPublisher eventPublisher;
    private final MedicalFeeNotificationService medicalFeeNotificationService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final NonCoveredMedicalFeeSyncService nonCoveredMedicalFeeSyncService;

    private static final String CACHE_PREFIX_MEDICAL_FEE = "medicalfee:";
    private static final String CACHE_PREFIX_MEDICAL_FEE_TREATMENT = "medicalfee:treatment:";
    private static final long CACHE_TTL_DAYS = 1; // 1일

    @Transactional
    @AuditLog(action = AuditLog.ActionType.CREATE)
    public MedicalFeeResponse createMedicalFee(MedicalFeeRequest request, Long treatmentId) {

        TreatmentEntity treatment = treatmentService.getTreatmentById(treatmentId);

        MedicalTypeEntity medicalType = medicalTypeRepository.findById(request.getMedicalTypeId())
                .orElseThrow(() -> new NotFoundException("진료 유형을 찾을 수 없습니다. ID: " + request.getMedicalTypeId()));

        if (medicalFeeRepository.findByTreatmentEntity_TreatmentIdAndMedicalTypeEntity_MedicalTypeId(
                treatmentId, request.getMedicalTypeId()).isPresent()) {
            throw new DuplicateException("이미 등록된 진료 유형입니다.");
        }

        Money feeAmount = medicalType.getMedicalTypeFee();

        if (feeAmount == null || feeAmount.isZero()) {
            log.warn("DB에 금액이 없음. API 조회 시도: MedicalTypeId={}", medicalType.getMedicalTypeId());

            Long apiAmount = nonCoveredMedicalFeeSyncService.getNonCoveredFeeAmountForCurrentInstitution(
                    medicalType.getMedicalTypeCode()
            ).block(Duration.ofSeconds(5));

            if (apiAmount != null && apiAmount > 0) {
                feeAmount = Money.of(apiAmount);
                medicalType.updateFee(feeAmount);
                medicalTypeRepository.save(medicalType);
            } else {
                throw new BusinessException(
                        "진료 유형의 금액을 조회할 수 없습니다. 관리자에게 문의하세요. " +
                        "MedicalTypeCode: " + medicalType.getMedicalTypeCode()
                );
            }
        }

        MedicalFeeEntity medicalFee = MedicalFeeEntity.builder()
                .medicalTypeEntity(medicalType)
                .treatmentEntity(treatment)
                .medicalFeeAmount(feeAmount)
                .quantity(request.getQuantity() != null ? request.getQuantity() : 1)
                .build();

        MedicalFeeEntity saved = medicalFeeRepository.save(medicalFee);

        invalidateMedicalFeeCache(saved);
        eventPublisher.publish(new MedicalFeeCreatedEvent(
                saved.getMedicalFeeId(),
                saved.getTreatmentEntity().getTreatmentId(),
                saved.getMedicalTypeEntity().getMedicalTypeId()));
        medicalFeeNotificationService.sendMedicalFeeRegisteredNotification(saved);
        invalidateMedicalFeeCache(saved);

        nonCoveredMedicalFeeSyncService.syncMedicalTypeFeeForCurrentInstitution(medicalType.getMedicalTypeId());

        return MedicalFeeResponse.from(saved);
    }

    public MedicalFeeResponse getMedicalFeeById(Long medicalFeeId) {
        MedicalFeeEntity medicalFee = validateExists(medicalFeeRepository, medicalFeeId,
                "진료비를 찾을 수 없습니다. ID: " + medicalFeeId);
        return MedicalFeeResponse.from(medicalFee);
    }

    public MedicalFeeDetailResponse getMedicalFeeDetailById(Long medicalFeeId) {
        MedicalFeeEntity medicalFee = validateExists(medicalFeeRepository, medicalFeeId,
                "진료비를 찾을 수 없습니다. ID: " + medicalFeeId);
        return MedicalFeeDetailResponse.from(medicalFee);
    }

    public List<MedicalFeeResponse> getMedicalFeesByTreatmentId(Long treatmentId) {
        String cacheKey = CACHE_PREFIX_MEDICAL_FEE_TREATMENT + treatmentId;

        @SuppressWarnings("unchecked")
        List<MedicalFeeResponse> cached = (List<MedicalFeeResponse>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("진료별 진료비 목록 캐시 히트: TreatmentId={}", treatmentId);
            return cached;
        }

        treatmentService.getTreatmentById(treatmentId);
        List<MedicalFeeEntity> medicalFees = medicalFeeRepository.findByTreatmentEntity_TreatmentId(treatmentId);
        List<MedicalFeeResponse> response = medicalFees.stream().map(MedicalFeeResponse::from)
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL_DAYS, TimeUnit.DAYS);
        log.debug("진료별 진료비 목록 캐시 저장: TreatmentId={}", treatmentId);

        return response;
    }

    public Page<MedicalFeeResponse> getMedicalFeesByMedicalTypeId(Long medicalTypeId, Pageable pageable) {
        medicalTypeRepository.findById(medicalTypeId)
                .orElseThrow(() -> new NotFoundException("진료 유형을 찾을 수 없습니다. ID: " + medicalTypeId));
        Page<MedicalFeeEntity> medicalFees = medicalFeeRepository.findByMedicalTypeEntity_MedicalTypeId(medicalTypeId)
                .stream()
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        list -> new org.springframework.data.domain.PageImpl<>(list, pageable, list.size())));
        return medicalFees.map(MedicalFeeResponse::from);
    }

    public Long getTotalMedicalFeeByTreatmentId(Long treatmentId) {
        treatmentService.getTreatmentById(treatmentId);
        Long totalFee = medicalFeeRepository.getTotalMedicalFeeByTreatmentId(treatmentId);
        return totalFee != null ? totalFee : 0L;
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.UPDATE)
    public MedicalFeeResponse updateMedicalFee(Long medicalFeeId, MedicalFeeUpdateRequest request) {
        MedicalFeeEntity medicalFee = validateExists(medicalFeeRepository, medicalFeeId,
                "진료비를 찾을 수 없습니다. ID: " + medicalFeeId);

        if (request.getMedicalTypeId() != null) {
            MedicalTypeEntity medicalType = medicalTypeRepository.findById(request.getMedicalTypeId())
                    .orElseThrow(() -> new NotFoundException("진료 유형을 찾을 수 없습니다. ID: " + request.getMedicalTypeId()));

            Long treatmentId = medicalFee.getTreatmentEntity().getTreatmentId();
            if (medicalFeeRepository.findByTreatmentEntity_TreatmentIdAndMedicalTypeEntity_MedicalTypeId(
                    treatmentId, request.getMedicalTypeId())
                    .filter(existing -> !existing.getMedicalFeeId().equals(medicalFeeId))
                    .isPresent()) {
                throw new DuplicateException("이미 등록된 진료 유형입니다.");
            }

            medicalFee.updateMedicalTypeEntity(medicalType);

            if (request.getMedicalFeeAmount() == null) {
                medicalFee.updateAmount(medicalType.getMedicalTypeFee());
            }
        }

        if (request.getMedicalFeeAmount() != null) {
            medicalFee.updateAmount(Money.of(request.getMedicalFeeAmount()));
        }

        if (request.getQuantity() != null) {
            medicalFee.updateQuantity(request.getQuantity());
        }

        MedicalFeeEntity saved = medicalFeeRepository.save(medicalFee);

        invalidateMedicalFeeCache(saved);

        eventPublisher.publish(new MedicalFeeUpdatedEvent(
                saved.getMedicalFeeId(),
                saved.getTreatmentEntity().getTreatmentId(),
                saved.getMedicalTypeEntity().getMedicalTypeId()));

        return MedicalFeeResponse.from(saved);
    }

    @Transactional
    @AuditLog(action = AuditLog.ActionType.DELETE)
    public void deleteMedicalFee(Long medicalFeeId) {
        MedicalFeeEntity medicalFee = validateExists(medicalFeeRepository, medicalFeeId,
                "진료비를 찾을 수 없습니다. ID: " + medicalFeeId);

        // 결제 완료된 진료비는 삭제 불가 검증
        // 실제로는 PaymentService를 통해 검증해야 하는기능 구현예증

        medicalFeeRepository.delete(medicalFee);

        invalidateMedicalFeeCache(medicalFee);

        eventPublisher.publish(new MedicalFeeDeletedEvent(
                medicalFeeId,
                medicalFee.getTreatmentEntity().getTreatmentId()));
    }

    public Page<MedicalFeeResponse> getAllMedicalFees(Pageable pageable) {
        Page<MedicalFeeEntity> medicalFees = medicalFeeRepository.findAll(pageable);
        return medicalFees.map(MedicalFeeResponse::from);
    }

    private void invalidateMedicalFeeCache(MedicalFeeEntity medicalFee) {
        try {

            if (medicalFee.getTreatmentEntity() != null) {
                redisTemplate
                        .delete(CACHE_PREFIX_MEDICAL_FEE_TREATMENT + medicalFee.getTreatmentEntity().getTreatmentId());
            }

            log.debug("진료비 캐시 무효화 완료: MedicalFeeId={}", medicalFee.getMedicalFeeId());
        } catch (Exception e) {
            log.warn("진료비 캐시 무효화 실패: MedicalFeeId={}", medicalFee.getMedicalFeeId(), e);
        }
    }
}
