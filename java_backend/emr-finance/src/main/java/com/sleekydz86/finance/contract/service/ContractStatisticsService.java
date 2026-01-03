package com.sleekydz86.finance.contract.service;

import com.sleekydz86.finance.contract.dto.ContractStatistics;
import com.sleekydz86.finance.contract.entity.ContractEntity;
import com.sleekydz86.finance.contract.entity.ContractRelayEntity;
import com.sleekydz86.finance.contract.repository.ContractRelayRepository;
import com.sleekydz86.finance.contract.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ContractStatisticsService {

    private final ContractRepository contractRepository;
    private final ContractRelayRepository contractRelayRepository;

    public ContractStatistics getContractStatistics() {
        List<ContractEntity> contracts = contractRepository.findAll();
        List<ContractRelayEntity> relays = contractRelayRepository.findAll();

        Map<Long, Long> patientCountByContract = relays.stream()
                .filter(r -> r.getContractEntity() != null && r.getIsActive())
                .collect(Collectors.groupingBy(
                        r -> r.getContractEntity().getContractCode(),
                        Collectors.counting()
                ));

        // 계약처별 할인 실제 사용 시 Payment 정보 필요 금액 합계
        Map<Long, Long> discountAmountByContract = new HashMap<>();

        Map<Long, Long> usageFrequencyByContract = relays.stream()
                .filter(r -> r.getContractEntity() != null && r.getIsActive())
                .collect(Collectors.groupingBy(
                        r -> r.getContractEntity().getContractCode(),
                        Collectors.counting()
                ));

        return ContractStatistics.builder()
                .patientCountByContract(patientCountByContract)
                .discountAmountByContract(discountAmountByContract)
                .usageFrequencyByContract(usageFrequencyByContract)
                .build();
    }

}

