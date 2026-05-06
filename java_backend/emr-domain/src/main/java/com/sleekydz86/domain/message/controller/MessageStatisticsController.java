package com.sleekydz86.domain.message.controller;

import com.sleekydz86.core.common.annotation.AuthRole;
import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.domain.message.dto.MessageStatisticsResponse;
import com.sleekydz86.domain.message.service.MessageStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/messages/statistics")
@RequiredArgsConstructor
public class MessageStatisticsController {

    private final MessageStatisticsService messageStatisticsService;

    @GetMapping("/daily")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getDailyStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        MessageStatisticsResponse response = messageStatisticsService.getDailyStatistics(date);
        return ResponseEntity.ok(Map.of(
                "message", "일별 통계 조회 성공",
                "data", response
        ));
    }

    @GetMapping("/weekly")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getWeeklyStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        MessageStatisticsResponse response = messageStatisticsService.getWeeklyStatistics(startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "message", "주별 통계 조회 성공",
                "data", response
        ));
    }

    @GetMapping("/monthly")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getMonthlyStatistics(
            @RequestParam int year,
            @RequestParam int month) {
        MessageStatisticsResponse response = messageStatisticsService.getMonthlyStatistics(year, month);
        return ResponseEntity.ok(Map.of(
                "message", "월별 통계 조회 성공",
                "data", response
        ));
    }

    @GetMapping("/user")
    @AuthRole
    public ResponseEntity<Map<String, Object>> getUserStatistics(
            @AuthUser Long userId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        MessageStatisticsResponse response = messageStatisticsService.getUserStatistics(userId, startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "message", "사용자별 통계 조회 성공",
                "data", response
        ));
    }

    @GetMapping("/admin")
    @AuthRole(roles = {"ADMIN"})
    public ResponseEntity<Map<String, Object>> getAdminStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        MessageStatisticsResponse response = messageStatisticsService.getAdminStatistics(startDate, endDate);
        return ResponseEntity.ok(Map.of(
                "message", "관리자 통계 조회 성공",
                "data", response
        ));
    }
}

