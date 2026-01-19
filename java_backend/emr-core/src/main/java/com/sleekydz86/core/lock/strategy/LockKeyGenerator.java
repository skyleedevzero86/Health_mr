package com.sleekydz86.core.lock.strategy;

import java.lang.reflect.Method;

public interface LockKeyGenerator {
    String generate(String expression, Method method, Object[] args);
}
