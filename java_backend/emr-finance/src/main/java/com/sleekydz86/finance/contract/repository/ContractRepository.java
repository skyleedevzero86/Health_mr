package com.sleekydz86.finance.contract.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.finance.contract.entity.ContractRelayEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRelayRepository extends BaseRepository<ContractRelayEntity, Long> {

    List<ContractRelayEntity> findByPatientEntity_PatientNo(Long patientNo);

    List<ContractRelayEntity> findByContractEntity_ContractCode(Long contractCode);

    Optional<ContractRelayEntity> findByPatientEntity_PatientNoAndContractEntity_ContractCode(
            Long patientNo, Long contractCode);

    List<ContractRelayEntity> findByIsActive(Boolean isActive);

    Page<ContractRelayEntity> findAll(Pageable pageable);

    Page<ContractRelayEntity> findByPatientEntity_PatientNo(Long patientNo, Pageable pageable);

    List<ContractRelayEntity> findAllByOrderByCreatedDateDesc();

    @Query("SELECT cr FROM ContractRelayEntity cr " +
            "WHERE cr.patientEntity.patientNo = :patientNo AND cr.isActive = true")
    List<ContractRelayEntity> findActiveContractRelaysByPatientNo(@Param("patientNo") Long patientNo);
}

