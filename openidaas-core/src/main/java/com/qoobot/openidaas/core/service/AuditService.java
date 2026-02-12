package com.qoobot.openidaas.core.service;

import com.qoobot.openidaas.core.domain.AuditLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计服务接口
 *
 * @author QooBot
 */
public interface AuditService {

    /**
     * 记录审计日志
     */
    void recordAuditLog(AuditLog auditLog);

    /**
     * 批量记录审计日志
     */
    void recordAuditLogs(List<AuditLog> auditLogs);

    /**
     * 根据ID获取审计日志
     */
    AuditLog getAuditLogById(Long logId);

    /**
     * 获取用户操作日志
     */
    List<AuditLog> getUserAuditLogs(Long userId, int page, int size);

    /**
     * 获取租户操作日志
     */
    List<AuditLog> getTenantAuditLogs(Long tenantId, int page, int size);

    /**
     * 获取应用操作日志
     */
    List<AuditLog> getAppAuditLogs(Long appId, int page, int size);

    /**
     * 根据操作类型获取审计日志
     */
    List<AuditLog> getAuditLogsByOperationType(String operationType, int page, int size);

    /**
     * 根据模块获取审计日志
     */
    List<AuditLog> getAuditLogsByModule(String module, int page, int size);

    /**
     * 根据时间范围获取审计日志
     */
    List<AuditLog> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, int page, int size);

    /**
     * 获取失败的操作日志
     */
    List<AuditLog> getFailedAuditLogs(int page, int size);

    /**
     * 获取最近的操作日志
     */
    List<AuditLog> getRecentAuditLogs(int page, int size);

    /**
     * 统计指定时间范围内的操作次数
     */
    long countOperationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户操作次数
     */
    long countUserOperations(Long userId);

    /**
     * 统计模块操作分布
     */
    List<Object[]> countOperationsByModule(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计操作类型分布
     */
    List<Object[]> countOperationsByType(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理过期的审计日志
     */
    int cleanupExpiredLogs(LocalDateTime beforeTime);

    /**
     * 导出审计日志
     */
    byte[] exportAuditLogs(LocalDateTime startTime, LocalDateTime endTime);
}