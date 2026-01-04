package com.sleekydz86.support.doctortreatment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class DailyStatistics {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private Long count;
    private Long totalDuration;
    
    public DailyStatistics(LocalDate date, Long count, Long totalDuration) {
        this.date = date;
        this.count = count;
        this.totalDuration = totalDuration;
    }
}

