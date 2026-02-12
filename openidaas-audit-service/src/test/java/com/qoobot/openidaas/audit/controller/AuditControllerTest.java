package com.qoobot.openidaas.audit.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openidaas.audit.service.AuditService;
import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.dto.audit.AuditLogQueryDTO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.audit.AuditLogVO;
import com.qoobot.openidaas.common.vo.audit.AuditStatisticsVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 审计控制器单元测试
 *
 * @author QooBot
 */
@ExtendWith(MockitoExtension.class)
class AuditControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditController auditController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private AuditLogCreateDTO testCreateDTO;
    private AuditLogVO testAuditLogVO;
    private AuditStatisticsVO testStatisticsVO;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditController).build();
        objectMapper = new ObjectMapper();

        // 准备测试数据
        testCreateDTO = new AuditLogCreateDTO();
        testCreateDTO.setOperationType("CREATE");
        testCreateDTO.setOperatorId(1L);
        testCreateDTO.setOperatorName("admin");

        testAuditLogVO = new AuditLogVO();
        testAuditLogVO.setId(1L);
        testAuditLogVO.setOperationType("CREATE");
        testAuditLogVO.setOperatorName("admin");
        testAuditLogVO.setOperationTime(LocalDateTime.now());

        testStatisticsVO = new AuditStatisticsVO();
        testStatisticsVO.setTotalOperations(100L);
        testStatisticsVO.setSuccessCount(95L);
        testStatisticsVO.setFailureCount(5L);
        testStatisticsVO.setOperationTypeDistribution(new HashMap<>());
        testStatisticsVO.setModuleDistribution(new HashMap<>());
        testStatisticsVO.setTopUsers(new HashMap<>());
    }

    @Test
    void testRecordAuditLog_Success() throws Exception {
        // Mock
        doNothing().when(auditService).recordAuditLog(any(AuditLogCreateDTO.class));

        // 执行和验证
        mockMvc.perform(post("/api/audit/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).recordAuditLog(any(AuditLogCreateDTO.class));
    }

    @Test
    void testRecordAuditLogs_Success() throws Exception {
        // 准备
        List<AuditLogCreateDTO> dtoList = Arrays.asList(testCreateDTO, testCreateDTO);

        // Mock
        doNothing().when(auditService).recordAuditLogs(anyList());

        // 执行和验证
        mockMvc.perform(post("/api/audit/logs/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).recordAuditLogs(anyList());
    }

    @Test
    void testRecordAuditLogAsync_Success() throws Exception {
        // Mock
        doNothing().when(auditService).sendAuditLogAsync(any(AuditLogCreateDTO.class));

        // 执行和验证
        mockMvc.perform(post("/api/audit/logs/async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).sendAuditLogAsync(any(AuditLogCreateDTO.class));
    }

    @Test
    void testGetAuditLogById_Success() throws Exception {
        // 准备
        Long logId = 1L;

        // Mock
        when(auditService.getAuditLogById(logId)).thenReturn(testAuditLogVO);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/{logId}", logId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(logId))
                .andExpect(jsonPath("$.data.operationType").value("CREATE"));

        // 验证调用
        verify(auditService, times(1)).getAuditLogById(logId);
    }

    @Test
    void testQueryAuditLogs_Success() throws Exception {
        // 准备
        AuditLogQueryDTO queryDTO = new AuditLogQueryDTO();
        queryDTO.setOperationType("CREATE");
        queryDTO.setPage(1);
        queryDTO.setSize(20);

        Page<AuditLogVO> page = new Page<>(1, 20, 1);
        page.setRecords(Collections.singletonList(testAuditLogVO));

        // Mock
        when(auditService.queryAuditLogs(any(AuditLogQueryDTO.class))).thenReturn(page);

        // 执行和验证
        mockMvc.perform(post("/api/audit/logs/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1));

        // 验证调用
        verify(auditService, times(1)).queryAuditLogs(any(AuditLogQueryDTO.class));
    }

    @Test
    void testGetUserAuditLogs_Success() throws Exception {
        // 准备
        Long userId = 1L;
        Page<AuditLogVO> page = new Page<>(1, 20, 1);
        page.setRecords(Collections.singletonList(testAuditLogVO));

        // Mock
        when(auditService.getUserAuditLogs(userId, 1, 20)).thenReturn(page);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/users/{userId}", userId)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(1));

        // 验证调用
        verify(auditService, times(1)).getUserAuditLogs(userId, 1, 20);
    }

    @Test
    void testGetTenantAuditLogs_Success() throws Exception {
        // 准备
        Long tenantId = 1L;
        Page<AuditLogVO> page = new Page<>(1, 20, 1);
        page.setRecords(Collections.singletonList(testAuditLogVO));

        // Mock
        when(auditService.getTenantAuditLogs(tenantId, 1, 20)).thenReturn(page);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/tenants/{tenantId}", tenantId)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).getTenantAuditLogs(tenantId, 1, 20);
    }

    @Test
    void testGetAppAuditLogs_Success() throws Exception {
        // 准备
        Long appId = 1L;
        Page<AuditLogVO> page = new Page<>(1, 20, 1);
        page.setRecords(Collections.singletonList(testAuditLogVO));

        // Mock
        when(auditService.getAppAuditLogs(appId, 1, 20)).thenReturn(page);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/apps/{appId}", appId)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).getAppAuditLogs(appId, 1, 20);
    }

    @Test
    void testGetAuditLogsByOperationType_Success() throws Exception {
        // 准备
        String operationType = "CREATE";
        Page<AuditLogVO> page = new Page<>(1, 20, 1);
        page.setRecords(Collections.singletonList(testAuditLogVO));

        // Mock
        when(auditService.getAuditLogsByOperationType(operationType, 1, 20)).thenReturn(page);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/operation-type/{operationType}", operationType)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).getAuditLogsByOperationType(operationType, 1, 20);
    }

    @Test
    void testGetAuditLogsByModule_Success() throws Exception {
        // 准备
        String module = "USER";
        Page<AuditLogVO> page = new Page<>(1, 20, 1);
        page.setRecords(Collections.singletonList(testAuditLogVO));

        // Mock
        when(auditService.getAuditLogsByModule(module, 1, 20)).thenReturn(page);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/module/{module}", module)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).getAuditLogsByModule(module, 1, 20);
    }

    @Test
    void testGetAuditLogsByTimeRange_Success() throws Exception {
        // 准备
        String startTime = "2024-01-01 00:00:00";
        String endTime = "2024-01-31 23:59:59";
        Page<AuditLogVO> page = new Page<>(1, 20, 1);
        page.setRecords(Collections.singletonList(testAuditLogVO));

        // Mock
        when(auditService.getAuditLogsByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class), eq(1), eq(20)))
                .thenReturn(page);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/time-range")
                        .param("startTime", startTime)
                        .param("endTime", endTime)
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).getAuditLogsByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class), eq(1), eq(20));
    }

    @Test
    void testGetFailedAuditLogs_Success() throws Exception {
        // 准备
        Page<AuditLogVO> page = new Page<>(1, 20, 1);
        page.setRecords(Collections.singletonList(testAuditLogVO));

        // Mock
        when(auditService.getFailedAuditLogs(1, 20)).thenReturn(page);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/failed")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).getFailedAuditLogs(1, 20);
    }

    @Test
    void testGetRecentAuditLogs_Success() throws Exception {
        // 准备
        Page<AuditLogVO> page = new Page<>(1, 20, 1);
        page.setRecords(Collections.singletonList(testAuditLogVO));

        // Mock
        when(auditService.getRecentAuditLogs(1, 20)).thenReturn(page);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/recent")
                        .param("page", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).getRecentAuditLogs(1, 20);
    }

    @Test
    void testGetAuditStatistics_Success() throws Exception {
        // 准备
        String startTime = "2024-01-01 00:00:00";
        String endTime = "2024-01-31 23:59:59";

        // Mock
        when(auditService.getAuditStatistics(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(testStatisticsVO);

        // 执行和验证
        mockMvc.perform(get("/api/audit/statistics")
                        .param("startTime", startTime)
                        .param("endTime", endTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOperations").value(100))
                .andExpect(jsonPath("$.data.successCount").value(95))
                .andExpect(jsonPath("$.data.failureCount").value(5));

        // 验证调用
        verify(auditService, times(1)).getAuditStatistics(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testCountOperationsByTimeRange_Success() throws Exception {
        // 准备
        String startTime = "2024-01-01 00:00:00";
        String endTime = "2024-01-31 23:59:59";

        // Mock
        when(auditService.countOperationsByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(100L);

        // 执行和验证
        mockMvc.perform(get("/api/audit/statistics/count")
                        .param("startTime", startTime)
                        .param("endTime", endTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));

        // 验证调用
        verify(auditService, times(1)).countOperationsByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testCountUserOperations_Success() throws Exception {
        // 准备
        Long userId = 1L;

        // Mock
        when(auditService.countUserOperations(userId)).thenReturn(50L);

        // 执行和验证
        mockMvc.perform(get("/api/audit/statistics/users/{userId}/count", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(50));

        // 验证调用
        verify(auditService, times(1)).countUserOperations(userId);
    }

    @Test
    void testCleanupExpiredLogs_Success() throws Exception {
        // 准备
        String beforeTime = "2024-01-01 00:00:00";

        // Mock
        when(auditService.cleanupExpiredLogs(any(LocalDateTime.class))).thenReturn(100);

        // 执行和验证
        mockMvc.perform(delete("/api/audit/logs/cleanup")
                        .param("beforeTime", beforeTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(100));

        // 验证调用
        verify(auditService, times(1)).cleanupExpiredLogs(any(LocalDateTime.class));
    }

    @Test
    void testDeleteAuditLog_Success() throws Exception {
        // 准备
        Long logId = 1L;

        // Mock
        doNothing().when(auditService).deleteAuditLog(logId);

        // 执行和验证
        mockMvc.perform(delete("/api/audit/logs/{logId}", logId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).deleteAuditLog(logId);
    }

    @Test
    void testBatchDeleteAuditLogs_Success() throws Exception {
        // 准备
        List<Long> logIds = Arrays.asList(1L, 2L, 3L);

        // Mock
        doNothing().when(auditService).batchDeleteAuditLogs(anyList());

        // 执行和验证
        mockMvc.perform(delete("/api/audit/logs/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证调用
        verify(auditService, times(1)).batchDeleteAuditLogs(anyList());
    }
}
