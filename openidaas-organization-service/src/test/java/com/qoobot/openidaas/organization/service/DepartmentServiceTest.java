package com.qoobot.openidaas.organization.service;

import com.qoobot.openidaas.common.dto.department.DepartmentCreateDTO;
import com.qoobot.openidaas.common.dto.department.DepartmentUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.department.DepartmentVO;
import com.qoobot.openidaas.organization.entity.Department;
import com.qoobot.openidaas.organization.mapper.DepartmentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 部门服务单元测试
 *
 * @author QooBot
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("部门服务测试")
class DepartmentServiceTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentMapper departmentMapper;

    private Department testDepartment;
    private DepartmentCreateDTO createDTO;
    private DepartmentUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        // 创建测试部门（根部门）
        testDepartment = new Department();
        testDepartment.setDeptCode("TEST_ROOT");
        testDepartment.setDeptName("测试根部门");
        testDepartment.setParentId(0L);
        testDepartment.setLevelPath("/TEST_ROOT/");
        testDepartment.setLevelDepth(1);
        testDepartment.setSortOrder(100);
        testDepartment.setStatus(1);
        testDepartment.setDescription("测试用");
        departmentMapper.insert(testDepartment);

        // 创建DTO
        createDTO = new DepartmentCreateDTO();
        createDTO.setDeptCode("NEW_DEPT");
        createDTO.setDeptName("新部门");
        createDTO.setParentId(0L);
        createDTO.setEnabled(true);
        createDTO.setSortOrder(10);
        createDTO.setDescription("新创建的部门");

        updateDTO = new DepartmentUpdateDTO();
        updateDTO.setId(testDepartment.getId());
        updateDTO.setDeptCode("UPDATED_DEPT");
        updateDTO.setDeptName("更新后的部门");
        updateDTO.setDescription("更新后的描述");
    }

    @Test
    @DisplayName("测试获取部门树 - 根节点")
    void testGetDepartmentTree_Root() {
        // 执行
        List<DepartmentVO> tree = departmentService.getDepartmentTree(0L);

        // 验证
        assertNotNull(tree);
        assertFalse(tree.isEmpty());
    }

    @Test
    @DisplayName("测试获取部门树 - Null父ID")
    void testGetDepartmentTree_NullParentId() {
        // 执行
        List<DepartmentVO> tree = departmentService.getDepartmentTree(null);

        // 验证
        assertNotNull(tree);
        assertFalse(tree.isEmpty());
    }

    @Test
    @DisplayName("测试获取部门树 - 指定父节点")
    void testGetDepartmentTree_WithParentId() {
        // 先创建子部门
        DepartmentCreateDTO childDTO = new DepartmentCreateDTO();
        childDTO.setDeptCode("CHILD_DEPT");
        childDTO.setDeptName("子部门");
        childDTO.setParentId(testDepartment.getId());
        childDTO.setEnabled(true);
        DepartmentVO child = departmentService.createDepartment(childDTO);

        // 执行
        List<DepartmentVO> tree = departmentService.getDepartmentTree(testDepartment.getId());

        // 验证
        assertNotNull(tree);
        assertFalse(tree.isEmpty());
        assertEquals("CHILD_DEPT", tree.get(0).getDeptCode());
        assertEquals("子部门", tree.get(0).getDeptName());
    }

    @Test
    @DisplayName("测试创建部门 - 根部门")
    void testCreateDepartment_RootDepartment() {
        // 执行
        DepartmentVO result = departmentService.createDepartment(createDTO);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("NEW_DEPT", result.getDeptCode());
        assertEquals("新部门", result.getDeptName());
        assertEquals(0L, result.getParentId());
        assertEquals("/NEW_DEPT/", result.getTreePath());
        assertTrue(result.getEnabled());
    }

    @Test
    @DisplayName("测试创建部门 - 子部门")
    void testCreateDepartment_ChildDepartment() {
        // 准备
        createDTO.setParentId(testDepartment.getId());

        // 执行
        DepartmentVO result = departmentService.createDepartment(createDTO);

        // 验证
        assertNotNull(result);
        assertEquals(testDepartment.getId(), result.getParentId());
        assertEquals("/TEST_ROOT/NEW_DEPT/", result.getTreePath());
    }

    @Test
    @DisplayName("测试创建部门 - 重复部门编码")
    void testCreateDepartment_DuplicateCode() {
        // 准备
        createDTO.setDeptCode("TEST_ROOT");

        // 执行和验证
        assertThrows(BusinessException.class, () -> departmentService.createDepartment(createDTO));
    }

    @Test
    @DisplayName("测试创建部门 - 父部门不存在")
    void testCreateDepartment_ParentNotExist() {
        // 准备
        createDTO.setParentId(99999L);

        // 执行和验证
        assertThrows(BusinessException.class, () -> departmentService.createDepartment(createDTO));
    }

    @Test
    @DisplayName("测试创建部门 - 禁用状态")
    void testCreateDepartment_Disabled() {
        // 准备
        createDTO.setEnabled(false);

        // 执行
        DepartmentVO result = departmentService.createDepartment(createDTO);

        // 验证
        assertNotNull(result);
        assertFalse(result.getEnabled());
    }

    @Test
    @DisplayName("测试更新部门 - 成功")
    void testUpdateDepartment_Success() {
        // 执行
        DepartmentVO result = departmentService.updateDepartment(updateDTO);

        // 验证
        assertNotNull(result);
        assertEquals("UPDATED_DEPT", result.getDeptCode());
        assertEquals("更新后的部门", result.getDeptName());
        assertEquals("更新后的描述", result.getDescription());
    }

    @Test
    @DisplayName("测试更新部门 - 部门不存在")
    void testUpdateDepartment_DepartmentNotExist() {
        // 准备
        updateDTO.setId(99999L);

        // 执行和验证
        assertThrows(BusinessException.class, () -> departmentService.updateDepartment(updateDTO));
    }

    @Test
    @DisplayName("测试更新部门 - 重复部门编码")
    void testUpdateDepartment_DuplicateCode() {
        // 准备
        updateDTO.setDeptCode("TECH");

        // 执行和验证
        assertThrows(BusinessException.class, () -> departmentService.updateDepartment(updateDTO));
    }

    @Test
    @DisplayName("测试更新部门 - 更新父部门")
    void testUpdateDepartment_UpdateParent() {
        // 先创建新的父部门
        DepartmentCreateDTO newParentDTO = new DepartmentCreateDTO();
        newParentDTO.setDeptCode("NEW_PARENT");
        newParentDTO.setDeptName("新父部门");
        newParentDTO.setParentId(0L);
        newParentDTO.setEnabled(true);
        DepartmentVO newParent = departmentService.createDepartment(newParentDTO);

        // 创建子部门
        DepartmentCreateDTO childDTO = new DepartmentCreateDTO();
        childDTO.setDeptCode("CHILD");
        childDTO.setDeptName("子部门");
        childDTO.setParentId(testDepartment.getId());
        childDTO.setEnabled(true);
        DepartmentVO child = departmentService.createDepartment(childDTO);

        // 更新子部门的父部门
        DepartmentUpdateDTO childUpdate = new DepartmentUpdateDTO();
        childUpdate.setId(child.getId());
        childUpdate.setDeptCode("CHILD");
        childUpdate.setDeptName("子部门");
        childUpdate.setParentId(newParent.getId());
        childUpdate.setEnabled(true);

        // 执行
        DepartmentVO result = departmentService.updateDepartment(childUpdate);

        // 验证
        assertNotNull(result);
        assertEquals(newParent.getId(), result.getParentId());
    }

    @Test
    @DisplayName("测试更新部门 - 设置自己为父部门")
    void testUpdateDepartment_SelfAsParent() {
        // 准备
        updateDTO.setParentId(testDepartment.getId());

        // 执行和验证
        assertThrows(BusinessException.class, () -> departmentService.updateDepartment(updateDTO));
    }

    @Test
    @DisplayName("测试更新部门 - 设置子孙部门为父部门")
    void testUpdateDepartment_DescendantAsParent() {
        // 先创建子部门
        DepartmentCreateDTO childDTO = new DepartmentCreateDTO();
        childDTO.setDeptCode("CHILD_OF_ROOT");
        childDTO.setDeptName("子部门");
        childDTO.setParentId(testDepartment.getId());
        childDTO.setEnabled(true);
        DepartmentVO child = departmentService.createDepartment(childDTO);

        // 尝试将父部门设置为子部门
        updateDTO.setParentId(child.getId());

        // 执行和验证
        assertThrows(BusinessException.class, () -> departmentService.updateDepartment(updateDTO));
    }

    @Test
    @DisplayName("测试删除部门 - 成功")
    void testDeleteDepartment_Success() {
        // 创建一个没有子部门和用户的部门
        DepartmentCreateDTO dto = new DepartmentCreateDTO();
        dto.setDeptCode("TO_DELETE");
        dto.setDeptName("待删除");
        dto.setParentId(0L);
        dto.setEnabled(true);
        DepartmentVO created = departmentService.createDepartment(dto);

        // 执行
        departmentService.deleteDepartment(created.getId());

        // 验证
        Department deleted = departmentMapper.selectById(created.getId());
        assertNull(deleted);
    }

    @Test
    @DisplayName("测试删除部门 - 部门不存在")
    void testDeleteDepartment_DepartmentNotExist() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> departmentService.deleteDepartment(99999L));
    }

    @Test
    @DisplayName("测试删除部门 - 有子部门")
    void testDeleteDepartment_HasChildren() {
        // 先创建子部门
        DepartmentCreateDTO childDTO = new DepartmentCreateDTO();
        childDTO.setDeptCode("CHILD");
        childDTO.setDeptName("子部门");
        childDTO.setParentId(testDepartment.getId());
        childDTO.setEnabled(true);
        departmentService.createDepartment(childDTO);

        // 执行和验证
        assertThrows(BusinessException.class, () -> departmentService.deleteDepartment(testDepartment.getId()));
    }

    @Test
    @DisplayName("测试获取部门详情 - 成功")
    void testGetDepartmentById_Success() {
        // 执行
        DepartmentVO result = departmentService.getDepartmentById(testDepartment.getId());

        // 验证
        assertNotNull(result);
        assertEquals(testDepartment.getId(), result.getId());
        assertEquals("TEST_ROOT", result.getDeptCode());
        assertEquals("测试根部门", result.getDeptName());
    }

    @Test
    @DisplayName("测试获取部门详情 - 部门不存在")
    void testGetDepartmentById_DepartmentNotExist() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> departmentService.getDepartmentById(99999L));
    }

    @Test
    @DisplayName("测试多层部门树")
    void testMultiLevelDepartmentTree() {
        // 创建3层部门树
        DepartmentVO level1 = departmentService.createDepartment(new DepartmentCreateDTO() {{
            setDeptCode("L1");
            setDeptName("一级");
            setParentId(0L);
            setEnabled(true);
        }});

        DepartmentVO level2 = departmentService.createDepartment(new DepartmentCreateDTO() {{
            setDeptCode("L2");
            setDeptName("二级");
            setParentId(level1.getId());
            setEnabled(true);
        }});

        DepartmentVO level3 = departmentService.createDepartment(new DepartmentCreateDTO() {{
            setDeptCode("L3");
            setDeptName("三级");
            setParentId(level2.getId());
            setEnabled(true);
        }});

        // 验证层级
        assertEquals("/L1/", level1.getTreePath());
        assertEquals("/L1/L2/", level2.getTreePath());
        assertEquals("/L1/L2/L3/", level3.getTreePath());
    }

    @Test
    @DisplayName("测试更新部门 - 部分字段")
    void testUpdateDepartment_PartialFields() {
        // 准备
        DepartmentUpdateDTO partialUpdate = new DepartmentUpdateDTO();
        partialUpdate.setId(testDepartment.getId());
        partialUpdate.setDescription("仅更新描述");

        // 执行
        DepartmentVO result = departmentService.updateDepartment(partialUpdate);

        // 验证
        assertNotNull(result);
        assertEquals("仅更新描述", result.getDescription());
    }

    @Test
    @DisplayName("测试层级路径正确性")
    void testLevelPathCorrectness() {
        // 创建父部门
        DepartmentVO parent = departmentService.createDepartment(new DepartmentCreateDTO() {{
            setDeptCode("PARENT");
            setDeptName("父");
            setParentId(0L);
            setEnabled(true);
        }});

        // 创建子部门
        DepartmentVO child1 = departmentService.createDepartment(new DepartmentCreateDTO() {{
            setDeptCode("CHILD1");
            setDeptName("子1");
            setParentId(parent.getId());
            setEnabled(true);
        }});

        DepartmentVO child2 = departmentService.createDepartment(new DepartmentCreateDTO() {{
            setDeptCode("CHILD2");
            setDeptName("子2");
            setParentId(parent.getId());
            setEnabled(true);
        }});

        // 验证路径
        assertEquals("/PARENT/", parent.getTreePath());
        assertEquals("/PARENT/CHILD1/", child1.getTreePath());
        assertEquals("/PARENT/CHILD2/", child2.getTreePath());
    }

    @Test
    @DisplayName("测试部门状态")
    void testDepartmentStatus() {
        // 创建禁用的部门
        DepartmentCreateDTO dto = new DepartmentCreateDTO();
        dto.setDeptCode("DISABLED");
        dto.setDeptName("禁用部门");
        dto.setParentId(0L);
        dto.setEnabled(false);
        DepartmentVO disabled = departmentService.createDepartment(dto);

        // 验证
        assertFalse(disabled.getEnabled());
    }
}
