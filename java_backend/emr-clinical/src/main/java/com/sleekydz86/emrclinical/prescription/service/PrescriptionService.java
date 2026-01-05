package com.sleekydz86.emrclinical.prescription.service;

import com.sleekydz86.core.common.exception.custom.BusinessException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.service.UserService;
import com.sleekydz86.emrclinical.prescription.api.dto.DrugInfoItemResponse;
import com.sleekydz86.emrclinical.prescription.dto.PrescriptionCreateRequest;
import com.sleekydz86.emrclinical.prescription.dto.PrescriptionItemCreateRequest;
import com.sleekydz86.emrclinical.prescription.dto.PrescriptionUpdateRequest;
import com.sleekydz86.emrclinical.prescription.entity.PrescriptionEntity;
import com.sleekydz86.emrclinical.prescription.entity.PrescriptionItemEntity;
import com.sleekydz86.emrclinical.prescription.repository.PrescriptionRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.service.TreatmentService;
import com.sleekydz86.emrclinical.types.PrescriptionStatus;
import com.sleekydz86.emrclinical.types.PrescriptionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrescriptionService implements BaseService<PrescriptionEntity, Long> {

    private final PrescriptionRepository prescriptionRepository;
    private final TreatmentService treatmentService;
    private final PatientService patientService;
    private final UserService userService;
    private final DrugInfoService drugInfoService;
    
    @Value("${drug-info.api.validation.enabled:true}")
    private boolean drugValidationEnabled;

    public PrescriptionEntity getPrescriptionById(Long prescriptionId) {
        return validateExists(prescriptionRepository, prescriptionId, "처방을 찾을 수 없습니다. ID: " + prescriptionId);
    }

    public PrescriptionEntity getPrescriptionByTreatmentId(Long treatmentId) {
        return prescriptionRepository.findByTreatmentEntity_TreatmentId(treatmentId)
                .orElseThrow(() -> new NotFoundException("해당 진료에 대한 처방을 찾을 수 없습니다. TreatmentId: " + treatmentId));
    }

    public Page<PrescriptionEntity> getPrescriptionsByPatientNo(Long patientNo, Pageable pageable) {
        patientService.getPatientByNo(patientNo);
        return prescriptionRepository.findByPatientEntity_PatientNo(patientNo, pageable);
    }

    public Page<PrescriptionEntity> getPrescriptionsByDoctor(Long doctorId, Pageable pageable) {
        userService.getUserById(doctorId);
        return prescriptionRepository.findByPrescriptionDoc_Id(doctorId, pageable);
    }

    public Page<PrescriptionEntity> getPrescriptionsByStatus(PrescriptionStatus status, Pageable pageable) {
        return prescriptionRepository.findByPrescriptionStatus(status, pageable);
    }

    public Page<PrescriptionEntity> getPrescriptionsByType(PrescriptionType type, Pageable pageable) {
        return prescriptionRepository.findByPrescriptionType(type, pageable);
    }

    public Page<PrescriptionEntity> getAllPrescriptions(Pageable pageable) {
        return prescriptionRepository.findAll(pageable);
    }

    public List<PrescriptionEntity> getPrescriptionsByDateRange(LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        return prescriptionRepository.findByPrescriptionDateBetween(startDateTime, endDateTime);
    }

    public List<PrescriptionEntity> getTodayPrescriptions() {
        return prescriptionRepository.findTodayPrescriptions(LocalDate.now());
    }

    @Transactional
    public PrescriptionEntity registerPrescription(PrescriptionCreateRequest request) {
        TreatmentEntity treatment = treatmentService.getTreatmentById(request.getTreatmentId());
        PatientEntity patient = patientService.getPatientByNo(request.getPatientNo());
        UserEntity doctor = userService.getUserById(request.getPrescriptionDocId());

        PrescriptionEntity prescription = PrescriptionEntity.builder()
                .treatmentEntity(treatment)
                .patientEntity(patient)
                .prescriptionDoc(doctor)
                .prescriptionType(request.getPrescriptionType())
                .prescriptionDate(LocalDateTime.now())
                .prescriptionStatus(PrescriptionStatus.PENDING)
                .prescriptionMemo(request.getPrescriptionMemo())
                .build();

        if (request.getPrescriptionItems() != null && !request.getPrescriptionItems().isEmpty()) {
            if (drugValidationEnabled && request.getPrescriptionItems().size() > 1) {
                List<String> drugCodes = request.getPrescriptionItems().stream()
                        .map(PrescriptionItemCreateRequest::getDrugCode)
                        .filter(code -> code != null && !code.isBlank())
                        .collect(Collectors.toList());
                
                if (drugCodes.size() > 1) {
                    List<String> interactionWarnings = drugInfoService.checkDrugInteractions(drugCodes);
                    if (!interactionWarnings.isEmpty()) {
                        log.warn("약물 상호작용 경고: prescriptionId={}, warnings={}", 
                                prescription.getPrescriptionId(), interactionWarnings.size());
                    }
                }
            }
            
            List<PrescriptionItemEntity> items = request.getPrescriptionItems().stream()
                    .map(itemRequest -> createPrescriptionItemWithValidation(itemRequest, prescription))
                    .collect(Collectors.toList());
            items.forEach(prescription::addItem);
        }

        return prescriptionRepository.save(prescription);
    }

    private PrescriptionItemEntity createPrescriptionItem(PrescriptionItemCreateRequest request, PrescriptionEntity prescription) {
        PrescriptionItemEntity item = PrescriptionItemEntity.builder()
                .prescriptionEntity(prescription)
                .drugCode(request.getDrugCode())
                .drugName(request.getDrugName())
                .dosage(request.getDosage())
                .dose(request.getDose())
                .frequency(request.getFrequency())
                .days(request.getDays())
                .totalQuantity(request.getTotalQuantity())
                .unit(request.getUnit())
                .specialNote(request.getSpecialNote())
                .build();
        item.calculateTotalQuantity();
        return item;
    }
    
    private PrescriptionItemEntity createPrescriptionItemWithValidation(
            PrescriptionItemCreateRequest request, PrescriptionEntity prescription) {
        
        String specialNote = request.getSpecialNote();
        
        if (drugValidationEnabled && request.getDrugCode() != null && !request.getDrugCode().isBlank()) {
            try {
                DrugInfoItemResponse drugInfo = drugInfoService.getDrugInfoByItemSeq(request.getDrugCode());
                
                if (drugInfo != null) {
                    if (specialNote == null || specialNote.isBlank()) {
                        specialNote = drugInfoService.buildSpecialNote(drugInfo);
                    } else {
                        String drugInfoNote = drugInfoService.buildSpecialNote(drugInfo);
                        if (drugInfoNote != null && !drugInfoNote.isBlank()) {
                            specialNote = specialNote + "\n\n" + drugInfoNote;
                        }
                    }
                    
                    if ((request.getDosage() == null || request.getDosage().isBlank()) 
                            && drugInfo.getUseMethodQesitm() != null 
                            && !drugInfo.getUseMethodQesitm().isBlank()) {
                        request.setDosage(drugInfo.getUseMethodQesitm());
                    }
                    
                    log.debug("약물 정보 검증 완료: drugCode={}, drugName={}", 
                            request.getDrugCode(), drugInfo.getItemName());
                } else {
                    log.warn("약물 정보를 찾을 수 없습니다: drugCode={}", request.getDrugCode());
                }
            } catch (Exception e) {
                log.error("약물 정보 검증 중 오류 발생: drugCode={}, error={}", 
                        request.getDrugCode(), e.getMessage(), e);
            }
        }
        
        PrescriptionItemEntity item = PrescriptionItemEntity.builder()
                .prescriptionEntity(prescription)
                .drugCode(request.getDrugCode())
                .drugName(request.getDrugName())
                .dosage(request.getDosage())
                .dose(request.getDose())
                .frequency(request.getFrequency())
                .days(request.getDays())
                .totalQuantity(request.getTotalQuantity())
                .unit(request.getUnit())
                .specialNote(specialNote)
                .build();
        item.calculateTotalQuantity();
        return item;
    }

    @Transactional
    public PrescriptionEntity updatePrescription(Long prescriptionId, PrescriptionUpdateRequest request) {
        PrescriptionEntity prescription = getPrescriptionById(prescriptionId);

        if (!prescription.isPending()) {
            throw new BusinessException("대기 상태인 처방만 수정할 수 있습니다.");
        }

        if (request.getPrescriptionMemo() != null && !request.getPrescriptionMemo().isBlank()) {
            prescription.update(request.getPrescriptionMemo());
        }

        if (request.getPrescriptionItems() != null && !request.getPrescriptionItems().isEmpty()) {
            if (drugValidationEnabled && request.getPrescriptionItems().size() > 1) {
                List<String> drugCodes = request.getPrescriptionItems().stream()
                        .map(PrescriptionItemCreateRequest::getDrugCode)
                        .filter(code -> code != null && !code.isBlank())
                        .collect(Collectors.toList());
                
                if (drugCodes.size() > 1) {
                    List<String> interactionWarnings = drugInfoService.checkDrugInteractions(drugCodes);
                    if (!interactionWarnings.isEmpty()) {
                        log.warn("약물 상호작용 경고: prescriptionId={}, warnings={}", 
                                prescription.getPrescriptionId(), interactionWarnings.size());
                    }
                }
            }
            
            prescription.getPrescriptionItems().clear();
            List<PrescriptionItemEntity> items = request.getPrescriptionItems().stream()
                    .map(itemRequest -> createPrescriptionItemWithValidation(itemRequest, prescription))
                    .collect(Collectors.toList());
            items.forEach(prescription::addItem);
        }

        return prescriptionRepository.save(prescription);
    }

    @Transactional
    public void dispensePrescription(Long prescriptionId) {
        PrescriptionEntity prescription = getPrescriptionById(prescriptionId);
        prescription.dispense();
        prescriptionRepository.save(prescription);
    }

    @Transactional
    public void cancelPrescription(Long prescriptionId, String cancelReason) {
        PrescriptionEntity prescription = getPrescriptionById(prescriptionId);
        prescription.cancel(cancelReason);
        prescriptionRepository.save(prescription);
    }
}
