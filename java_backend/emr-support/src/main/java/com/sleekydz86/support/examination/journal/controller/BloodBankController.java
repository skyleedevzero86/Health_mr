package com.sleekydz86.support.examination.journal.controller;

import com.sleekydz86.support.examination.journal.dto.BloodBankRegisterRequest;
import com.sleekydz86.support.examination.journal.dto.BloodBankResponse;
import com.sleekydz86.support.examination.journal.dto.BloodBankUpdateRequest;
import com.sleekydz86.support.examination.journal.service.BloodBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examination/blood-bank")
@RequiredArgsConstructor
public class BloodBankController {

    private final BloodBankService bloodBankService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerBloodBank(@RequestBody BloodBankRegisterRequest request) {
        try {
            BloodBankResponse response = bloodBankService.registerBloodBank(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "혈액은행 정보가 등록되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "혈액은행 정보 등록 실패",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "혈액은행 정보 등록 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{bloodBankId}")
    public ResponseEntity<Map<String, Object>> getBloodBank(@PathVariable Long bloodBankId) {
        try {
            BloodBankResponse response = bloodBankService.getBloodBank(bloodBankId);
            return ResponseEntity.ok(Map.of(
                    "message", "혈액은행 정보 조회 성공",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "혈액은행 정보 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/patient/{patientNo}")
    public ResponseEntity<Map<String, Object>> getBloodBanksByPatient(@PathVariable Long patientNo) {
        try {
            List<BloodBankResponse> responses = bloodBankService.getBloodBanksByPatient(patientNo);
            return ResponseEntity.ok(Map.of(
                    "message", "환자별 혈액은행 정보 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "환자별 혈액은행 정보 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/blood-type/{bloodType}")
    public ResponseEntity<Map<String, Object>> getBloodBanksByBloodType(@PathVariable String bloodType) {
        try {
            List<BloodBankResponse> responses = bloodBankService.getBloodBanksByBloodType(bloodType);
            return ResponseEntity.ok(Map.of(
                    "message", "혈액형별 혈액은행 정보 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "혈액형별 혈액은행 정보 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{bloodBankId}")
    public ResponseEntity<Map<String, Object>> updateBloodBank(
            @PathVariable Long bloodBankId,
            @RequestBody BloodBankUpdateRequest request) {
        try {
            BloodBankResponse response = bloodBankService.updateBloodBank(bloodBankId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "혈액은행 정보가 수정되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "혈액은행 정보 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{bloodBankId}")
    public ResponseEntity<Map<String, Object>> deleteBloodBank(@PathVariable Long bloodBankId) {
        try {
            BloodBankResponse response = bloodBankService.deleteBloodBank(bloodBankId);
            return ResponseEntity.ok(Map.of(
                    "message", "혈액은행 정보가 삭제되었습니다.",
                    "deletedBloodBank", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "혈액은행 정보 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

