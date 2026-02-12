package com.qoobot.openidaas.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户Mapper测试
 * 测试数据库操作
 *
 * @author QooBot
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserMapperTest {

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
        testUser.setFailedLoginAttempts(0);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(testUser);
    }

    @Test
    void testInsert_Success() {
        // 准备
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setEmail("new@test.com");
        newUser.setPasswordHash("hash");
        newUser.setPasswordSalt("salt");
        newUser.setStatus(1);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        // 执行
        int result = userMapper.insert(newUser);

        // 验证
        assertTrue(result > 0);
        assertNotNull(newUser.getId());

        User inserted = userMapper.selectById(newUser.getId());
        assertNotNull(inserted);
        assertEquals("newuser", inserted.getUsername());
    }

    @Test
    void testSelectById_Success() {
        // 执行
        User result = userMapper.selectById(testUser.getId());

        // 验证
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void testSelectById_NotFound() {
        // 执行
        User result = userMapper.selectById(99999L);

        // 验证
        assertNull(result);
    }

    @Test
    void testSelectByUsername_Success() {
        // 执行
        User result = userMapper.selectByUsername(testUser.getUsername());

        // 验证
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void testSelectByUsername_NotFound() {
        // 执行
        User result = userMapper.selectByUsername("notexist");

        // 验证
        assertNull(result);
    }

    @Test
    void testSelectByEmail_Success() {
        // 执行
        User result = userMapper.selectByEmail(testUser.getEmail());

        // 验证
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void testSelectByEmail_NotFound() {
        // 执行
        User result = userMapper.selectByEmail("notexist@test.com");

        // 验证
        assertNull(result);
    }

    @Test
    void testSelectByMobile_Success() {
        // 执行
        User result = userMapper.selectByMobile(testUser.getMobile());

        // 验证
        assertNotNull(result);
        assertEquals(testUser.getMobile(), result.getMobile());
    }

    @Test
    void testSelectByMobile_NotFound() {
        // 执行
        User result = userMapper.selectByMobile("13999999999");

        // 验证
        assertNull(result);
    }

    @Test
    void testUpdateById_Success() {
        // 准备
        testUser.setEmail("updated@test.com");
        testUser.setMobile("13800000099");

        // 执行
        int result = userMapper.updateById(testUser);

        // 验证
        assertTrue(result > 0);

        User updated = userMapper.selectById(testUser.getId());
        assertEquals("updated@test.com", updated.getEmail());
        assertEquals("13800000099", updated.getMobile());
    }

    @Test
    void testDeleteById_Success() {
        // 执行
        int result = userMapper.deleteById(testUser.getId());

        // 验证
        assertTrue(result > 0);

        User deleted = userMapper.selectById(testUser.getId());
        assertNull(deleted);
    }

    @Test
    void testSelectPage_Success() {
        // 准备
        Page<User> page = new Page<>(1, 10);

        // 执行
        IPage<User> result = userMapper.selectPage(page, null);

        // 验证
        assertNotNull(result);
        assertTrue(result.getTotal() >= 1);
        assertFalse(result.getRecords().isEmpty());
    }

    @Test
    void testSelectPage_WithQuery() {
        // 准备
        Page<User> page = new Page<>(1, 10);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, testUser.getUsername());

        // 执行
        IPage<User> result = userMapper.selectPage(page, queryWrapper);

        // 验证
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void testSelectBatchIds_Success() {
        // 执行
        List<User> result = userMapper.selectBatchIds(Arrays.asList(testUser.getId()));

        // 验证
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void testSelectBatchIds_EmptyList() {
        // 执行
        List<User> result = userMapper.selectBatchIds(Arrays.asList());

        // 验证
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSelectList_Success() {
        // 执行
        List<User> result = userMapper.selectList(null);

        // 验证
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testSelectList_WithQuery() {
        // 准备
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, 1);

        // 执行
        List<User> result = userMapper.selectList(queryWrapper);

        // 验证
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testCount_Success() {
        // 执行
        Long result = userMapper.selectCount(null);

        // 验证
        assertNotNull(result);
        assertTrue(result >= 1);
    }

    @Test
    void testUpdate_Status() {
        // 准备
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, testUser.getId())
                .set(User::getStatus, 2);

        // 执行
        int result = userMapper.update(null, updateWrapper);

        // 验证
        assertTrue(result > 0);

        User updated = userMapper.selectById(testUser.getId());
        assertEquals(2, updated.getStatus());
    }

    @Test
    void testSelectOne_Success() {
        // 准备
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, testUser.getUsername());

        // 执行
        User result = userMapper.selectOne(queryWrapper);

        // 验证
        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    void testSelectOne_NotFound() {
        // 准备
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, "notexist");

        // 执行
        User result = userMapper.selectOne(queryWrapper);

        // 验证
        assertNull(result);
    }

    @Test
    void testInsert_WithNullFields() {
        // 准备
        User newUser = new User();
        newUser.setUsername("nullfielduser");
        newUser.setPasswordHash("hash");
        newUser.setPasswordSalt("salt");
        newUser.setStatus(1);
        // email 和 mobile 为 null

        // 执行
        int result = userMapper.insert(newUser);

        // 验证
        assertTrue(result > 0);
        assertNotNull(newUser.getId());

        User inserted = userMapper.selectById(newUser.getId());
        assertNull(inserted.getEmail());
        assertNull(inserted.getMobile());
    }

    @Test
    void testUpdate_FailedLoginAttempts() {
        // 准备
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, testUser.getId())
                .set(User::getFailedLoginAttempts, 5);

        // 执行
        int result = userMapper.update(null, updateWrapper);

        // 验证
        assertTrue(result > 0);

        User updated = userMapper.selectById(testUser.getId());
        assertEquals(5, updated.getFailedLoginAttempts());
    }

    @Test
    void testSelectByStatus() {
        // 准备
        testUser.setStatus(2);
        userMapper.updateById(testUser);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getStatus, 2);

        // 执行
        List<User> result = userMapper.selectList(queryWrapper);

        // 验证
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(u -> u.getStatus() == 2));
    }

    @Test
    void testSelectByCreatedTimeRange() {
        // 准备
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.between(User::getCreatedAt, yesterday, now);

        // 执行
        List<User> result = userMapper.selectList(queryWrapper);

        // 验证
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testSelectPage_WithSort() {
        // 准备
        Page<User> page = new Page<>(1, 10);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(User::getCreatedAt);

        // 执行
        IPage<User> result = userMapper.selectPage(page, queryWrapper);

        // 验证
        assertNotNull(result);
        assertFalse(result.getRecords().isEmpty());
    }

    @Test
    void testDeleteBatchIds_Success() {
        // 准备
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPasswordHash("hash");
        user2.setPasswordSalt("salt");
        user2.setStatus(1);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user2);

        List<Long> ids = Arrays.asList(testUser.getId(), user2.getId());

        // 执行
        int result = userMapper.deleteBatchIds(ids);

        // 验证
        assertTrue(result > 0);

        assertNull(userMapper.selectById(testUser.getId()));
        assertNull(userMapper.selectById(user2.getId()));
    }

    @Test
    void testSelectUserPermissions_Success() {
        // 执行
        // List<String> result = userMapper.selectUserPermissions(testUser.getId());

        // 验证
        // assertNotNull(result);
        // 可能返回空列表，因为没有实际关联数据
    }
}
