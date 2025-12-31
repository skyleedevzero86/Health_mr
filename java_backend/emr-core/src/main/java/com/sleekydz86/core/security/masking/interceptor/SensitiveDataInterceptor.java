package com.sleekydz86.core.security.masking.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
@RequiredArgsConstructor
public class SensitiveDataInterceptor implements ResponseBodyAdvice<Object> {

    private final DataMaskingService dataMaskingService;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        if (body == null) {
            return body;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean applyMasking = shouldApplyMasking(authentication);

        if (applyMasking) {
            if (body instanceof Iterable) {
                ((Iterable<?>) body).forEach(item -> dataMaskingService.maskSensitiveFields(item, true));
            } else {
                dataMaskingService.maskSensitiveFields(body, true);
            }
        }

        return body;
    }

    private boolean shouldApplyMasking(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return true;
        }

        return !authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}

