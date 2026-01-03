package com.sleekydz86.finance.contract.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.patient.entity.PatientEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity(name = "Contract_relay")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContractRelayEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_relay_id", nullable = false)
    private Long contractRelayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_no", referencedColumnName = "patient_no", nullable = false)
    @NotNull(message = "환자 정보는 필수입니다.")
    private PatientEntity patientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_code", referencedColumnName = "contract_code", nullable = false)
    @NotNull(message = "계약처 정보는 필수입니다.")
    private ContractEntity contractEntity;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "relay_start_date")
    private LocalDate relayStartDate;

    @Column(name = "relay_end_date")
    private LocalDate relayEndDate;

    @Builder
    private ContractRelayEntity(
            Long contractRelayId,
            PatientEntity patientEntity,
            ContractEntity contractEntity,
            Boolean isActive,
            LocalDate relayStartDate,
            LocalDate relayEndDate
    ) {
        validate(patientEntity, contractEntity, relayStartDate, relayEndDate);
        this.contractRelayId = contractRelayId;
        this.patientEntity = patientEntity;
        this.contractEntity = contractEntity;
        this.isActive = isActive != null ? isActive : true;
        this.relayStartDate = relayStartDate;
        this.relayEndDate = relayEndDate;
    }

    private void validate(PatientEntity patientEntity, ContractEntity contractEntity,
                          LocalDate relayStartDate, LocalDate relayEndDate) {
        if (patientEntity == null) {
            throw new IllegalArgumentException("환자 정보는 필수입니다.");
        }
        if (contractEntity == null) {
            throw new IllegalArgumentException("계약처 정보는 필수입니다.");
        }
        if (relayStartDate != null && relayEndDate != null && relayStartDate.isAfter(relayEndDate)) {
            throw new IllegalArgumentException("연결 시작일은 종료일보다 이전이어야 합니다.");
        }
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("연결 시작일은 종료일보다 이전이어야 합니다.");
        }
        this.relayStartDate = startDate;
        this.relayEndDate = endDate;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isValidPeriod() {
        if (relayStartDate == null || relayEndDate == null) {
            return true; // 기간이 설정되지 않으면 항상 유효
        }
        LocalDate now = LocalDate.now();
        return !now.isBefore(relayStartDate) && !now.isAfter(relayEndDate);
    }

    public boolean isFullyValid() {
        return isActive() && isValidPeriod();
    }
}

