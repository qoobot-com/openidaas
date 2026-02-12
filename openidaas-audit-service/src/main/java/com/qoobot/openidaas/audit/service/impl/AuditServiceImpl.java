package com.qoobot.openidaas.audit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.audit.alert.AuditAlertManager;
import com.qoobot.openidaas.audit.converter.AuditLogConverter;
import com.qoobot.openidaas.audit.entity.AuditLog;
import com.qoobot.openidaas.audit.health.AuditHealthIndicator;
import com.qoobot.openidaas.audit.mapper.AuditLogMapper;
import com.qoobot.openidaas.audit.metrics.AuditMetricsCollector;
import com.qoobot.openidaas.audit.service.AuditService;
import com.qoobot.openidaas.audit.log.StructuredLogger;
import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.dto.audit.AuditLogQueryDTO;
import com.qoobot.openidaas.common.enumeration.AuditResultEnum;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.util.JsonUtil;
import com.qoobot.openidaas.common.vo.audit.AuditLogVO;
import com.qoobot.openidaas.common.vo.audit.AuditStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审计日志服务实现类
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogMapper auditLogMapper;
    private final AuditLogConverter auditLogConverter;
    private final KafkaTemplate<String, AuditLogCreateDTO> kafkaTemplate;
    private final AuditMetricsCollector metricsCollector;
    private final AuditHealthIndicator healthIndicator;
    private final StructuredLogger structuredLogger;
    private final AuditAlertManager alertManager;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordAuditLog(AuditLogCreateDTO createDTO) {
        metricsCollector.recordRecordDuration(() -> {
            log.debug("记录审计日志，操作类型：{}，操作人：{}", createDTO.getOperationType(), createDTO.getOperatorName());

            try {
                // 设置默认操作时间
                if (createDTO.getOperationTime() == null) {
                    createDTO.setOperationTime(LocalDateTime.now());
                }

                // 设置默认操作结果
                if (StringUtils.isBlank(createDTO.getResult())) {
                    createDTO.setResult(AuditResultEnum.SUCCESS.getCode());
                }

                AuditLog auditLog = auditLogConverter.toEntity(createDTO);
                auditLog.setCreatedAt(LocalDateTime.now());
                auditLog.setUpdatedAt(LocalDateTime.now());

                auditLogMapper.insert(auditLog);

                // 收集指标
                metricsCollector.incrementTotalLogs();
                metricsCollector.incrementByOperationType(createDTO.getOperationType());
                if (createDTO.getModule() != null) {
                    metricsCollector.incrementByModule(createDTO.getModule());
                }
                if (createDTO.getTenantId() != null) {
                    metricsCollector.incrementByTenant(createDTO.getTenantId());
                }

                // 记录成功
                if (AuditResultEnum.SUCCESS.getCode().equals(auditLog.getResult())) {
                    metricsCollector.incrementSuccessLogs();
                    healthIndicator.recordSuccess();
                } else {
                    metricsCollector.incrementFailedLogs();
                    healthIndicator.recordFailure(auditLog.getErrorMessage());
                }

                // 结构化日志
                structuredLogger.logAuditOperation(
                        createDTO.getOperationType(),
                        createDTO.getModule() != null ? createDTO.getModule() : "unknown",
                        createDTO.getOperatorName() != null ? createDTO.getOperatorName() : "unknown",
                        createDTO.getTargetName() != null ? createDTO.getTargetName() : "unknown",
                        AuditResultEnum.SUCCESS.getCode().equals(auditLog.getResult()),
                        createDTO.getExecutionTime() != null ? createDTO.getExecutionTime() : 0
                );

                log.debug("审计日志记录成功，日志ID：{}", auditLog.getId());
            } catch (Exception e) {
                log.error("记录审计日志失败", e);
                metricsCollector.incrementFailedLogs();
                healthIndicator.recordFailure(e.getMessage());
                alertManager.recordError("recordAuditLog", e.getMessage());
                // 审计日志记录失败不影响主业务流程
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordAuditLogs(List<AuditLogCreateDTO> createDTOList) {
        log.debug("批量记录审计日志，数量：{}", createDTOList.size());

        if (createDTOList == null || createDTOList.isEmpty()) {
            return;
        }

        try {
            for (AuditLogCreateDTO createDTO : createDTOList) {
                recordAuditLog(createDTO);
            }
            log.debug("批量审计日志记录成功");
        } catch (Exception e) {
            log.error("批量记录审计日志失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordAuditLogAsync(AuditLogCreateDTO createDTO) {
        log.debug("异步记录审计日志，操作类型：{}", createDTO.getOperationType());
        recordAuditLog(createDTO);
    }

    @Override
    public void sendAuditLogAsync(AuditLogCreateDTO createDTO) {
        try {
            kafkaTemplate.send("audit-log-topic", createDTO);
            log.debug("审计日志已发送到Kafka，操作类型：{}", createDTO.getOperationType());
        } catch (Exception e) {
            log.error("发送审计日志到Kafka失败", e);
        }
    }

    @Override
    public AuditLogVO getAuditLogById(Long logId) {
        return metricsCollector.recordQueryDuration(() -> {
            log.debug("获取审计日志，ID：{}", logId);

            AuditLog auditLog = auditLogMapper.selectById(logId);
            if (auditLog == null) {
                throw new BusinessException("审计日志不存在");
            }

            AuditLogVO result = auditLogConverter.toVO(auditLog);

            structuredLogger.logAuditQuery("getById", "id=" + logId, 1, 0);

            return result;
        });
    }

    @Override
    public IPage<AuditLogVO> queryAuditLogs(AuditLogQueryDTO queryDTO) {
        log.debug("查询审计日志，查询条件：{}", queryDTO);

        Page<AuditLog> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
        LambdaQueryWrapper<AuditLog> queryWrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (StringUtils.isNotBlank(queryDTO.getOperationType())) {
            queryWrapper.eq(AuditLog::getOperationType, queryDTO.getOperationType());
        }
        if (StringUtils.isNotBlank(queryDTO.getModule())) {
            queryWrapper.eq(AuditLog::getModule, queryDTO.getModule());
        }
        if (StringUtils.isNotBlank(queryDTO.getSubModule())) {
            queryWrapper.eq(AuditLog::getSubModule, queryDTO.getSubModule());
        }
        if (StringUtils.isNotBlank(queryDTO.getTargetType())) {
            queryWrapper.eq(AuditLog::getTargetType, queryDTO.getTargetType());
        }
        if (queryDTO.getTargetId() != null) {
            queryWrapper.eq(AuditLog::getTargetId, queryDTO.getTargetId());
        }
        if (queryDTO.getOperatorId() != null) {
            queryWrapper.eq(AuditLog::getOperatorId, queryDTO.getOperatorId());
        }
        if (StringUtils.isNotBlank(queryDTO.getOperatorName())) {
            queryWrapper.like(AuditLog::getOperatorName, queryDTO.getOperatorName());
        }
        if (StringUtils.isNotBlank(queryDTO.getResult())) {
            queryWrapper.eq(AuditLog::getResult, queryDTO.getResult());
        }
        if (queryDTO.getTenantId() != null) {
            queryWrapper.eq(AuditLog::getTenantId, queryDTO.getTenantId());
        }
        if (queryDTO.getAppId() != null) {
            queryWrapper.eq(AuditLog::getAppId, queryDTO.getAppId());
        }
        if (queryDTO.getStartTime() != null) {
            queryWrapper.ge(AuditLog::getOperationTime, queryDTO.getStartTime());
        }
        if (queryDTO.getEndTime() != null) {
            queryWrapper.le(AuditLog::getOperationTime, queryDTO.getEndTime());
        }

        // 按操作时间倒序排列
        queryWrapper.orderByDesc(AuditLog::getOperationTime);

        Page<AuditLog> resultPage = auditLogMapper.selectPage(page, queryWrapper);

        // 转换为VO
        Page<AuditLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<AuditLogVO> voList = resultPage.getRecords().stream()
                .map(auditLogConverter::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public IPage<AuditLogVO> getUserAuditLogs(Long userId, Integer page, Integer size) {
        log.debug("获取用户操作日志，用户ID：{}", userId);

        Page<AuditLog> pageParam = new Page<>(page, size);
        IPage<AuditLog> resultPage = auditLogMapper.selectByOperatorId(userId, pageParam);

        Page<AuditLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<AuditLogVO> voList = resultPage.getRecords().stream()
                .map(auditLogConverter::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public IPage<AuditLogVO> getTenantAuditLogs(Long tenantId, Integer page, Integer size) {
        log.debug("获取租户操作日志，租户ID：{}", tenantId);

        Page<AuditLog> pageParam = new Page<>(page, size);
        IPage<AuditLog> resultPage = auditLogMapper.selectByTenantId(tenantId, pageParam);

        Page<AuditLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<AuditLogVO> voList = resultPage.getRecords().stream()
                .map(auditLogConverter::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public IPage<AuditLogVO> getAppAuditLogs(Long appId, Integer page, Integer size) {
        log.debug("获取应用操作日志，应用ID：{}", appId);

        Page<AuditLog> pageParam = new Page<>(page, size);
        IPage<AuditLog> resultPage = auditLogMapper.selectByAppId(appId, pageParam);

        Page<AuditLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<AuditLogVO> voList = resultPage.getRecords().stream()
                .map(auditLogConverter::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public IPage<AuditLogVO> getAuditLogsByOperationType(String operationType, Integer page, Integer size) {
        log.debug("根据操作类型获取审计日志，操作类型：{}", operationType);

        Page<AuditLog> pageParam = new Page<>(page, size);
        IPage<AuditLog> resultPage = auditLogMapper.selectByOperationType(operationType, pageParam);

        Page<AuditLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<AuditLogVO> voList = resultPage.getRecords().stream()
                .map(auditLogConverter::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public IPage<AuditLogVO> getAuditLogsByModule(String module, Integer page, Integer size) {
        log.debug("根据模块获取审计日志，模块：{}", module);

        Page<AuditLog> pageParam = new Page<>(page, size);
        IPage<AuditLog> resultPage = auditLogMapper.selectByModule(module, pageParam);

        Page<AuditLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<AuditLogVO> voList = resultPage.getRecords().stream()
                .map(auditLogConverter::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public IPage<AuditLogVO> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Integer page, Integer size) {
        log.debug("根据时间范围获取审计日志，开始时间：{}，结束时间：{}", startTime, endTime);

        Page<AuditLog> pageParam = new Page<>(page, size);
        IPage<AuditLog> resultPage = auditLogMapper.selectByTimeRange(startTime, endTime, pageParam);

        Page<AuditLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<AuditLogVO> voList = resultPage.getRecords().stream()
                .map(auditLogConverter::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public IPage<AuditLogVO> getFailedAuditLogs(Integer page, Integer size) {
        log.debug("获取失败的操作日志");

        Page<AuditLog> pageParam = new Page<>(page, size);
        IPage<AuditLog> resultPage = auditLogMapper.selectFailedLogs(pageParam);

        Page<AuditLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<AuditLogVO> voList = resultPage.getRecords().stream()
                .map(auditLogConverter::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public IPage<AuditLogVO> getRecentAuditLogs(Integer page, Integer size) {
        log.debug("获取最近的操作日志");

        Page<AuditLog> pageParam = new Page<>(page, size);
        IPage<AuditLog> resultPage = auditLogMapper.selectRecentLogs(pageParam);

        Page<AuditLogVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<AuditLogVO> voList = resultPage.getRecords().stream()
                .map(auditLogConverter::toVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public AuditStatisticsVO getAuditStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        log.debug("获取审计统计数据，开始时间：{}，结束时间：{}", startTime, endTime);

        AuditStatisticsVO statistics = new AuditStatisticsVO();

        // 总操作次数
        long totalOperations = countOperationsByTimeRange(startTime, endTime);
        statistics.setTotalOperations(totalOperations);

        // 操作结果分布
        List<Map<String, Object>> resultDistribution = auditLogMapper.countByResultAndTimeRange(startTime, endTime);
        for (Map<String, Object> item : resultDistribution) {
            String result = (String) item.get("result");
            Long count = ((Number) item.get("count")).longValue();
            if (AuditResultEnum.SUCCESS.getCode().equals(result)) {
                statistics.setSuccessCount(count);
            } else if (AuditResultEnum.FAILURE.getCode().equals(result)) {
                statistics.setFailureCount(count);
            }
        }

        // 操作类型分布
        List<Map<String, Object>> typeDistribution = countOperationsByType(startTime, endTime);
        Map<String, Long> typeMap = new HashMap<>();
        for (Map<String, Object> item : typeDistribution) {
            String type = (String) item.get("operation_type");
            Long count = ((Number) item.get("count")).longValue();
            typeMap.put(type, count);
        }
        statistics.setOperationTypeDistribution(typeMap);

        // 模块操作分布
        List<Map<String, Object>> moduleDistribution = countOperationsByModule(startTime, endTime);
        Map<String, Long> moduleMap = new HashMap<>();
        for (Map<String, Object> item : moduleDistribution) {
            String module = (String) item.get("module");
            Long count = ((Number) item.get("count")).longValue();
            moduleMap.put(module, count);
        }
        statistics.setModuleDistribution(moduleMap);

        // 热门操作用户
        List<Map<String, Object>> topUsers = getTopOperators(startTime, endTime, 10);
        Map<String, Long> userMap = new HashMap<>();
        for (Map<String, Object> item : topUsers) {
            String userName = (String) item.get("operator_name");
            Long count = ((Number) item.get("count")).longValue();
            userMap.put(userName, count);
        }
        statistics.setTopUsers(userMap);

        return statistics;
    }

    @Override
    public long countOperationsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.countByTimeRange(startTime, endTime);
    }

    @Override
    public long countUserOperations(Long userId) {
        return auditLogMapper.countByOperatorId(userId);
    }

    @Override
    public List<Map<String, Object>> countOperationsByModule(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.countByModuleAndTimeRange(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> countOperationsByType(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogMapper.countByOperationTypeAndTimeRange(startTime, endTime);
    }

    @Override
    public List<Map<String, Object>> getTopOperators(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        return auditLogMapper.countTopOperatorsByTimeRange(startTime, endTime, limit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cleanupExpiredLogs(LocalDateTime beforeTime) {
        log.info("清理过期的审计日志，截止时间：{}", beforeTime);

        int deletedCount = auditLogMapper.deleteByOperationTimeBefore(beforeTime);
        log.info("清理过期审计日志完成，删除数量：{}", deletedCount);

        structuredLogger.logAuditCleanup(beforeTime.toEpochSecond(java.time.ZoneOffset.UTC), deletedCount);

        return deletedCount;
    }

    @Override
    public byte[] exportAuditLogs(LocalDateTime startTime, LocalDateTime endTime) {
        return metricsCollector.recordExportDuration(() -> {
            log.debug("导出审计日志，开始时间：{}，结束时间：{}", startTime, endTime);

            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                // 创建工作表
                Sheet sheet = workbook.createSheet("审计日志");

                // 创建表头样式
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                // 创建表头
                Row headerRow = sheet.createRow(0);
                String[] headers = {"操作时间", "操作类型", "操作描述", "操作人", "操作人IP", "操作结果", "执行耗时", "模块", "目标"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // 查询数据
                IPage<AuditLogVO> auditLogs = getAuditLogsByTimeRange(startTime, endTime, 1, 10000);

                // 填充数据
                int rowNum = 1;
                for (AuditLogVO log : auditLogs.getRecords()) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(log.getOperationTime() != null ? log.getOperationTime().format(DATE_FORMATTER) : "");
                    row.createCell(1).setCellValue(log.getOperationType() != null ? log.getOperationType() : "");
                    row.createCell(2).setCellValue(log.getOperationDesc() != null ? log.getOperationDesc() : "");
                    row.createCell(3).setCellValue(log.getOperatorName() != null ? log.getOperatorName() : "");
                    row.createCell(4).setCellValue(log.getOperatorIp() != null ? log.getOperatorIp() : "");
                    row.createCell(5).setCellValue(log.getResult() != null ? log.getResult() : "");
                    row.createCell(6).setCellValue(log.getExecutionTime() != null ? log.getExecutionTime() : 0);
                    row.createCell(7).setCellValue(log.getModule() != null ? log.getModule() : "");
                    row.createCell(8).setCellValue(log.getTargetName() != null ? log.getTargetName() : "");
                }

                // 自动调整列宽
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                byte[] result = out.toByteArray();
                workbook.write(out);

                // 结构化日志
                structuredLogger.logAuditExport("xlsx",
                        String.format("%s ~ %s", startTime, endTime),
                        auditLogs.getRecords().size(),
                        result.length);

                return result;

            } catch (Exception e) {
                log.error("导出审计日志失败", e);
                healthIndicator.recordFailure(e.getMessage());
                alertManager.recordError("exportAuditLogs", e.getMessage());
                throw new BusinessException("导出审计日志失败");
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAuditLog(Long logId) {
        log.info("删除审计日志，日志ID：{}", logId);

        AuditLog auditLog = auditLogMapper.selectById(logId);
        if (auditLog == null) {
            throw new BusinessException("审计日志不存在");
        }

        auditLogMapper.deleteById(logId);
        log.info("审计日志删除成功，日志ID：{}", logId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteAuditLogs(List<Long> logIds) {
        log.info("批量删除审计日志，数量：{}", logIds.size());

        if (logIds == null || logIds.isEmpty()) {
            return;
        }

        auditLogMapper.deleteBatchIds(logIds);
        log.info("批量删除审计日志成功，数量：{}", logIds.size());
    }
}
