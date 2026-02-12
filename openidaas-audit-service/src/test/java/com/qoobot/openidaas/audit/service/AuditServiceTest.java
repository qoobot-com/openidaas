package com.qoobot.openidaas.audit.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.audit.converter.AuditLogConverter;
import com.qoobot.openidaas.audit.entity.AuditLog;
import com.qoobot.openidaas.audit.mapper.AuditLogMapper;
import com.qoobot.openidaas.audit.service.impl.AuditServiceImpl;
import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.dto.audit.AuditLogQueryDTO;
import com.qoobot.openidaas.common.enumeration.AuditResultEnum;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.audit.AuditLogVO;
import com.qoobot.openidaas.common.vo.audit.AuditStatisticsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 审计服务单元测试
 *
 * @author QooBot
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogMapper auditLogMapper;

    @Mock
    private AuditLogConverter auditLogConverter;

    @Mock
    private KafkaTemplate<String, AuditLogCreateDTO> kafkaTemplate;

    @InjectMocks
    private AuditServiceImpl auditService;

    private AuditLogCreateDTO testCreateDTO;
    private AuditLog testAuditLog;
    private AuditLogVO testAuditLogVO;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        testCreateDTO = new AuditLogCreateDTO();
        testCreateDTO.setOperationType("CREATE");
        testCreateDTO.setOperationDesc("创建用户");
        testCreateDTO.setModule("USER");
        testCreateDTO.setTargetType("USER");
        testCreateDTO.setTargetId(1L);
        testCreateDTO.setTargetName("testuser");
        testCreateDTO.setOperatorId(1L);
        testCreateDTO.setOperatorName("admin");
        testCreateDTO.setOperatorIp("192.168.1.1");
        testCreateDTO.setRequestUrl("/api/users");
        testCreateDTO.setRequestMethod("POST");
        testCreateDTO.setExecutionTime(150L);
        testCreateDTO.setResult("SUCCESS");

        testAuditLog = new AuditLog();
        testAuditLog.setId(1L);
        testAuditLog.setOperationType("CREATE");
        testAuditLog.setOperationDesc("创建用户");
        testAuditLog.setModule("USER");
        testAuditLog.setTargetType("USER");
        testAuditLog.setTargetId(1L);
        testAuditLog.setTargetName("testuser");
        testAuditLog.setOperatorId(1L);
        testAuditLog.setOperatorName("admin");
        testAuditLog.setOperatorIp("192.168.1.1");
        testAuditLog.setRequestUrl("/api/users");
        testAuditLog.setRequestMethod("POST");
        testAuditLog.setExecutionTime(150L);
        testAuditLog.setResult("SUCCESS");
        testAuditLog.setOperationTime(LocalDateTime.now());
        testAuditLog.setCreatedAt(LocalDateTime.now());

        testAuditLogVO = new AuditLogVO();
        testAuditLogVO.setId(1L);
        testAuditLogVO.setOperationType("CREATE");
        testAuditLogVO.setOperationDesc("创建用户");
        testAuditLogVO.setModule("USER");
        testAuditLogVO.setTargetType("USER");
        testAuditLogVO.setTargetId(1L);
        testAuditLogVO.setTargetName("testuser");
        testAuditLogVO.setOperatorId(1L);
        testAuditLogVO.setOperatorName("admin");
        testAuditLogVO.setOperatorIp("192.168.1.1");
        testAuditLogVO.setRequestUrl("/api/users");
        testAuditLogVO.setRequestMethod("POST");
        testAuditLogVO.setExecutionTime(150L);
        testAuditLogVO.setResult("SUCCESS");
        testAuditLogVO.setOperationTime(LocalDateTime.now());
    }

    @Test
    void testRecordAuditLog_Success() {
        // 准备
        when(auditLogConverter.toEntity(any(AuditLogCreateDTO.class))).thenReturn(testAuditLog);
        when(auditLogMapper.insert(any(AuditLog.class))).thenReturn(1);

        // 执行
        auditService.recordAuditLog(testCreateDTO);

        // 验证
        verify(auditLogConverter, times(1)).toEntity(any(AuditLogCreateDTO.class));
        verify(auditLogMapper, times(1)).insert(any(AuditLog.class));
    }

    @Test
    void testRecordAuditLog_WithDefaultValues() {
        // 准备
        AuditLogCreateDTO dto = new AuditLogCreateDTO();
        dto.setOperationType("CREATE");
        dto.setOperatorId(1L);
        dto.setOperatorName("admin");

        when(auditLogConverter.toEntity(any(AuditLogCreateDTO.class))).thenReturn(testAuditLog);
        when(auditLogMapper.insert(any(AuditLog.class))).thenReturn(1);

        // 执行
        auditService.recordAuditLog(dto);

        // 验证
        verify(auditLogMapper, times(1)).insert(any(AuditLog.class));
        assertNotNull(testAuditLog.getOperationTime());
        assertNotNull(testAuditLog.getCreatedAt());
    }

    @Test
    void testRecordAuditLogs_EmptyList() {
        // 执行
        auditService.recordAuditLogs(new ArrayList<>());

        // 验证
        verify(auditLogMapper, never()).insert(any(AuditLog.class));
    }

    @Test
    void testRecordAuditLogs_MultipleLogs() {
        // 准备
        List<AuditLogCreateDTO> dtoList = Arrays.asList(testCreateDTO, testCreateDTO);
        when(auditLogConverter.toEntity(any(AuditLogCreateDTO.class))).thenReturn(testAuditLog);
        when(auditLogMapper.insert(any(AuditLog.class))).thenReturn(1);

        // 执行
        auditService.recordAuditLogs(dtoList);

        // 验证
        verify(auditLogMapper, times(2)).insert(any(AuditLog.class));
    }

    @Test
    void testRecordAuditLogAsync_Success() {
        // 准备
        when(auditLogConverter.toEntity(any(AuditLogCreateDTO.class))).thenReturn(testAuditLog);
        when(auditLogMapper.insert(any(AuditLog.class))).thenReturn(1);

        // 执行
        auditService.recordAuditLogAsync(testCreateDTO);

        // 验证
        verify(auditLogMapper, times(1)).insert(any(AuditLog.class));
    }

    @Test
    void testSendAuditLogAsync_Success() {
        // 执行
        auditService.sendAuditLogAsync(testCreateDTO);

        // 验证
        verify(kafkaTemplate, times(1)).send(eq("audit-log-topic"), any(AuditLogCreateDTO.class));
    }

    @Test
    void testGetAuditLogById_Success() {
        // 准备
        Long logId = 1L;
        when(auditLogMapper.selectById(logId)).thenReturn(testAuditLog);
        when(auditLogConverter.toVO(any(AuditLog.class))).thenReturn(testAuditLogVO);

        // 执行
        AuditLogVO result = auditService.getAuditLogById(logId);

        // 验证
        assertNotNull(result);
        assertEquals(logId, result.getId());
        verify(auditLogMapper, times(1)).selectById(logId);
    }

    @Test
    void testGetAuditLogById_NotFound() {
        // 准备
        Long logId = 999L;
        when(auditLogMapper.selectById(logId)).thenReturn(null);

        // 执行和验证
        assertThrows(BusinessException.class, () -> auditService.getAuditLogById(logId));
    }

    @Test
    void testQueryAuditLogs_WithFilters() {
        // 准备
        AuditLogQueryDTO queryDTO = new AuditLogQueryDTO();
        queryDTO.setOperationType("CREATE");
        queryDTO.setModule("USER");
        queryDTO.setOperatorId(1L);
        queryDTO.setPage(1);
        queryDTO.setSize(20);

        Page<AuditLog> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Collections.singletonList(testAuditLog));

        when(auditLogMapper.selectPage(any(Page.class), any())).thenReturn(pageResult);
        when(auditLogConverter.toVO(any(AuditLog.class))).thenReturn(testAuditLogVO);

        // 执行
        IPage<AuditLogVO> result = auditService.queryAuditLogs(queryDTO);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertFalse(result.getRecords().isEmpty());
    }

    @Test
    void testQueryAuditLogs_EmptyResult() {
        // 准备
        AuditLogQueryDTO queryDTO = new AuditLogQueryDTO();
        queryDTO.setPage(1);
        queryDTO.setSize(20);

        Page<AuditLog> pageResult = new Page<>(1, 20, 0);
        pageResult.setRecords(Collections.emptyList());

        when(auditLogMapper.selectPage(any(Page.class), any())).thenReturn(pageResult);

        // 执行
        IPage<AuditLogVO> result = auditService.queryAuditLogs(queryDTO);

        // 验证
        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    void testGetUserAuditLogs_Success() {
        // 准备
        Long userId = 1L;
        Page<AuditLog> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Collections.singletonList(testAuditLog));

        when(auditLogMapper.selectByOperatorId(eq(userId), any(Page.class))).thenReturn(pageResult);
        when(auditLogConverter.toVO(any(AuditLog.class))).thenReturn(testAuditLogVO);

        // 执行
        IPage<AuditLogVO> result = auditService.getUserAuditLogs(userId, 1, 20);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        verify(auditLogMapper, times(1)).selectByOperatorId(eq(userId), any(Page.class));
    }

    @Test
    void testGetTenantAuditLogs_Success() {
        // 准备
        Long tenantId = 1L;
        Page<AuditLog> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Collections.singletonList(testAuditLog));

        when(auditLogMapper.selectByTenantId(eq(tenantId), any(Page.class))).thenReturn(pageResult);
        when(auditLogConverter.toVO(any(AuditLog.class))).thenReturn(testAuditLogVO);

        // 执行
        IPage<AuditLogVO> result = auditService.getTenantAuditLogs(tenantId, 1, 20);

        // 验证
        assertNotNull(result);
        verify(auditLogMapper, times(1)).selectByTenantId(eq(tenantId), any(Page.class));
    }

    @Test
    void testGetAppAuditLogs_Success() {
        // 准备
        Long appId = 1L;
        Page<AuditLog> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Collections.singletonList(testAuditLog));

        when(auditLogMapper.selectByAppId(eq(appId), any(Page.class))).thenReturn(pageResult);
        when(auditLogConverter.toVO(any(AuditLog.class))).thenReturn(testAuditLogVO);

        // 执行
        IPage<AuditLogVO> result = auditService.getAppAuditLogs(appId, 1, 20);

        // 验证
        assertNotNull(result);
        verify(auditLogMapper, times(1)).selectByAppId(eq(appId), any(Page.class));
    }

    @Test
    void testGetAuditLogsByOperationType_Success() {
        // 准备
        String operationType = "CREATE";
        Page<AuditLog> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Collections.singletonList(testAuditLog));

        when(auditLogMapper.selectByOperationType(eq(operationType), any(Page.class))).thenReturn(pageResult);
        when(auditLogConverter.toVO(any(AuditLog.class))).thenReturn(testAuditLogVO);

        // 执行
        IPage<AuditLogVO> result = auditService.getAuditLogsByOperationType(operationType, 1, 20);

        // 验证
        assertNotNull(result);
        verify(auditLogMapper, times(1)).selectByOperationType(eq(operationType), any(Page.class));
    }

    @Test
    void testGetAuditLogsByModule_Success() {
        // 准备
        String module = "USER";
        Page<AuditLog> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Collections.singletonList(testAuditLog));

        when(auditLogMapper.selectByModule(eq(module), any(Page.class))).thenReturn(pageResult);
        when(auditLogConverter.toVO(any(AuditLog.class))).thenReturn(testAuditLogVO);

        // 执行
        IPage<AuditLogVO> result = auditService.getAuditLogsByModule(module, 1, 20);

        // 验证
        assertNotNull(result);
        verify(auditLogMapper, times(1)).selectByModule(eq(module), any(Page.class));
    }

    @Test
    void testGetAuditLogsByTimeRange_Success() {
        // 准备
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();
        Page<AuditLog> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Collections.singletonList(testAuditLog));

        when(auditLogMapper.selectByTimeRange(eq(startTime), eq(endTime), any(Page.class))).thenReturn(pageResult);
        when(auditLogConverter.toVO(any(AuditLog.class))).thenReturn(testAuditLogVO);

        // 执行
        IPage<AuditLogVO> result = auditService.getAuditLogsByTimeRange(startTime, endTime, 1, 20);

        // 验证
        assertNotNull(result);
        verify(auditLogMapper, times(1)).selectByTimeRange(eq(startTime), eq(endTime), any(Page.class));
    }

    @Test
    void testGetFailedAuditLogs_Success() {
        // 准备
        testAuditLog.setResult("FAILURE");
        Page<AuditLog> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Collections.singletonList(testAuditLog));

        when(auditLogMapper.selectFailedLogs(any(Page.class))).thenReturn(pageResult);
        when(auditLogConverter.toVO(any(AuditLog.class))).thenReturn(testAuditLogVO);

        // 执行
        IPage<AuditLogVO> result = auditService.getFailedAuditLogs(1, 20);

        // 验证
        assertNotNull(result);
        verify(auditLogMapper, times(1)).selectFailedLogs(any(Page.class));
    }

    @Test
    void testGetRecentAuditLogs_Success() {
        // 准备
        Page<AuditLog> pageResult = new Page<>(1, 20, 1);
        pageResult.setRecords(Collections.singletonList(testAuditLog));

        when(auditLogMapper.selectRecentLogs(any(Page.class))).thenReturn(pageResult);
        when(auditLogConverter.toVO(any(AuditLog.class))).thenReturn(testAuditLogVO);

        // 执行
        IPage<AuditLogVO> result = auditService.getRecentAuditLogs(1, 20);

        // 验证
        assertNotNull(result);
        verify(auditLogMapper, times(1)).selectRecentLogs(any(Page.class));
    }

    @Test
    void testGetAuditStatistics_Success() {
        // 准备
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        when(auditLogMapper.countByTimeRange(startTime, endTime)).thenReturn(100L);

        List<Map<String, Object>> resultDistribution = new ArrayList<>();
        Map<String, Object> successMap = new HashMap<>();
        successMap.put("result", "SUCCESS");
        successMap.put("count", 95L);
        resultDistribution.add(successMap);

        Map<String, Object> failureMap = new HashMap<>();
        failureMap.put("result", "FAILURE");
        failureMap.put("count", 5L);
        resultDistribution.add(failureMap);

        when(auditLogMapper.countByResultAndTimeRange(startTime, endTime)).thenReturn(resultDistribution);

        List<Map<String, Object>> typeDistribution = new ArrayList<>();
        Map<String, Object> typeMap = new HashMap<>();
        typeMap.put("operation_type", "CREATE");
        typeMap.put("count", 50L);
        typeDistribution.add(typeMap);
        when(auditLogMapper.countByOperationTypeAndTimeRange(startTime, endTime)).thenReturn(typeDistribution);

        List<Map<String, Object>> moduleDistribution = new ArrayList<>();
        Map<String, Object> moduleMap = new HashMap<>();
        moduleMap.put("module", "USER");
        moduleMap.put("count", 50L);
        moduleDistribution.add(moduleMap);
        when(auditLogMapper.countByModuleAndTimeRange(startTime, endTime)).thenReturn(moduleDistribution);

        List<Map<String, Object>> topUsers = new ArrayList<>();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("operator_name", "admin");
        userMap.put("count", 50L);
        topUsers.add(userMap);
        when(auditLogMapper.countTopOperatorsByTimeRange(startTime, endTime, 10)).thenReturn(topUsers);

        // 执行
        AuditStatisticsVO result = auditService.getAuditStatistics(startTime, endTime);

        // 验证
        assertNotNull(result);
        assertEquals(100L, result.getTotalOperations());
        assertEquals(95L, result.getSuccessCount());
        assertEquals(5L, result.getFailureCount());
        assertFalse(result.getOperationTypeDistribution().isEmpty());
        assertFalse(result.getModuleDistribution().isEmpty());
        assertFalse(result.getTopUsers().isEmpty());
    }

    @Test
    void testCountOperationsByTimeRange_Success() {
        // 准备
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now();

        when(auditLogMapper.countByTimeRange(startTime, endTime)).thenReturn(100L);

        // 执行
        long result = auditService.countOperationsByTimeRange(startTime, endTime);

        // 验证
        assertEquals(100L, result);
    }

    @Test
    void testCountUserOperations_Success() {
        // 准备
        Long userId = 1L;
        when(auditLogMapper.countByOperatorId(userId)).thenReturn(50L);

        // 执行
        long result = auditService.countUserOperations(userId);

        // 验证
        assertEquals(50L, result);
    }

    @Test
    void testCleanupExpiredLogs_Success() {
        // 准备
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(90);
        when(auditLogMapper.deleteByOperationTimeBefore(beforeTime)).thenReturn(100);

        // 执行
        int result = auditService.cleanupExpiredLogs(beforeTime);

        // 验证
        assertEquals(100, result);
        verify(auditLogMapper, times(1)).deleteByOperationTimeBefore(beforeTime);
    }

    @Test
    void testDeleteAuditLog_Success() {
        // 准备
        Long logId = 1L;
        when(auditLogMapper.selectById(logId)).thenReturn(testAuditLog);
        when(auditLogMapper.deleteById(logId)).thenReturn(1);

        // 执行
        auditService.deleteAuditLog(logId);

        // 验证
        verify(auditLogMapper, times(1)).selectById(logId);
        verify(auditLogMapper, times(1)).deleteById(logId);
    }

    @Test
    void testDeleteAuditLog_NotFound() {
        // 准备
        Long logId = 999L;
        when(auditLogMapper.selectById(logId)).thenReturn(null);

        // 执行和验证
        assertThrows(BusinessException.class, () -> auditService.deleteAuditLog(logId));
        verify(auditLogMapper, never()).deleteById(anyLong());
    }

    @Test
    void testBatchDeleteAuditLogs_EmptyList() {
        // 执行
        auditService.batchDeleteAuditLogs(Collections.emptyList());

        // 验证
        verify(auditLogMapper, never()).deleteBatchIds(anyList());
    }

    @Test
    void testBatchDeleteAuditLogs_Success() {
        // 准备
        List<Long> logIds = Arrays.asList(1L, 2L, 3L);
        when(auditLogMapper.deleteBatchIds(logIds)).thenReturn(3);

        // 执行
        auditService.batchDeleteAuditLogs(logIds);

        // 验证
        verify(auditLogMapper, times(1)).deleteBatchIds(logIds);
    }
}
