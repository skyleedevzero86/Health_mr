package com.sleekydz86.finance.contract.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.finance.contract.dto.ContractRelayDetailResponse;
import com.sleekydz86.finance.contract.dto.ContractRelayRequest;
import com.sleekydz86.finance.contract.dto.ContractRelayResponse;
import com.sleekydz86.finance.contract.dto.ContractRelayUpdateRequest;
import com.sleekydz86.finance.contract.service.ContractRelayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contract/relay")
@RequiredArgsConstructor
public class ContractRelayController {

    private final ContractRelayService contractRelayService;

    @PostMapping
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Map<String, Object>> createContractRelay(
            @Valid @RequestBody ContractRelayRequest request) {
        ContractRelayResponse response = contractRelayService.createContractRelay(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "연결 생성 성공",
                "data", response
        ));
    }

    @GetMapping("/{contractRelayId}")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Map<String, Object>> getContractRelay(@PathVariable Long contractRelayId) {
        ContractRelayDetailResponse response = contractRelayService.getContractRelayDetailById(contractRelayId);
        return ResponseEntity.ok(Map.of(
                "message", "연결 조회 성공",
                "data", response
        ));
    }

    @GetMapping
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Page<ContractRelayResponse>> getAllContractRelays(Pageable pageable) {
        Page<ContractRelayResponse> relays = contractRelayService.getAllContractRelays(pageable);
        return ResponseEntity.ok(relays);
    }

    @GetMapping("/patient/{patientNo}")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getContractRelaysByPatientNo(@PathVariable Long patientNo) {
        List<ContractRelayResponse> relays = contractRelayService.getContractRelaysByPatientNo(patientNo);
        return ResponseEntity.ok(Map.of(
                "message", "연결 조회 성공",
                "data", relays
        ));
    }

    @GetMapping("/patient/{patientNo}/active")
    @AuthRole({"STAFF", "ADMIN", "DOCTOR"})
    public ResponseEntity<Map<String, Object>> getActiveContractRelaysByPatientNo(@PathVariable Long patientNo) {
        List<ContractRelayResponse> relays = contractRelayService.getActiveContractRelaysByPatientNo(patientNo);
        return ResponseEntity.ok(Map.of(
                "message", "활성화된 연결 조회 성공",
                "data", relays
        ));
    }

    @GetMapping("/contract/{contractCode}")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Page<ContractRelayResponse>> getContractRelaysByContractCode(
            @PathVariable Long contractCode,
            Pageable pageable) {
        Page<ContractRelayResponse> relays = contractRelayService.getContractRelaysByContractCode(contractCode, pageable);
        return ResponseEntity.ok(relays);
    }

    @PutMapping("/{contractRelayId}")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Map<String, Object>> updateContractRelay(
            @PathVariable Long contractRelayId,
            @Valid @RequestBody ContractRelayUpdateRequest request) {
        ContractRelayResponse response = contractRelayService.updateContractRelay(contractRelayId, request);
        return ResponseEntity.ok(Map.of(
                "message", "연결 수정 성공",
                "data", response
        ));
    }

    @DeleteMapping("/{contractRelayId}")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Map<String, Object>> deleteContractRelay(@PathVariable Long contractRelayId) {
        contractRelayService.deleteContractRelay(contractRelayId);
        return ResponseEntity.ok(Map.of(
                "message", "연결 삭제 성공"
        ));
    }

    @PostMapping("/{contractRelayId}/activate")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Map<String, Object>> activateContractRelay(@PathVariable Long contractRelayId) {
        contractRelayService.activateContractRelay(contractRelayId);
        return ResponseEntity.ok(Map.of(
                "message", "연결 활성화 성공"
        ));
    }

    @PostMapping("/{contractRelayId}/deactivate")
    @AuthRole({"STAFF", "ADMIN"})
    public ResponseEntity<Map<String, Object>> deactivateContractRelay(@PathVariable Long contractRelayId) {
        contractRelayService.deactivateContractRelay(contractRelayId);
        return ResponseEntity.ok(Map.of(
                "message", "연결 비활성화 성공"
        ));
    }
}