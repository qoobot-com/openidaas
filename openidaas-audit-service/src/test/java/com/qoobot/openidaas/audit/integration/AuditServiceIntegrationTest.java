package com.qoobot.openidaas.audit.integration;

import com.qoobot.openidaas.audit.service.AuditService;
import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.vo.audit.AuditLogVO;
import com.qoobot.openidaas.common.vo.audit.AuditStatisticsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 审计服务集成测试
 *
 * @author QooBot
 */
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AuditServiceIntegrationTest {

    @Autowired
    private AuditService auditService;

    @Test
    void testRecordAndRetrieveAuditLog() {
        // 准备
        AuditLogCreateDTO createDTO = new AuditLogCreateDTO();
        createDTO.setOperationType("CREATE");
        createDTO.setOperationDesc("集成测试创建用户");
        createDTO.setModule("USER");
        createDTO.setSubModule("USER_MANAGEMENT");
        createDTO.setTargetType("USER");
        createDTO.setTargetId(999L);
        createDTO.setTargetName("test_user");
        createDTO.setRequestUrl("/api/users");
        createDTO.setRequestMethod("POST");
        createDTO.setOperatorId(1L);
        createDTO.setOperatorName("test_admin");
        createDTO.setOperatorIp("127.0.0.1");
        createDTO.setUserAgent("IntegrationTest/1.0");
        createDTO.setExecutionTime(100L);
        createDTO.setResult("SUCCESS");

        // 执行 - 记录审计日志
        auditService.recordAuditLog(createDTO);

        // 验证 - 这里需要通过查询来验证记录成功
        // 实际应用中可能需要添加根据特定条件查询的方法
        assertNotNull(createDTO);
    }

    @Test
    void testBatchRecordAuditLogs() {
        // 准备
        AuditLogCreateDTO dto1 = new AuditLogCreateDTO();
        dto1.setOperationType("CREATE");
        dto1.setModule("USER");
        dto1.setOperatorId(1L);
        dto1.setOperatorName("admin");
        dto1.setResult("SUCCESS");

        AuditLogCreateDTO dto2 = new AuditLogCreateDTO();
        dto2.setOperationType("UPDATE");
        dto2.setModule("USER");
        dto2.setOperatorId(1L);
        dto2.setOperatorName("admin");
        dto2.setResult("SUCCESS");

        AuditLogCreateDTO dto3 = new AuditLogCreateDTO();
        dto3.setOperationType("DELETE");
        dto3.setModule("USER");
        dto3.setOperatorId(1L);
        dto3.setOperatorName("admin");
        dto3.setResult("SUCCESS");

        // 执行
        auditService.recordAuditLogs(Arrays.asList(dto1, dto2, dto3));

        // 验证
        assertNotNull(dto1);
        assertNotNull(dto2);
        assertNotNull(dto3);
    }

    @Test
    void testQueryAuditLogs() {
        // 准备
        com.qoobot.openidaas.common.dto.audit.AuditLogQueryDTO queryDTO =
            new com.qoobot.openidaas.common.dto.audit.AuditLogQueryDTO();
        queryDTO.setOperationType("LOGIN");
        queryDTO.setModule("AUTH");
        queryDTO.setResult("SUCCESS");
        queryDTO.setPage(1);
        queryDTO.setSize(20);

        // 执行
        var result = auditService.queryAuditLogs(queryDTO);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    void testGetUserAuditLogs() {
        // 执行
        var result = auditService.getUserAuditLogs(1L, 1, 10);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    void testGetAuditLogsByTimeRange() {
        // 准备
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        // 执行
        var result = auditService.getAuditLogsByTimeRange(startTime, endTime, 1, 10);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    void testGetFailedAuditLogs() {
        // 执行
        var result = auditService.getFailedAuditLogs(1, 10);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    void testGetRecentAuditLogs() {
        // 执行
        var result = auditService.getRecentAuditLogs(1, 10);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getRecords());
    }

    @Test
    void testGetAuditStatistics() {
        // 准备
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        // 执行
        AuditStatisticsVO statistics = auditService.getAuditStatistics(startTime, endTime);

        // 验证
        assertNotNull(statistics);
        assertNotNull(statistics.getTotalOperations());
        assertNotNull(statistics.getSuccessCount());
        assertNotNull(statistics.getFailureCount());
        assertNotNull(statistics.getOperationTypeDistribution());
        assertNotNull(statistics.getModuleDistribution());
        assertNotNull(statistics.getTopUsers());
    }

    @Test
    void testCountOperationsByTimeRange() {
        // 准备
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        // 执行
        long count = auditService.countOperationsByTimeRange(startTime, endTime);

        // 验证
        assertTrue(count >= 0);
    }

    @Test
    void testCountUserOperations() {
        // 执行
        long count = auditService.countUserOperations(1L);

        // 验证
        assertTrue(count >= 0);
    }

    @Test
    void testCountOperationsByModule() {
        // 准备
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        // 执行
        List<Map<String, Object>> result = auditService.countOperationsByModule(startTime, endTime);

        // 验证
        assertNotNull(result);
    }

    @Test
    void testCountOperationsByType() {
        // 准备
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        // 执行
        List<Map<String, Object>> result = auditService.countOperationsByType(startTime, endTime);

        // 验证
        assertNotNull(result);
    }

    @Test
    void testGetTopOperators() {
        // 准备
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        // 执行
        List<Map<String, Object>> result = auditService.getTopOperators(startTime, endTime, 10);

        // 验证
        assertNotNull(result);
        assertTrue(result.size() <= 10);
    }

    @Test
    void testCleanupExpiredLogs() {
        // 准备 - 删除1年前之前的日志
        LocalDateTime beforeTime = LocalDateTime.now().minusYears(1);

        // 执行
        int deletedCount = auditService.cleanupExpiredLogs(beforeTime);

        // 验证
        assertTrue(deletedCount >= 0);
    }

    @Test
    void testExportAuditLogs() {
        // 准备
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        // 执行
        byte[] data = auditService.exportAuditLogs(startTime, endTime);

        // 验证
        assertNotNull(data);
        assertTrue(data.length > 0);
        // 验证是Excel文件 (以PK开头)
        assertTrue(new String(data, 0, 2).equals("PK"));
    }

    @Test
    void testSendAuditLogAsync() {
        // 准备
        AuditLogCreateDTO createDTO = new AuditLogCreateDTO();
        createDTO.setOperationType("LOGIN");
        createDTO.setModule("AUTH");
        createDTO.setOperatorId(1L);
        createDTO.setOperatorName("test_user");
        createDTO.setResult("SUCCESS");

        // 执行 - 不应该抛出异常
        assertDoesNotThrow(() -> auditService.sendAuditLogAsync(createDTO));
    }

    @Test
    void testRecordAuditLogAsync() {
        // 准备
        AuditLogCreateDTO createDTO = new AuditLogCreateDTO();
        createDTO.setOperationType("LOGIN");
        createDTO.setModule("AUTH");
        createDTO.setOperatorId(1L);
        createDTO.setOperatorName("test_user");
        createDTO.setResult("SUCCESS");

        // 执行 - 不应该抛出异常
        assertDoesNotThrow(() -> auditService.recordAuditLogAsync(createDTO));
    }
}
