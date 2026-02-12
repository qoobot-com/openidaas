package com.qoobot.openidaas.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qoobot.openidaas.common.dto.permission.PermissionCreateDTO;
import com.qoobot.openidaas.common.dto.permission.PermissionUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.permission.PermissionVO;
import com.qoobot.openidaas.role.entity.Permission;
import com.qoobot.openidaas.role.mapper.PermissionMapper;
import com.qoobot.openidaas.role.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String USER_PERMISSIONS_CACHE_PREFIX = "user:permissions:";
    private static final long CACHE_EXPIRE_HOURS = 1;

    @Override
    public List<PermissionVO> getPermissionList(String permType) {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        if (permType != null && !permType.isEmpty()) {
            wrapper.eq(Permission::getPermType, permType);
        }
        wrapper.orderByAsc(Permission::getSortOrder, Permission::getId);
        List<Permission> permissions = permissionMapper.selectList(wrapper);
        return permissions.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> getPermissionTree(Long parentId) {
        Long actualParentId = parentId == null ? 0L : parentId;
        List<Permission> permissions = permissionMapper.selectByParentId(actualParentId);
        return buildPermissionTree(permissions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionVO createPermission(PermissionCreateDTO createDTO) {
        log.info("创建权限，权限编码：{}，权限名称：{}", createDTO.getPermCode(), createDTO.getPermName());

        // 检查权限编码是否已存在
        Permission existPermission = permissionMapper.selectByCode(createDTO.getPermCode());
        if (existPermission != null) {
            throw new BusinessException("权限编码已存在：" + createDTO.getPermCode());
        }

        // 验证父权限是否存在
        Long parentId = createDTO.getParentId() == null ? 0L : createDTO.getParentId();
        if (parentId != 0L) {
            Permission parentPermission = permissionMapper.selectById(parentId);
            if (parentPermission == null) {
                throw new BusinessException("父权限不存在");
            }
        }

        // 构建权限实体
        Permission permission = new Permission();
        BeanUtils.copyProperties(createDTO, permission);
        permission.setParentId(parentId);

        permissionMapper.insert(permission);

        // 清除相关缓存
        clearPermissionsCache();

        log.info("权限创建成功，权限ID：{}", permission.getId());
        return convertToVO(permission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PermissionVO updatePermission(PermissionUpdateDTO updateDTO) {
        log.info("更新权限，权限ID：{}", updateDTO.getId());

        // 检查权限是否存在
        Permission existPermission = permissionMapper.selectById(updateDTO.getId());
        if (existPermission == null) {
            throw new BusinessException("权限不存在");
        }

        // 检查权限编码是否重复（排除自己）
        Permission codePermission = permissionMapper.selectByCode(updateDTO.getPermCode());
        if (codePermission != null && !codePermission.getId().equals(updateDTO.getId())) {
            throw new BusinessException("权限编码已存在：" + updateDTO.getPermCode());
        }

        // 更新权限信息
        Permission permission = new Permission();
        BeanUtils.copyProperties(updateDTO, permission);
        permissionMapper.updateById(permission);

        // 清除相关缓存
        clearPermissionsCache();

        log.info("权限更新成功，权限ID：{}", updateDTO.getId());
        return convertToVO(permissionMapper.selectById(updateDTO.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        log.info("删除权限，权限ID：{}", id);

        // 检查权限是否存在
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }

        // 检查是否为内置权限
        if (permission.isBuiltin()) {
            throw new BusinessException("内置权限不能删除");
        }

        // 检查是否有子权限
        int childrenCount = permissionMapper.countChildren(id);
        if (childrenCount > 0) {
            throw new BusinessException("权限下存在子权限，无法删除");
        }

        // 删除权限
        permissionMapper.deleteById(id);

        // 清除相关缓存
        clearPermissionsCache();

        log.info("权限删除成功，权限ID：{}", id);
    }

    @Override
    public PermissionVO getPermissionById(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BusinessException("权限不存在");
        }
        return convertToVO(permission);
    }

    @Override
    public List<PermissionVO> getUserPermissions(Long userId) {
        // 先从缓存获取
        String cacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            log.debug("从缓存获取用户权限，userId: {}", userId);
            // 这里简化处理，实际应序列化/反序列化
        }

        // 从数据库查询
        List<Permission> permissions = permissionMapper.selectByUserId(userId);
        List<PermissionVO> result = permissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 存入缓存
        redisTemplate.opsForValue().set(cacheKey, String.valueOf(result.size()), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        return result;
    }

    @Override
    public boolean hasPermission(Long userId, String permCode) {
        // 先从缓存获取
        List<PermissionVO> permissions = getUserPermissions(userId);
        return permissions.stream()
                .anyMatch(p -> permCode.equals(p.getPermCode()));
    }

    @Override
    public List<PermissionVO> getUserMenuTree(Long userId) {
        // 获取用户的所有菜单权限
        List<PermissionVO> allPermissions = getUserPermissions(userId);

        // 过滤出菜单类型
        List<PermissionVO> menuPermissions = allPermissions.stream()
                .filter(p -> "menu".equals(p.getPermType()))
                .filter(PermissionVO::getEnabled)
                .collect(Collectors.toList());

        // 构建菜单树
        return buildMenuTree(menuPermissions, 0L);
    }

    /**
     * 构建权限树
     */
    private List<PermissionVO> buildPermissionTree(List<Permission> permissions) {
        List<PermissionVO> voList = permissions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 为每个权限递归加载子权限
        for (PermissionVO vo : voList) {
            List<Permission> children = permissionMapper.selectByParentId(vo.getId());
            if (!children.isEmpty()) {
                vo.setChildren(buildPermissionTree(children));
            }
        }

        return voList;
    }

    /**
     * 构建菜单树
     */
    private List<PermissionVO> buildMenuTree(List<PermissionVO> permissions, Long parentId) {
        return permissions.stream()
                .filter(p -> parentId.equals(p.getParentId()))
                .peek(p -> p.setChildren(buildMenuTree(permissions, p.getId())))
                .collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private PermissionVO convertToVO(Permission permission) {
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(permission, vo);
        vo.setEnabled(permission.isEnabled());
        vo.setExternalLink(permission.isExternalLink());
        vo.setHidden(permission.isHidden());
        vo.setPermTypeDesc(permission.getPermissionType().getDescription());
        return vo;
    }

    /**
     * 清除权限缓存
     */
    private void clearPermissionsCache() {
        // 清除所有用户权限缓存
        // 实际应使用Redis通配符删除
        log.debug("清除权限缓存");
    }
}
