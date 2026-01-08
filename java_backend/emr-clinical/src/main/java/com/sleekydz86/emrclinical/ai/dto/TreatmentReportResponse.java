package com.sleekydz86.emrclinical.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TreatmentReportResponse {
    
    private String title;
    private String period;
    private LocalDateTime generatedAt;
    private Summary summary;
    private Statistics statistics;
    private List<DoctorStat> topDoctors;
    private String content;
    private String format;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Integer totalTreatments;
        private Integer totalPatients;
        private Integer totalDoctors;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Map<String, Integer> byType;
        private Map<String, Integer> byDepartment;
        private Map<String, Integer> byStatus;
    }
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorStat {
        private String doctorName;
        private Integer count;
    }
}

