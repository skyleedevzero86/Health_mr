package com.sleekydz86.domain.patient.service;

import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.core.security.encryption.BlindIndexService;
import com.sleekydz86.domain.common.service.BaseService;
import com.sleekydz86.domain.common.valueobject.PatientNumber;
import com.sleekydz86.domain.patient.dto.PatientRegisterRequest;
import com.sleekydz86.domain.patient.dto.PatientSearchRequest;
import com.sleekydz86.domain.patient.dto.PatientUpdateRequest;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.patient.service.generators.PatientNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService implements BaseService<PatientEntity, Long> {

    private final PatientRepository patientRepository;
    private final PatientNumberGenerator patientNumberGenerator;
    private final EventPublisher eventPublisher;
    private final BlindIndexService blindIndexService;

    public PatientEntity getPatientByNo(Long patientNo) {
        return patientRepository.findByPatientNo(patientNo)
                .orElseThrow(() -> new NotFoundException("환자를 찾을 수 없습니다. PatientNo: " + patientNo));
    }

    public List<PatientEntity> searchPatientsByName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("환자 이름은 필수입니다.");
        }
        return patientRepository.findByPatientName(name);
    }

    public List<PatientEntity> searchPatients(PatientSearchRequest request) {
        return patientRepository.searchPatients(
                request.getName(),
                blindIndexService.hmacPhone(request.getTel()),
                blindIndexService.hmacEmail(request.getEmail())
        );
    }

    public Page<PatientEntity> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable);
    }

    public List<PatientEntity> getRecentPatients(int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        return patientRepository.findRecentPatients(since);
    }

    public String findPatientRrnByPatientNo(Long patientNo) {
        return patientRepository.findByPatientNo(patientNo)
                .map(PatientEntity::getPatientRrnValue)
                .orElseThrow(() -> new NotFoundException("해당 환자의 주민번호를 찾을 수 없습니다. patientNo: " + patientNo));
    }

    @Transactional
    public PatientEntity registerPatient(PatientRegisterRequest request) {

        validateNotDuplicate(patientRepository.existsByRrnHmac(blindIndexService.hmacRrn(request.getPatientRrn())),
                "이미 등록된 주민등록번호입니다.");
        if ("Y".equals(request.getPatientForeign())
                && request.getPatientPassport() != null
                && !request.getPatientPassport().isBlank()) {
            validateNotDuplicate(
                    patientRepository.existsByPassportHmac(blindIndexService.hmacPassport(request.getPatientPassport())),
                    "이미 등록된 여권번호입니다.");
        }

        Long patientNoValue = patientNumberGenerator.generate();
        PatientNumber patientNo = PatientNumber.of(patientNoValue);

        PatientEntity patient = request.toEntity(patientNo);
        patient.updateLastVisit();

        PatientEntity savedPatient = patientRepository.save(patient);

        eventPublisher.publish(new com.sleekydz86.core.event.domain.PatientRegisteredEvent(
                savedPatient.getPatientNoValue(),
                savedPatient.getPatientName(),
                savedPatient.getPatientRrnValue()
        ));

        return savedPatient;
    }

    @Transactional
    public PatientEntity updatePatient(Long patientNo, PatientUpdateRequest request) {
        PatientEntity patient = getPatientByNo(patientNo);

        patient.updateInfo(
                request.getPatientName(),
                request.getPatientAddress(),
                request.getEmailValueObject(),
                request.getTelValueObject()
        );

        if (request.getPatientEmail() != null) {
            patient.changeEmail(request.getEmailValueObject());
        }

        if (request.getPatientTel() != null) {
            patient.changePhoneNumber(request.getTelValueObject());
        }

        if (request.getPatientHypassYN() != null) {
            patient.setHypass("Y".equals(request.getPatientHypassYN()));
        }

        if (request.getGuardian() != null) {
            patient.setGuardian(request.getGuardian());
        }

        PatientEntity updatedPatient = patientRepository.save(patient);

        eventPublisher.publish(new com.sleekydz86.core.event.domain.PatientUpdatedEvent(
                updatedPatient.getPatientNoValue(),
                updatedPatient.getPatientName()
        ));

        return updatedPatient;
    }

    @Transactional
    public void deletePatient(Long patientNo) {
        PatientEntity patient = getPatientByNo(patientNo);
        patientRepository.delete(patient);
    }
}

