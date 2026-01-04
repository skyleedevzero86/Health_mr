package com.sleekydz86.support.recess.controller;

import com.sleekydz86.domain.user.type.RoleType;
import com.sleekydz86.support.recess.dto.RecessRequest;
import com.sleekydz86.support.recess.dto.RecessResponse;
import com.sleekydz86.support.recess.service.RecessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recess")
@RequiredArgsConstructor
public class RecessController {

    private final RecessService recessService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> register(@RequestBody RecessRequest req) {
        try {
            RecessResponse dto = recessService.registerRecess(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "휴진 등록 성공",
                    "data", dto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴진 등록 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody RecessRequest req) {
        try {
            RecessResponse dto = recessService.updateRecess(id, req);
            return ResponseEntity.ok(Map.of(
                    "message", "휴진 수정 성공",
                    "data", dto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴진 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            recessService.deleteRecess(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of(
                    "message", "휴진 삭제 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴진 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam RoleType role) {
        try {
            List<RecessResponse> list = recessService.listByRole(role);

            if (list.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of(
                        "message", "조회된 휴진이 없습니다."
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "휴진 목록 조회 성공",
                    "data", list
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴진 목록 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

