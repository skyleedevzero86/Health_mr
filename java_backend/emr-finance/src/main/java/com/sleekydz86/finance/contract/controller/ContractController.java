package com.sleekydz86.finance.contract.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.file.excel.export.ExcelExportService;
import com.sleekydz86.finance.contract.dto.*;
import com.sleekydz86.finance.contract.entity.ContractEntity;
import com.sleekydz86.finance.contract.repository.ContractRepository;
import com.sleekydz86.finance.contract.service.ContractService;
import com.sleekydz86.finance.contract.service.ContractStatisticsService;
import com.sleekydz86.finance.type.ContractStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contract")
@RequiredArgsConstructor
public class ContractController {

    private final ContractService contractService;
    private final ContractStatisticsService contractStatisticsService;
    private final ExcelExportService excelExportService;
    private final ContractRepository contractRepository;

    @GetMapping("/list")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Page<ContractResponse>> getAllContracts(Pageable pageable) {
        Page<ContractResponse> contracts = contractService.getAllContracts(pageable);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/{contractCode}")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Map<String, Object>> getContract(@PathVariable Long contractCode) {
        ContractDetailResponse response = contractService.getContractDetailByCode(contractCode);
        return ResponseEntity.ok(Map.of(
                "message", "계약처 조회 성공",
                "data", response
        ));
    }

    @GetMapping("/status/{status}")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Page<ContractResponse>> getContractsByStatus(
            @PathVariable ContractStatus status,
            Pageable pageable) {
        Page<ContractResponse> contracts = contractService.getContractsByStatus(status, pageable);
        return ResponseEntity.ok(contracts);
    }

    @GetMapping("/search")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Map<String, Object>> searchContracts(@RequestParam String keyword) {
        List<ContractResponse> contracts = contractService.searchContracts(keyword);
        return ResponseEntity.ok(Map.of(
                "message", "계약처 검색 성공",
                "data", contracts
        ));
    }

    @PostMapping("/create")
    @AuthRole({"ADMIN"})
    public ResponseEntity<Map<String, Object>> createContract(@Valid @RequestBody ContractRequest request) {
        ContractResponse response = contractService.createContract(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "계약처 생성 성공",
                "data", response
        ));
    }

    @PutMapping("/{contractCode}")
    @AuthRole({"ADMIN"})
    public ResponseEntity<Map<String, Object>> updateContract(
            @PathVariable Long contractCode,
            @Valid @RequestBody ContractUpdateRequest request) {
        ContractResponse response = contractService.updateContract(contractCode, request);
        return ResponseEntity.ok(Map.of(
                "message", "계약처 정보 수정 성공",
                "data", response
        ));
    }

    @PostMapping("/delete")
    @AuthRole({"ADMIN"})
    public ResponseEntity<Map<String, Object>> deleteContract(@RequestParam Long contractCode) {
        contractService.deleteContract(contractCode);
        return ResponseEntity.ok(Map.of(
                "message", "계약처 삭제 성공"
        ));
    }

    @PostMapping("/{contractCode}/activate")
    @AuthRole({"ADMIN"})
    public ResponseEntity<Map<String, Object>> activateContract(@PathVariable Long contractCode) {
        contractService.activateContract(contractCode);
        return ResponseEntity.ok(Map.of(
                "message", "계약처 활성화 성공"
        ));
    }

    @PostMapping("/{contractCode}/deactivate")
    @AuthRole({"ADMIN"})
    public ResponseEntity<Map<String, Object>> deactivateContract(@PathVariable Long contractCode) {
        contractService.deactivateContract(contractCode);
        return ResponseEntity.ok(Map.of(
                "message", "계약처 비활성화 성공"
        ));
    }

    @GetMapping("/statistics")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Map<String, Object>> getContractStatistics() {
        ContractStatistics statistics =
                contractStatisticsService.getContractStatistics();
        return ResponseEntity.ok(Map.of(
                "message", "계약처 통계 조회 성공",
                "data", statistics));
    }

    @GetMapping("/export")
    @AuthRole({"STAFF", "ADMIN"})
    public void exportContracts(HttpServletResponse response) throws IOException {

        List<ContractEntity> contracts = contractRepository.findAll();

        List<String> headers = List.of(
                "계약처코드", "계약처명", "계약관계", "전화번호", "할인율",
                "계약상태", "담당자", "담당자전화", "담당자이메일", "생성일시"
        );

        List<Map<String, Object>> data = new ArrayList<>();
        for (ContractEntity contract : contracts) {
            Map<String, Object> row = new HashMap<>();
            row.put("계약처코드", contract.getContractCode());
            row.put("계약처명", contract.getContractName());
            row.put("계약관계", contract.getContractRelationship());
            row.put("전화번호", contract.getContractTelephoneValue());
            row.put("할인율", contract.getContractDiscount() != null ? contract.getContractDiscount() : 0);
            row.put("계약상태", contract.getContractStatus() != null ? contract.getContractStatus().name() : "");
            row.put("담당자", contract.getContractManager());
            row.put("담당자전화", contract.getContractManagerTelValue());
            row.put("담당자이메일", contract.getContractManagerEmailValue());
            row.put("생성일시", contract.getCreatedDate() != null ? contract.getCreatedDate().toString() : "");
            data.add(row);
        }

        String filename = "계약처목록_" + java.time.LocalDate.now() + ".xlsx";

        excelExportService.exportToExcel(headers, data, filename, response);
    }
}

