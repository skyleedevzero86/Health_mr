package com.sleekydz86.support.equipment.controller;

import com.sleekydz86.support.equipment.dto.EquipmentRegisterRequest;
import com.sleekydz86.support.equipment.dto.EquipmentResponse;
import com.sleekydz86.support.equipment.dto.EquipmentUpdateRequest;
import com.sleekydz86.support.equipment.entity.EquipmentEntity;
import com.sleekydz86.support.equipment.service.EquipmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerEquipment(@RequestBody EquipmentRegisterRequest request) {
        try {
            EquipmentEntity responseData = equipmentService.registerEquipment(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "등록 성공",
                    "data", responseData
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "등록 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/read/{equipmentId}")
    public ResponseEntity<Map<String, Object>> viewEquipment(@PathVariable Long equipmentId) {
        try {
            EquipmentResponse equipmentResponse = equipmentService.readEquipment(equipmentId);
            if (equipmentResponse == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "장비 정보를 찾을 수 없습니다."));
            }

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "조회 성공",
                    "data", equipmentResponse
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "조회 실패",
                    "error", e.getMessage()
            ));
        }
    }


    @GetMapping("/read/name/{equipmentName}")
    public ResponseEntity<Map<String, Object>> getEquipmentByEquipmentName(@PathVariable String equipmentName) {
        try {
            List<EquipmentResponse> equipmentResponses = equipmentService.readEquipmentByEquipmentName(equipmentName);
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "장비명 조회 성공",
                    "data", equipmentResponses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "장비명 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/read/all")
    public ResponseEntity<Map<String, Object>> getAllEquipmentInfo() {
        try {
            List<EquipmentResponse> equipmentResponses = equipmentService.readAllEquipment();
            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "전체 조회 성공",
                    "data", equipmentResponses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "전체 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/update/{equipmentId}")
    public ResponseEntity<Map<String, Object>> updateEquipment(
            @PathVariable Long equipmentId,
            @RequestBody EquipmentUpdateRequest request) {

        try {
            EquipmentEntity updatedData = equipmentService.updateEquipment(equipmentId, request);

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "수정 성공",
                    "data", updatedData
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/delete/{equipmentId}")
    public ResponseEntity<Map<String, Object>> deleteEquipment(@PathVariable Long equipmentId) {
        try {
            EquipmentResponse deletedEquipment = equipmentService.deleteEquipment(equipmentId);

            return ResponseEntity.status(HttpStatus.OK).body(Map.of(
                    "message", "삭제 성공",
                    "deletedEquipment", deletedEquipment
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }
}