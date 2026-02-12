package com.qoobot.openidaas.common.sentinel;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.exception.RateLimitException;
import com.qoobot.openidaas.common.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sentinel 降级处理类
 * 处理流控和降级异常
 */
@Slf4j
@Component
public class SentinelFallbackHandler {

    /**
     * 默认流控降级处理
     */
    public ResultVO<Void> handleBlock(BlockException ex) {
        log.warn("Request blocked by Sentinel: resource={}, rule={}", ex.getRule(), ex.getRuleLimitApp());

        if (ex.getRule() instanceof com.alibaba.csp.sentinel.slots.block.flow.FlowRule) {
            return ResultVO.error("LIMIT_EXCEEDED", "系统繁忙，请稍后重试");
        } else if (ex.getRule() instanceof com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule) {
            return ResultVO.error("SERVICE_UNAVAILABLE", "服务暂时不可用，请稍后重试");
        }

        return ResultVO.error("SYSTEM_BUSY", "系统繁忙，请稍后重试");
    }

    /**
     * 用户服务降级处理
     */
    public ResultVO<Void> handleUserServiceFallback(BlockException ex) {
        log.warn("User service blocked: {}", ex.getMessage());
        return ResultVO.error("USER_SERVICE_UNAVAILABLE", "用户服务暂时不可用");
    }

    /**
     * 认证服务降级处理
     */
    public ResultVO<Void> handleAuthServiceFallback(BlockException ex) {
        log.warn("Auth service blocked: {}", ex.getMessage());
        return ResultVO.error("AUTH_RATE_LIMIT", "登录请求过于频繁，请稍后重试");
    }

    /**
     * 组织服务降级处理
     */
    public ResultVO<Void> handleOrganizationServiceFallback(BlockException ex) {
        log.warn("Organization service blocked: {}", ex.getMessage());
        return ResultVO.error("ORG_SERVICE_UNAVAILABLE", "组织服务暂时不可用");
    }

    /**
     * 角色服务降级处理
     */
    public ResultVO<Void> handleRoleServiceFallback(BlockException ex) {
        log.warn("Role service blocked: {}", ex.getMessage());
        return ResultVO.error("ROLE_SERVICE_UNAVAILABLE", "角色服务暂时不可用");
    }

    /**
     * 网关降级处理
     */
    public ResultVO<Void> handleGatewayFallback(BlockException ex) {
        log.warn("Gateway blocked: {}", ex.getMessage());
        return ResultVO.error("GATEWAY_RATE_LIMIT", "系统繁忙，请稍后重试");
    }
}