package com.qoobot.openidaas.common.sentinel;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Sentinel 资源 AOP 切面
 * 拦截 @SentinelResource 注解的方法
 */
@Slf4j
@Aspect
@Component
public class SentinelResourceAspect {

    @Around("@annotation(com.qoobot.openidaas.common.sentinel.SentinelResource)")
    public Object aroundSentinelResource(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethod(joinPoint);
        SentinelResource sentinelResource = method.getAnnotation(SentinelResource.class);

        String resourceName = sentinelResource.value().isEmpty()
                ? buildResourceName(method)
                : sentinelResource.value();

        Entry entry = null;
        try {
            entry = SphU.entry(resourceName, EntryType.IN);
            return joinPoint.proceed();
        } catch (BlockException ex) {
            log.warn("Sentinel blocked: resource={}, exception={}", resourceName, ex.getClass().getSimpleName());
            handleBlockException(sentinelResource, ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Exception in Sentinel resource: {}", resourceName, ex);
            throw ex;
        } finally {
            if (entry != null) {
                entry.exit(1);
            }
        }
    }

    /**
     * 获取方法对象
     */
    private Method getMethod(ProceedingJoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            Class<?> targetClass = joinPoint.getTarget().getClass();
            Class<?>[] parameterTypes = new Class[joinPoint.getArgs().length];
            for (int i = 0; i < joinPoint.getArgs().length; i++) {
                parameterTypes[i] = joinPoint.getArgs()[i].getClass();
            }
            return targetClass.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method not found", e);
        }
    }

    /**
     * 构建资源名称
     */
    private String buildResourceName(Method method) {
        String className = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();
        return className + ":" + methodName;
    }

    /**
     * 处理 BlockException
     */
    private void handleBlockException(SentinelResource sentinelResource, BlockException ex) {
        // 可以根据注解配置调用指定的 blockHandler 或 fallback 方法
        String blockHandler = sentinelResource.blockHandler();
        String fallback = sentinelResource.fallback();

        if (!blockHandler.isEmpty()) {
            log.debug("Invoke blockHandler: {}", blockHandler);
        } else if (!fallback.isEmpty()) {
            log.debug("Invoke fallback: {}", fallback);
        }
    }
}
