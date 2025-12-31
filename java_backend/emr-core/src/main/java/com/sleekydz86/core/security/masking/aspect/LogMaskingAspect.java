package com.sleekydz86.core.security.masking.aspect;

import com.sleekydz86.core.security.masking.util.DataMaskingUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Aspect
@Component
@Slf4j
public class LogMaskingAspect {

    private static final Pattern RRN_PATTERN = Pattern.compile("\\d{6}-\\d{7}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(010|011|016|017|018|019)-?\\d{3,4}-?\\d{4}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    @Around("execution(* org.slf4j.Logger.*(..))")
    public Object maskLogMessage(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Object[] maskedArgs = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String) {
                maskedArgs[i] = maskSensitiveData((String) args[i]);
            } else {
                maskedArgs[i] = args[i];
            }
        }

        return joinPoint.proceed(maskedArgs);
    }

    private String maskSensitiveData(String message) {
        if (message == null) {
            return null;
        }

        String masked = message;

        masked = RRN_PATTERN.matcher(masked).replaceAll(matchResult -> {
            String rrn = matchResult.group();
            return DataMaskingUtil.maskRRN(rrn);
        });

        masked = PHONE_PATTERN.matcher(masked).replaceAll(matchResult -> {
            String phone = matchResult.group();
            return DataMaskingUtil.maskPhone(phone);
        });

        masked = EMAIL_PATTERN.matcher(masked).replaceAll(matchResult -> {
            String email = matchResult.group();
            return DataMaskingUtil.maskEmail(email);
        });

        return masked;
    }
}

