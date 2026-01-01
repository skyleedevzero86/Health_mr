package com.sleekydz86.domain.auth.service;

import com.sleekydz86.core.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationService notificationService;

    @Value("${email.verification.code.length:6}")
    private int codeLength;

    @Value("${email.verification.code.expiry:600000}")
    private long codeExpiry;

    private static final String VERIFICATION_CODE_PREFIX = "email_verification:";
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public void sendVerificationCode(String email) {
        // 인증 코드 생성
        String code = generateVerificationCode();

        // Redis에 저장 (10분 만료)
        String key = VERIFICATION_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, codeExpiry, TimeUnit.MILLISECONDS);

        // 이메일 발송
        String subject = "이메일 인증 코드";
        String message = "인증 코드: " + code;
        notificationService.send(email, subject, message);
    }

    public boolean verifyCode(String email, String code) {
        String key = VERIFICATION_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            return false;
        }

        boolean verified = storedCode.equals(code);

        if (verified) {
            redisTemplate.delete(key);
        }

        return verified;
    }

    private String generateVerificationCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}

