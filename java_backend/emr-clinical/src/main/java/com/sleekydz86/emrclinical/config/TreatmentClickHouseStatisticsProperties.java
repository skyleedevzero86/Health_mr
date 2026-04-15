package com.sleekydz86.emrclinical.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "treatment.clickhouse-statistics")
public class TreatmentClickHouseStatisticsProperties {

    private int defaultRangeDays = 7;
    private int maxRangeDays = 366;
}
