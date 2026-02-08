package com.qoobot.openidaas.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final String USER_PERMISSIONS_PREFIX = "user:permissions:";
    private static final String DEPARTMENT_CACHE_PREFIX = "dept:";
    private static final long CACHE_EXPIRE_HOURS = 1;

    /**
     * 缓存用户信息
     */
    public void cacheUser(Long userId, Object user) {
        String key = USER_CACHE_PREFIX + userId;
        redisTemplate.opsForValue().set(key, user, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        log.debug("Cached user: {}", userId);
    }

    /**
     * 获取缓存的用户信息
     */
    public Object getCachedUser(Long userId) {
        String key = USER_CACHE_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 清除用户缓存
     */
    public void evictUserCache(Long userId) {
        String key = USER_CACHE_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Evicted user cache: {}", userId);
    }

    /**
     * 缓存用户权限
     */
    public void cacheUserPermissions(Long userId, Object permissions) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        redisTemplate.opsForValue().set(key, permissions, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        log.debug("Cached user permissions: {}", userId);
    }

    /**
     * 获取缓存的用户权限
     */
    public Object getCachedUserPermissions(Long userId) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 清除用户权限缓存
     */
    public void evictUserPermissionsCache(Long userId) {
        String key = USER_PERMISSIONS_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Evicted user permissions cache: {}", userId);
    }

    /**
     * 缓存部门信息
     */
    public void cacheDepartment(Long deptId, Object department) {
        String key = DEPARTMENT_CACHE_PREFIX + deptId;
        redisTemplate.opsForValue().set(key, department, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        log.debug("Cached department: {}", deptId);
    }

    /**
     * 获取缓存的部门信息
     */
    public Object getCachedDepartment(Long deptId) {
        String key = DEPARTMENT_CACHE_PREFIX + deptId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 清除部门缓存
     */
    public void evictDepartmentCache(Long deptId) {
        String key = DEPARTMENT_CACHE_PREFIX + deptId;
        redisTemplate.delete(key);
        log.debug("Evicted department cache: {}", deptId);
    }

    /**
     * 批量清除缓存
     */
    public void evictAllCache() {
        redisTemplate.delete(redisTemplate.keys(USER_CACHE_PREFIX + "*"));
        redisTemplate.delete(redisTemplate.keys(USER_PERMISSIONS_PREFIX + "*"));
        redisTemplate.delete(redisTemplate.keys(DEPARTMENT_CACHE_PREFIX + "*"));
        log.info("Evicted all cache");
    }
}
