package com.sleekydz86.core.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
                "com.sleekydz86.domain",
                "com.sleekydz86.emrclinical",
                "com.sleekydz86.finance",
                "com.sleekydz86.support",
                "com.sleekydz86.core"
}, entityManagerFactoryRef = "mysqlEntityManagerFactory", transactionManagerRef = "mysqlTransactionManager")
public class MySQLDataSourceConfig {

        @Primary
        @Bean(name = "mysqlDataSource")
        @ConfigurationProperties(prefix = "spring.datasource.mysql")
        public DataSource mysqlDataSource() {
                return DataSourceBuilder.create()
                                .type(HikariDataSource.class)
                                .build();
        }

        @Primary
        @Bean(name = "mysqlEntityManagerFactory")
        public LocalContainerEntityManagerFactoryBean mysqlEntityManagerFactory(
                        EntityManagerFactoryBuilder builder,
                        @Qualifier("mysqlDataSource") DataSource dataSource) {
                Map<String, String> properties = new HashMap<>();
                properties.put("hibernate.hbm2ddl.auto", "validate");
                properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
                properties.put("hibernate.show_sql", "true");
                properties.put("hibernate.format_sql", "true");

                return builder
                                .dataSource(dataSource)
                                .packages(
                                                "com.sleekydz86.domain",
                                                "com.sleekydz86.emrclinical",
                                                "com.sleekydz86.finance",
                                                "com.sleekydz86.support",
                                                "com.sleekydz86.core")
                                .persistenceUnit("mysql")
                                .properties(properties)
                                .build();
        }

        @Primary
        @Bean(name = "mysqlTransactionManager")
        public PlatformTransactionManager mysqlTransactionManager(
                        @Qualifier("mysqlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
                return new JpaTransactionManager(entityManagerFactory);
        }
}
