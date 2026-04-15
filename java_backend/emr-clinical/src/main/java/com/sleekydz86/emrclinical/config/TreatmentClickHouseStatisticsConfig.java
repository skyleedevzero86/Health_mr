package com.sleekydz86.emrclinical.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(TreatmentClickHouseStatisticsProperties.class)
public class TreatmentClickHouseStatisticsConfig {

    @Bean(name = "treatmentClickhouseDataSource")
    @ConfigurationProperties(prefix = "treatment.clickhouse-statistics.datasource")
    public DataSource treatmentClickhouseDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "treatmentClickhouseNamedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate treatmentClickhouseNamedParameterJdbcTemplate(
            @Qualifier("treatmentClickhouseDataSource") DataSource treatmentClickhouseDataSource) {
        return new NamedParameterJdbcTemplate(treatmentClickhouseDataSource);
    }
}
