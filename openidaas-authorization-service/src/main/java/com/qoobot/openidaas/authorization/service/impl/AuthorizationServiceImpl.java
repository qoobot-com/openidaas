package com.qoobot.openidaas.authorization.service.impl;

import com.qoobot.openidaas.authorization.service.AuthorizationService;
import com.qoobot.openidaas.common.feign.FeignHelper;
import com.qoobot.openidaas.common.feign.RoleClient;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.role.RoleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 授权服务实现类
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationServiceImpl implements AuthorizationService {

    private final RoleClient roleClient;
    private final StringRedisTemplate redisTemplate;

    private static final String USER_PERMISSIONS_CACHE_PREFIX = "auth:user:permissions:";
    private static final String USER_ROLES_CACHE_PREFIX = "auth:user:roles:";
    private static final long CACHE_EXPIRE_HOURS = 1;

    @Override
    public boolean hasPermission(Long userId, String permCode) {
        List<String> permissions = getUserPermissions(userId);
        return permissions.contains(permCode);
    }

    @Override
    public boolean hasAnyPermission(Long userId, List<String> permCodes) {
        List<String> permissions = getUserPermissions(userId);
        return permCodes.stream().anyMatch(permissions::contains);
    }

    @Override
    public boolean hasAllPermissions(Long userId, List<String> permCodes) {
        List<String> permissions = getUserPermissions(userId);
        return permCodes.stream().allMatch(permissions::contains);
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        List<String> roles = getUserRoles(userId);
        return roles.contains(roleCode);
    }

    @Override
    public boolean hasAnyRole(Long userId, List<String> roleCodes) {
        List<String> roles = getUserRoles(userId);
        return roleCodes.stream().anyMatch(roles::contains);
    }

    @Override
    public boolean hasAllRoles(Long userId, List<String> roleCodes) {
        List<String> roles = getUserRoles(userId);
        return roleCodes.stream().allMatch(roles::contains);
    }

    @Override
    public boolean hasResourceAccess(Long userId, String resourceType, String resourceId, String action) {
        // 构建资源权限编码：resourceType:resourceId:action
        String resourcePermCode = String.format("%s:%s:%s", resourceType, resourceId, action);
        return hasPermission(userId, resourcePermCode);
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        // 先从缓存获取
        String cacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            log.debug("从缓存获取用户权限，userId: {}", userId);
            return parsePermissions(cached);
        }

        // 通过Feign客户端获取用户角色，然后获取角色权限
        try {
            List<RoleVO> userRoles = FeignHelper.call(() -> roleClient.getUserRoles(userId));
            List<String> permCodes = new ArrayList<>();
            
            for (RoleVO role : userRoles) {
                List<Long> rolePerms = FeignHelper.call(() -> roleClient.getRolePermissions(role.getId()));
                // 这里需要将权限ID转换为权限编码，暂时返回空列表
                // 实际应该通过PermissionClient获取权限详情
                permCodes.addAll(rolePerms.stream().map(String::valueOf).collect(Collectors.toList()));
            }

            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, String.join(",", permCodes), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            return permCodes;
        } catch (Exception e) {
            log.error("获取用户权限失败，userId: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        // 先从缓存获取
        String cacheKey = USER_ROLES_CACHE_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            log.debug("从缓存获取用户角色，userId: {}", userId);
            return List.of(cached.split(","));
        }

        // 通过Feign客户端获取用户角色
        try {
            List<RoleVO> roles = FeignHelper.call(() -> roleClient.getUserRoles(userId));
            List<String> roleCodes = roles.stream()
                    .map(RoleVO::getRoleCode)
                    .collect(Collectors.toList());

            // 存入缓存
            redisTemplate.opsForValue().set(cacheKey, String.join(",", roleCodes), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            return roleCodes;
        } catch (Exception e) {
            log.error("获取用户角色失败，userId: {}", userId, e);
            return List.of();
        }
    }

    @Override
    public void clearUserPermissionCache(Long userId) {
        String permissionsCacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
        String rolesCacheKey = USER_ROLES_CACHE_PREFIX + userId;
        redisTemplate.delete(permissionsCacheKey);
        redisTemplate.delete(rolesCacheKey);
        log.debug("清除用户权限缓存，userId: {}", userId);
    }

    /**
     * 解析权限字符串
     */
    private List<String> parsePermissions(String cached) {
        if (cached == null || cached.isEmpty()) {
            return List.of();
        }
        return List.of(cached.split(","));
    }
}
