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
        RecessResponse dto = recessService.registerRecess(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "휴진 등록 성공",
                "data", dto
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody RecessRequest req) {
        RecessResponse dto = recessService.updateRecess(id, req);
        return ResponseEntity.ok(Map.of(
                "message", "휴진 수정 성공",
                "data", dto
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        recessService.deleteRecess(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of(
                "message", "휴진 삭제 성공"
        ));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam RoleType role) {
        List<RecessResponse> list = recessService.listByRole(role);
        return ResponseEntity.ok(Map.of(
                "message", "휴진 목록 조회 성공",
                "data", list
        ));
    }
}

