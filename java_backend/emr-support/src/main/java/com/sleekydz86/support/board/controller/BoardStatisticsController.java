package com.sleekydz86.support.board.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.support.board.dto.response.BoardStatisticsResponse;
import com.sleekydz86.support.board.service.BoardStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/boards/statistics")
@RequiredArgsConstructor
public class BoardStatisticsController {

    private final BoardStatisticsService boardStatisticsService;

    @GetMapping("/daily")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        try {
            BoardStatisticsResponse response = boardStatisticsService.getDailyStatistics(date);
            return ResponseEntity.ok(Map.of(
                    "message", "일별 통계 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "일별 통계 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/weekly")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getWeeklyStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            BoardStatisticsResponse response = boardStatisticsService.getWeeklyStatistics(startDate, endDate);
            return ResponseEntity.ok(Map.of(
                    "message", "주별 통계 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "주별 통계 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/monthly")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getMonthlyStatistics(
            @RequestParam int year,
            @RequestParam int month) {
        try {
            BoardStatisticsResponse response = boardStatisticsService.getMonthlyStatistics(year, month);
            return ResponseEntity.ok(Map.of(
                    "message", "월별 통계 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "월별 통계 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/user")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            BoardStatisticsResponse response = boardStatisticsService.getUserStatistics(userId, startDate, endDate);
            return ResponseEntity.ok(Map.of(
                    "message", "사용자별 통계 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "사용자별 통계 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/admin")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> getAdminStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        try {
            BoardStatisticsResponse response = boardStatisticsService.getAdminStatistics(startDate, endDate);
            return ResponseEntity.ok(Map.of(
                    "message", "관리자 통계 조회 성공",
                    "data", response
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "관리자 통계 조회 실패",
                    "error", e.getMessage()
            ));
        }
    }
}

