package com.qoobot.openidaas.security.service;

import com.qoobot.openidaas.security.config.SecurityProperties;
import com.qoobot.openidaas.security.constants.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 登录尝试服务
 * 
 * 记录登录失败次数，实现账户锁定功能
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SecurityProperties securityProperties;

    /**
     * 记录登录失败
     * 
     * @param username 用户名
     */
    public void loginFailed(String username) {
        String key = getLoginFailedKey(username);
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);

        if (attempts == null) {
            attempts = 0;
        }

        attempts++;
        redisTemplate.opsForValue().set(key, attempts, 1, TimeUnit.HOURS);

        log.warn("Login failed for user: {}, attempts: {}", username, attempts);

        // 如果达到最大失败次数，锁定账户
        if (attempts >= securityProperties.getPassword().getMaxFailedAttempts()) {
            lockAccount(username);
        }
    }

    /**
     * 记录登录成功
     * 
     * @param username 用户名
     */
    public void loginSucceeded(String username) {
        String key = getLoginFailedKey(username);
        redisTemplate.delete(key);

        log.debug("Login succeeded for user: {}, reset attempts", username);
    }

    /**
     * 检查账户是否被锁定
     * 
     * @param username 用户名
     * @return 是否被锁定
     */
    public boolean isLocked(String username) {
        String lockKey = getAccountLockKey(username);
        Boolean locked = redisTemplate.hasKey(lockKey);
        return Boolean.TRUE.equals(locked);
    }

    /**
     * 锁定账户
     * 
     * @param username 用户名
     */
    private void lockAccount(String username) {
        String lockKey = getAccountLockKey(username);
        long lockMinutes = securityProperties.getPassword().getLockMinutes();
        
        redisTemplate.opsForValue().set(
                lockKey, 
                "LOCKED", 
                lockMinutes, 
                TimeUnit.MINUTES
        );

        log.warn("Account locked for user: {} for {} minutes", username, lockMinutes);
    }

    /**
     * 解锁账户
     * 
     * @param username 用户名
     */
    public void unlockAccount(String username) {
        String lockKey = getAccountLockKey(username);
        redisTemplate.delete(lockKey);
        log.info("Account unlocked for user: {}", username);
    }

    /**
     * 获取登录失败次数
     * 
     * @param username 用户名
     * @return 失败次数
     */
    public int getFailedAttempts(String username) {
        String key = getLoginFailedKey(username);
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        return attempts != null ? attempts : 0;
    }

    /**
     * 重置登录失败次数
     * 
     * @param username 用户名
     */
    public void resetFailedAttempts(String username) {
        loginSucceeded(username);
    }

    /**
     * 获取登录失败Key
     */
    private String getLoginFailedKey(String username) {
        return SecurityConstants.LOGIN_FAILED_PREFIX + username;
    }

    /**
     * 获取账户锁定Key
     */
    private String getAccountLockKey(String username) {
        return SecurityConstants.ACCOUNT_LOCK_PREFIX + username;
    }
}
