package com.sleekydz86.finance.contract.entity;

import com.sleekydz86.domain.common.entity.BaseEntity;
import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.finance.type.ContractStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;


@Entity(name = "Contract")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContractEntity extends BaseEntity {

    @Id
    @Column(name = "contract_code", nullable = false, unique = true)
    @NotNull(message = "계약처 코드는 필수입니다.")
    private Long contractCode;

    @Column(name = "contract_name", nullable = false)
    @NotBlank(message = "계약처명은 필수입니다.")
    private String contractName;

    @Column(name = "contract_relationship", nullable = false)
    @NotBlank(message = "계약 관계는 필수입니다.")
    private String contractRelationship;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "contract_telephone", length = 20))
    private PhoneNumber contractTelephone;

    @Column(name = "contract_discount")
    @Min(value = 0, message = "할인율은 0 이상이어야 합니다.")
    @Max(value = 100, message = "할인율은 100 이하여야 합니다.")
    private Long contractDiscount;

    @Column(name = "contract_status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ContractStatus contractStatus;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "contract_manager", length = 50)
    private String contractManager;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "contract_manager_tel", length = 20))
    private PhoneNumber contractManagerTel;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "contract_manager_email", length = 100))
    private Email contractManagerEmail;

    @Builder
    private ContractEntity(
            Long contractCode,
            String contractName,
            String contractRelationship,
            PhoneNumber contractTelephone,
            Long contractDiscount,
            ContractStatus contractStatus,
            LocalDate contractStartDate,
            LocalDate contractEndDate,
            String contractManager,
            PhoneNumber contractManagerTel,
            Email contractManagerEmail
    ) {
        validate(contractCode, contractName, contractRelationship, contractDiscount);
        this.contractCode = contractCode;
        this.contractName = contractName;
        this.contractRelationship = contractRelationship;
        this.contractTelephone = contractTelephone;
        this.contractDiscount = contractDiscount;
        this.contractStatus = contractStatus != null ? contractStatus : ContractStatus.ACTIVE;
        this.contractStartDate = contractStartDate;
        this.contractEndDate = contractEndDate;
        this.contractManager = contractManager;
        this.contractManagerTel = contractManagerTel;
        this.contractManagerEmail = contractManagerEmail;
    }

    private void validate(Long contractCode, String contractName, String contractRelationship, Long contractDiscount) {
        if (contractCode == null) {
            throw new IllegalArgumentException("계약처 코드는 필수입니다.");
        }
        if (contractName == null || contractName.isBlank()) {
            throw new IllegalArgumentException("계약처명은 필수입니다.");
        }
        if (contractRelationship == null || contractRelationship.isBlank()) {
            throw new IllegalArgumentException("계약 관계는 필수입니다.");
        }
        if (contractDiscount != null && (contractDiscount < 0 || contractDiscount > 100)) {
            throw new IllegalArgumentException("할인율은 0 이상 100 이하여야 합니다: " + contractDiscount);
        }
    }

    public void activate() {
        this.contractStatus = ContractStatus.ACTIVE;
    }

    public void deactivate() {
        this.contractStatus = ContractStatus.INACTIVE;
    }

    public void suspend() {
        this.contractStatus = ContractStatus.SUSPENDED;
    }

    public void updateContractPeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("계약 시작일은 종료일보다 이전이어야 합니다.");
        }
        this.contractStartDate = startDate;
        this.contractEndDate = endDate;
    }

    public void updateDiscount(Long discount) {
        if (discount == null) {
            this.contractDiscount = null;
            return;
        }
        if (discount < 0 || discount > 100) {
            throw new IllegalArgumentException("할인율은 0 이상 100 이하여야 합니다: " + discount);
        }
        this.contractDiscount = discount;
    }

    public void updateManager(String managerName, PhoneNumber managerTel, Email managerEmail) {
        this.contractManager = managerName;
        this.contractManagerTel = managerTel;
        this.contractManagerEmail = managerEmail;
    }

    public boolean isActive() {
        return this.contractStatus == ContractStatus.ACTIVE;
    }

    public boolean isValidPeriod() {
        if (contractStartDate == null || contractEndDate == null) {
            return true;
        }
        LocalDate now = LocalDate.now();
        return !now.isBefore(contractStartDate) && !now.isAfter(contractEndDate);
    }

    public String getContractTelephoneValue() {
        return contractTelephone != null ? contractTelephone.getValue() : null;
    }

    public String getContractManagerTelValue() {
        return contractManagerTel != null ? contractManagerTel.getValue() : null;
    }

    public String getContractManagerEmailValue() {
        return contractManagerEmail != null ? contractManagerEmail.getValue() : null;
    }
}

