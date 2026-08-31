package com.sleekydz86.domain.auth.service;

import com.sleekydz86.core.common.exception.custom.UnauthorizedException;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.core.security.jwt.JwtUtil;
import com.sleekydz86.core.security.jwt.TokenBlacklistService;
import com.sleekydz86.domain.auth.dto.LoginRequest;
import com.sleekydz86.domain.department.repository.DepartmentRepository;
import com.sleekydz86.domain.institution.service.InstitutionService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.repository.UserInstitutionRepository;
import com.sleekydz86.domain.user.repository.UserRepository;
import com.sleekydz86.domain.user.type.AccountStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceAccountStatusTest {
    @Mock UserRepository userRepository;
    @Mock UserInstitutionRepository userInstitutionRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock InstitutionService institutionService;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtil jwtUtil;
    @Mock RefreshTokenService refreshTokenService;
    @Mock AccountLockService accountLockService;
    @Mock EmailVerificationService emailVerificationService;
    @Mock TokenBlacklistService tokenBlacklistService;
    @Mock EventPublisher eventPublisher;
    @InjectMocks AuthService authService;

    @Test
    void waitingApprovalAccountCannotLogin() {
        LoginRequest request = mock(LoginRequest.class);
        UserEntity user = mock(UserEntity.class);
        when(request.getLoginId()).thenReturn("waiting-user");
        when(request.getPassword()).thenReturn("password1!");
        when(userRepository.findByLoginId("waiting-user")).thenReturn(Optional.of(user));
        when(user.verifyPassword("password1!", passwordEncoder)).thenReturn(true);
        when(user.canLogin()).thenReturn(false);
        when(user.getAccountStatus()).thenReturn(AccountStatus.WAITING_APPROVAL);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("승인 대기");

        verify(jwtUtil, never()).generateTokens(any(), any());
    }
}
