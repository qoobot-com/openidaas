package com.qoobot.openidaas.user.integration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserPasswordDTO;
import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.user.UserServiceApplication;
import com.qoobot.openidaas.user.controller.UserController;
import com.qoobot.openidaas.user.entity.User;
import com.qoobot.openidaas.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 用户控制器集成测试
 *
 * @author QooBot
 */
@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 清空测试数据
        userMapper.delete(null);

        // 创建测试用户
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setMobile("13800000001");
        testUser.setPasswordHash("$2a$12$test_hash");
        testUser.setPasswordSalt("salt");
        testUser.setStatus(1);
        userMapper.insert(testUser);
    }

    @Test
    void testGetUser_Success() throws Exception {
        // 执行
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                .andExpect(jsonPath("$.data.username").value(testUser.getUsername()));
    }

    @Test
    void testGetUser_NotFound() throws Exception {
        // 执行和验证
        mockMvc.perform(get("/api/users/{id}", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void testListUsers_Success() throws Exception {
        // 执行
        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records", hasSize(greaterThan(0))));
    }

    @Test
    void testListUsers_WithFilters() throws Exception {
        // 执行
        mockMvc.perform(get("/api/users")
                        .param("username", testUser.getUsername())
                        .param("status", "1")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void testCreateUser_Success() throws Exception {
        // 准备
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("newuser");
        createDTO.setEmail("new@test.com");
        createDTO.setMobile("13800000002");
        createDTO.setPassword("Test@123456");

        // 执行
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.username").value("newuser"))
                .andExpect(jsonPath("$.data.email").value("new@test.com"));
    }

    @Test
    void testCreateUser_DuplicateUsername() throws Exception {
        // 准备
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername(testUser.getUsername());
        createDTO.setEmail("new@test.com");
        createDTO.setPassword("Test@123456");

        // 执行和验证
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    void testUpdateUser_Success() throws Exception {
        // 准备
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("updated@test.com");
        updateDTO.setMobile("13800000003");

        // 执行
        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.email").value("updated@test.com"));
    }

    @Test
    void testDeleteUser_Success() throws Exception {
        // 执行
        mockMvc.perform(delete("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        User deletedUser = userMapper.selectById(testUser.getId());
        assertTrue(deletedUser == null || deletedUser.getStatus() == 4);
    }

    @Test
    void testSearchUsers_Success() throws Exception {
        // 执行
        mockMvc.perform(get("/api/users/search")
                        .param("keyword", testUser.getUsername())
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    void testLockUser_Success() throws Exception {
        // 执行
        mockMvc.perform(post("/api/users/{id}/lock", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        User lockedUser = userMapper.selectById(testUser.getId());
        assertEquals(2, lockedUser.getStatus());
    }

    @Test
    void testUnlockUser_Success() throws Exception {
        // 先锁定用户
        testUser.setStatus(2);
        userMapper.updateById(testUser);

        // 执行
        mockMvc.perform(post("/api/users/{id}/unlock", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        User unlockedUser = userMapper.selectById(testUser.getId());
        assertEquals(1, unlockedUser.getStatus());
        assertEquals(0, unlockedUser.getFailedLoginAttempts());
    }

    @Test
    void testDisableUser_Success() throws Exception {
        // 执行
        mockMvc.perform(post("/api/users/{id}/disable", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        User disabledUser = userMapper.selectById(testUser.getId());
        assertEquals(3, disabledUser.getStatus());
    }

    @Test
    void testEnableUser_Success() throws Exception {
        // 先禁用用户
        testUser.setStatus(3);
        userMapper.updateById(testUser);

        // 执行
        mockMvc.perform(post("/api/users/{id}/enable", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        User enabledUser = userMapper.selectById(testUser.getId());
        assertEquals(1, enabledUser.getStatus());
    }

    @Test
    void testResetPassword_Success() throws Exception {
        // 准备
        UserPasswordDTO passwordDTO = new UserPasswordDTO();
        passwordDTO.setNewPassword("NewPass@123");

        // 执行
        mockMvc.perform(post("/api/users/{id}/reset-password", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testAssignDepartments_Success() throws Exception {
        // 准备
        var request = new UserController.AssignDepartmentRequest();
        request.setDeptIds(Arrays.asList(1L, 2L));
        request.setPositionId(1L);
        request.setIsPrimary(true);

        // 执行
        mockMvc.perform(post("/api/users/{id}/departments", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testAssignRoles_Success() throws Exception {
        // 准备
        var request = new UserController.AssignRoleRequest();
        request.setRoleIds(Arrays.asList(1L, 2L));
        request.setScopeType(1);
        request.setScopeId(1L);

        // 执行
        mockMvc.perform(post("/api/users/{id}/roles", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testRemoveRoles_Success() throws Exception {
        // 准备
        List<Long> roleIds = Arrays.asList(1L);

        // 执行
        mockMvc.perform(delete("/api/users/{id}/roles", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testListUsers_WithPagination() throws Exception {
        // 执行
        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalPages").exists())
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.number").value(1))
                .andExpect(jsonPath("$.data.first").value(true));
    }

    @Test
    void testListUsers_WithSorting() throws Exception {
        // 执行
        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "10")
                        .param("sortField", "createdAt")
                        .param("sortOrder", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testListUsers_WithMultipleFilters() throws Exception {
        // 执行
        mockMvc.perform(get("/api/users")
                        .param("username", "test")
                        .param("email", "test")
                        .param("status", "1")
                        .param("startDate", "2023-01-01")
                        .param("endDate", "2024-12-31")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testListUsers_EmptyResult() throws Exception {
        // 清空数据
        userMapper.delete(null);

        // 执行
        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records").isEmpty());
    }

    @Test
    void testListUsers_InvalidPageParameters() throws Exception {
        // 执行
        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testCreateUser_InvalidEmail() throws Exception {
        // 准备
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("testuser2");
        createDTO.setEmail("invalid-email");
        createDTO.setPassword("Test@123");

        // 执行和验证
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUser_InvalidMobile() throws Exception {
        // 准备
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("testuser2");
        createDTO.setEmail("test2@test.com");
        createDTO.setMobile("invalid-mobile");
        createDTO.setPassword("Test@123");

        // 执行和验证
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testCreateUser_WithAllFields() throws Exception {
        // 准备
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("fulluser");
        createDTO.setEmail("full@test.com");
        createDTO.setMobile("13800000099");
        createDTO.setPassword("Test@123456");
        createDTO.setRealName("Full Name");
        createDTO.setNickname("Full");

        // 执行
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testUpdateUser_WithAllFields() throws Exception {
        // 准备
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("updated@test.com");
        updateDTO.setMobile("13800000099");
        updateDTO.setRealName("Updated Name");
        updateDTO.setNickname("Updated");

        // 执行
        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.email").value("updated@test.com"));
    }

    @Test
    void testUpdateUser_WithEmptyUpdates() throws Exception {
        // 准备
        UserUpdateDTO updateDTO = new UserUpdateDTO();

        // 执行
        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testAssignDepartments_WithoutPosition() throws Exception {
        // 准备
        var request = new UserController.AssignDepartmentRequest();
        request.setDeptIds(Arrays.asList(1L));
        request.setPositionId(null);
        request.setIsPrimary(true);

        // 执行
        mockMvc.perform(post("/api/users/{id}/departments", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testAssignRoles_WithoutScope() throws Exception {
        // 准备
        var request = new UserController.AssignRoleRequest();
        request.setRoleIds(Arrays.asList(1L));
        request.setScopeType(1);
        request.setScopeId(null);

        // 执行
        mockMvc.perform(post("/api/users/{id}/roles", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testAssignDepartments_EmptyDepartmentList() throws Exception {
        // 准备
        var request = new UserController.AssignDepartmentRequest();
        request.setDeptIds(Collections.emptyList());

        // 执行
        mockMvc.perform(post("/api/users/{id}/departments", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testAssignRoles_EmptyRoleList() throws Exception {
        // 准备
        var request = new UserController.AssignRoleRequest();
        request.setRoleIds(Collections.emptyList());

        // 执行
        mockMvc.perform(post("/api/users/{id}/roles", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testLockUser_AlreadyLocked() throws Exception {
        // 先锁定用户
        testUser.setStatus(2);
        userMapper.updateById(testUser);

        // 执行 - 应该仍然返回成功
        mockMvc.perform(post("/api/users/{id}/lock", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testUnlockUser_AlreadyUnlocked() throws Exception {
        // 确保用户是解锁状态
        testUser.setStatus(1);
        userMapper.updateById(testUser);

        // 执行 - 应该仍然返回成功
        mockMvc.perform(post("/api/users/{id}/unlock", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteUser_NotFound() throws Exception {
        // 执行和验证
        mockMvc.perform(delete("/api/users/{id}", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    void testLockUser_NotFound() throws Exception {
        // 执行和验证
        mockMvc.perform(post("/api/users/{id}/lock", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    void testUnlockUser_NotFound() throws Exception {
        // 执行和验证
        mockMvc.perform(post("/api/users/{id}/unlock", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    void testDisableUser_NotFound() throws Exception {
        // 执行和验证
        mockMvc.perform(post("/api/users/{id}/disable", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    void testEnableUser_NotFound() throws Exception {
        // 执行和验证
        mockMvc.perform(post("/api/users/{id}/enable", 99999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    void testGetUserDetail_WithAllFields() throws Exception {
        // 确保用户存在
        userMapper.updateById(testUser);

        // 执行
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.username").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.status").exists())
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    void testResponseStructure() throws Exception {
        // 执行
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testCreateUser_MinimalRequiredFields() throws Exception {
        // 准备 - 只提供必填字段
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("minimal");
        createDTO.setPassword("Test@123");

        // 执行
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testUpdateUser_PartialUpdate() throws Exception {
        // 准备 - 只更新邮箱
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setEmail("partial@test.com");

        // 执行
        mockMvc.perform(put("/api/users/{id}", testUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.email").value("partial@test.com"));
    }

    @Test
    void testListUsers_LargePage() throws Exception {
        // 执行
        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.size").value(100));
    }

    @Test
    void testListUsers_LastPage() throws Exception {
        // 创建少量数据
        userMapper.delete(null);
        userMapper.insert(testUser);

        // 执行
        mockMvc.perform(get("/api/users")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.last").value(true));
    }
}
