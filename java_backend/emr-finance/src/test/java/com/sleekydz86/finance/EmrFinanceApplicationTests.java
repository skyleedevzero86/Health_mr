package com.sleekydz86.finance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = EmrFinanceApplicationTests.TestApp.class)
@ActiveProfiles("test")
class EmrFinanceApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @SpringBootApplication(scanBasePackages = "com.sleekydz86")
    static class TestApp {
        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            return Mockito.mock(RedisConnectionFactory.class);
        }

        @Bean
        StringRedisTemplate stringRedisTemplate() {
            return Mockito.mock(StringRedisTemplate.class);
        }
    }

    @Test
    @DisplayName("재무 모듈 애플리케이션 컨텍스트가 정상적으로 기동된다")
    void contextLoads() {
        // given
        String[] activeProfiles = environment.getActiveProfiles();
        String mysqlJdbcUrl = environment.getProperty("spring.datasource.mysql.url");

        // when
        int beanCount = applicationContext.getBeanDefinitionCount();

        // then
        assertThat(activeProfiles).contains("test");
        assertThat(mysqlJdbcUrl).contains("jdbc:h2:mem:");
        assertThat(applicationContext).isNotNull();
        assertThat(beanCount).isPositive();
        assertThat(applicationContext.getBean(ObjectMapper.class)).isNotNull();
    }

}
