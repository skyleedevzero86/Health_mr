package com.sleekydz86.finance.payment.service;

import com.sleekydz86.domain.patient.entity.PatientEntity;
import com.sleekydz86.emrclinical.treatment.entity.TreatmentEntity;
import com.sleekydz86.finance.contract.entity.ContractEntity;
import com.sleekydz86.finance.contract.entity.ContractRelayEntity;
import com.sleekydz86.finance.contract.repository.ContractRelayRepository;
import com.sleekydz86.finance.medicalfee.repository.MedicalFeeRepository;
import com.sleekydz86.finance.payment.dto.PaymentCalculationResult;
import com.sleekydz86.finance.qualification.dto.AllQualificationsResponse;
import com.sleekydz86.finance.qualification.service.QualificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PaymentCalculationService {

    private final MedicalFeeRepository medicalFeeRepository;
    private final QualificationService qualificationService;
    private final ContractRelayRepository contractRelayRepository;

    public PaymentCalculationResult calculatePaymentAmount(TreatmentEntity treatment, PatientEntity patient) {

        Long totalMedicalFee = calculateTotalMedicalFee(treatment.getTreatmentId());

        AllQualificationsResponse qualifications = qualificationService.getAllQualifications(patient.getPatientNo())
                .block();

        double selfPayRate = calculateSelfPayRate(qualifications);

        Long selfPay = Math.round(totalMedicalFee * selfPayRate);

        Long insuranceMoney = totalMedicalFee - selfPay;

        Long discountAmount = calculateContractDiscount(patient.getPatientNo(), selfPay);

        Long finalSelfPay = selfPay - discountAmount;
        if (finalSelfPay < 0) {
            finalSelfPay = 0L;
        }

        return PaymentCalculationResult.builder()
                .totalAmount(totalMedicalFee)
                .selfPay(finalSelfPay)
                .insuranceMoney(insuranceMoney)
                .discountAmount(discountAmount)
                .build();
    }

    private Long calculateTotalMedicalFee(Long treatmentId) {
        Long totalFee = medicalFeeRepository.getTotalMedicalFeeByTreatmentId(treatmentId);
        return totalFee != null ? totalFee : 0L;
    }

    private double calculateSelfPayRate(AllQualificationsResponse qualifications) {
        if (qualifications == null) {
            return 1.0;
        }

        if (qualifications.getBasicLivelihood() != null &&
                qualifications.getBasicLivelihood().getEligible()) {
            return 0.0;
        }

        if (qualifications.getMedicalAssistance() != null &&
                qualifications.getMedicalAssistance().getEligible()) {
            String type = qualifications.getMedicalAssistance().getType();
            if ("1종".equals(type)) {
                return 0.0;
            } else if ("2종".equals(type)) {
                return 0.05;
            }
        }

        if (qualifications.getHealthInsurance() != null &&
                qualifications.getHealthInsurance().getEligible()) {
            String type = qualifications.getHealthInsurance().getType();
            if ("일반".equals(type)) {
                return 0.2;
            } else if ("의원".equals(type)) {
                return 0.3;
            } else if ("약국".equals(type)) {
                return 0.5;
            }
        }

        return 1.0;
    }

    private Long calculateContractDiscount(Long patientNo, Long selfPay) {
        List<ContractRelayEntity> activeContracts = contractRelayRepository
                .findActiveContractRelaysByPatientNo(patientNo);

        if (activeContracts.isEmpty()) {
            return 0L;
        }

        ContractEntity contract = activeContracts.get(0).getContractEntity();
        Long discountRate = contract.getContractDiscount();

        if (discountRate == null || discountRate == 0) {
            return 0L;
        }

        return Math.round(selfPay * discountRate / 100.0);
    }
}

