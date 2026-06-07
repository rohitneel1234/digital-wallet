package com.wallet.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    // ─── Log all service method entries with arguments ─────────────────────────
    @Before("execution(* com.wallet.service.*.*(..))")
    public void logMethodEntry(JoinPoint joinPoint) {
        log.debug("[AOP] >> Entering: {}.{}() with args: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
    }

    // ─── Log all service method exits ──────────────────────────────────────────
    @AfterReturning(pointcut = "execution(* com.wallet.service.*.*(..))", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        log.debug("[AOP] << Exiting: {}.{}()",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
    }

    // ─── Log exceptions ────────────────────────────────────────────────────────
    @AfterThrowing(pointcut = "execution(* com.wallet.service.*.*(..))", throwing = "ex")
    public void logException(JoinPoint joinPoint, Throwable ex) {
        log.error("[AOP] Exception in {}.{}(): {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ex.getMessage());
    }

    // ─── Performance monitoring for transaction methods ────────────────────────
    @Around("execution(* com.wallet.service.TransactionService.*(..))")
    public Object monitorTransactionPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            if (duration > 500) {
                log.warn("[PERF] Slow transaction method: {}() took {}ms — potential bottleneck!", methodName, duration);
            } else {
                log.info("[PERF] TransactionService.{}() executed in {}ms", methodName, duration);
            }
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[PERF] TransactionService.{}() FAILED after {}ms", methodName, duration);
            throw t;
        }
    }

    // ─── Performance monitoring for wallet balance queries ─────────────────────
    @Around("execution(* com.wallet.service.WalletService.getWalletById(..))" +
            " || execution(* com.wallet.service.WalletService.getMyWallet(..))")
    public Object monitorWalletPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - startTime;
        log.info("[PERF] WalletService.{}() executed in {}ms",
                joinPoint.getSignature().getName(), duration);
        return result;
    }
}
