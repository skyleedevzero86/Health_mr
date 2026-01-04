package com.sleekydz86.support.holiday.controller;

import com.sleekydz86.support.holiday.dto.HolidayRequest;
import com.sleekydz86.support.holiday.dto.HolidayResponse;
import com.sleekydz86.support.holiday.service.HolidayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/holiday")
@RequiredArgsConstructor
public class HolidayController {

    private final HolidayService holidayService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> register(@RequestBody HolidayRequest req) {
        try {
            HolidayResponse dto = holidayService.registerHoliday(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "휴일 등록 성공",
                    "data", dto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴일 등록 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Long id,
            @RequestBody HolidayRequest req) {
        try {
            HolidayResponse dto = holidayService.updateHoliday(id, req);
            return ResponseEntity.ok(Map.of(
                    "message", "휴일 수정 성공",
                    "data", dto
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴일 수정 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            holidayService.deleteHoliday(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of(
                    "message", "휴일 삭제 성공"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴일 삭제 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{period}/{number}")
    public ResponseEntity<Map<String, Object>> list(
            @PathVariable String period,
            @PathVariable String number) {
        try {
            List<HolidayResponse> list = holidayService.listByPeriod(period, number);

            if (list.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of(
                        "message", "조회된 휴일이 없습니다."
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "휴일 목록 조회 성공",
                    "data", list
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "message", "휴일 목록 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

