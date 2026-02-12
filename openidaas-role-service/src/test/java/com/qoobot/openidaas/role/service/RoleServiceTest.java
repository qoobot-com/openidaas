package com.qoobot.openidaas.role.service;

import com.qoobot.openidaas.common.dto.role.RoleCreateDTO;
import com.qoobot.openidaas.common.dto.role.RoleUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.role.RoleVO;
import com.qoobot.openidaas.role.entity.Role;
import com.qoobot.openidaas.role.entity.UserRole;
import com.qoobot.openidaas.role.mapper.RoleMapper;
import com.qoobot.openidaas.role.mapper.RolePermissionMapper;
import com.qoobot.openidaas.role.mapper.UserRoleMapper;
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
 * 角色服务单元测试
 *
 * @author QooBot
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("角色服务测试")
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    private Role testRole;
    private RoleCreateDTO createDTO;
    private RoleUpdateDTO updateDTO;

    @BeforeEach
    void setUp() {
        // 创建测试角色
        testRole = new Role();
        testRole.setRoleCode("TEST_ROLE");
        testRole.setRoleName("测试角色");
        testRole.setRoleType(2);
        testRole.setParentId(0L);
        testRole.setDescription("测试用的角色");
        testRole.setIsBuiltin(0);
        testRole.setEnabled(1);
        testRole.setSortOrder(10);
        roleMapper.insert(testRole);

        // 创建DTO
        createDTO = new RoleCreateDTO();
        createDTO.setRoleCode("NEW_ROLE");
        createDTO.setRoleName("新角色");
        createDTO.setRoleType(2);
        createDTO.setParentId(0L);
        createDTO.setDescription("新创建的角色");
        createDTO.setEnabled( true);
        createDTO.setSortOrder(20);

        updateDTO = new RoleUpdateDTO();
        updateDTO.setId(testRole.getId());
        updateDTO.setRoleCode("UPDATED_ROLE");
        updateDTO.setRoleName("更新后的角色");
        updateDTO.setDescription("更新后的描述");
    }

    @Test
    @DisplayName("测试获取角色列表 - 成功")
    void testGetRoleList_Success() {
        // 执行
        List<RoleVO> roles = roleService.getRoleList(null);

        // 验证
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertTrue(roles.size() >= 5);
    }

    @Test
    @DisplayName("测试获取角色列表 - 按类型筛选")
    void testGetRoleList_WithRoleType() {
        // 执行
        List<RoleVO> systemRoles = roleService.getRoleList(1);
        List<RoleVO> customRoles = roleService.getRoleList(2);

        // 验证
        assertNotNull(systemRoles);
        assertNotNull(customRoles);
        assertTrue(systemRoles.stream().allMatch(r -> r.getRoleType() == 1));
        assertTrue(customRoles.stream().allMatch(r -> r.getRoleType() == 2));
    }

    @Test
    @DisplayName("测试获取角色树 - 成功")
    void testGetRoleTree_Success() {
        // 执行
        List<RoleVO> tree = roleService.getRoleTree(0L);

        // 验证
        assertNotNull(tree);
        assertFalse(tree.isEmpty());
    }

    @Test
    @DisplayName("测试获取角色树 - 指定父角色")
    void testGetRoleTree_WithParentId() {
        // 先创建一个子角色
        Role childRole = new Role();
        childRole.setRoleCode("CHILD_ROLE");
        childRole.setRoleName("子角色");
        childRole.setRoleType(2);
        childRole.setParentId(testRole.getId());
        roleMapper.insert(childRole);

        // 执行
        List<RoleVO> tree = roleService.getRoleTree(testRole.getId());

        // 验证
        assertNotNull(tree);
        assertFalse(tree.isEmpty());
        assertEquals("CHILD_ROLE", tree.get(0).getRoleCode());
    }

    @Test
    @DisplayName("测试创建角色 - 成功")
    void testCreateRole_Success() {
        // 执行
        RoleVO result = roleService.createRole(createDTO);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("NEW_ROLE", result.getRoleCode());
        assertEquals("新角色", result.getRoleName());
        assertEquals(2, result.getRoleType());
    }

    @Test
    @DisplayName("测试创建角色 - 重复角色编码")
    void testCreateRole_DuplicateCode() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> {
            createDTO.setRoleCode("TEST_ROLE");
            roleService.createRole(createDTO);
        });
    }

    @Test
    @DisplayName("测试创建角色 - 父角色不存在")
    void testCreateRole_ParentNotExist() {
        // 准备
        createDTO.setParentId(99999L);

        // 执行和验证
        assertThrows(BusinessException.class, () -> roleService.createRole(createDTO));
    }

    @Test
    @DisplayName("测试创建角色 - 带父角色")
    void testCreateRole_WithParent() {
        // 准备
        createDTO.setParentId(testRole.getId());

        // 执行
        RoleVO result = roleService.createRole(createDTO);

        // 验证
        assertNotNull(result);
        assertEquals(testRole.getId(), result.getParentId());
    }

    @Test
    @DisplayName("测试更新角色 - 成功")
    void testUpdateRole_Success() {
        // 执行
        RoleVO result = roleService.updateRole(updateDTO);

        // 验证
        assertNotNull(result);
        assertEquals("UPDATED_ROLE", result.getRoleCode());
        assertEquals("更新后的角色", result.getRoleName());
        assertEquals("更新后的描述", result.getDescription());
    }

    @Test
    @DisplayName("测试更新角色 - 角色不存在")
    void testUpdateRole_RoleNotExist() {
        // 准备
        updateDTO.setId(99999L);

        // 执行和验证
        assertThrows(BusinessException.class, () -> roleService.updateRole(updateDTO));
    }

    @Test
    @DisplayName("测试更新角色 - 重复角色编码")
    void testUpdateRole_DuplicateCode() {
        // 准备
        updateDTO.setRoleCode("ADMIN");

        // 执行和验证
        assertThrows(BusinessException.class, () -> roleService.updateRole(updateDTO));
    }

    @Test
    @DisplayName("测试删除角色 - 成功")
    void testDeleteRole_Success() {
        // 先确保角色没有子角色和用户
        rolePermissionMapper.deleteByRoleId(testRole.getId());

        // 执行
        roleService.deleteRole(testRole.getId());

        // 验证
        Role deleted = roleMapper.selectById(testRole.getId());
        assertNull(deleted);
    }

    @Test
    @DisplayName("测试删除角色 - 角色不存在")
    void testDeleteRole_RoleNotExist() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> roleService.deleteRole(99999L));
    }

    @Test
    @DisplayName("测试删除角色 - 内置角色")
    void testDeleteRole_BuiltinRole() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> roleService.deleteRole(1L));
    }

    @Test
    @DisplayName("测试获取角色详情 - 成功")
    void testGetRoleById_Success() {
        // 执行
        RoleVO result = roleService.getRoleById(testRole.getId());

        // 验证
        assertNotNull(result);
        assertEquals(testRole.getId(), result.getId());
        assertEquals("TEST_ROLE", result.getRoleCode());
    }

    @Test
    @DisplayName("测试获取角色详情 - 角色不存在")
    void testGetRoleById_RoleNotExist() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> roleService.getRoleById(99999L));
    }

    @Test
    @DisplayName("测试分配权限给角色 - 成功")
    void testAssignPermissions_Success() {
        // 准备
        List<Long> permIds = Arrays.asList(1L, 2L, 3L);

        // 执行
        roleService.assignPermissions(testRole.getId(), permIds);

        // 验证
        List<Long> result = roleService.getRolePermissions(testRole.getId());
        assertEquals(3, result.size());
        assertTrue(result.containsAll(permIds));
    }

    @Test
    @DisplayName("测试分配权限给角色 - 角色不存在")
    void testAssignPermissions_RoleNotExist() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> {
            roleService.assignPermissions(99999L, Arrays.asList(1L, 2L));
        });
    }

    @Test
    @DisplayName("测试分配权限给角色 - 空权限列表")
    void testAssignPermissions_EmptyList() {
        // 执行
        roleService.assignPermissions(testRole.getId(), Arrays.asList());

        // 验证
        List<Long> result = roleService.getRolePermissions(testRole.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试分配权限给角色 - Null权限列表")
    void testAssignPermissions_NullList() {
        // 执行
        roleService.assignPermissions(testRole.getId(), null);

        // 验证
        List<Long> result = roleService.getRolePermissions(testRole.getId());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试移除角色权限 - 成功")
    void testRemovePermissions_Success() {
        // 先分配权限
        List<Long> permIds = Arrays.asList(1L, 2L, 3L);
        roleService.assignPermissions(testRole.getId(), permIds);

        // 执行
        roleService.removePermissions(testRole.getId(), Arrays.asList(1L, 2L));

        // 验证
        List<Long> result = roleService.getRolePermissions(testRole.getId());
        assertEquals(1, result.size());
        assertEquals(3L, result.get(0));
    }

    @Test
    @DisplayName("测试移除角色权限 - 角色不存在")
    void testRemovePermissions_RoleNotExist() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> {
            roleService.removePermissions(99999L, Arrays.asList(1L, 2L));
        });
    }

    @Test
    @DisplayName("测试获取角色权限 - 成功")
    void testGetRolePermissions_Success() {
        // 先分配权限
        List<Long> permIds = Arrays.asList(1L, 2L, 3L);
        roleService.assignPermissions(testRole.getId(), permIds);

        // 执行
        List<Long> result = roleService.getRolePermissions(testRole.getId());

        // 验证
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(permIds));
    }

    @Test
    @DisplayName("测试获取角色权限 - 无权限")
    void testGetRolePermissions_NoPermissions() {
        // 执行
        List<Long> result = roleService.getRolePermissions(testRole.getId());

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试分配角色给用户 - 成功")
    void testAssignRoleToUser_Success() {
        // 执行
        roleService.assignRoleToUser(10L, testRole.getId(), 1, 1L, null);

        // 验证
        List<RoleVO> userRoles = roleService.getUserRoles(10L);
        assertFalse(userRoles.isEmpty());
        assertEquals(testRole.getId(), userRoles.get(0).getId());
    }

    @Test
    @DisplayName("测试分配角色给用户 - 角色不存在")
    void testAssignRoleToUser_RoleNotExist() {
        // 执行和验证
        assertThrows(BusinessException.class, () -> {
            roleService.assignRoleToUser(10L, 99999L, 1, 1L, null);
        });
    }

    @Test
    @DisplayName("测试分配角色给用户 - 用户已拥有该角色")
    void testAssignRoleToUser_AlreadyHasRole() {
        // 先分配角色
        roleService.assignRoleToUser(10L, testRole.getId(), 1, 1L, null);

        // 执行和验证
        assertThrows(BusinessException.class, () -> {
            roleService.assignRoleToUser(10L, testRole.getId(), 1, 1L, null);
        });
    }

    @Test
    @DisplayName("测试分配角色给用户 - 带过期时间")
    void testAssignRoleToUser_WithExpireTime() {
        // 准备
        java.time.LocalDateTime expireTime = java.time.LocalDateTime.now().plusDays(30);

        // 执行
        roleService.assignRoleToUser(10L, testRole.getId(), 1, 1L, expireTime);

        // 验证
        List<UserRole> userRoles = userRoleMapper.selectByUserId(10L);
        assertFalse(userRoles.isEmpty());
        assertEquals(1, userRoles.get(0).getIsTemporary());
        assertNotNull(userRoles.get(0).getExpireTime());
    }

    @Test
    @DisplayName("测试移除用户角色 - 成功")
    void testRemoveRoleFromUser_Success() {
        // 先分配角色
        roleService.assignRoleToUser(10L, testRole.getId(), 1, 1L, null);

        // 执行
        roleService.removeRoleFromUser(10L, testRole.getId());

        // 验证
        List<RoleVO> userRoles = roleService.getUserRoles(10L);
        assertTrue(userRoles.isEmpty());
    }

    @Test
    @DisplayName("测试获取用户角色 - 成功")
    void testGetUserRoles_Success() {
        // 先分配角色
        roleService.assignRoleToUser(10L, testRole.getId(), 1, 1L, null);

        // 执行
        List<RoleVO> result = roleService.getUserRoles(10L);

        // 验证
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(testRole.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("测试获取用户角色 - 无角色")
    void testGetUserRoles_NoRoles() {
        // 执行
        List<RoleVO> result = roleService.getUserRoles(99999L);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试创建角色 - 所有字段")
    void testCreateRole_AllFields() {
        // 准备
        createDTO.setRoleCode("FULL_ROLE");
        createDTO.setRoleName("完整角色");
        createDTO.setRoleType(2);
        createDTO.setParentId(testRole.getId());
        createDTO.setDescription("完整的角色信息");
        createDTO.setEnabled(true);
        createDTO.setSortOrder(100);

        // 执行
        RoleVO result = roleService.createRole(createDTO);

        // 验证
        assertNotNull(result);
        assertEquals("FULL_ROLE", result.getRoleCode());
        assertEquals("完整角色", result.getRoleName());
        assertEquals(2, result.getRoleType());
        assertEquals(testRole.getId(), result.getParentId());
        assertEquals("完整的角色信息", result.getDescription());
        assertEquals(true, result.getEnabled());
        assertEquals(100, result.getSortOrder());
    }

    @Test
    @DisplayName("测试更新角色 - 部分字段")
    void testUpdateRole_PartialFields() {
        // 准备
        RoleUpdateDTO partialUpdate = new RoleUpdateDTO();
        partialUpdate.setId(testRole.getId());
        partialUpdate.setDescription("仅更新描述");

        // 执行
        RoleVO result = roleService.updateRole(partialUpdate);

        // 验证
        assertNotNull(result);
        assertEquals("仅更新描述", result.getDescription());
    }

    @Test
    @DisplayName("测试覆盖权限分配")
    void testAssignPermissions_Override() {
        // 先分配权限1,2,3
        roleService.assignPermissions(testRole.getId(), Arrays.asList(1L, 2L, 3L));

        // 重新分配权限4,5
        roleService.assignPermissions(testRole.getId(), Arrays.asList(4L, 5L));

        // 验证 - 应该只有4,5
        List<Long> result = roleService.getRolePermissions(testRole.getId());
        assertEquals(2, result.size());
        assertTrue(result.containsAll(Arrays.asList(4L, 5L)));
        assertFalse(result.contains(1L));
    }

    @Test
    @DisplayName("测试角色状态")
    void testRoleStatus() {
        // 创建禁用的角色
        createDTO.setRoleCode("DISABLED_ROLE");
        createDTO.setEnabled(false);
        RoleVO disabledRole = roleService.createRole(createDTO);

        // 验证
        assertEquals(false, disabledRole.getEnabled());
        assertFalse(disabledRole.getEnabled());
    }
}
