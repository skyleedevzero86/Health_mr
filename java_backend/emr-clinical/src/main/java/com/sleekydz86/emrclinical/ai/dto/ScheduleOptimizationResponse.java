package com.sleekydz86.emrclinical.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleOptimizationResponse {
    
    private LocalDate date;
    private Integer totalTreatments;
    private Map<String, List<Long>> doctorSchedule;
    private List<Suggestion> suggestions;
    private String optimization;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Suggestion {
        private String type;
        private String doctor;
        private String message;
        private String recommendation;
    }
}

