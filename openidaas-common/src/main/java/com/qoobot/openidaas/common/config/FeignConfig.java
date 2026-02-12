package com.qoobot.openidaas.common.config;

import feign.Logger;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenFeign 配置
 *
 * @author QooBot
 */
@Slf4j
@Configuration
public class FeignConfig {

    /**
     * Feign 日志级别
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        // 记录请求和响应的完整信息（生产环境建议使用 BASIC）
        return Logger.Level.FULL;
    }

    /**
     * Feign 重试策略
     * 默认不重试，避免雪崩效应
     */
    @Bean
    Retryer feignRetryer() {
        // 最大重试次数 1 次，初始间隔 100ms，最大间隔 1s
        return new Retryer.Default(100, 1000, 1);
    }

    /**
     * 请求拦截器 - 添加认证信息
     */
    @Bean
    public RequestInterceptor authRequestInterceptor() {
        return template -> {
            // 从当前请求上下文获取认证头，传递给下游服务
            Object tokenObj = org.springframework.web.context.request.RequestContextHolder
                    .currentRequestAttributes()
                    .getAttribute("Authorization", 0);
            if (tokenObj != null) {
                template.header("Authorization", tokenObj.toString());
            }
            // 添加链路追踪 ID
            Object traceIdObj = org.springframework.web.context.request.RequestContextHolder
                    .currentRequestAttributes()
                    .getAttribute("X-Trace-Id", 0);
            if (traceIdObj != null) {
                template.header("X-Trace-Id", traceIdObj.toString());
            }
            // 添加服务调用标识
            template.header("X-From-Service", System.getenv("SPRING_APPLICATION_NAME"));
        };
    }

    /**
     * 错误解码器
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return (methodKey, response) -> {
            log.error("Feign 调用失败 - Method: {}, Status: {}, Reason: {}",
                    methodKey, response.status(), response.reason());

            switch (response.status()) {
                case 400:
                    return new IllegalArgumentException("请求参数错误");
                case 401:
                    return new com.qoobot.openidaas.common.exception.BusinessException(
                            com.qoobot.openidaas.common.exception.ErrorCode.UNAUTHORIZED);
                case 403:
                    return new com.qoobot.openidaas.common.exception.BusinessException(
                            com.qoobot.openidaas.common.exception.ErrorCode.FORBIDDEN);
                case 404:
                    return new IllegalArgumentException("请求的资源不存在");
                case 500:
                    return new com.qoobot.openidaas.common.exception.BusinessException(
                            com.qoobot.openidaas.common.exception.ErrorCode.INTERNAL_SERVER_ERROR);
                default:
                    return new Exception("服务调用异常: " + response.reason());
            }
        };
    }
}
