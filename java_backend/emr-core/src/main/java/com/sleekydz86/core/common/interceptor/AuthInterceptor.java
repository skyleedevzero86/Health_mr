package com.sleekydz86.core.common.interceptor;

import com.sleekydz86.core.common.annotation.AuthRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.Arrays;

@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        AuthRole authCheck = method.getMethodAnnotation(AuthRole.class);
        if (authCheck == null) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증 정보가 없습니다. 로그인 필요.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return false;
        }

        String[] allowedRoles = authCheck.value().length > 0 ? authCheck.value() : authCheck.roles();
        if (allowedRoles.length == 0) {

            return true;
        }

        boolean hasAccess = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .anyMatch(role -> Arrays.asList(allowedRoles).contains(role));

        if (!hasAccess) {
            log.warn("접근 거부: {} (요청 URI: {})", authentication.getAuthorities(), request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "권한이 없습니다.");
            return false;
        }

        return true;
    }
}
