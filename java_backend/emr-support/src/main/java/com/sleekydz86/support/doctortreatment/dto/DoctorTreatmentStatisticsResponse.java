package com.sleekydz86.support.doctortreatment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorTreatmentStatisticsResponse {

    private Long userId;
    private String doctorName;
    private Long totalTreatments;
    private Long totalDuration;
    private Double averageDuration;
    private List<DailyStatistics> dailyStatistics;
}
