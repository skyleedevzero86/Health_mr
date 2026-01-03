package com.sleekydz86.finance.contract.repository;

import com.sleekydz86.domain.common.repository.BaseRepository;
import com.sleekydz86.finance.contract.entity.ContractEntity;
import com.sleekydz86.finance.type.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends BaseRepository<ContractEntity, Long> {

    Optional<ContractEntity> findByContractCode(Long contractCode);

    boolean existsByContractCode(Long contractCode);

    List<ContractEntity> findByContractNameContaining(String name);

    List<ContractEntity> findByContractRelationship(String relationship);

    List<ContractEntity> findByContractStatus(ContractStatus status);

    Page<ContractEntity> findAll(Pageable pageable);

    Page<ContractEntity> findByContractStatus(ContractStatus status, Pageable pageable);

    List<ContractEntity> findAllByOrderByContractNameAsc();

    @Query("SELECT c FROM ContractEntity c " +
            "WHERE c.contractStatus = :status AND " +
            "(c.contractName LIKE %:keyword% OR c.contractRelationship LIKE %:keyword%)")
    List<ContractEntity> searchContracts(@Param("keyword") String keyword,
                                         @Param("status") ContractStatus status);
}
