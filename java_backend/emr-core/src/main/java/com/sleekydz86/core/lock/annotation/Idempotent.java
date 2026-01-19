package com.sleekydz86.core.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    String key() default "";

    long ttl() default 86400L;

    IdempotencyKeyType keyType() default IdempotencyKeyType.SPEL;

    String headerName() default "X-Idempotency-Key";

    boolean throwExceptionOnDuplicate() default true;

    enum IdempotencyKeyType {
        SPEL,
        HEADER,
        PARAMETER,
        CUSTOM
    }
}
