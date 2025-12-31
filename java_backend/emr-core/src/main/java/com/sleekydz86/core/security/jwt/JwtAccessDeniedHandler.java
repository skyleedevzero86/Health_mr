package com.sleekydz86.core.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final JwtSendErrorService jwtSendErrorService;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        jwtSendErrorService.sendErrorResponseProcess(
                response,
                ErrorCode.ACCESS_FORBIDDEN,
                ErrorCode.ACCESS_FORBIDDEN.getStatus()
        );
    }
}