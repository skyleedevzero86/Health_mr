package com.sleekydz86.core.common.exception;

import com.sleekydz86.core.common.annotation.AuthUser;
import com.sleekydz86.core.security.jwt.JwtUtil;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserResolver implements HandlerMethodArgumentResolver {

    private final JwtUtil jwtUtil;

    public AuthUserResolver(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        String header = webRequest.getHeader("Authorization");
        String token = jwtUtil.resolveToken(header);

        if (token == null) {
            throw new com.sleekydz86.core.security.jwt.CustomAuthenticationException("JWT 토큰이 없습니다.");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);

        return userId;
    }
}

