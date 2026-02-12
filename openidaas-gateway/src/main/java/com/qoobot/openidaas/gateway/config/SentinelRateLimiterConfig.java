package com.qoobot.openidaas.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sentinel限流配置
 *
 * @author QooBot
 */
@Configuration
public class SentinelRateLimiterConfig {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 初始化限流规则
     */
    @PostConstruct
    public void initGatewayRules() {
        // Gateway限流规则
        initGatewayFlowRules();

        // 熔断降级规则
        initDegradeRules();

        // 设置限流回调处理器
        setBlockRequestHandler();
    }

    /**
     * 初始化Gateway限流规则
     */
    private void initGatewayFlowRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();

        // 登录接口限流: 同IP每分钟最多5次
        rules.add(buildGatewayRule(
            "/api/auth/login",
            "ip",
            5,
            60,
            0,
            1
        ));

        // 发送短信验证码限流: 同IP每分钟1次,同号码每天10次
        rules.add(buildGatewayRule(
            "/api/auth/mfa/send-sms",
            "ip",
            1,
            60,
            0,
            1
        ));

        // 发送邮箱验证码限流: 同IP每分钟1次,同邮箱每天10次
        rules.add(buildGatewayRule(
            "/api/auth/mfa/send-email",
            "ip",
            1,
            60,
            0,
            1
        ));

        // 用户查询接口限流: 每秒100次
        rules.add(buildGatewayRule(
            "/api/users",
            "default",
            100,
            1,
            0,
            0
        ));

        // 密码重置接口限流: 同IP每小时3次
        rules.add(buildGatewayRule(
            "/api/auth/reset-password",
            "ip",
            3,
            3600,
            0,
            1
        ));

        // Token刷新限流: 每秒20次
        rules.add(buildGatewayRule(
            "/api/auth/refresh",
            "default",
            20,
            1,
            0,
            0
        ));

        GatewayRuleManager.loadRules(rules);
    }

    /**
     * 构建Gateway限流规则
     */
    private GatewayFlowRule buildGatewayRule(String resource, String limitApp,
                                              long count, long intervalSec,
                                              long intervalMs, int strategy) {
        GatewayFlowRule rule = new GatewayFlowRule();
        rule.setResource(resource);
        rule.setResourceMode(GatewayFlowRule.RESOURCE_MODE_ROUTE_ID);
        rule.setGrade(GatewayFlowRule.GRADE_QPS);
        rule.setCount(count);
        rule.setIntervalSec(intervalSec);
        rule.setIntervalMs(intervalMs);
        rule.setControlBehavior(0); // 直接拒绝

        // 策略: 0=默认,1=按IP限流
        if ("ip".equals(limitApp)) {
            rule.setLimitApp("origin");
        }

        return rule;
    }

    /**
     * 初始化熔断降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 认证服务降级: 异常比例超过50%时熔断
        DegradeRule authDegradeRule = new DegradeRule();
        authDegradeRule.setResource("openidaas-auth-service");
        authDegradeRule.setGrade(DegradeRule.GRADE_EXCEPTION_RATIO);
        authDegradeRule.setCount(0.5); // 50%异常比例
        authDegradeRule.setTimeWindow(60); // 熔断60秒
        authDegradeRule.setMinRequestAmount(10); // 最小请求数
        rules.add(authDegradeRule);

        // 用户服务降级: 慢调用比例超过50%时熔断
        DegradeRule userDegradeRule = new DegradeRule();
        userDegradeRule.setResource("openidaas-user-service");
        userDegradeRule.setGrade(DegradeRule.GRADE_SLOW_REQUEST_RATIO);
        userDegradeRule.setCount(0.5); // 50%慢调用比例
        userDegradeRule.setTimeWindow(30); // 熔断30秒
        userDegradeRule.setMinRequestAmount(5); // 最小请求数
        userDegradeRule.setSlowRatioThreshold(200); // 慢调用阈值: 200ms
        rules.add(userDegradeRule);

        DegradeRuleManager.loadRules(rules);
    }

    /**
     * 设置限流回调处理器
     */
    private void setBlockRequestHandler() {
        BlockRequestHandler blockRequestHandler = new BlockRequestHandler() {
            @Override
            public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
                Map<String, Object> result = new HashMap<>();

                if (ex instanceof BlockException) {
                    BlockException blockEx = (BlockException) ex;
                    String rule = blockEx.getRule() != null ? blockEx.getRule().toString() : "unknown";

                    if (rule.contains("login")) {
                        result.put("code", 4291);
                        result.put("message", "登录请求过于频繁,请稍后再试");
                    } else if (rule.contains("send-sms") || rule.contains("send-email")) {
                        result.put("code", 4292);
                        result.put("message", "验证码发送过于频繁,请稍后再试");
                    } else if (rule.contains("reset-password")) {
                        result.put("code", 4293);
                        result.put("message", "密码重置请求过于频繁,请稍后再试");
                    } else {
                        result.put("code", 4290);
                        result.put("message", "请求过于频繁,请稍后再试");
                    }

                    result.put("data", null);
                    result.put("timestamp", System.currentTimeMillis());

                    return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(result));
                }

                // 其他异常
                result.put("code", 500);
                result.put("message", "服务暂时不可用");
                result.put("data", null);
                result.put("timestamp", System.currentTimeMillis());

                return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(result));
            }
        };

        GatewayCallbackManager.setBlockHandler(blockRequestHandler);
    }

    /**
     * 注册Sentinel过滤器
     */
    @Bean
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }
}
