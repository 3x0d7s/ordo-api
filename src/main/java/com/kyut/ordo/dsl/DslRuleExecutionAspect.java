package com.kyut.ordo.dsl;

import dev.kyut.dsl.core.RuleContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Aspect
@Component
@ConditionalOnProperty(value = "ordo.dsl.execution-logging.enabled", havingValue = "true", matchIfMissing = true)
public class DslRuleExecutionAspect {
    private static final Logger log = LoggerFactory.getLogger(DslRuleExecutionAspect.class);

    @Around("execution(* dev.kyut.dsl.engine.RuleEngine.execute(..))")
    public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startNanos = System.nanoTime();
        try {
            Object result = joinPoint.proceed();
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            RuleContext context = extractContext(joinPoint.getArgs());
            log.debug(
                    "DSL execution finished in {} ms, contextKeys={}",
                    elapsedMs,
                    context == null ? "[]" : context.asMap().keySet()
            );
            return result;
        } catch (Throwable throwable) {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            log.error("DSL execution failed after {} ms: {}", elapsedMs, throwable.getMessage(), throwable);
            throw throwable;
        }
    }

    private RuleContext extractContext(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        Object first = args[0];
        if (first instanceof RuleContext ctx) {
            return ctx;
        }
        return null;
    }
}
