package com.sleekydz86.core.config;

import com.sleekydz86.core.common.exception.AuthUserResolver;
import com.sleekydz86.core.common.interceptor.AuthInterceptor;
import com.sleekydz86.core.common.interceptor.LoggingInterceptor;
import com.sleekydz86.core.security.masking.interceptor.SensitiveDataInterceptor;
import com.sleekydz86.core.security.ratelimit.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AuthUserResolver authUserResolver;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final SensitiveDataInterceptor sensitiveDataInterceptor;
    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/swagger-ui/**", "/api-docs/**", "/actuator/**");

        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/**", "/swagger-ui/**", "/api-docs/**", "/actuator/**");

        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/**", "/swagger-ui/**", "/api-docs/**", "/actuator/**");

    }


    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserResolver);
    }
}

