package com.qoobot.openidaas.common.feign;

import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.dto.audit.AuditLogQueryDTO;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 审计服务 Feign 客户端
 *
 * @author QooBot
 */
@FeignClient(
        name = "audit-service",
        configuration = com.qoobot.openidaas.common.config.FeignConfig.class
)
public interface AuditClient {

    /**
     * 创建审计日志（异步）
     */
    @PostMapping("/api/audit/logs")
    ResultVO<Void> createAuditLog(@RequestBody AuditLogCreateDTO createDTO);

    /**
     * 批量创建审计日志（异步）
     */
    @PostMapping("/api/audit/logs/batch")
    ResultVO<Void> createAuditLogBatch(@RequestBody java.util.List<AuditLogCreateDTO> createDTOs);

    /**
     * 查询审计日志（分页）
     */
    @PostMapping("/api/audit/logs/query")
    ResultVO<PageResultVO<Object>> queryAuditLogs(@RequestBody AuditLogQueryDTO queryDTO);

    /**
     * 根据日志 ID 获取审计日志详情
     */
    @GetMapping("/api/audit/logs/{id}")
    ResultVO<Object> getAuditLogById(@PathVariable("id") Long id);
}
