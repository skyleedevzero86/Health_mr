package com.sleekydz86.core.lock.strategy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

@Slf4j
@Component
public class HeaderKeyGenerator implements LockKeyGenerator {

    @Override
    public String generate(String headerName, Method method, Object[] args) {
        try {
            ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String headerValue = request.getHeader(headerName);
                
                if (headerValue != null && !headerValue.isEmpty()) {
                    return headerValue;
                }
            }
        } catch (Exception e) {
            log.warn("HTTP 헤더에서 키 가져오기 실패: headerName={}", headerName, e);
        }
        
        return "header:" + headerName;
    }
}
