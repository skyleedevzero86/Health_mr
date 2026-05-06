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
        RRN,
        PHONE,
        EMAIL,
        ACCOUNT,
        NAME,
        ADDRESS,
        DEFAULT
    }
}