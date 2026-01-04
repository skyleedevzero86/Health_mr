package com.sleekydz86.support.examination.journal.service;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.emrclinical.treatment.repository.TreatmentRepository;
import com.sleekydz86.support.examination.entity.BloodBankEntity;
import com.sleekydz86.support.examination.entity.ExaminationEntity;
import com.sleekydz86.support.examination.journal.dto.BloodBankRegisterRequest;
import com.sleekydz86.support.examination.journal.dto.BloodBankResponse;
import com.sleekydz86.support.examination.journal.dto.BloodBankUpdateRequest;
import com.sleekydz86.support.examination.journal.repository.BloodBankRepository;
import com.sleekydz86.support.examination.repository.ExaminationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BloodBankService {

    private final BloodBankRepository bloodBankRepository;
    private final ExaminationRepository examinationRepository;
    private final PatientRepository patientRepository;
    private final TreatmentRepository treatmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public BloodBankResponse registerBloodBank(BloodBankRegisterRequest request) {

        ExaminationEntity examination = examinationRepository.findByExaminationId(request.getExaminationId())
                .orElseThrow(() -> new IllegalArgumentException("검사 정보를 찾을 수 없습니다."));


        PatientEntity patient = patientRepository.findByPatientNo(request.getPatientNo())
                .orElseThrow(() -> new IllegalArgumentException("환자 정보를 찾을 수 없습니다."));

        TreatmentEntity treatment = treatmentRepository.findById(request.getTreatmentId())
                .orElseThrow(() -> new IllegalArgumentException("진료 정보를 찾을 수 없습니다."));

        UserEntity user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        BloodBankEntity bloodBank = BloodBankEntity.builder()
                .examinationEntity(examination)
                .patientEntity(patient)
                .treatmentEntity(treatment)
                .userEntity(user)
                .examinationTime(request.getExaminationTime())
                .bloodType(request.getBloodType())
                .build();

        BloodBankEntity saved = bloodBankRepository.save(bloodBank);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BloodBankResponse getBloodBank(Long bloodBankId) {
        BloodBankEntity bloodBank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new IllegalArgumentException("혈액은행 정보를 찾을 수 없습니다."));
        return toResponse(bloodBank);
    }

    @Transactional(readOnly = true)
    public List<BloodBankResponse> getBloodBanksByPatient(Long patientNo) {
        List<BloodBankEntity> bloodBanks = bloodBankRepository.findByPatientEntity_PatientNo(patientNo);
        return bloodBanks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BloodBankResponse> getBloodBanksByBloodType(String bloodType) {
        List<BloodBankEntity> bloodBanks = bloodBankRepository.findByBloodType(bloodType);
        return bloodBanks.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public BloodBankResponse updateBloodBank(Long bloodBankId, BloodBankUpdateRequest request) {
        BloodBankEntity bloodBank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new IllegalArgumentException("혈액은행 정보를 찾을 수 없습니다."));

        BloodBankEntity updated = bloodBank.toBuilder()
                .examinationTime(request.getExaminationTime() != null ? request.getExaminationTime() : bloodBank.getExaminationTime())
                .bloodType(request.getBloodType() != null ? request.getBloodType() : bloodBank.getBloodType())
                .build();

        BloodBankEntity saved = bloodBankRepository.save(updated);
        return toResponse(saved);
    }

    @Transactional
    public BloodBankResponse deleteBloodBank(Long bloodBankId) {
        BloodBankEntity bloodBank = bloodBankRepository.findById(bloodBankId)
                .orElseThrow(() -> new IllegalArgumentException("혈액은행 정보를 찾을 수 없습니다."));

        BloodBankResponse response = toResponse(bloodBank);
        bloodBankRepository.delete(bloodBank);
        return response;
    }

    private BloodBankResponse toResponse(BloodBankEntity bloodBank) {
        ExaminationEntity examination = bloodBank.getExaminationEntity();
        PatientEntity patient = bloodBank.getPatientEntity();
        TreatmentEntity treatment = bloodBank.getTreatmentEntity();
        UserEntity user = bloodBank.getUserEntity();

        return BloodBankResponse.builder()
                .bloodBankId(bloodBank.getBloodBankId())
                .examinationId(examination.getExaminationId())
                .examinationName(examination.getExaminationName())
                .patientNo(patient.getPatientNo())
                .patientName(patient.getPatientName())
                .treatmentId(treatment.getTreatmentId())
                .userId(user.getId())
                .userName(user.getName())
                .examinationTime(bloodBank.getExaminationTime())
                .bloodType(bloodBank.getBloodType())
                .build();
    }
}

