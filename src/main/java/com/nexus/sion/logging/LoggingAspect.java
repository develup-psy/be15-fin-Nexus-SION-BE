package com.nexus.sion.infra.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.*;
import org.aspectj.lang.annotation.*;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.nexus.sion..controller..*(..)) || execution(* com.nexus.sion..service..*(..))")
    public void applicationPackagePointcut() {}

    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String args = Arrays.toString(joinPoint.getArgs());

        MDC.put("class", className);
        MDC.put("method", methodName);

        log.info("▶️ Start: {}.{} with args {}", className, methodName, args);

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;
            MDC.put("elapsedTime", String.valueOf(elapsed));
            log.info("End: {}.{} | result: {} | time: {}ms", className, methodName, result, elapsed);
            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            MDC.put("elapsedTime", String.valueOf(elapsed));
            log.error("Exception in {}.{} | {}ms | msg: {}", className, methodName, elapsed, t.getMessage(), t);
            throw t;
        } finally {
            MDC.remove("class");
            MDC.remove("method");
            MDC.remove("elapsedTime");
        }
    }
}
