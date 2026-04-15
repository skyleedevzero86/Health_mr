package com.sleekydz86.finance.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@Profile("clickhouse")
@EnableConfigurationProperties(PaymentClickHouseStatisticsProperties.class)
public class PaymentClickHouseStatisticsConfig {

    @Bean(name = "paymentClickhouseDataSource")
    @ConfigurationProperties(prefix = "payment.clickhouse-statistics.datasource")
    public DataSource paymentClickhouseDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "paymentClickhouseNamedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate paymentClickhouseNamedParameterJdbcTemplate(
            @Qualifier("paymentClickhouseDataSource") DataSource paymentClickhouseDataSource) {
        return new NamedParameterJdbcTemplate(paymentClickhouseDataSource);
    }
}
