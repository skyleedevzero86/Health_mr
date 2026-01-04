package com.sleekydz86.support.examination.journal.controller;

import com.sleekydz86.support.examination.journal.dto.ExaminationJournalRegisterRequest;
import com.sleekydz86.support.examination.journal.dto.ExaminationJournalResponse;
import com.sleekydz86.support.examination.journal.dto.ExaminationJournalUpdateRequest;
import com.sleekydz86.support.examination.journal.service.ExaminationJournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/examination/journal")
@RequiredArgsConstructor
public class ExaminationJournalController {

    private final ExaminationJournalService journalService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerJournal(@RequestBody ExaminationJournalRegisterRequest request) {
        try {
            ExaminationJournalResponse response = journalService.registerJournal(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "검사 일지가 등록되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "검사 일지 등록 실패",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "검사 일지 등록 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{journalId}")
    public ResponseEntity<Map<String, Object>> getJournal(@PathVariable Long journalId) {
        try {
            ExaminationJournalResponse response = journalService.getJournal(journalId);
            return ResponseEntity.ok(Map.of(
                    "message", "검사 일지 조회 성공",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "message", "검사 일지 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/patient/{patientNo}")
    public ResponseEntity<Map<String, Object>> getJournalsByPatient(@PathVariable Long patientNo) {
        try {
            List<ExaminationJournalResponse> responses = journalService.getJournalsByPatient(patientNo);
            return ResponseEntity.ok(Map.of(
                    "message", "환자별 검사 일지 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "환자별 검사 일지 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/examination/{examinationId}")
    public ResponseEntity<Map<String, Object>> getJournalsByExamination(@PathVariable Long examinationId) {
        try {
            List<ExaminationJournalResponse> responses = journalService.getJournalsByExamination(examinationId);
            return ResponseEntity.ok(Map.of(
                    "message", "검사별 검사 일지 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "검사별 검사 일지 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/treatment/{treatmentId}")
    public ResponseEntity<Map<String, Object>> getJournalsByTreatment(@PathVariable Long treatmentId) {
        try {
            List<ExaminationJournalResponse> responses = journalService.getJournalsByTreatment(treatmentId);
            return ResponseEntity.ok(Map.of(
                    "message", "진료별 검사 일지 조회 성공",
                    "data", responses
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "진료별 검사 일지 조회 중 오류가 발생했습니다.",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{journalId}")
    public ResponseEntity<Map<String, Object>> updateJournal(
            @PathVariable Long journalId,
            @RequestBody ExaminationJournalUpdateRequest request) {
        try {
            ExaminationJournalResponse response = journalService.updateJournal(journalId, request);
            return ResponseEntity.ok(Map.of(
                    "message", "검사 일지가 수정되었습니다.",
                    "data", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "검사 일지 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{journalId}")
    public ResponseEntity<Map<String, Object>> deleteJournal(@PathVariable Long journalId) {
        try {
            ExaminationJournalResponse response = journalService.deleteJournal(journalId);
            return ResponseEntity.ok(Map.of(
                    "message", "검사 일지가 삭제되었습니다.",
                    "deletedJournal", response
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "검사 일지 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

