package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.common.enumeration.UserStatusEnum;
import com.qoobot.openidaas.core.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户仓储测试
 *
 * @author QooBot
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByUsername_UserExists_ReturnsUser() {
        // 准备测试数据
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setStatus(UserStatusEnum.NORMAL);
        user.setTenantId(1L);
        user.setCreatedAt(LocalDateTime.now());
        
        entityManager.persistAndFlush(user);

        // 执行测试
        Optional<User> result = userRepository.findByUsername("testuser");

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testFindByUsername_UserNotExists_ReturnsEmpty() {
        // 执行测试
        Optional<User> result = userRepository.findByUsername("nonexistent");

        // 验证结果
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByEmail_UserExists_ReturnsUser() {
        // 准备测试数据
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setStatus(UserStatusEnum.NORMAL);
        user.setTenantId(1L);
        user.setCreatedAt(LocalDateTime.now());
        
        entityManager.persistAndFlush(user);

        // 执行测试
        Optional<User> result = userRepository.findByEmail("test@example.com");

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testFindByMobile_UserExists_ReturnsUser() {
        // 准备测试数据
        User user = new User();
        user.setUsername("testuser");
        user.setMobile("13800138000");
        user.setPassword("password123");
        user.setStatus(UserStatusEnum.NORMAL);
        user.setTenantId(1L);
        user.setCreatedAt(LocalDateTime.now());
        
        entityManager.persistAndFlush(user);

        // 执行测试
        Optional<User> result = userRepository.findByMobile("13800138000");

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals("13800138000", result.get().getMobile());
    }

    @Test
    void testFindByUsernameOrEmail_MatchesUsername_ReturnsUser() {
        // 准备测试数据
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setStatus(UserStatusEnum.NORMAL);
        user.setTenantId(1L);
        user.setCreatedAt(LocalDateTime.now());
        
        entityManager.persistAndFlush(user);

        // 执行测试
        Optional<User> result = userRepository.findByUsernameOrEmail("testuser", "other@example.com");

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testFindByUsernameOrEmail_MatchesEmail_ReturnsUser() {
        // 准备测试数据
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setStatus(UserStatusEnum.NORMAL);
        user.setTenantId(1L);
        user.setCreatedAt(LocalDateTime.now());
        
        entityManager.persistAndFlush(user);

        // 执行测试
        Optional<User> result = userRepository.findByUsernameOrEmail("otheruser", "test@example.com");

        // 验证结果
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testFindByTenantId_ReturnsPage() {
        // 准备测试数据
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password123");
        user1.setStatus(UserStatusEnum.NORMAL);
        user1.setTenantId(1L);
        user1.setCreatedAt(LocalDateTime.now());
        
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password123");
        user2.setStatus(UserStatusEnum.NORMAL);
        user2.setTenantId(1L);
        user2.setCreatedAt(LocalDateTime.now());
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // 执行测试
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> result = userRepository.findByTenantId(1L, pageable);

        // 验证结果
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(user -> user.getTenantId().equals(1L)));
    }

    @Test
    void testCountByTenantId_ReturnsCorrectCount() {
        // 准备测试数据
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password123");
        user1.setStatus(UserStatusEnum.NORMAL);
        user1.setTenantId(1L);
        user1.setCreatedAt(LocalDateTime.now());
        
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password123");
        user2.setStatus(UserStatusEnum.NORMAL);
        user2.setTenantId(1L);
        user2.setCreatedAt(LocalDateTime.now());
        
        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("password123");
        user3.setStatus(UserStatusEnum.NORMAL);
        user3.setTenantId(2L);
        user3.setCreatedAt(LocalDateTime.now());
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);

        // 执行测试
        long count = userRepository.countByTenantId(1L);

        // 验证结果
        assertEquals(2, count);
    }

    @Test
    void testCountByTenantIdAndEnabledTrue_ReturnsCorrectCount() {
        // 准备测试数据
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password123");
        user1.setStatus(UserStatusEnum.NORMAL);
        user1.setTenantId(1L);
        user1.setEnabled(true);
        user1.setCreatedAt(LocalDateTime.now());
        
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password123");
        user2.setStatus(UserStatusEnum.NORMAL);
        user2.setTenantId(1L);
        user2.setEnabled(false); // 禁用用户
        user2.setCreatedAt(LocalDateTime.now());
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // 执行测试
        long count = userRepository.countByTenantIdAndEnabledTrue(1L);

        // 验证结果
        assertEquals(1, count);
    }

    @Test
    void testExistsByIdAndTenantId_Exists_ReturnsTrue() {
        // 准备测试数据
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setStatus(UserStatusEnum.NORMAL);
        user.setTenantId(1L);
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = entityManager.persistAndFlush(user);

        // 执行测试
        boolean result = userRepository.existsByIdAndTenantId(savedUser.getId(), 1L);

        // 验证结果
        assertTrue(result);
    }

    @Test
    void testExistsByIdAndTenantId_NotExists_ReturnsFalse() {
        // 执行测试
        boolean result = userRepository.existsByIdAndTenantId(999L, 1L);

        // 验证结果
        assertFalse(result);
    }

    @Test
    void testFindByIdAndTenantId_Exists_ReturnsUser() {
        // 准备测试数据
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setStatus(UserStatusEnum.NORMAL);
        user.setTenantId(1L);
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = entityManager.persistAndFlush(user);

        // 执行测试
        User result = userRepository.findByIdAndTenantId(savedUser.getId(), 1L);

        // 验证结果
        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testFindByIdAndTenantId_WrongTenant_ReturnsNull() {
        // 准备测试数据
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setStatus(UserStatusEnum.NORMAL);
        user.setTenantId(1L);
        user.setCreatedAt(LocalDateTime.now());
        
        User savedUser = entityManager.persistAndFlush(user);

        // 执行测试
        User result = userRepository.findByIdAndTenantId(savedUser.getId(), 2L);

        // 验证结果
        assertNull(result);
    }
}