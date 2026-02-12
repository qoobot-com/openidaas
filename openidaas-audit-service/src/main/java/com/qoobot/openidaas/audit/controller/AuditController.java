package com.qoobot.openidaas.audit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openidaas.audit.service.AuditService;
import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.dto.audit.AuditLogQueryDTO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.audit.AuditLogVO;
import com.qoobot.openidaas.common.vo.audit.AuditStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志Controller
 *
 * @author QooBot
 */
@Slf4j
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Tag(name = "审计日志", description = "审计日志相关接口")
public class AuditController {

    private final AuditService auditService;

    /**
     * 记录审计日志
     */
    @PostMapping("/logs")
    @Operation(summary = "记录审计日志", description = "记录一条审计日志")
    public ResultVO<Void> recordAuditLog(@RequestBody AuditLogCreateDTO createDTO) {
        auditService.recordAuditLog(createDTO);
        return ResultVO.success();
    }

    /**
     * 查询审计日志 - 符合OpenAPI规范，使用GET方法
     */
    @GetMapping("/logs")
    @Operation(summary = "查询审计日志", description = "分页查询系统操作审计日志")
    public ResultVO<IPage<AuditLogVO>> queryAuditLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "用户ID筛选") @RequestParam(required = false) Long operatorId,
            @Parameter(description = "操作类型筛选") @RequestParam(required = false) String operationType,
            @Parameter(description = "开始时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        AuditLogQueryDTO queryDTO = new AuditLogQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setOperatorId(operatorId);
        queryDTO.setOperationType(operationType);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        IPage<AuditLogVO> result = auditService.queryAuditLogs(queryDTO);
        return ResultVO.success(result);
    }

    /**
     * 批量记录审计日志
     */
    @PostMapping("/logs/batch")
    @Operation(summary = "批量记录审计日志", description = "批量记录审计日志")
    public ResultVO<Void> recordAuditLogs(@RequestBody List<AuditLogCreateDTO> createDTOList) {
        auditService.recordAuditLogs(createDTOList);
        return ResultVO.success();
    }

    /**
     * 异步记录审计日志
     */
    @PostMapping("/logs/async")
    @Operation(summary = "异步记录审计日志", description = "通过Kafka异步记录审计日志")
    public ResultVO<Void> recordAuditLogAsync(@RequestBody AuditLogCreateDTO createDTO) {
        auditService.sendAuditLogAsync(createDTO);
        return ResultVO.success();
    }

    /**
     * 根据ID获取审计日志
     */
    @GetMapping("/logs/{logId}")
    @Operation(summary = "获取审计日志详情", description = "根据ID获取审计日志详情")
    public ResultVO<AuditLogVO> getAuditLogById(
            @Parameter(description = "日志ID") @PathVariable Long logId) {
        AuditLogVO auditLogVO = auditService.getAuditLogById(logId);
        return ResultVO.success(auditLogVO);
    }

    /**
     * 分页查询审计日志
     */
    @PostMapping("/logs/query")
    @Operation(summary = "分页查询审计日志", description = "根据条件分页查询审计日志")
    public ResultVO<IPage<AuditLogVO>> queryAuditLogs(@RequestBody AuditLogQueryDTO queryDTO) {
        IPage<AuditLogVO> page = auditService.queryAuditLogs(queryDTO);
        return ResultVO.success(page);
    }

    /**
     * 获取用户操作日志
     */
    @GetMapping("/logs/users/{userId}")
    @Operation(summary = "获取用户操作日志", description = "获取指定用户的操作日志")
    public ResultVO<IPage<AuditLogVO>> getUserAuditLogs(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {
        IPage<AuditLogVO> result = auditService.getUserAuditLogs(userId, page, size);
        return ResultVO.success(result);
    }

    /**
     * 获取租户操作日志
     */
    @GetMapping("/logs/tenants/{tenantId}")
    @Operation(summary = "获取租户操作日志", description = "获取指定租户的操作日志")
    public ResultVO<IPage<AuditLogVO>> getTenantAuditLogs(
            @Parameter(description = "租户ID") @PathVariable Long tenantId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {
        IPage<AuditLogVO> result = auditService.getTenantAuditLogs(tenantId, page, size);
        return ResultVO.success(result);
    }

    /**
     * 获取应用操作日志
     */
    @GetMapping("/logs/apps/{appId}")
    @Operation(summary = "获取应用操作日志", description = "获取指定应用的操作日志")
    public ResultVO<IPage<AuditLogVO>> getAppAuditLogs(
            @Parameter(description = "应用ID") @PathVariable Long appId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {
        IPage<AuditLogVO> result = auditService.getAppAuditLogs(appId, page, size);
        return ResultVO.success(result);
    }

    /**
     * 根据操作类型获取审计日志
     */
    @GetMapping("/logs/operation-type/{operationType}")
    @Operation(summary = "按操作类型获取审计日志", description = "根据操作类型获取审计日志")
    public ResultVO<IPage<AuditLogVO>> getAuditLogsByOperationType(
            @Parameter(description = "操作类型") @PathVariable String operationType,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {
        IPage<AuditLogVO> result = auditService.getAuditLogsByOperationType(operationType, page, size);
        return ResultVO.success(result);
    }

    /**
     * 根据模块获取审计日志
     */
    @GetMapping("/logs/module/{module}")
    @Operation(summary = "按模块获取审计日志", description = "根据模块获取审计日志")
    public ResultVO<IPage<AuditLogVO>> getAuditLogsByModule(
            @Parameter(description = "模块名称") @PathVariable String module,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {
        IPage<AuditLogVO> result = auditService.getAuditLogsByModule(module, page, size);
        return ResultVO.success(result);
    }

    /**
     * 根据时间范围获取审计日志
     */
    @GetMapping("/logs/time-range")
    @Operation(summary = "按时间范围获取审计日志", description = "根据时间范围获取审计日志")
    public ResultVO<IPage<AuditLogVO>> getAuditLogsByTimeRange(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {
        IPage<AuditLogVO> result = auditService.getAuditLogsByTimeRange(startTime, endTime, page, size);
        return ResultVO.success(result);
    }

    /**
     * 获取失败的操作日志
     */
    @GetMapping("/logs/failed")
    @Operation(summary = "获取失败的操作日志", description = "获取所有失败的操作日志")
    public ResultVO<IPage<AuditLogVO>> getFailedAuditLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {
        IPage<AuditLogVO> result = auditService.getFailedAuditLogs(page, size);
        return ResultVO.success(result);
    }

    /**
     * 获取最近的操作日志
     */
    @GetMapping("/logs/recent")
    @Operation(summary = "获取最近的操作日志", description = "获取最近的操作日志")
    public ResultVO<IPage<AuditLogVO>> getRecentAuditLogs(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer size) {
        IPage<AuditLogVO> result = auditService.getRecentAuditLogs(page, size);
        return ResultVO.success(result);
    }

    /**
     * 获取审计统计数据
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取审计统计数据", description = "获取指定时间范围内的审计统计数据")
    public ResultVO<AuditStatisticsVO> getAuditStatistics(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        AuditStatisticsVO statistics = auditService.getAuditStatistics(startTime, endTime);
        return ResultVO.success(statistics);
    }

    /**
     * 统计指定时间范围内的操作次数
     */
    @GetMapping("/statistics/count")
    @Operation(summary = "统计操作次数", description = "统计指定时间范围内的操作次数")
    public ResultVO<Long> countOperationsByTimeRange(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        long count = auditService.countOperationsByTimeRange(startTime, endTime);
        return ResultVO.success(count);
    }

    /**
     * 统计用户操作次数
     */
    @GetMapping("/statistics/users/{userId}/count")
    @Operation(summary = "统计用户操作次数", description = "统计指定用户的操作总次数")
    public ResultVO<Long> countUserOperations(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        long count = auditService.countUserOperations(userId);
        return ResultVO.success(count);
    }

    /**
     * 统计模块操作分布
     */
    @GetMapping("/statistics/module-distribution")
    @Operation(summary = "统计模块操作分布", description = "统计指定时间范围内各模块的操作次数")
    public ResultVO<List<Map<String, Object>>> countOperationsByModule(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<Map<String, Object>> result = auditService.countOperationsByModule(startTime, endTime);
        return ResultVO.success(result);
    }

    /**
     * 统计操作类型分布
     */
    @GetMapping("/statistics/operation-type-distribution")
    @Operation(summary = "统计操作类型分布", description = "统计指定时间范围内各操作类型的次数")
    public ResultVO<List<Map<String, Object>>> countOperationsByType(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        List<Map<String, Object>> result = auditService.countOperationsByType(startTime, endTime);
        return ResultVO.success(result);
    }

    /**
     * 清理过期的审计日志
     */
    @DeleteMapping("/logs/cleanup")
    @Operation(summary = "清理过期审计日志", description = "删除指定时间之前的所有审计日志")
    public ResultVO<Integer> cleanupExpiredLogs(
            @Parameter(description = "截止时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        int deletedCount = auditService.cleanupExpiredLogs(beforeTime);
        return ResultVO.success(deletedCount);
    }

    /**
     * 导出审计日志
     */
    @GetMapping("/logs/export")
    @Operation(summary = "导出审计日志", description = "导出指定时间范围内的审计日志为Excel文件")
    public ResponseEntity<byte[]> exportAuditLogs(
            @Parameter(description = "开始时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        byte[] data = auditService.exportAuditLogs(startTime, endTime);
        String filename = "审计日志_" + startTime.toString() + "_至_" + endTime.toString() + ".xlsx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", URLEncoder.encode(filename, StandardCharsets.UTF_8));

        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    /**
     * 删除审计日志
     */
    @DeleteMapping("/logs/{logId}")
    @Operation(summary = "删除审计日志", description = "根据ID删除审计日志")
    public ResultVO<Void> deleteAuditLog(
            @Parameter(description = "日志ID") @PathVariable Long logId) {
        auditService.deleteAuditLog(logId);
        return ResultVO.success();
    }

    /**
     * 批量删除审计日志
     */
    @DeleteMapping("/logs/batch")
    @Operation(summary = "批量删除审计日志", description = "批量删除审计日志")
    public ResultVO<Void> batchDeleteAuditLogs(@RequestBody List<Long> logIds) {
        auditService.batchDeleteAuditLogs(logIds);
        return ResultVO.success();
    }
}
