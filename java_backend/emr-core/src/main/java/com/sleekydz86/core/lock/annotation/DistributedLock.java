package com.sleekydz86.core.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    String key();

    long waitTime() default 3L;

    long leaseTime() default 10L;

    boolean throwExceptionOnFailure() default true;

    LockKeyType keyType() default LockKeyType.SPEL;

    enum LockKeyType {
        SPEL,
        PARAMETER,
        CUSTOM
    }
}
