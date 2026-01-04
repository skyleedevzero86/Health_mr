package com.sleekydz86.support.equipment.journal.controller;

import com.sleekydz86.support.equipment.journal.dto.EquipmentJournalRegisterRequest;
import com.sleekydz86.support.equipment.journal.dto.EquipmentJournalResponse;
import com.sleekydz86.support.equipment.journal.dto.EquipmentJournalUpdateRequest;
import com.sleekydz86.support.equipment.journal.service.EquipmentJournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/equipment/journal")
@RequiredArgsConstructor
public class EquipmentJournalController {

    private final EquipmentJournalService journalService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerJournal(@RequestBody EquipmentJournalRegisterRequest request) {
        try {
            EquipmentJournalResponse response = journalService.registerJournal(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "장비 점검 일지가 등록되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "장비 점검 일지 등록 실패",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "장비 점검 일지 등록 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{journalId}")
    public ResponseEntity<Map<String, Object>> getJournal(@PathVariable Long journalId) {
        try {
            EquipmentJournalResponse response = journalService.getJournal(journalId);
            return ResponseEntity.ok(Map.of(
                    "message", "장비 점검 일지 조회 성공",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "장비 점검 일지 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<Map<String, Object>> getJournalsByEquipment(@PathVariable Long equipmentId) {
        try {
            List<EquipmentJournalResponse> responses = journalService.getJournalsByEquipment(equipmentId);
            return ResponseEntity.ok(Map.of(
                    "message", "장비별 점검 일지 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "장비별 점검 일지 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getJournalsByUser(@PathVariable Long userId) {
        try {
            List<EquipmentJournalResponse> responses = journalService.getJournalsByUser(userId);
            return ResponseEntity.ok(Map.of(
                    "message", "사용자별 점검 일지 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "사용자별 점검 일지 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<Map<String, Object>> getJournalsByDate(@PathVariable LocalDate date) {
        try {
            List<EquipmentJournalResponse> responses = journalService.getJournalsByDate(date);
            return ResponseEntity.ok(Map.of(
                    "message", "날짜별 점검 일지 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "날짜별 점검 일지 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/equipment/{equipmentId}/period")
    public ResponseEntity<Map<String, Object>> getJournalsByEquipmentAndPeriod(
            @PathVariable Long equipmentId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {
        try {
            List<EquipmentJournalResponse> responses = journalService.getJournalsByEquipmentAndPeriod(equipmentId, start, end);
            return ResponseEntity.ok(Map.of(
                    "message", "장비별 기간별 점검 일지 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "장비별 기간별 점검 일지 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{journalId}")
    public ResponseEntity<Map<String, Object>> updateJournal(
            @PathVariable Long journalId,
            @RequestBody EquipmentJournalUpdateRequest request) {
        try {
            EquipmentJournalResponse response = journalService.updateJournal(journalId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "장비 점검 일지가 수정되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "장비 점검 일지 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{journalId}")
    public ResponseEntity<Map<String, Object>> deleteJournal(@PathVariable Long journalId) {
        try {
            EquipmentJournalResponse response = journalService.deleteJournal(journalId);
            return ResponseEntity.ok(Map.of(
                    "message", "장비 점검 일지가 삭제되었습니다.",
                    "deletedJournal", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "장비 점검 일지 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

