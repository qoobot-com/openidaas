package com.qoobot.openidaas.organization.integration;

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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OrganizationController 集成测试
 *
 * @author QooBot
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("组织控制器集成测试")
class OrganizationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testDeptId;
    private Long testPositionId;

    @BeforeEach
    void setUp() throws Exception {
        // 创建测试部门
        String deptJson = "{\n" +
                "  \"deptCode\": \"TEST_DEPT\",\n" +
                "  \"deptName\": \"测试部门\",\n" +
                "  \"parentId\": 0,\n" +
                "  \"enabled\": true,\n" +
                "  \"sortOrder\": 10\n" +
                "}";

        String deptResponse = mockMvc.perform(post("/api/org/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deptJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 提取部门ID
        String deptIdStr = deptResponse.substring(deptResponse.indexOf("\"id\":") + 5, deptResponse.indexOf(",\"deptCode\""));
        testDeptId = Long.parseLong(deptIdStr);

        // 创建测试职位
        String posJson = "{\n" +
                "  \"positionCode\": \"TEST_POS\",\n" +
                "  \"positionName\": \"测试职位\",\n" +
                "  \"deptId\": " + testDeptId + ",\n" +
                "  \"level\": 10,\n" +
                "  \"jobGrade\": \"P10\"\n" +
                "}";

        String posResponse = mockMvc.perform(post("/api/org/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(posJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String posIdStr = posResponse.substring(posResponse.indexOf("\"id\":") + 5, posResponse.indexOf(",\"positionCode\""));
        testPositionId = Long.parseLong(posIdStr);
    }

    // ============ 部门相关测试 ============

    @Test
    @DisplayName("测试获取部门树")
    void testGetDepartmentTree() throws Exception {
        // 执行
        mockMvc.perform(get("/api/org/departments/tree"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("测试获取部门树 - 指定父节点")
    void testGetDepartmentTree_WithParentId() throws Exception {
        // 执行
        mockMvc.perform(get("/api/org/departments/tree")
                        .param("parentId", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("测试创建部门")
    void testCreateDepartment() throws Exception {
        // 准备
        String deptJson = "{\n" +
                "  \"deptCode\": \"NEW_DEPT\",\n" +
                "  \"deptName\": \"新部门\",\n" +
                "  \"parentId\": 0,\n" +
                "  \"enabled\": true,\n" +
                "  \"sortOrder\": 20\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/org/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deptJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.deptCode").value("NEW_DEPT"))
                .andExpect(jsonPath("$.data.deptName").value("新部门"));
    }

    @Test
    @DisplayName("测试创建部门 - 重复编码")
    void testCreateDepartment_DuplicateCode() throws Exception {
        // 准备
        String deptJson = "{\n" +
                "  \"deptCode\": \"TEST_DEPT\",\n" +
                "  \"deptName\": \"重复部门\",\n" +
                "  \"parentId\": 0\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/org/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deptJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("测试更新部门")
    void testUpdateDepartment() throws Exception {
        // 准备
        String updateJson = "{\n" +
                "  \"id\": " + testDeptId + ",\n" +
                "  \"deptCode\": \"UPDATED_DEPT\",\n" +
                "  \"deptName\": \"更新后的部门\",\n" +
                "  \"enabled\": true\n" +
                "}";

        // 执行
        mockMvc.perform(put("/api/org/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.deptCode").value("UPDATED_DEPT"))
                .andExpect(jsonPath("$.data.deptName").value("更新后的部门"));
    }

    @Test
    @DisplayName("测试删除部门")
    void testDeleteDepartment() throws Exception {
        // 先创建一个可删除的部门
        String createJson = "{\n" +
                "  \"deptCode\": \"TO_DELETE\",\n" +
                "  \"deptName\": \"待删除\",\n" +
                "  \"parentId\": 0,\n" +
                "  \"enabled\": true\n" +
                "}";

        String response = mockMvc.perform(post("/api/org/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String idStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(",\"deptCode\""));
        Long id = Long.parseLong(idStr);

        // 执行删除
        mockMvc.perform(delete("/api/org/departments")
                        .param("id", String.valueOf(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取部门详情")
    void testGetDepartmentById() throws Exception {
        // 执行
        mockMvc.perform(get("/api/org/departments/{id}", testDeptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testDeptId))
                .andExpect(jsonPath("$.data.deptCode").exists())
                .andExpect(jsonPath("$.data.deptName").exists());
    }

    // ============ 职位相关测试 ============

    @Test
    @DisplayName("测试获取职位列表")
    void testGetPositionList() throws Exception {
        // 执行
        mockMvc.perform(get("/api/org/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("测试获取职位列表 - 按部门")
    void testGetPositionList_ByDepartment() throws Exception {
        // 执行
        mockMvc.perform(get("/api/org/positions")
                        .param("deptId", String.valueOf(testDeptId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", everyItem(hasEntry("deptId", testDeptId.intValue()))));
    }

    @Test
    @DisplayName("测试创建职位")
    void testCreatePosition() throws Exception {
        // 准备
        String posJson = "{\n" +
                "  \"positionCode\": \"NEW_POS\",\n" +
                "  \"positionName\": \"新职位\",\n" +
                "  \"deptId\": " + testDeptId + ",\n" +
                "  \"level\": 12,\n" +
                "  \"jobGrade\": \"P12\"\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/org/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(posJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.positionCode").value("NEW_POS"))
                .andExpect(jsonPath("$.data.positionName").value("新职位"));
    }

    @Test
    @DisplayName("测试创建职位 - 重复编码")
    void testCreatePosition_DuplicateCode() throws Exception {
        // 准备
        String posJson = "{\n" +
                "  \"positionCode\": \"TEST_POS\",\n" +
                "  \"positionName\": \"重复职位\",\n" +
                "  \"deptId\": " + testDeptId + "\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/org/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(posJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }

    @Test
    @DisplayName("测试更新职位")
    void testUpdatePosition() throws Exception {
        // 准备
        String updateJson = "{\n" +
                "  \"id\": " + testPositionId + ",\n" +
                "  \"positionCode\": \"UPDATED_POS\",\n" +
                "  \"positionName\": \"更新后的职位\",\n" +
                "  \"jobGrade\": \"P14\"\n" +
                "}";

        // 执行
        mockMvc.perform(put("/api/org/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.positionCode").value("UPDATED_POS"))
                .andExpect(jsonPath("$.data.positionName").value("更新后的职位"));
    }

    @Test
    @DisplayName("测试删除职位")
    void testDeletePosition() throws Exception {
        // 先创建一个可删除的职位
        String createJson = "{\n" +
                "  \"positionCode\": \"TO_DEL_POS\",\n" +
                "  \"positionName\": \"待删除职位\",\n" +
                "  \"deptId\": " + testDeptId + ",\n" +
                "  \"level\": 10\n" +
                "}";

        String response = mockMvc.perform(post("/api/org/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String idStr = response.substring(response.indexOf("\"id\":") + 5, response.indexOf(",\"positionCode\""));
        Long id = Long.parseLong(idStr);

        // 执行删除
        mockMvc.perform(delete("/api/org/positions")
                        .param("id", String.valueOf(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取职位详情")
    void testGetPositionById() throws Exception {
        // 执行
        mockMvc.perform(get("/api/org/positions/{id}", testPositionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(testPositionId))
                .andExpect(jsonPath("$.data.positionCode").exists())
                .andExpect(jsonPath("$.data.positionName").exists());
    }

    // ============ 通用测试 ============

    @Test
    @DisplayName("测试响应结构")
    void testResponseStructure() throws Exception {
        // 执行
        mockMvc.perform(get("/api/org/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("测试创建子部门")
    void testCreateChildDepartment() throws Exception {
        // 准备
        String childJson = "{\n" +
                "  \"deptCode\": \"CHILD_DEPT\",\n" +
                "  \"deptName\": \"子部门\",\n" +
                "  \"parentId\": " + testDeptId + ",\n" +
                "  \"enabled\": true\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/org/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(childJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.parentId").value(testDeptId))
                .andExpect(jsonPath("$.data.levelDepth").value(2));
    }

    @Test
    @DisplayName("测试创建管理岗位")
    void testCreateManagerPosition() throws Exception {
        // 准备
        String managerJson = "{\n" +
                "  \"positionCode\": \"MGR_POS\",\n" +
                "  \"positionName\": \"经理职位\",\n" +
                "  \"deptId\": " + testDeptId + ",\n" +
                "  \"level\": 20,\n" +
                "  \"isManager\": true\n" +
                "}";

        // 执行
        mockMvc.perform(post("/api/org/positions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(managerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.isManager").value(true));
    }

    @Test
    @DisplayName("测试资源不存在")
    void testResourceNotFound() throws Exception {
        // 执行
        mockMvc.perform(get("/api/org/departments/{id}", 99999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));

        mockMvc.perform(get("/api/org/positions/{id}", 99999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(not(200)));
    }
}
