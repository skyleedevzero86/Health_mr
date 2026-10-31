package com.sleekydz86.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = EmrSupportApplicationTests.TestApp.class)
@ActiveProfiles("test")
class EmrSupportApplicationTests {

    @SpringBootApplication(scanBasePackages = "com.sleekydz86")
    static class TestApp {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }

        @Bean
        @SuppressWarnings("rawtypes")
        RedisTemplate redisTemplate() {
            return Mockito.mock(RedisTemplate.class);
        }

        @Bean
        StringRedisTemplate stringRedisTemplate() {
            return Mockito.mock(StringRedisTemplate.class);
        }
    }

    @Test
    @DisplayName("운영지원 모듈 애플리케이션 컨텍스트가 정상적으로 기동된다")
    void contextLoads() {
        // given 테스트 프로필과 로컬 H2 설정이 적용된 상태
        // when 스프링 컨텍스트를 기동한다
        // then 빈 생성 과정에서 예외가 발생하지 않는다
    }

}
