package com.qoobot.openidaas.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openidaas.audit.entity.AuditLog;
import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.dto.audit.AuditLogQueryDTO;
import com.qoobot.openidaas.common.vo.audit.AuditLogVO;
import com.qoobot.openidaas.common.vo.audit.AuditStatisticsVO;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志服务接口
 *
 * @author QooBot
 */
public interface AuditService {

    /**
     * 记录审计日志
     */
    void recordAuditLog(AuditLogCreateDTO createDTO);

    /**
     * 批量记录审计日志
     */
    void recordAuditLogs(List<AuditLogCreateDTO> createDTOList);

    /**
     * 通过Kafka消息异步记录审计日志
     */
    @KafkaListener(topics = "audit-log-topic", groupId = "audit-service-group")
    void recordAuditLogAsync(AuditLogCreateDTO createDTO);

    /**
     * 根据ID获取审计日志
     */
    AuditLogVO getAuditLogById(Long logId);

    /**
     * 分页查询审计日志
     */
    IPage<AuditLogVO> queryAuditLogs(AuditLogQueryDTO queryDTO);

    /**
     * 获取用户操作日志
     */
    IPage<AuditLogVO> getUserAuditLogs(Long userId, Integer page, Integer size);

    /**
     * 获取租户操作日志
     */
    IPage<AuditLogVO> getTenantAuditLogs(Long tenantId, Integer page, Integer size);

    /**
     * 获取应用操作日志
     */
    IPage<AuditLogVO> getAppAuditLogs(Long appId, Integer page, Integer size);

    /**
     * 根据操作类型获取审计日志
     */
    IPage<AuditLogVO> getAuditLogsByOperationType(String operationType, Integer page, Integer size);

    /**
     * 根据模块获取审计日志
     */
    IPage<AuditLogVO> getAuditLogsByModule(String module, Integer page, Integer size);

    /**
     * 根据时间范围获取审计日志
     */
    IPage<AuditLogVO> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Integer page, Integer size);

    /**
     * 获取失败的操作日志
     */
    IPage<AuditLogVO> getFailedAuditLogs(Integer page, Integer size);

    /**
     * 获取最近的操作日志
     */
    IPage<AuditLogVO> getRecentAuditLogs(Integer page, Integer size);

    /**
     * 获取审计统计数据
     */
    AuditStatisticsVO getAuditStatistics(LocalDateTime startTime, LocalDateTime endTime);

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
    List<Map<String, Object>> countOperationsByModule(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计操作类型分布
     */
    List<Map<String, Object>> countOperationsByType(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计热门操作用户
     */
    List<Map<String, Object>> getTopOperators(LocalDateTime startTime, LocalDateTime endTime, int limit);

    /**
     * 清理过期的审计日志
     */
    int cleanupExpiredLogs(LocalDateTime beforeTime);

    /**
     * 导出审计日志
     */
    byte[] exportAuditLogs(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 删除审计日志
     */
    void deleteAuditLog(Long logId);

    /**
     * 批量删除审计日志
     */
    void batchDeleteAuditLogs(List<Long> logIds);

    /**
     * 发送审计日志到Kafka（异步处理）
     */
    void sendAuditLogAsync(AuditLogCreateDTO createDTO);
}
