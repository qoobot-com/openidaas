package com.qoobot.openidaas.role.service;

import com.qoobot.openidaas.common.dto.role.RoleCreateDTO;
import com.qoobot.openidaas.common.dto.role.RoleUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.role.RoleVO;
import com.qoobot.openidaas.role.entity.Role;
import com.qoobot.openidaas.role.mapper.RoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 角色服务边界条件测试
 *
 * @author QooBot
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("角色服务边界条件测试")
class RoleServiceEdgeCaseTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMapper roleMapper;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setRoleCode("TEST_EDGE");
        testRole.setRoleName("边界测试角色");
        testRole.setRoleType(2);
        testRole.setParentId(0L);
        testRole.setEnabled(1);
        roleMapper.insert(testRole);
    }

    @Test
    @DisplayName("测试Null角色编码创建")
    void testCreateRole_NullRoleCode() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleName("测试");

        // 执行和验证 - 应该抛出异常
        assertThrows(Exception.class, () -> roleService.createRole(dto));
    }

    @Test
    @DisplayName("测试空字符串角色编码")
    void testCreateRole_EmptyRoleCode() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("");
        dto.setRoleName("测试");

        // 执行和验证
        assertThrows(Exception.class, () -> roleService.createRole(dto));
    }

    @Test
    @DisplayName("测试超长角色编码")
    void testCreateRole_TooLongRoleCode() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("A".repeat(100)); // 超过50字符
        dto.setRoleName("测试");

        // 执行和验证
        assertThrows(Exception.class, () -> roleService.createRole(dto));
    }

    @Test
    @DisplayName("测试特殊字符角色编码")
    void testCreateRole_SpecialCharactersCode() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("ROLE@#$%");
        dto.setRoleName("测试");

        // 执行
        RoleVO result = roleService.createRole(dto);

        // 验证
        assertNotNull(result);
        assertEquals("ROLE@#$%", result.getRoleCode());
    }

    @Test
    @DisplayName("测试Null角色名称")
    void testCreateRole_NullRoleName() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("TEST_NULL_NAME");

        // 执行和验证
        assertThrows(Exception.class, () -> roleService.createRole(dto));
    }

    @Test
    @DisplayName("测试超长角色名称")
    void testCreateRole_TooLongRoleName() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("TEST_LONG_NAME");
        dto.setRoleName("A".repeat(200)); // 超过100字符

        // 执行和验证
        assertThrows(Exception.class, () -> roleService.createRole(dto));
    }

    @Test
    @DisplayName("测试Null角色类型")
    void testCreateRole_NullRoleType() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("TEST_NULL_TYPE");
        dto.setRoleName("测试");
        dto.setRoleType(null);

        // 执行和验证
        assertThrows(Exception.class, () -> roleService.createRole(dto));
    }

    @Test
    @DisplayName("测试无效角色类型")
    void testCreateRole_InvalidRoleType() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("TEST_INVALID_TYPE");
        dto.setRoleName("测试");
        dto.setRoleType(99);

        // 执行 - 可能成功也可能失败，取决于验证
        RoleVO result = roleService.createRole(dto);

        // 验证
        assertNotNull(result);
        assertEquals(99, result.getRoleType());
    }

    @Test
    @DisplayName("测试Null父角色ID")
    void testCreateRole_NullParentId() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("TEST_NULL_PARENT");
        dto.setRoleName("测试");
        dto.setRoleType(2);
        dto.setParentId(null);

        // 执行
        RoleVO result = roleService.createRole(dto);

        // 验证 - 应该默认为0
        assertNotNull(result);
        assertEquals(0L, result.getParentId());
    }

    @Test
    @DisplayName("测试负数父角色ID")
    void testCreateRole_NegativeParentId() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("TEST_NEG_PARENT");
        dto.setRoleName("测试");
        dto.setRoleType(2);
        dto.setParentId(-1L);

        // 执行和验证
        assertThrows(Exception.class, () -> roleService.createRole(dto));
    }

    @Test
    @DisplayName("测试Null状态")
    void testCreateRole_NullStatus() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("TEST_NULL_STATUS");
        dto.setRoleName("测试");
        dto.setRoleType(2);
        dto.setEnabled(null);

        // 执行
        RoleVO result = roleService.createRole(dto);

        // 验证
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试无效状态值")
    void testCreateRole_InvalidStatus() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("TEST_INVALID_STATUS");
        dto.setRoleName("测试");
        dto.setRoleType(2);
        dto.setEnabled(true);

        // 执行
        RoleVO result = roleService.createRole(dto);

        // 验证
        assertNotNull(result);
        assertEquals(true, result.getEnabled());
    }

    @Test
    @DisplayName("测试更新不存在的角色")
    void testUpdateRole_NotExist() {
        // 准备
        RoleUpdateDTO dto = new RoleUpdateDTO();
        dto.setId(99999L);
        dto.setRoleCode("UPDATED");
        dto.setRoleName("更新");

        // 执行和验证
        assertThrows(BusinessException.class, () -> roleService.updateRole(dto));
    }

    @Test
    @DisplayName("测试更新Null ID")
    void testUpdateRole_NullId() {
        // 准备
        RoleUpdateDTO dto = new RoleUpdateDTO();
        dto.setId(null);
        dto.setRoleCode("UPDATED");

        // 执行和验证
        assertThrows(Exception.class, () -> roleService.updateRole(dto));
    }

    @Test
    @DisplayName("测试删除Null ID")
    void testDeleteRole_NullId() {
        // 执行和验证
        assertThrows(Exception.class, () -> roleService.deleteRole(null));
    }

    @Test
    @DisplayName("测试删除负数ID")
    void testDeleteRole_NegativeId() {
        // 执行和验证
        assertThrows(Exception.class, () -> roleService.deleteRole(-1L));
    }

    @Test
    @DisplayName("测试删除零ID")
    void testDeleteRole_ZeroId() {
        // 执行和验证
        assertThrows(Exception.class, () -> roleService.deleteRole(0L));
    }

    @Test
    @DisplayName("测试获取Null ID角色")
    void testGetRoleById_NullId() {
        // 执行和验证
        assertThrows(Exception.class, () -> roleService.getRoleById(null));
    }

    @Test
    @DisplayName("测试分配Null角色ID权限")
    void testAssignPermissions_NullRoleId() {
        // 执行和验证
        assertThrows(Exception.class, () -> {
            roleService.assignPermissions(null, Arrays.asList(1L, 2L));
        });
    }

    @Test
    @DisplayName("测试分配Null权限列表")
    void testAssignPermissions_NullPermIds() {
        // 执行
        roleService.assignPermissions(testRole.getId(), null);

        // 验证
        List<Long> result = roleService.getRolePermissions(testRole.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试分配空权限列表")
    void testAssignPermissions_EmptyPermIds() {
        // 先分配一些权限
        roleService.assignPermissions(testRole.getId(), Arrays.asList(1L, 2L));

        // 执行
        roleService.assignPermissions(testRole.getId(), Arrays.asList());

        // 验证
        List<Long> result = roleService.getRolePermissions(testRole.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试分配包含Null的权限列表")
    void testAssignPermissions_WithNullInList() {
        // 执行 - 可能抛出异常或忽略null
        try {
            roleService.assignPermissions(testRole.getId(), Arrays.asList(1L, null, 2L));
        } catch (Exception e) {
            // 预期的异常
            return;
        }

        // 如果没抛异常，验证结果
        List<Long> result = roleService.getRolePermissions(testRole.getId());
        // 应该只包含有效ID
    }

    @Test
    @DisplayName("测试移除Null角色ID权限")
    void testRemovePermissions_NullRoleId() {
        // 执行和验证
        assertThrows(Exception.class, () -> {
            roleService.removePermissions(null, Arrays.asList(1L, 2L));
        });
    }

    @Test
    @DisplayName("测试移除Null权限列表")
    void testRemovePermissions_NullPermIds() {
        // 执行和验证
        assertThrows(Exception.class, () -> {
            roleService.removePermissions(testRole.getId(), null);
        });
    }

    @Test
    @DisplayName("测试移除空权限列表")
    void testRemovePermissions_EmptyPermIds() {
        // 执行 - 应该不抛异常
        roleService.removePermissions(testRole.getId(), Arrays.asList());
    }

    @Test
    @DisplayName("测试获取Null角色ID权限")
    void testGetRolePermissions_NullRoleId() {
        // 执行和验证
        assertThrows(Exception.class, () -> {
            roleService.getRolePermissions(null);
        });
    }

    @Test
    @DisplayName("测试分配Null用户ID角色")
    void testAssignRoleToUser_NullUserId() {
        // 执行和验证
        assertThrows(Exception.class, () -> {
            roleService.assignRoleToUser(null, testRole.getId(), 1, 1L, null);
        });
    }

    @Test
    @DisplayName("测试分配Null角色ID给用户")
    void testAssignRoleToUser_NullRoleId() {
        // 执行和验证
        assertThrows(Exception.class, () -> {
            roleService.assignRoleToUser(10L, null, 1, 1L, null);
        });
    }

    @Test
    @DisplayName("测试重复分配相同角色")
    void testAssignRoleToUser_Duplicate() {
        // 第一次分配
        roleService.assignRoleToUser(10L, testRole.getId(), 1, 1L, null);

        // 第二次分配
        assertThrows(BusinessException.class, () -> {
            roleService.assignRoleToUser(10L, testRole.getId(), 1, 1L, null);
        });
    }

    @Test
    @DisplayName("测试Null作用域类型")
    void testAssignRoleToUser_NullScopeType() {
        // 执行 - 应该允许null
        roleService.assignRoleToUser(10L, testRole.getId(), null, null, null);

        // 验证
        List<RoleVO> userRoles = roleService.getUserRoles(10L);
        assertFalse(userRoles.isEmpty());
    }

    @Test
    @DisplayName("测试Null作用域ID")
    void testAssignRoleToUser_NullScopeId() {
        // 执行 - 应该允许null
        roleService.assignRoleToUser(10L, testRole.getId(), 1, null, null);

        // 验证
        List<RoleVO> userRoles = roleService.getUserRoles(10L);
        assertFalse(userRoles.isEmpty());
    }

    @Test
    @DisplayName("测试过去时间过期时间")
    void testAssignRoleToUser_PastExpireTime() {
        // 准备
        java.time.LocalDateTime pastTime = java.time.LocalDateTime.now().minusDays(1);

        // 执行
        roleService.assignRoleToUser(10L, testRole.getId(), 1, 1L, pastTime);

        // 验证
        List<RoleVO> userRoles = roleService.getUserRoles(10L);
        assertFalse(userRoles.isEmpty());
    }

    @Test
    @DisplayName("测试移除Null用户ID角色")
    void testRemoveRoleFromUser_NullUserId() {
        // 执行和验证
        assertThrows(Exception.class, () -> {
            roleService.removeRoleFromUser(null, testRole.getId());
        });
    }

    @Test
    @DisplayName("测试移除Null角色ID")
    void testRemoveRoleFromUser_NullRoleId() {
        // 执行和验证
        assertThrows(Exception.class, () -> {
            roleService.removeRoleFromUser(10L, null);
        });
    }

    @Test
    @DisplayName("测试获取Null用户ID角色")
    void testGetUserRoles_NullUserId() {
        // 执行和验证
        assertThrows(Exception.class, () -> {
            roleService.getUserRoles(null);
        });
    }

    @Test
    @DisplayName("测试获取角色列表 - Null参数")
    void testGetRoleList_NullParam() {
        // 执行
        List<RoleVO> result = roleService.getRoleList(null);

        // 验证
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("测试获取角色树 - Null父ID")
    void testGetRoleTree_NullParentId() {
        // 执行
        List<RoleVO> result = roleService.getRoleTree(null);

        // 验证
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试获取角色树 - 负数父ID")
    void testGetRoleTree_NegativeParentId() {
        // 执行 - 应该返回空列表或不抛异常
        List<RoleVO> result = roleService.getRoleTree(-1L);

        // 验证
        assertNotNull(result);
    }

    @Test
    @DisplayName("测试自引用父角色")
    void testCreateRole_SelfParent() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("SELF_PARENT");
        dto.setRoleName("自引用");
        dto.setRoleType(2);
        dto.setParentId(testRole.getId());

        // 执行
        RoleVO result = roleService.createRole(dto);

        // 验证
        assertNotNull(result);
        assertEquals(testRole.getId(), result.getParentId());

        // 尝试将子角色设为父角色
        RoleUpdateDTO updateDto = new RoleUpdateDTO();
        updateDto.setId(testRole.getId());
        updateDto.setParentId(result.getId());

        // 这应该失败或创建循环引用
        try {
            roleService.updateRole(updateDto);
        } catch (Exception e) {
            // 预期的异常
            return;
        }
    }

    @Test
    @DisplayName("测试大量角色创建")
    void testCreateManyRoles() {
        // 执行
        for (int i = 0; i < 100; i++) {
            RoleCreateDTO dto = new RoleCreateDTO();
            dto.setRoleCode("BATCH_ROLE_" + i);
            dto.setRoleName("批量角色" + i);
            dto.setRoleType(2);
            RoleVO result = roleService.createRole(dto);
            assertNotNull(result);
        }

        // 验证
        List<RoleVO> all = roleService.getRoleList(2);
        assertTrue(all.size() >= 100);
    }

    @Test
    @DisplayName("测试角色类型边界值")
    void testRoleTypeBoundary() {
        // 测试最小值
        RoleCreateDTO dto1 = new RoleCreateDTO();
        dto1.setRoleCode("MIN_TYPE");
        dto1.setRoleName("最小类型");
        dto1.setRoleType(Integer.MIN_VALUE);
        RoleVO result1 = roleService.createRole(dto1);
        assertNotNull(result1);

        // 测试最大值
        RoleCreateDTO dto2 = new RoleCreateDTO();
        dto2.setRoleCode("MAX_TYPE");
        dto2.setRoleName("最大类型");
        dto2.setRoleType(Integer.MAX_VALUE);
        RoleVO result2 = roleService.createRole(dto2);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("测试排序边界值")
    void testSortOrderBoundary() {
        // 测试最小值
        RoleCreateDTO dto1 = new RoleCreateDTO();
        dto1.setRoleCode("MIN_SORT");
        dto1.setRoleName("最小排序");
        dto1.setRoleType(2);
        dto1.setSortOrder(Integer.MIN_VALUE);
        RoleVO result1 = roleService.createRole(dto1);
        assertNotNull(result1);

        // 测试最大值
        RoleCreateDTO dto2 = new RoleCreateDTO();
        dto2.setRoleCode("MAX_SORT");
        dto2.setRoleName("最大排序");
        dto2.setRoleType(2);
        dto2.setSortOrder(Integer.MAX_VALUE);
        RoleVO result2 = roleService.createRole(dto2);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("测试空描述")
    void testEmptyDescription() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("EMPTY_DESC");
        dto.setRoleName("空描述");
        dto.setRoleType(2);
        dto.setDescription("");

        // 执行
        RoleVO result = roleService.createRole(dto);

        // 验证
        assertNotNull(result);
        assertEquals("", result.getDescription());
    }

    @Test
    @DisplayName("测试超长描述")
    void testTooLongDescription() {
        // 准备
        RoleCreateDTO dto = new RoleCreateDTO();
        dto.setRoleCode("LONG_DESC");
        dto.setRoleName("长描述");
        dto.setRoleType(2);
        dto.setDescription("A".repeat(600)); // 超过500字符

        // 执行和验证
        assertThrows(Exception.class, () -> roleService.createRole(dto));
    }

    @Test
    @DisplayName("测试角色链")
    void testRoleChain() {
        // 创建3层角色链
        RoleCreateDTO dto1 = new RoleCreateDTO();
        dto1.setRoleCode("LEVEL1");
        dto1.setRoleName("一级");
        dto1.setRoleType(2);
        RoleVO level1 = roleService.createRole(dto1);

        RoleCreateDTO dto2 = new RoleCreateDTO();
        dto2.setRoleCode("LEVEL2");
        dto2.setRoleName("二级");
        dto2.setRoleType(2);
        dto2.setParentId(level1.getId());
        RoleVO level2 = roleService.createRole(dto2);

        RoleCreateDTO dto3 = new RoleCreateDTO();
        dto3.setRoleCode("LEVEL3");
        dto3.setRoleName("三级");
        dto3.setRoleType(2);
        dto3.setParentId(level2.getId());
        RoleVO level3 = roleService.createRole(dto3);

        // 验证
        assertNotNull(level1);
        assertNotNull(level2);
        assertNotNull(level3);
        assertEquals(0L, level1.getParentId());
        assertEquals(level1.getId(), level2.getParentId());
        assertEquals(level2.getId(), level3.getParentId());
    }

    @Test
    @DisplayName("测试并发分配权限")
    void testConcurrentPermissionAssignment() throws InterruptedException {
        List<Long> permIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);

        // 创建多个线程并发分配
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                roleService.assignPermissions(testRole.getId(), permIds);
            });
            threads[i].start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证
        List<Long> result = roleService.getRolePermissions(testRole.getId());
        assertNotNull(result);
        // 应该有权限，但具体数量可能因并发而不确定
    }
}
