package com.qoobot.openidaas.audit.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 审计日志健康检查指示器
 * 提供审计服务的健康状态信息
 */
@Component
public class AuditHealthIndicator implements HealthIndicator {

    // 记录最后处理时间
    private volatile long lastProcessedTime = System.currentTimeMillis();
    
    // 处理失败次数
    private final AtomicInteger failureCount = new AtomicInteger(0);
    
    // 处理成功次数
    private final AtomicInteger successCount = new AtomicInteger(0);
    
    // 异常错误信息
    private volatile String lastError = null;
    
    /**
     * 记录处理成功
     */
    public void recordSuccess() {
        successCount.incrementAndGet();
        lastProcessedTime = System.currentTimeMillis();
        lastError = null;
    }
    
    /**
     * 记录处理失败
     */
    public void recordFailure(String error) {
        failureCount.incrementAndGet();
        lastProcessedTime = System.currentTimeMillis();
        lastError = error;
    }
    
    /**
     * 重置计数器
     */
    public void resetCounters() {
        successCount.set(0);
        failureCount.set(0);
        lastError = null;
    }
    
    /**
     * 获取成功次数
     */
    public int getSuccessCount() {
        return successCount.get();
    }
    
    /**
     * 获取失败次数
     */
    public int getFailureCount() {
        return failureCount.get();
    }
    
    /**
     * 获取最后处理时间
     */
    public long getLastProcessedTime() {
        return lastProcessedTime;
    }
    
    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        // 添加基本信息
        builder.withDetail("lastProcessedTime", lastProcessedTime)
                .withDetail("successCount", successCount.get())
                .withDetail("failureCount", failureCount.get());
        
        // 检查是否有错误
        if (lastError != null) {
            builder.withDetail("lastError", lastError);
            
            // 如果失败次数过多，返回降级状态
            if (failureCount.get() > 10) {
                return Health.down()
                        .withDetail("reason", "Too many failures")
                        .withDetail("failureCount", failureCount.get())
                        .withDetail("lastError", lastError)
                        .build();
            }
        }
        
        // 检查最后处理时间（如果超过5分钟没有处理，返回降级状态）
        long timeSinceLastProcess = System.currentTimeMillis() - lastProcessedTime;
        builder.withDetail("timeSinceLastProcessMs", timeSinceLastProcess);
        
        if (timeSinceLastProcess > 5 * 60 * 1000) {
            return Health.down()
                    .withDetail("reason", "No activity for more than 5 minutes")
                    .withDetail("timeSinceLastProcessMs", timeSinceLastProcess)
                    .build();
        }
        
        // 检查失败率
        int total = successCount.get() + failureCount.get();
        if (total > 0) {
            double failureRate = (double) failureCount.get() / total;
            builder.withDetail("failureRate", failureRate);
            
            // 如果失败率超过50%，返回降级状态
            if (failureRate > 0.5) {
                return Health.down()
                        .withDetail("reason", "High failure rate")
                        .withDetail("failureRate", failureRate)
                        .build();
            }
        }
        
        return builder.build();
    }
}
