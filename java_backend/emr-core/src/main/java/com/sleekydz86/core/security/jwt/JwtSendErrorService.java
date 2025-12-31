package com.sleekydz86.core.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class JwtSendErrorService {

    private final ObjectMapper objectMapper;

    public void sendErrorResponseProcess(
            HttpServletResponse response, ErrorCode errorCode, int status)
            throws IOException {
        String path = getRequestPath();

        CustomErrorResponse errorResponse = CustomErrorResponse.of(
                errorCode.getStatus(),
                errorCode.getMessage(),
                errorCode.name(),
                path
        );

        String body = objectMapper.writeValueAsString(errorResponse);
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(body);
    }

    private String getRequestPath() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRequestURI();
        }
        return "unknown";
    }
}
