package com.sleekydz86.domain.auth.filter;

import com.sleekydz86.core.security.jwt.CustomAuthenticationException;
import com.sleekydz86.core.security.jwt.JwtAuthenticationEntryPoint;
import com.sleekydz86.core.security.jwt.JwtUtil;
import com.sleekydz86.core.security.jwt.TokenBlacklistService;
import com.sleekydz86.core.security.jwt.valueobject.AccessToken;
import com.sleekydz86.core.tenant.TenantContext;
import com.sleekydz86.domain.user.repository.UserInstitutionRepository;
import com.sleekydz86.domain.user.type.RoleType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final TokenBlacklistService tokenBlacklistService;
    private final UserInstitutionRepository userInstitutionRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            TenantContext.clear();

            String header = request.getHeader(AUTHORIZATION_HEADER);
            String token = jwtUtil.resolveToken(header);

            if (StringUtils.hasText(token)) {

                if (tokenBlacklistService.isTokenBlacklisted(token)) {
                    throw new CustomAuthenticationException("로그아웃된 토큰입니다.");
                }

                if (jwtUtil.validateToken(token)) {
                    var authentication = jwtUtil.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    AccessToken accessToken = jwtUtil.parseAccessToken(token);
                    String primaryInttCd = accessToken.getInttCd();
                    Long userId = accessToken.getUserId();

                    boolean isAdmin = authentication.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(auth -> auth.equals("ROLE_" + RoleType.ADMIN.name()));

                    TenantContext.setAdmin(isAdmin);

                    if (!isAdmin && userId != null) {
                        var institutionCodes = userInstitutionRepository.findInstitutionCodesByUserId(userId);
                        if (!institutionCodes.isEmpty()) {
                            TenantContext.setTenantIds(institutionCodes);

                            if (primaryInttCd != null && institutionCodes.contains(primaryInttCd)) {
                                TenantContext.setTenantId(primaryInttCd);
                            } else if (!institutionCodes.isEmpty()) {

                                TenantContext.setTenantId(institutionCodes.get(0));
                            }
                        } else if (primaryInttCd != null) {

                            TenantContext.setTenantId(primaryInttCd);
                        }
                    }

                    log.debug("Security Context에 인증 정보를 저장 성공. 인증된 사용자: {}, 권한: {}, 기본기관코드: {}, 소속기관코드: {}, 관리자: {}",
                            authentication.getPrincipal(), authentication.getAuthorities(),
                            primaryInttCd, TenantContext.getTenantIds(), isAdmin);
                } else {
                    throw new CustomAuthenticationException("유효하지 않은 토큰입니다.");
                }
            }

            filterChain.doFilter(request, response);

        } catch (CustomAuthenticationException e) {
            log.error("JWT 인증 실패: {}", e.getMessage());
            jwtAuthenticationEntryPoint.commence(request, response, e);
        } finally {

            TenantContext.clear();
        }
    }
}

