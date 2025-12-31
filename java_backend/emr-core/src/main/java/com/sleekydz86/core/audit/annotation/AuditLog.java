package com.sleekydz86.core.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditLog {

    ActionType action();

    enum ActionType {
        CREATE,
        UPDATE,
        DELETE,
        READ
    }
}