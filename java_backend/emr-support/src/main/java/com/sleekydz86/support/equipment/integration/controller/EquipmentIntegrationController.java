package com.sleekydz86.support.equipment.integration.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.support.equipment.integration.dto.EquipmentIntegrationRequest;
import com.sleekydz86.support.equipment.integration.dto.EquipmentIntegrationResponse;
import com.sleekydz86.support.equipment.integration.service.EquipmentIntegrationService;
import com.sleekydz86.support.equipment.integration.type.IntegrationProtocol;
import com.sleekydz86.support.equipment.integration.type.IntegrationStatus;
import com.sleekydz86.support.equipment.integration.type.IntegrationType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment/integration")
@RequiredArgsConstructor
public class EquipmentIntegrationController {

    private final EquipmentIntegrationService integrationService;


    @PostMapping("/register")
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<Map<String, Object>> registerIntegration(@RequestBody EquipmentIntegrationRequest request) {
        try {
            EquipmentIntegrationResponse response = integrationService.registerIntegration(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "장비 연동이 등록되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "장비 연동 등록 실패",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "장비 연동 등록 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{integrationId}")
    @AuthRole()
    public ResponseEntity<Map<String, Object>> getIntegration(@PathVariable Long integrationId) {
        try {
            EquipmentIntegrationResponse response = integrationService.getIntegration(integrationId);
            return ResponseEntity.ok(Map.of(
                    "message", "장비 연동 조회 성공",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "장비 연동 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/equipment/{equipmentId}")
    @AuthRole()
    public ResponseEntity<Map<String, Object>> getIntegrationByEquipmentId(@PathVariable Long equipmentId) {
        try {
            EquipmentIntegrationResponse response = integrationService.getIntegrationByEquipmentId(equipmentId);
            return ResponseEntity.ok(Map.of(
                    "message", "장비 연동 조회 성공",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "장비 연동 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/type/{type}")
    @AuthRole()
    public ResponseEntity<Map<String, Object>> getIntegrationsByType(@PathVariable IntegrationType type) {
        try {
            List<EquipmentIntegrationResponse> responses = integrationService.getIntegrationsByType(type);
            return ResponseEntity.ok(Map.of(
                    "message", "장비 연동 목록 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "장비 연동 목록 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/protocol/{protocol}")
    @AuthRole()
    public ResponseEntity<Map<String, Object>> getIntegrationsByProtocol(@PathVariable IntegrationProtocol protocol) {
        try {
            List<EquipmentIntegrationResponse> responses = integrationService.getIntegrationsByProtocol(protocol);
            return ResponseEntity.ok(Map.of(
                    "message", "장비 연동 목록 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "장비 연동 목록 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/{integrationId}/status")
    @AuthRole(roles = {RoleType.ADMIN})
    public ResponseEntity<Map<String, Object>> updateIntegrationStatus(
            @PathVariable Long integrationId,
            @RequestParam IntegrationStatus status) {
        try {
            EquipmentIntegrationResponse response = integrationService.updateIntegrationStatus(integrationId, status);
            return ResponseEntity.ok(Map.of(
                    "message", "장비 연동 상태가 변경되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "장비 연동 상태 변경 실패",
                    "error", e.getMessage()
            ));
        }
    }
}