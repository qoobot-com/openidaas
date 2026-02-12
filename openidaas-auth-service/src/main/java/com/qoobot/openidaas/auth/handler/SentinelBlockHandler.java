package com.qoobot.openidaas.auth.handler;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.qoobot.openidaas.common.vo.ResultVO;
import org.springframework.stereotype.Component;

/**
 * Sentinel限流和熔断处理器
 *
 * @author QooBot
 */
@Component
public class SentinelBlockHandler {

    /**
     * 登录接口限流处理
     */
    public static ResultVO<?> handleLoginBlock(Object[] args, BlockException ex) {
        return ResultVO.error(4291, "登录请求过于频繁,请稍后再试");
    }

    /**
     * 登录接口降级处理
     */
    public static ResultVO<?> handleLoginFallback(Object[] args, Throwable ex) {
        return ResultVO.error(5031, "认证服务暂时不可用,请稍后再试");
    }

    /**
     * 发送短信验证码限流处理
     */
    public static ResultVO<?> handleSendSMSBlock(Object[] args, BlockException ex) {
        return ResultVO.error(4292, "验证码发送过于频繁,请稍后再试");
    }

    /**
     * 发送邮件验证码限流处理
     */
    public static ResultVO<?> handleSendEmailBlock(Object[] args, BlockException ex) {
        return ResultVO.error(4292, "验证码发送过于频繁,请稍后再试");
    }

    /**
     * 密码重置限流处理
     */
    public static ResultVO<?> handleResetPasswordBlock(Object[] args, BlockException ex) {
        return ResultVO.error(4293, "密码重置请求过于频繁,请稍后再试");
    }

    /**
     * Token刷新限流处理
     */
    public static ResultVO<?> handleRefreshTokenBlock(Object[] args, BlockException ex) {
        return ResultVO.error(4290, "Token刷新过于频繁,请稍后再试");
    }

    /**
     * 通用限流处理
     */
    public static ResultVO<?> handleGenericBlock(Object[] args, BlockException ex) {
        String resource = ex.getRule() != null ? ex.getRule().getResource() : "unknown";
        return ResultVO.error(4290, "请求: " + resource + " 过于频繁,请稍后再试");
    }

    /**
     * 通用降级处理
     */
    public static ResultVO<?> handleGenericFallback(Object[] args, Throwable ex) {
        return ResultVO.error(503, "服务暂时不可用,请稍后再试");
    }
}
