package com.sleekydz86.support.disability.service;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import com.sleekydz86.domain.patient.service.PatientService;
import com.sleekydz86.support.disability.dto.DisabilityRegisterRequest;
import com.sleekydz86.support.disability.dto.DisabilityResponse;
import com.sleekydz86.support.disability.dto.DisabilityUpdateRequest;
import com.sleekydz86.support.disability.entity.DisabilityEntity;
import com.sleekydz86.support.disability.repository.DisabilityRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisabilityService {

    private final DisabilityRepository disabilityRepository;
    private final PatientRepository patientRepository;
    private final PatientService patientService;

    @Transactional
    public DisabilityEntity registerDisability(DisabilityRegisterRequest request) {

        PatientEntity patient = patientService.getPatientByNo(request.getPatientNo());
        if (patient == null) {
            throw new IllegalStateException("환자를 찾을 수 없습니다.");
        }

      DisabilityEntity existingDisability = disabilityRepository.findByPatientEntity(patient);
        if (existingDisability != null) {
            throw new IllegalStateException("이미 해당 환자에 대한 장애인 정보가 등록되어 있습니다.");
        }

        Boolean needsAssistiveDevice = "Y".equalsIgnoreCase(request.getDisabilityDeviceYN()) || 
                                        "true".equalsIgnoreCase(request.getDisabilityDeviceYN());
        
        DisabilityEntity disabilityEntity = DisabilityEntity.builder()
                .patientEntity(patient)
                .disabilityGrade(request.getDisabilityGrade())
                .disabilityType(request.getDisabilityType())
                .needsAssistiveDevice(needsAssistiveDevice)
                .disabilityDeviceType(request.getDisabilityDeviceType())
                .build();

        return disabilityRepository.save(disabilityEntity);
    }

    public DisabilityResponse readDisabilityByPatientNo(Long patientNo) {
        Optional<DisabilityEntity> disabilityEntityOptional =
                disabilityRepository.findByPatientEntity_PatientNo(patientNo);

        if (disabilityEntityOptional.isPresent()) {
            DisabilityEntity disabilityEntity = disabilityEntityOptional.get();
            PatientEntity patient = disabilityEntity.getPatientEntity();

            return new DisabilityResponse(
                    disabilityEntity.getDisabilityId(),
                    patient.getPatientNo(),
                    patient.getPatientName(),
                    disabilityEntity.getDisabilityGrade(),
                    disabilityEntity.getDisabilityType(),
                    disabilityEntity.getDisabilityDeviceYNValue(),
                    disabilityEntity.getDisabilityDeviceType()
            );
        }
        return null;
    }

    public List<DisabilityResponse> readAllDisabilities() {
        List<DisabilityEntity> disabilityEntities = disabilityRepository.findAll();

        return disabilityEntities.stream()
                .map(disabilityEntity -> {
                    PatientEntity patient = disabilityEntity.getPatientEntity();
                    return new DisabilityResponse(
                            disabilityEntity.getDisabilityId(),
                            patient.getPatientNo(),
                            patient.getPatientName(),
                            disabilityEntity.getDisabilityGrade(),
                            disabilityEntity.getDisabilityType(),
                            disabilityEntity.getDisabilityDeviceYNValue(),
                            disabilityEntity.getDisabilityDeviceType()
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public DisabilityEntity updateDisability(Long patientNo, DisabilityUpdateRequest request) {
        PatientEntity patientEntity = patientRepository.findByPatientNo(patientNo)
                .orElseThrow(() -> new RuntimeException("해당 환자 정보가 없습니다."));

        DisabilityEntity disabilityEntity = disabilityRepository.findByPatientEntity(patientEntity);
        if (disabilityEntity == null) {
            throw new RuntimeException("해당 장애인 정보가 없습니다.");
        }

        Boolean needsAssistiveDevice;
        if (request.getDisabilityDeviceYN() != null && !request.getDisabilityDeviceYN().isBlank()) {
            needsAssistiveDevice = "Y".equalsIgnoreCase(request.getDisabilityDeviceYN()) || 
                                   "true".equalsIgnoreCase(request.getDisabilityDeviceYN());
        } else {
            needsAssistiveDevice = disabilityEntity.getNeedsAssistiveDevice();
        }
        
        DisabilityEntity updatedEntity = DisabilityEntity.builder()
                .disabilityId(disabilityEntity.getDisabilityId())
                .patientEntity(patientEntity)
                .disabilityGrade(request.getDisabilityGrade() != null && !request.getDisabilityGrade().isBlank()
                        ? request.getDisabilityGrade()
                        : disabilityEntity.getDisabilityGrade())
                .disabilityType(request.getDisabilityType() != null && !request.getDisabilityType().isBlank()
                        ? request.getDisabilityType()
                        : disabilityEntity.getDisabilityType())
                .needsAssistiveDevice(needsAssistiveDevice)
                .disabilityDeviceType(request.getDisabilityDeviceType() != null && !request.getDisabilityDeviceType().isBlank()
                        ? request.getDisabilityDeviceType()
                        : disabilityEntity.getDisabilityDeviceType())
                .build();

        return disabilityRepository.save(updatedEntity);
    }

    @Transactional
    public DisabilityResponse deleteDisability(Long patientNo) {

        PatientEntity patientEntity = patientRepository.findByPatientNo(patientNo)
                .orElseThrow(() -> new RuntimeException("해당 환자 정보가 없습니다."));

   DisabilityEntity disabilityEntity = disabilityRepository.findByPatientEntity(patientEntity);
        if (disabilityEntity == null) {
            throw new RuntimeException("해당 장애인 정보가 없습니다.");
        }

        PatientEntity patient = disabilityEntity.getPatientEntity();
        DisabilityResponse deletedDisability = new DisabilityResponse(
                disabilityEntity.getDisabilityId(),
                patient.getPatientNo(),
                patient.getPatientName(),
                disabilityEntity.getDisabilityGrade(),
                disabilityEntity.getDisabilityType(),
                disabilityEntity.getDisabilityDeviceYNValue(),
                disabilityEntity.getDisabilityDeviceType()
        );

        disabilityRepository.delete(disabilityEntity);

        return deletedDisability;
    }
}