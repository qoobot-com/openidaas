package com.qoobot.openidaas.gateway.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API调用统计模型
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Builder
public class ApiCallStat {

    /**
     * 请求URI
     */
    private String uri;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 响应时长（毫秒）
     */
    private Long duration;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String error;
}
