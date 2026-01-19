package com.sleekydz86.core.lock.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Component
public class ParameterKeyGenerator implements LockKeyGenerator {

    @Override
    public String generate(String expression, Method method, Object[] args) {
        try {
            int index = Integer.parseInt(expression);
            if (index >= 0 && index < args.length) {
                return args[index] != null ? args[index].toString() : expression;
            }
        } catch (NumberFormatException e) {
            log.warn("파라미터 인덱스 파싱 실패: keyExpression={}", expression);
        }
        return expression;
    }
}
