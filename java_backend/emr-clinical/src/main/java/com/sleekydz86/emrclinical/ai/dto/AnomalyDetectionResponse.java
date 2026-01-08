package com.sleekydz86.emrclinical.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetectionResponse {
    
    private List<Anomaly> anomalies;
    private Integer totalDetected;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Anomaly {
        private String type;
        private LocalDate date;
        private Integer count;
        private String message;
    }
}

