package com.qoobot.openidaas.role.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qoobot.openidaas.role.entity.Permission;
import com.qoobot.openidaas.role.entity.Role;
import com.qoobot.openidaas.role.entity.RolePermission;
import com.qoobot.openidaas.role.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RoleMapper 测试
 *
 * @author QooBot
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RoleMapper 测试")
class RoleMapperTest {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setRoleCode("TEST_MAPPER_ROLE");
        testRole.setRoleName("测试角色Mapper");
        testRole.setRoleType(2);
        testRole.setParentId(0L);
        testRole.setDescription("测试用");
        testRole.setIsBuiltin(0);
        testRole.setEnabled(1);
        testRole.setSortOrder(10);
        roleMapper.insert(testRole);
    }

    @Test
    @DisplayName("测试根据ID查询角色")
    void testSelectById() {
        // 执行
        Role result = roleMapper.selectById(testRole.getId());

        // 验证
        assertNotNull(result);
        assertEquals(testRole.getId(), result.getId());
        assertEquals("TEST_MAPPER_ROLE", result.getRoleCode());
    }

    @Test
    @DisplayName("测试根据角色编码查询")
    void testSelectByCode() {
        // 执行
        Role result = roleMapper.selectByCode("TEST_MAPPER_ROLE");

        // 验证
        assertNotNull(result);
        assertEquals(testRole.getId(), result.getId());
        assertEquals("TEST_MAPPER_ROLE", result.getRoleCode());
    }

    @Test
    @DisplayName("测试查询不存在的角色编码")
    void testSelectByCode_NotFound() {
        // 执行
        Role result = roleMapper.selectByCode("NOT_EXIST");

        // 验证
        assertNull(result);
    }

    @Test
    @DisplayName("测试统计子角色数量")
    void testCountChildren() {
        // 创建子角色
        Role childRole = new Role();
        childRole.setRoleCode("CHILD_ROLE");
        childRole.setRoleName("子角色");
        childRole.setRoleType(2);
        childRole.setParentId(testRole.getId());
        roleMapper.insert(childRole);

        // 执行
        int count = roleMapper.countChildren(testRole.getId());

        // 验证
        assertEquals(1, count);
    }

    @Test
    @DisplayName("测试统计用户数量")
    void testCountUsers() {
        // 创建用户角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(10L);
        userRole.setRoleId(testRole.getId());
        userRole.setGrantTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);

        // 执行
        int count = roleMapper.countUsers(testRole.getId());

        // 验证
        assertEquals(1, count);
    }

    @Test
    @DisplayName("测试根据用户ID查询角色")
    void testSelectByUserId() {
        // 创建用户角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(10L);
        userRole.setRoleId(testRole.getId());
        userRole.setGrantTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);

        // 执行
        List<Role> roles = roleMapper.selectByUserId(10L);

        // 验证
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(testRole.getId(), roles.get(0).getId());
    }

    @Test
    @DisplayName("测试根据用户ID查询角色 - 无结果")
    void testSelectByUserId_NoResult() {
        // 执行
        List<Role> roles = roleMapper.selectByUserId(99999L);

        // 验证
        assertNotNull(roles);
        assertTrue(roles.isEmpty());
    }

    @Test
    @DisplayName("测试插入角色")
    void testInsert() {
        // 准备
        Role newRole = new Role();
        newRole.setRoleCode("NEW_INSERT_ROLE");
        newRole.setRoleName("新角色");
        newRole.setRoleType(2);
        newRole.setParentId(0L);

        // 执行
        int result = roleMapper.insert(newRole);

        // 验证
        assertEquals(1, result);
        assertNotNull(newRole.getId());
    }

    @Test
    @DisplayName("测试更新角色")
    void testUpdateById() {
        // 准备
        testRole.setRoleName("更新后的角色");

        // 执行
        int result = roleMapper.updateById(testRole);

        // 验证
        assertEquals(1, result);
        Role updated = roleMapper.selectById(testRole.getId());
        assertEquals("更新后的角色", updated.getRoleName());
    }

    @Test
    @DisplayName("测试删除角色")
    void testDeleteById() {
        // 执行
        int result = roleMapper.deleteById(testRole.getId());

        // 验证
        assertEquals(1, result);
        Role deleted = roleMapper.selectById(testRole.getId());
        assertNull(deleted);
    }

    @Test
    @DisplayName("测试条件查询")
    void testSelectList() {
        // 执行
        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleType, 2)
                .orderByAsc(Role::getSortOrder));

        // 验证
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertTrue(roles.stream().allMatch(r -> r.getRoleType() == 2));
    }

    @Test
    @DisplayName("测试查询所有角色")
    void testSelectAll() {
        // 执行
        List<Role> roles = roleMapper.selectList(null);

        // 验证
        assertNotNull(roles);
        assertTrue(roles.size() >= 5); // 初始化数据有5个
    }

    @Test
    @DisplayName("测试分页查询")
    void testSelectPage() {
        // 执行
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Role> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, 2);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Role> result =
                roleMapper.selectPage(page, null);

        // 验证
        assertNotNull(result);
        assertNotNull(result.getRecords());
        assertTrue(result.getRecords().size() <= 2);
    }

    @Test
    @DisplayName("测试根据状态查询")
    void testSelectByStatus() {
        // 执行
        List<Role> enabledRoles = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getEnabled, 1));

        // 验证
        assertNotNull(enabledRoles);
        assertFalse(enabledRoles.isEmpty());
        assertTrue(enabledRoles.stream().allMatch(r -> r.getEnabled() == 1));
    }

    @Test
    @DisplayName("测试查询父角色")
    void testSelectByParentId() {
        // 创建子角色
        Role childRole = new Role();
        childRole.setRoleCode("CHILD_TEST");
        childRole.setRoleName("子测试");
        childRole.setParentId(testRole.getId());
        roleMapper.insert(childRole);

        // 执行
        List<Role> children = roleMapper.selectList(new LambdaQueryWrapper<Role>()
                .eq(Role::getParentId, testRole.getId()));

        // 验证
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals("CHILD_TEST", children.get(0).getRoleCode());
    }
}

