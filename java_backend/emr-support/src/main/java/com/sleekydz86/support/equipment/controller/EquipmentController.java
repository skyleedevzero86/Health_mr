package com.sleekydz86.support.equipment.controller;

import com.sleekydz86.support.equipment.dto.EquipmentRegisterRequest;
import com.sleekydz86.support.equipment.dto.EquipmentResponse;
import com.sleekydz86.support.equipment.dto.EquipmentUpdateRequest;
import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import com.sleekydz86.support.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerEquipment(@RequestBody EquipmentRegisterRequest request) {
        EquipmentEntity responseData = equipmentService.registerEquipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "등록 성공",
                "data", responseData
        ));
    }

    @GetMapping("/read/{equipmentId}")
    public ResponseEntity<Map<String, Object>> viewEquipment(@PathVariable Long equipmentId) {
        EquipmentResponse equipmentResponse = equipmentService.readEquipment(equipmentId);
        return ResponseEntity.ok(Map.of(
                "message", "조회 성공",
                "data", equipmentResponse
        ));
    }

    @GetMapping("/read/name/{equipmentName}")
    public ResponseEntity<Map<String, Object>> getEquipmentByEquipmentName(@PathVariable String equipmentName) {
        List<EquipmentResponse> equipmentResponses = equipmentService.readEquipmentByEquipmentName(equipmentName);
        return ResponseEntity.ok(Map.of(
                "message", "장비명 조회 성공",
                "data", equipmentResponses
        ));
    }

    @GetMapping("/read/all")
    public ResponseEntity<Map<String, Object>> getAllEquipmentInfo() {
        List<EquipmentResponse> equipmentResponses = equipmentService.readAllEquipment();
        return ResponseEntity.ok(Map.of(
                "message", "전체 조회 성공",
                "data", equipmentResponses
        ));
    }

    @PostMapping("/update/{equipmentId}")
    public ResponseEntity<Map<String, Object>> updateEquipment(
            @PathVariable Long equipmentId,
            @RequestBody EquipmentUpdateRequest request) {
        EquipmentEntity updatedData = equipmentService.updateEquipment(equipmentId, request);
        return ResponseEntity.ok(Map.of(
                "message", "수정 성공",
                "data", updatedData
        ));
    }

    @PostMapping("/delete/{equipmentId}")
    public ResponseEntity<Map<String, Object>> deleteEquipment(@PathVariable Long equipmentId) {
        EquipmentResponse deletedEquipment = equipmentService.deleteEquipment(equipmentId);
        return ResponseEntity.ok(Map.of(
                "message", "삭제 성공",
                "deletedEquipment", deletedEquipment
        ));
    }
}