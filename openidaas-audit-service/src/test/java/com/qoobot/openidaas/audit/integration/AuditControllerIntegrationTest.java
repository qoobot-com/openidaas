package com.qoobot.openidaas.audit.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openidaas.audit.service.AuditService;
import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.vo.audit.AuditLogVO;
import com.qoobot.openidaas.common.vo.audit.AuditStatisticsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 审计控制器集成测试
 *
 * @author QooBot
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class AuditControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testRecordAuditLog_Integration() throws Exception {
        // 准备
        AuditLogCreateDTO createDTO = new AuditLogCreateDTO();
        createDTO.setOperationType("CREATE");
        createDTO.setOperationDesc("集成测试");
        createDTO.setModule("TEST");
        createDTO.setOperatorId(1L);
        createDTO.setOperatorName("test_user");
        createDTO.setResult("SUCCESS");

        // 执行和验证
        mockMvc.perform(post("/api/audit/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRecordAuditLogs_Integration() throws Exception {
        // 准备
        AuditLogCreateDTO dto1 = new AuditLogCreateDTO();
        dto1.setOperationType("CREATE");
        dto1.setModule("TEST");
        dto1.setOperatorId(1L);
        dto1.setResult("SUCCESS");

        AuditLogCreateDTO dto2 = new AuditLogCreateDTO();
        dto2.setOperationType("UPDATE");
        dto2.setModule("TEST");
        dto2.setOperatorId(1L);
        dto2.setResult("SUCCESS");

        // 执行和验证
        mockMvc.perform(post("/api/audit/logs/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Arrays.asList(dto1, dto2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testQueryAuditLogs_Integration() throws Exception {
        // 准备
        com.qoobot.openidaas.common.dto.audit.AuditLogQueryDTO queryDTO =
            new com.qoobot.openidaas.common.dto.audit.AuditLogQueryDTO();
        queryDTO.setOperationType("LOGIN");
        queryDTO.setModule("AUTH");
        queryDTO.setResult("SUCCESS");
        queryDTO.setPage(1);
        queryDTO.setSize(20);

        // 执行和验证
        mockMvc.perform(post("/api/audit/logs/query")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(queryDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testGetUserAuditLogs_Integration() throws Exception {
        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/users/{userId}", 1L)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testGetTenantAuditLogs_Integration() throws Exception {
        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/tenants/{tenantId}", 1L)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAppAuditLogs_Integration() throws Exception {
        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/apps/{appId}", 1L)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAuditLogsByOperationType_Integration() throws Exception {
        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/operation-type/{operationType}", "LOGIN")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAuditLogsByModule_Integration() throws Exception {
        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/module/{module}", "USER")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAuditLogsByTimeRange_Integration() throws Exception {
        // 准备
        String startTime = LocalDateTime.now().minusDays(1).toString().replace("T", " ").substring(0, 19);
        String endTime = LocalDateTime.now().toString().replace("T", " ").substring(0, 19);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/time-range")
                        .param("startTime", startTime)
                        .param("endTime", endTime)
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetFailedAuditLogs_Integration() throws Exception {
        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/failed")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetRecentAuditLogs_Integration() throws Exception {
        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/recent")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAuditStatistics_Integration() throws Exception {
        // 准备
        String startTime = LocalDateTime.now().minusDays(7).toString().replace("T", " ").substring(0, 19);
        String endTime = LocalDateTime.now().toString().replace("T", " ").substring(0, 19);

        // 执行和验证
        mockMvc.perform(get("/api/audit/statistics")
                        .param("startTime", startTime)
                        .param("endTime", endTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalOperations").exists())
                .andExpect(jsonPath("$.data.successCount").exists())
                .andExpect(jsonPath("$.data.failureCount").exists())
                .andExpect(jsonPath("$.data.operationTypeDistribution").exists())
                .andExpect(jsonPath("$.data.moduleDistribution").exists())
                .andExpect(jsonPath("$.data.topUsers").exists());
    }

    @Test
    void testCountOperationsByTimeRange_Integration() throws Exception {
        // 准备
        String startTime = LocalDateTime.now().minusDays(1).toString().replace("T", " ").substring(0, 19);
        String endTime = LocalDateTime.now().toString().replace("T", " ").substring(0, 19);

        // 执行和验证
        mockMvc.perform(get("/api/audit/statistics/count")
                        .param("startTime", startTime)
                        .param("endTime", endTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testCountUserOperations_Integration() throws Exception {
        // 执行和验证
        mockMvc.perform(get("/api/audit/statistics/users/{userId}/count", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testGetModuleDistribution_Integration() throws Exception {
        // 准备
        String startTime = LocalDateTime.now().minusDays(1).toString().replace("T", " ").substring(0, 19);
        String endTime = LocalDateTime.now().toString().replace("T", " ").substring(0, 19);

        // 执行和验证
        mockMvc.perform(get("/api/audit/statistics/module-distribution")
                        .param("startTime", startTime)
                        .param("endTime", endTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetOperationTypeDistribution_Integration() throws Exception {
        // 准备
        String startTime = LocalDateTime.now().minusDays(1).toString().replace("T", " ").substring(0, 19);
        String endTime = LocalDateTime.now().toString().replace("T", " ").substring(0, 19);

        // 执行和验证
        mockMvc.perform(get("/api/audit/statistics/operation-type-distribution")
                        .param("startTime", startTime)
                        .param("endTime", endTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testCleanupExpiredLogs_Integration() throws Exception {
        // 准备 - 删除1年前之前的日志
        String beforeTime = LocalDateTime.now().minusYears(1).toString().replace("T", " ").substring(0, 19);

        // 执行和验证
        mockMvc.perform(delete("/api/audit/logs/cleanup")
                        .param("beforeTime", beforeTime))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void testExportAuditLogs_Integration() throws Exception {
        // 准备
        String startTime = LocalDateTime.now().minusDays(1).toString().replace("T", " ").substring(0, 19);
        String endTime = LocalDateTime.now().toString().replace("T", " ").substring(0, 19);

        // 执行和验证
        mockMvc.perform(get("/api/audit/logs/export")
                        .param("startTime", startTime)
                        .param("endTime", endTime))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Type", containsString("application/octet-stream")));
    }

    @Test
    void testDeleteAuditLog_Integration() throws Exception {
        // 首先创建一条日志
        AuditLogCreateDTO createDTO = new AuditLogCreateDTO();
        createDTO.setOperationType("DELETE_TEST");
        createDTO.setModule("TEST");
        createDTO.setOperatorId(1L);
        createDTO.setResult("SUCCESS");

        mockMvc.perform(post("/api/audit/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk());

        // 这里假设删除一个不存在的ID会返回错误
        mockMvc.perform(delete("/api/audit/logs/{logId}", 999999L))
                .andExpect(status().isOk());
    }

    @Test
    void testBatchDeleteAuditLogs_Integration() throws Exception {
        // 准备
        List<Long> logIds = Arrays.asList(999999L, 999998L, 999997L);

        // 执行和验证
        mockMvc.perform(delete("/api/audit/logs/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRecordAuditLogAsync_Integration() throws Exception {
        // 准备
        AuditLogCreateDTO createDTO = new AuditLogCreateDTO();
        createDTO.setOperationType("ASYNC_TEST");
        createDTO.setModule("TEST");
        createDTO.setOperatorId(1L);
        createDTO.setResult("SUCCESS");

        // 执行和验证
        mockMvc.perform(post("/api/audit/logs/async")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testGetAuditLogById_NotFound() throws Exception {
        // 执行和验证 - 获取不存在的ID
        mockMvc.perform(get("/api/audit/logs/{logId}", 99999999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }
}