/**
 * RolePermissionMapper 测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("RolePermissionMapper 测试")
class RolePermissionMapperTest {

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private RoleMapper roleMapper;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setRoleCode("TEST_RP_ROLE");
        testRole.setRoleName("测试RP角色");
        testRole.setRoleType(2);
        roleMapper.insert(testRole);
    }

    @Test
    @DisplayName("测试删除角色所有权限")
    void testDeleteByRoleId() {
        // 先添加权限
        RolePermission rp1 = new RolePermission();
        rp1.setRoleId(testRole.getId());
        rp1.setPermId(1L);
        rp1.setCreatedAt(LocalDateTime.now());
        rolePermissionMapper.insert(rp1);

        RolePermission rp2 = new RolePermission();
        rp2.setRoleId(testRole.getId());
        rp2.setPermId(2L);
        rp2.setCreatedAt(LocalDateTime.now());
        rolePermissionMapper.insert(rp2);

        // 执行
        rolePermissionMapper.deleteByRoleId(testRole.getId());

        // 验证
        List<RolePermission> remaining = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, testRole.getId()));
        assertTrue(remaining.isEmpty());
    }

    @Test
    @DisplayName("测试删除角色权限关联")
    void testDeleteRolePermission() {
        // 先添加权限
        RolePermission rp = new RolePermission();
        rp.setRoleId(testRole.getId());
        rp.setPermId(1L);
        rp.setCreatedAt(LocalDateTime.now());
        rolePermissionMapper.insert(rp);

        // 执行
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, testRole.getId())
                .eq(RolePermission::getPermId, 1L));

        // 验证
        RolePermission result = rolePermissionMapper.selectOne(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, testRole.getId())
                        .eq(RolePermission::getPermId, 1L));
        assertNull(result);
    }

    @Test
    @DisplayName("测试查询角色权限")
    void testSelectRolePermissions() {
        // 先添加权限
        RolePermission rp = new RolePermission();
        rp.setRoleId(testRole.getId());
        rp.setPermId(1L);
        rp.setCreatedAt(LocalDateTime.now());
        rolePermissionMapper.insert(rp);

        // 执行
        List<RolePermission> result = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, testRole.getId()));

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getPermId());
    }
}

/**
 * UserRoleMapper 测试
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("UserRoleMapper 测试")
class UserRoleMapperTest {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    private Role testRole;

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setRoleCode("TEST_UR_ROLE");
        testRole.setRoleName("测试UR角色");
        testRole.setRoleType(2);
        roleMapper.insert(testRole);
    }

    @Test
    @DisplayName("测试根据用户ID查询角色")
    void testSelectByUserId() {
        // 先添加用户角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(10L);
        userRole.setRoleId(testRole.getId());
        userRole.setScopeType(1);
        userRole.setScopeId(1L);
        userRole.setGrantTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);

        // 执行
        List<UserRole> result = userRoleMapper.selectByUserId(10L);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testRole.getId(), result.get(0).getRoleId());
    }

    @Test
    @DisplayName("测试根据用户ID查询角色 - 无结果")
    void testSelectByUserId_NoResult() {
        // 执行
        List<UserRole> result = userRoleMapper.selectByUserId(99999L);

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试删除用户角色关联")
    void testDeleteByUserIdAndRoleId() {
        // 先添加用户角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(10L);
        userRole.setRoleId(testRole.getId());
        userRole.setGrantTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);

        // 执行
        userRoleMapper.deleteByUserIdAndRoleId(10L, testRole.getId());

        // 验证
        List<UserRole> result = userRoleMapper.selectByUserId(10L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("测试插入用户角色关联")
    void testInsert() {
        // 准备
        UserRole userRole = new UserRole();
        userRole.setUserId(10L);
        userRole.setRoleId(testRole.getId());
        userRole.setScopeType(1);
        userRole.setScopeId(1L);
        userRole.setGrantTime(LocalDateTime.now());

        // 执行
        int result = userRoleMapper.insert(userRole);

        // 验证
        assertEquals(1, result);
        assertNotNull(userRole.getId());
    }
}
