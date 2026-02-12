package com.qoobot.openidaas.common.sentinel;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Sentinel 配置类
 * 配置流控规则和降级策略
 */
@Slf4j
@Configuration
public class SentinelConfig {

    @Value("${sentinel.enabled:true}")
    private boolean sentinelEnabled;

    @Value("${sentinel.flow.qps:100}")
    private int defaultQps;

    @Value("${sentinel.degrade.ratio:0.5}")
    private double degradeRatio;

    @Value("${sentinel.degrade.time-window:10}")
    private int degradeTimeWindow;

    /**
     * 初始化 Sentinel 规则
     */
    @PostConstruct
    public void initSentinelRules() {
        if (!sentinelEnabled) {
            log.info("Sentinel is disabled");
            return;
        }

        initFlowRules();
        initDegradeRules();

        log.info("Sentinel rules initialized successfully");
    }

    /**
     * 初始化流控规则
     */
    private void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        // 用户服务流控规则
        rules.add(createFlowRule(
                "user-service:getUser",
                defaultQps,
                RuleConstant.FLOW_GRADE_QPS,
                RuleConstant.CONTROL_BEHAVIOR_DEFAULT
        ));

        rules.add(createFlowRule(
                "user-service:listUsers",
                50,
                RuleConstant.FLOW_GRADE_QPS,
                RuleConstant.CONTROL_BEHAVIOR_DEFAULT
        ));

        rules.add(createFlowRule(
                "user-service:createUser",
                20,
                RuleConstant.FLOW_GRADE_QPS,
                RuleConstant.CONTROL_BEHAVIOR_DEFAULT
        ));

        // 认证服务流控规则
        rules.add(createFlowRule(
                "auth-service:login",
                10,
                RuleConstant.FLOW_GRADE_QPS,
                RuleConstant.CONTROL_BEHAVIOR_DEFAULT
        ));

        rules.add(createFlowRule(
                "auth-service:verifyMFA",
                30,
                RuleConstant.FLOW_GRADE_QPS,
                RuleConstant.CONTROL_BEHAVIOR_DEFAULT
        ));

        // 组织服务流控规则
        rules.add(createFlowRule(
                "organization-service:getDepartmentTree",
                30,
                RuleConstant.FLOW_GRADE_QPS,
                RuleConstant.CONTROL_BEHAVIOR_DEFAULT
        ));

        // 角色服务流控规则
        rules.add(createFlowRule(
                "role-service:assignPermissions",
                10,
                RuleConstant.FLOW_GRADE_QPS,
                RuleConstant.CONTROL_BEHAVIOR_DEFAULT
        ));

        // 网关流控规则
        rules.add(createFlowRule(
                "gateway:api-request",
                200,
                RuleConstant.FLOW_GRADE_QPS,
                RuleConstant.CONTROL_BEHAVIOR_DEFAULT
        ));

        FlowRuleManager.loadRules(rules);
        log.info("Loaded {} flow rules", rules.size());
    }

    /**
     * 初始化降级规则
     */
    private void initDegradeRules() {
        List<DegradeRule> rules = new ArrayList<>();

        // 用户服务降级规则
        rules.add(createDegradeRule(
                "user-service:getUser",
                RuleConstant.DEGRADE_GRADE_RT,
                (double) 500, // 500ms 超时
                degradeTimeWindow
        ));

        // 认证服务降级规则
        rules.add(createDegradeRule(
                "auth-service:login",
                RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO,
                degradeRatio, // 50% 异常率
                2, // 最小请求数
                degradeTimeWindow
        ));

        // 组织服务降级规则
        rules.add(createDegradeRule(
                "organization-service:getDepartmentTree",
                RuleConstant.DEGRADE_GRADE_RT,
                (double) 1000, // 1秒超时
                degradeTimeWindow
        ));

        DegradeRuleManager.loadRules(rules);
        log.info("Loaded {} degrade rules", rules.size());
    }

    /**
     * 创建流控规则
     */
    private FlowRule createFlowRule(String resource, int count, int grade, int controlBehavior) {
        FlowRule rule = new FlowRule();
        rule.setResource(resource);
        rule.setGrade(grade);
        rule.setCount(count);
        rule.setLimitApp("default");
        rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        rule.setControlBehavior(controlBehavior);
        return rule;
    }

    /**
     * 创建降级规则
     */
    private DegradeRule createDegradeRule(String resource, int grade, double count, int timeWindow) {
        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setGrade(grade);
        rule.setCount(count);
        rule.setTimeWindow(timeWindow);
        return rule;
    }

    /**
     * 创建基于异常比的降级规则
     */
    private DegradeRule createDegradeRule(String resource, int grade, double ratio, int minRequest, int timeWindow) {
        DegradeRule rule = new DegradeRule();
        rule.setResource(resource);
        rule.setGrade(grade);
        rule.setCount(ratio);
        rule.setTimeWindow(timeWindow);
        rule.setMinRequestAmount(minRequest);
        return rule;
    }
}
