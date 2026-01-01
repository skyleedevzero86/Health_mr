package com.sleekydz86.domain.patient.service.generators;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.domain.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SequenceBasedGenerator implements PatientNumberGenerator {

    private final PatientRepository patientRepository;
    private static final String PREFIX = "N";
    private static final int MAX_NUMBER = 9999999;

    @Override
    @Transactional
    public Long generate() {

        Long maxNumber = patientRepository.findTopByOrderByPatientNoDesc()
                .map(PatientEntity::getPatientNo)
                .map(no -> {

                    String noStr = String.valueOf(no);
                    if (noStr.startsWith(PREFIX)) {
                        return Long.parseLong(noStr.substring(1));
                    }
                    return no;
                })
                .orElse(0L);

        if (maxNumber >= MAX_NUMBER) {
            throw new IllegalStateException("환자 번호 생성 한도에 도달했습니다.");
        }

        Long nextNumber = maxNumber + 1;

        return Long.parseLong(PREFIX + String.format("%07d", nextNumber));
    }
}

