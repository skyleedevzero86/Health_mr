package com.sleekydz86.domain.patient.repository;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PatientRepositoryCustom {
    List<PatientEntity> searchPatients(String name, String tel, String email);
    Page<PatientEntity> searchPatientsWithPaging(String name, String tel, String email, Pageable pageable);
}
