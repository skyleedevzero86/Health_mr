package com.sleekydz86.core.security.masking.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Sensitive {

    MaskingType type() default MaskingType.DEFAULT;

    enum MaskingType {
        RRN,        // 주민등록번호
        PHONE,      // 전화번호
        EMAIL,      // 이메일
        ACCOUNT,    // 계좌번호
        NAME,       // 이름
        ADDRESS,    // 주소
        DEFAULT     // 기본 마스킹
    }
}