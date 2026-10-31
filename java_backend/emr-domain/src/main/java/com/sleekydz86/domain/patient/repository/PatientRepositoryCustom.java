package com.sleekydz86.domain.patient.repository;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PatientRepositoryCustom {
    List<PatientEntity> searchPatients(String name, String telHmac, String emailHmac);
    Page<PatientEntity> searchPatientsWithPaging(String name, String telHmac, String emailHmac, Pageable pageable);
}
