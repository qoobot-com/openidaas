package com.qoobot.openidaas.gateway.controller;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Sentinel 测试控制器
 * 用于验证 Sentinel 流控和降级功能
 */
@Slf4j
@RestController
@RequestMapping("/sentinel")
public class SentinelTestController {

    /**
     * 测试流控
     */
    @GetMapping("/test-flow")
    public ResultVO<Map<String, Object>> testFlow() {
        Entry entry = null;
        try {
            entry = SphU.entry("sentinel:test-flow", EntryType.IN);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "流控测试成功");
            data.put("timestamp", LocalDateTime.now());

            log.info("Flow test passed at {}", LocalDateTime.now());
            return ResultVO.success(data);

        } catch (BlockException ex) {
            log.warn("Flow control triggered: {}", ex.getMessage());
            throw new BusinessException("请求被限流，请稍后重试");
        } finally {
            if (entry != null) {
                entry.exit(1);
            }
        }
    }

    /**
     * 测试降级
     */
    @GetMapping("/test-degrade")
    public ResultVO<Map<String, Object>> testDegrade() throws InterruptedException {
        Entry entry = null;
        try {
            entry = SphU.entry("sentinel:test-degrade", EntryType.IN);

            // 模拟慢调用
            Thread.sleep(800);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "降级测试成功");
            data.put("timestamp", LocalDateTime.now());

            log.info("Degrade test passed at {}", LocalDateTime.now());
            return ResultVO.success(data);

        } catch (BlockException ex) {
            log.warn("Degrade triggered: {}", ex.getMessage());
            throw new BusinessException("服务降级，请稍后重试");
        } finally {
            if (entry != null) {
                entry.exit(1);
            }
        }
    }

    /**
     * 高 QPS 测试
     */
    @GetMapping("/high-qps")
    public ResultVO<Map<String, Object>> highQps() {
        Entry entry = null;
        try {
            entry = SphU.entry("sentinel:high-qps", EntryType.IN);

            Map<String, Object> data = new HashMap<>();
            data.put("message", "高 QPS 测试成功");
            data.put("timestamp", LocalDateTime.now());

            return ResultVO.success(data);

        } catch (BlockException ex) {
            log.warn("High QPS blocked: {}", ex.getMessage());
            throw new BusinessException("请求过于频繁");
        } finally {
            if (entry != null) {
                entry.exit(1);
            }
        }
    }

    /**
     * Sentinel 状态查询
     */
    @GetMapping("/status")
    public ResultVO<Map<String, Object>> getSentinelStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("enabled", true);
        status.put("rulesLoaded", true);
        status.put("timestamp", LocalDateTime.now());

        return ResultVO.success(status);
    }
}