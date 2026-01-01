package com.sleekydz86.domain.auth.service;

import com.sleekydz86.core.common.exception.custom.DuplicateException;
import com.sleekydz86.core.common.exception.custom.NotFoundException;
import com.sleekydz86.core.common.exception.custom.UnauthorizedException;
import com.sleekydz86.core.event.publisher.EventPublisher;
import com.sleekydz86.core.security.jwt.JwtUtil;
import com.sleekydz86.core.security.jwt.JwtUtil.TokenPair;
import com.sleekydz86.core.security.jwt.TokenBlacklistService;
import com.sleekydz86.domain.auth.dto.LoginRequest;
import com.sleekydz86.domain.auth.dto.RegisterRequest;
import com.sleekydz86.domain.auth.dto.TokenResponse;
import com.sleekydz86.domain.common.valueobject.Email;
import com.sleekydz86.domain.common.valueobject.LoginId;
import com.sleekydz86.domain.common.valueobject.Password;
import com.sleekydz86.domain.common.valueobject.PhoneNumber;
import com.sleekydz86.domain.department.entity.DepartmentEntity;
import com.sleekydz86.domain.department.repository.DepartmentRepository;
import com.sleekydz86.domain.institution.service.InstitutionService;
import com.sleekydz86.domain.user.entity.UserEntity;
import com.sleekydz86.domain.user.entity.UserInstitution;
import com.sleekydz86.domain.user.repository.UserInstitutionRepository;
import com.sleekydz86.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final UserInstitutionRepository userInstitutionRepository;
    private final DepartmentRepository departmentRepository;
    private final InstitutionService institutionService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final AccountLockService accountLockService;
    private final EmailVerificationService emailVerificationService;
    private final TokenBlacklistService tokenBlacklistService;
    private final EventPublisher eventPublisher;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new DuplicateException("이미 사용 중인 계정입니다.");
        }

        DepartmentEntity department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("부서가 존재하지 않습니다."));

        if (request.getInttCd() != null && !request.getInttCd().isBlank()) {
            if (!institutionService.existsActiveByCode(request.getInttCd())) {
                throw new NotFoundException("존재하지 않거나 비활성화된 기관입니다.");
            }
        }

        LoginId loginId = LoginId.of(request.getLoginId());
        Password password = Password.fromPlainText(request.getPassword(), passwordEncoder);
        Email email = request.getEmail() != null ? Email.of(request.getEmail()) : null;
        PhoneNumber telNum = request.getTelNum() != null ? PhoneNumber.of(request.getTelNum()) : null;

        UserEntity user = UserEntity.builder()
                .role(com.sleekydz86.domain.user.type.RoleType.WAIT)
                .loginId(loginId)
                .password(password)
                .department(department)
                .name(request.getName())
                .gender(request.getGender())
                .address(request.getAddress())
                .email(email)
                .telNum(telNum)
                .birth(request.getBirth() != null ? request.getBirth().atStartOfDay() : null)
                .hireDate(request.getHireDate() != null ? request.getHireDate().atStartOfDay() : null)
                .inttCd(request.getInttCd())
                .build();

        UserEntity savedUser = userRepository.save(user);

        eventPublisher.publish(new com.sleekydz86.core.event.domain.UserRegisteredEvent(
                savedUser.getId(),
                savedUser.getLoginIdValue(),
                savedUser.getRole().name()));
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {

        accountLockService.checkAccountLock(request.getLoginId());

        UserEntity user = userRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new UnauthorizedException("존재하지 않는 사용자입니다."));

        if (!user.verifyPassword(request.getPassword(), passwordEncoder)) {
            accountLockService.recordFailedAttempt(request.getLoginId());
            throw new UnauthorizedException("비밀번호가 일치하지 않습니다.");
        }

        accountLockService.clearFailedAttempts(request.getLoginId());

        String primaryInstitutionCode = null;
        if (!user.isAdmin()) {
            UserInstitution primaryInstitution = userInstitutionRepository.findPrimaryByUserId(user.getId())
                    .orElse(null);
            if (primaryInstitution != null) {
                primaryInstitutionCode = primaryInstitution.getInstitutionCode();
            } else {

                List<UserInstitution> userInstitutions = userInstitutionRepository.findByUserId(user.getId());
                if (!userInstitutions.isEmpty()) {
                    primaryInstitutionCode = userInstitutions.get(0).getInstitutionCode();
                }
            }
        }

        TokenPair tokenPair = jwtUtil.generateTokens(user, primaryInstitutionCode);
        TokenResponse tokenResponse = TokenResponse.of(
                tokenPair.getAccessToken().getValue(),
                tokenPair.getRefreshToken().getValue());

        refreshTokenService.saveRefreshToken(user.getId(), tokenResponse.refreshToken());

        eventPublisher.publish(new com.sleekydz86.core.event.domain.UserLoggedInEvent(
                user.getId(),
                user.getLoginIdValue()));

        return tokenResponse;
    }

    @Transactional
    public void logout(String token) {

        Long userId = jwtUtil.getUserIdFromToken(token);

        refreshTokenService.deleteRefreshToken(userId);

        tokenBlacklistService.blacklistToken(token);

        eventPublisher.publish(new com.sleekydz86.core.event.domain.UserLoggedOutEvent(userId));
    }

    @Transactional
    public TokenResponse refreshToken(String refreshToken) {

        if (!refreshTokenService.validateRefreshToken(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = refreshTokenService.getUserIdFromToken(refreshToken);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        TokenPair tokenPair = jwtUtil.generateTokens(user);
        TokenResponse tokenResponse = TokenResponse.of(
                tokenPair.getAccessToken().getValue(),
                tokenPair.getRefreshToken().getValue());

        refreshTokenService.rotateRefreshToken(userId, refreshToken, tokenResponse.refreshToken());

        return tokenResponse;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("해당 이메일로 등록된 사용자를 찾을 수 없습니다."));

        emailVerificationService.sendVerificationCode(email);

        eventPublisher.publish(new com.sleekydz86.core.event.domain.PasswordResetRequestedEvent(
                user.getId(),
                user.getEmailValue()));
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {

        if (!emailVerificationService.verifyCode(email, code)) {
            throw new UnauthorizedException("유효하지 않은 인증 코드입니다.");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("해당 이메일로 등록된 사용자를 찾을 수 없습니다."));

        user.changePassword(newPassword, passwordEncoder);
        userRepository.save(user);

        eventPublisher.publish(new com.sleekydz86.core.event.domain.PasswordResetCompletedEvent(
                user.getId(),
                user.getEmailValue()));
    }
}
