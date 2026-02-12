package com.qoobot.openidaas.role.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RoleController 集成测试
 *
 * @author QooBot
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("RoleController 集成测试")
class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testRoleId;

    @BeforeEach
    void setUp() throws Exception {
        // 创建测试角色
        String roleJson = "{\n" +
                "  \"roleCode\": \"TEST_ROLE\",\n" +
                "  \"roleName\": \"测试角色\",\n" +
                "  \"roleType\": 2,\n" +
                "  \"parentId\": 0,\n" +
                "  \"description\": \"测试用的角色\",\n" +
                "  \"status\": 1,\n" +
                "  \"sortOrder\": 10\n" +
                "}";

        String response = mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 提取ID
        String idStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(",\"roleCode\""));
        testRoleId = Long.parseLong(idStr);
    }

    @Test
    @DisplayName("测试获取角色列表")
    void testGetRoleList() throws Exception {
        // 执行
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    @DisplayName("测试获取角色列表 - 按类型筛选")
    void testGetRoleList_WithRoleType() throws Exception {
        // 执行
        mockMvc.perform(get("/api/roles")
                        .param("roleType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", everyItem(hasEntry("roleType", 1))));
    }

    @Test
    @DisplayName("测试获取角色树")
    void testGetRoleTree() throws Exception {
        // 执行
        mockMvc.perform(get("/api/roles/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("测试获取角色树 - 指定父角色")
    void testGetRoleTree_WithParentId() throws Exception {
        // 执行
        mockMvc.perform(get("/api/roles/tree")
                        .param("parentId", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("测试创建角色")
    void testCreateRole() throws Exception {
        // 准备
        String roleJson = "{\n" +
                "  \"roleCode\": \"NEW_ROLE\",\n" +
                "  \"roleName\": \"新角色\",\n" +
                "  \"roleType\": 2,\n" +
                "  \"parentId\": 0,\n" +
                "  \"description\": \"新创建的角色\",\n" +
                "  \"status\": 1,\n" +
                "  \"sortOrder\": 20\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.roleCode").value("NEW_ROLE"))
                .andExpect(jsonPath("$.data.roleName").value("新角色"));
    }

    @Test
    @DisplayName("测试创建角色 - 重复角色编码")
    void testCreateRole_DuplicateCode() throws Exception {
        // 准备
        String roleJson = "{\n" +
                "  \"roleCode\": \"TEST_ROLE\",\n" +
                "  \"roleName\": \"重复角色\",\n" +
                "  \"roleType\": 2,\n" +
                "  \"parentId\": 0\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("测试更新角色")
    void testUpdateRole() throws Exception {
        // 准备
        String updateJson = "{\n" +
                "  \"id\": " + testRoleId + ",\n" +
                "  \"roleCode\": \"UPDATED_ROLE\",\n" +
                "  \"roleName\": \"更新后的角色\",\n" +
                "  \"description\": \"更新后的描述\"\n" +
                "}";

        // 执行
        mockMvc.perform(put("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.roleCode").value("UPDATED_ROLE"))
                .andExpect(jsonPath("$.data.roleName").value("更新后的角色"));
    }

    @Test
    @DisplayName("测试更新角色 - 角色不存在")
    void testUpdateRole_NotFound() throws Exception {
        // 准备
        String updateJson = "{\n" +
                "  \"id\": 99999,\n" +
                "  \"roleCode\": \"UPDATED\",\n" +
                "  \"roleName\": \"更新\"\n" +
                "}";

        // 执行
        mockMvc.perform(put("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("测试删除角色")
    void testDeleteRole() throws Exception {
        // 执行
        mockMvc.perform(delete("/api/roles")
                        .param("id", String.valueOf(testRoleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试删除角色 - 内置角色")
    void testDeleteRole_Builtin() throws Exception {
        // 执行
        mockMvc.perform(delete("/api/roles")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("测试获取角色详情")
    void testGetRoleById() throws Exception {
        // 执行
        mockMvc.perform(get("/api/roles/{id}", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testRoleId))
                .andExpect(jsonPath("$.data.roleCode").exists())
                .andExpect(jsonPath("$.data.roleName").exists());
    }

    @Test
    @DisplayName("测试获取角色详情 - 角色不存在")
    void testGetRoleById_NotFound() throws Exception {
        // 执行
        mockMvc.perform(get("/api/roles/{id}", 99999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("测试分配权限给角色")
    void testAssignPermissions() throws Exception {
        // 准备
        List<Long> permIds = Arrays.asList(1L, 2L, 3L);
        String permJson = objectMapper.writeValueAsString(permIds);

        // 执行
        mockMvc.perform(post("/api/roles/{roleId}/permissions", testRoleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(permJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        mockMvc.perform(get("/api/roles/{roleId}/permissions", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    @DisplayName("测试分配权限给角色 - 角色不存在")
    void testAssignPermissions_RoleNotFound() throws Exception {
        // 准备
        List<Long> permIds = Arrays.asList(1L, 2L);
        String permJson = objectMapper.writeValueAsString(permIds);

        // 执行
        mockMvc.perform(post("/api/roles/{roleId}/permissions", 99999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(permJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("测试移除角色权限")
    void testRemovePermissions() throws Exception {
        // 先分配权限
        List<Long> permIds = Arrays.asList(1L, 2L, 3L);
        String permJson = objectMapper.writeValueAsString(permIds);
        mockMvc.perform(post("/api/roles/{roleId}/permissions", testRoleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(permJson));

        // 执行移除
        List<Long> removeIds = Arrays.asList(1L, 2L);
        String removeJson = objectMapper.writeValueAsString(removeIds);
        mockMvc.perform(delete("/api/roles/{roleId}/permissions", testRoleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(removeJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        mockMvc.perform(get("/api/roles/{roleId}/permissions", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @DisplayName("测试获取角色权限")
    void testGetRolePermissions() throws Exception {
        // 先分配权限
        List<Long> permIds = Arrays.asList(1L, 2L, 3L);
        String permJson = objectMapper.writeValueAsString(permIds);
        mockMvc.perform(post("/api/roles/{roleId}/permissions", testRoleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(permJson));

        // 执行
        mockMvc.perform(get("/api/roles/{roleId}/permissions", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(3)));
    }

    @Test
    @DisplayName("测试获取角色权限 - 无权限")
    void testGetRolePermissions_NoPermissions() throws Exception {
        // 执行
        mockMvc.perform(get("/api/roles/{roleId}/permissions", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", empty()));
    }

    @Test
    @DisplayName("测试分配角色给用户")
    void testAssignRoleToUser() throws Exception {
        // 执行
        mockMvc.perform(post("/api/roles/users/{userId}/roles/{roleId}", 10, testRoleId)
                        .param("scopeType", "1")
                        .param("scopeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        mockMvc.perform(get("/api/roles/users/{userId}/roles", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("测试分配角色给用户 - 角色不存在")
    void testAssignRoleToUser_RoleNotFound() throws Exception {
        // 执行
        mockMvc.perform(post("/api/roles/users/{userId}/roles/{roleId}", 10, 99999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("测试移除用户角色")
    void testRemoveRoleFromUser() throws Exception {
        // 先分配角色
        mockMvc.perform(post("/api/roles/users/{userId}/roles/{roleId}", 10, testRoleId)
                .param("scopeType", "1")
                .param("scopeId", "1"));

        // 执行移除
        mockMvc.perform(delete("/api/roles/users/{userId}/roles/{roleId}", 10, testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证
        mockMvc.perform(get("/api/roles/users/{userId}/roles", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", empty()));
    }

    @Test
    @DisplayName("测试获取用户角色")
    void testGetUserRoles() throws Exception {
        // 先分配角色
        mockMvc.perform(post("/api/roles/users/{userId}/roles/{roleId}", 10, testRoleId)
                .param("scopeType", "1")
                .param("scopeId", "1"));

        // 执行
        mockMvc.perform(get("/api/roles/users/{userId}/roles", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("测试获取用户角色 - 无角色")
    void testGetUserRoles_NoRoles() throws Exception {
        // 执行
        mockMvc.perform(get("/api/roles/users/{userId}/roles", 99999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", empty()));
    }

    @Test
    @DisplayName("测试响应结构")
    void testResponseStructure() throws Exception {
        // 执行
        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("测试创建角色 - 所有字段")
    void testCreateRole_AllFields() throws Exception {
        // 准备
        String roleJson = "{\n" +
                "  \"roleCode\": \"FULL_ROLE\",\n" +
                "  \"roleName\": \"完整角色\",\n" +
                "  \"roleType\": 2,\n" +
                "  \"parentId\": 0,\n" +
                "  \"description\": \"完整的角色信息\",\n" +
                "  \"status\": 1,\n" +
                "  \"sortOrder\": 100\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.roleCode").value("FULL_ROLE"))
                .andExpect(jsonPath("$.data.description").value("完整的角色信息"))
                .andExpect(jsonPath("$.data.sortOrder").value(100));
    }

    @Test
    @DisplayName("测试更新角色 - 部分字段")
    void testUpdateRole_PartialFields() throws Exception {
        // 准备
        String updateJson = "{\n" +
                "  \"id\": " + testRoleId + ",\n" +
                "  \"description\": \"仅更新描述\"\n" +
                "}";

        // 执行
        mockMvc.perform(put("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.description").value("仅更新描述"));
    }

    @Test
    @DisplayName("测试分配权限 - 空列表")
    void testAssignPermissions_EmptyList() throws Exception {
        // 准备
        List<Long> permIds = Arrays.asList();
        String permJson = objectMapper.writeValueAsString(permIds);

        // 执行
        mockMvc.perform(post("/api/roles/{roleId}/permissions", testRoleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(permJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试覆盖权限分配")
    void testAssignPermissions_Override() throws Exception {
        // 先分配权限1,2,3
        List<Long> permIds1 = Arrays.asList(1L, 2L, 3L);
        String permJson1 = objectMapper.writeValueAsString(permIds1);
        mockMvc.perform(post("/api/roles/{roleId}/permissions", testRoleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(permJson1));

        // 重新分配权限4,5
        List<Long> permIds2 = Arrays.asList(4L, 5L);
        String permJson2 = objectMapper.writeValueAsString(permIds2);
        mockMvc.perform(post("/api/roles/{roleId}/permissions", testRoleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(permJson2));

        // 验证
        mockMvc.perform(get("/api/roles/{roleId}/permissions", testRoleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("测试创建角色 - 带父角色")
    void testCreateRole_WithParent() throws Exception {
        // 准备
        String roleJson = "{\n" +
                "  \"roleCode\": \"CHILD_ROLE\",\n" +
                "  \"roleName\": \"子角色\",\n" +
                "  \"roleType\": 2,\n" +
                "  \"parentId\": " + testRoleId + "\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roleJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.parentId").value(testRoleId));
    }
}
