package com.sleekydz86.core.lock.strategy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Slf4j
@Component
public class SpelKeyGenerator implements LockKeyGenerator {

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    @Override
    public String generate(String expression, Method method, Object[] args) {
        try {
            EvaluationContext context = new StandardEvaluationContext();
            
            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                context.setVariable(parameters[i].getName(), args[i]);
            }

            context.setVariable("methodName", method.getName());
            context.setVariable("className", method.getDeclaringClass().getSimpleName());

            Expression expr = expressionParser.parseExpression(expression);
            Object value = expr.getValue(context);

            return value != null ? value.toString() : expression;
        } catch (Exception e) {
            log.error("SpEL 표현식 파싱 실패: expression={}", expression, e);
            return expression;
        }
    }
}
