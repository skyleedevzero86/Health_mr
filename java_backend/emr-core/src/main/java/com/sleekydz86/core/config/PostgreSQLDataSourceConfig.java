package com.sleekydz86.core.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableJpaRepositories(basePackages = "com.sleekydz86.external.postgresql", entityManagerFactoryRef = "postgresqlEntityManagerFactory", transactionManagerRef = "postgresqlTransactionManager")
public class PostgreSQLDataSourceConfig {

        @Bean(name = "postgresqlDataSource")
        @ConfigurationProperties(prefix = "spring.datasource.postgresql")
        public DataSource postgresqlDataSource() {
                return DataSourceBuilder.create()
                                .type(HikariDataSource.class)
                                .build();
        }

        @Bean(name = "postgresqlEntityManagerFactory")
        public LocalContainerEntityManagerFactoryBean postgresqlEntityManagerFactory(
                        EntityManagerFactoryBuilder builder,
                        @Qualifier("postgresqlDataSource") DataSource dataSource) {
                Map<String, String> properties = new HashMap<>();
                properties.put("hibernate.hbm2ddl.auto", "none");
                properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
                properties.put("hibernate.show_sql", "true");
                properties.put("hibernate.format_sql", "true");

                return builder
                                .dataSource(dataSource)
                                .packages("com.sleekydz86.external.postgresql")
                                .persistenceUnit("postgresql")
                                .properties(properties)
                                .build();
        }

        @Bean(name = "postgresqlTransactionManager")
        public PlatformTransactionManager postgresqlTransactionManager(
                        @Qualifier("postgresqlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
                return new JpaTransactionManager(entityManagerFactory);
        }
}
