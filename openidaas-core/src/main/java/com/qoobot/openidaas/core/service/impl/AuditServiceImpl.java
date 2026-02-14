package com.qoobot.openidaas.core.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.AuditLog;
import com.qoobot.openidaas.core.mapper.AuditLogMapper;
import com.qoobot.openidaas.core.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计服务实现类（MyBatis-Plus版本）
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional
    public void recordAuditLog(AuditLog auditLog) {
        if (auditLog.getOperationTime() == null) {
            auditLog.setOperationTime(LocalDateTime.now());
        }
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(auditLog);
    }

    @Override
    @Transactional
    public void recordAuditLogs(List<AuditLog> auditLogs) {
        LocalDateTime now = LocalDateTime.now();
        auditLogs.forEach(log -> {
            if (log.getOperationTime() == null) {
                log.setOperationTime(now);
            }
            log.setCreatedAt(now);
        });

        for (AuditLog auditLog : auditLogs) {
            auditLogMapper.insert(auditLog);
        }
    }

    @Override
    public AuditLog getAuditLogById(Long logId) {
        return auditLogMapper.selectById(logId);
    }

    @Override
    public List<AuditLog> getUserAuditLogs(Long userId, int page, int size) {
        Page<AuditLog> pageObj = new Page<>(page, size);
        IPage<AuditLog> result = auditLogMapper.findByOperatorId(pageObj, userId);
        return result.getRecords();
    }

    @Override
    public List<AuditLog> getTenantAuditLogs(Long tenantId, int page, int size) {
        Page<AuditLog> pageObj = new Page<>(page, size);
        IPage<AuditLog> result = auditLogMapper.findByTenantId(pageObj, tenantId);
        return result.getRecords();
    }

    @Override
    public List<AuditLog> getAppAuditLogs(Long appId, int page, int size) {
        Page<AuditLog> pageObj = new Page<>(page, size);
        IPage<AuditLog> result = auditLogMapper.findByAppId(pageObj, appId);
        return result.getRecords();
    }

    @Override
    public List<AuditLog> getAuditLogsByOperationType(String operationType, int page, int size) {
        Page<AuditLog> pageObj = new Page<>(page, size);
        IPage<AuditLog> result = auditLogMapper.findByOperationType(pageObj, operationType);
        return result.getRecords();
    }

    @Override
    public List<AuditLog> getAuditLogsByModule(String module, int page, int size) {
        Page<AuditLog> pageObj = new Page<>(page, size);
        IPage<AuditLog> result = auditLogMapper.findByModule(pageObj, module);
        return result.getRecords();
    }

    @Override
    public List<AuditLog> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, int page, int size) {
        Page<AuditLog> pageObj = new Page<>(page, size);
        IPage<AuditLog> result = auditLogMapper.findByOperationTimeBetween(pageObj, startTime, endTime);
        return result.getRecords();
    }

    @Override
    public List<AuditLog> getFailedAuditLogs(int page, int size) {
        Page<AuditLog> pageObj = new Page<>(page, size);
        IPage<AuditLog> result = auditLogMapper.findFailedLogs(pageObj);
        return result.getRecords();
    }

    @Override
    public List<AuditLog> getRecentAuditLogs(int page, int size) {
        Page<AuditLog> pageObj = new Page<>(page, size);
        IPage<AuditLog> result = auditLogMapper.findRecentLogs(pageObj);
        return result.getRecords();
    }

    @Override
    public long countOperationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.countByOperationTimeBetween(startTime, endTime);
    }

    @Override
    public long countUserOperations(Long userId) {
        return auditLogMapper.countByOperatorId(userId);
    }

    @Override
    public List<Object[]> countOperationsByModule(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.countByModuleAndOperationTimeBetween(startTime, endTime);
    }

    @Override
    public List<Object[]> countOperationsByType(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.countByOperationTypeAndOperationTimeBetween(startTime, endTime);
    }

    @Override
    @Transactional
    public int cleanupExpiredLogs(LocalDateTime beforeTime) {
        return auditLogMapper.deleteByOperationTimeBefore(beforeTime);
    }
}
