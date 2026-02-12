package com.qoobot.openidaas.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qoobot.openidaas.organization.entity.Organization;
import com.qoobot.openidaas.organization.mapper.OrganizationMapper;
import com.qoobot.openidaas.organization.service.OrganizationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织服务实现类
 *
 * @author Qoobot
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl extends ServiceImpl<OrganizationMapper, Organization> implements OrganizationService {

    private final OrganizationMapper organizationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrganization(Organization organization) {
        log.info("创建组织: {}", organization.getName());
        
        // 验证编码唯一性
        if (!isCodeUnique(organization.getCode(), null)) {
            throw new RuntimeException("组织编码已存在");
        }
        
        // 设置默认值
        if (organization.getParentId() == null) {
            organization.setParentId(0L);
        }
        
        // 计算层级和路径
        calculateLevelAndPath(organization);
        
        // 设置默认排序
        if (organization.getSort() == null) {
            organization.setSort(0);
        }
        
        if (organization.getStatus() == null) {
            organization.setStatus("ENABLED");
        }
        
        save(organization);
        log.info("组织创建成功，ID: {}", organization.getId());
        return organization.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrganization(Organization organization) {
        log.info("更新组织: {}", organization.getId());
        
        Organization existing = getById(organization.getId());
        if (existing == null) {
            throw new RuntimeException("组织不存在");
        }
        
        // 验证编码唯一性
        if (StringUtils.hasText(organization.getCode()) && 
            !organization.getCode().equals(existing.getCode())) {
            if (!isCodeUnique(organization.getCode(), organization.getId())) {
                throw new RuntimeException("组织编码已存在");
            }
        }
        
        // 如果父组织发生变化，重新计算层级和路径
        if (organization.getParentId() != null && 
            !organization.getParentId().equals(existing.getParentId())) {
            calculateLevelAndPath(organization);
        }
        
        return updateById(organization);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteOrganization(Long id) {
        log.info("删除组织: {}", id);
        
        Organization organization = getById(id);
        if (organization == null) {
            throw new RuntimeException("组织不存在");
        }
        
        // 检查是否有子组织
        List<Organization> children = organizationMapper.selectByParentId(id);
        if (!CollectionUtils.isEmpty(children)) {
            throw new RuntimeException("该组织下存在子组织，无法删除");
        }
        
        // 检查是否有用户
        Integer userCount = organizationMapper.selectUserCountByOrganizationId(id);
        if (userCount > 0) {
            throw new RuntimeException("该组织下存在用户，无法删除");
        }
        
        return removeById(id);
    }

    @Override
    public Organization getOrganizationById(Long id) {
        return getById(id);
    }

    @Override
    public List<Organization> getOrganizationTree(Long parentId) {
        if (parentId == null) {
            parentId = 0L;
        }
        
        List<Organization> organizations = organizationMapper.selectByParentId(parentId);
        return buildTree(organizations);
    }

    @Override
    public List<Organization> getUserOrganizations(Long userId) {
        return organizationMapper.selectByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean moveOrganization(Long id, Long newParentId) {
        log.info("移动组织 {} 到父组织 {}", id, newParentId);
        
        Organization organization = getById(id);
        if (organization == null) {
            throw new RuntimeException("组织不存在");
        }
        
        // 检查目标组织是否存在
        if (newParentId != 0) {
            Organization parent = getById(newParentId);
            if (parent == null) {
                throw new RuntimeException("目标父组织不存在");
            }
            
            // 检查是否形成循环引用
            if (isDescendant(newParentId, id)) {
                throw new RuntimeException("不能将组织移动到其子组织下");
            }
        }
        
        organization.setParentId(newParentId);
        calculateLevelAndPath(organization);
        
        return updateById(organization);
    }

    @Override
    public String getOrganizationPath(Long id) {
        Organization organization = getById(id);
        if (organization == null) {
            return "";
        }
        return organization.getPath();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteOrganizations(List<Long> ids) {
        log.info("批量删除组织: {}", ids);
        
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        
        // 检查每个组织是否可以删除
        for (Long id : ids) {
            Organization organization = getById(id);
            if (organization == null) {
                throw new RuntimeException("组织不存在: " + id);
            }
            
            // 检查是否有子组织
            List<Organization> children = organizationMapper.selectByParentId(id);
            if (!CollectionUtils.isEmpty(children)) {
                throw new RuntimeException("组织 " + organization.getName() + " 下存在子组织，无法删除");
            }
            
            // 检查是否有用户
            Integer userCount = organizationMapper.selectUserCountByOrganizationId(id);
            if (userCount > 0) {
                throw new RuntimeException("组织 " + organization.getName() + " 下存在用户，无法删除");
            }
        }
        
        return removeBatchByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableOrganization(Long id) {
        log.info("启用组织: {}", id);
        
        Organization organization = getById(id);
        if (organization == null) {
            throw new RuntimeException("组织不存在");
        }
        
        organization.setStatus("ENABLED");
        return updateById(organization);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableOrganization(Long id) {
        log.info("禁用组织: {}", id);
        
        Organization organization = getById(id);
        if (organization == null) {
            throw new RuntimeException("组织不存在");
        }
        
        organization.setStatus("DISABLED");
        return updateById(organization);
    }

    @Override
    public Page<Organization> getOrganizations(Page<Organization> page, String name, String code, String status) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getDeleted, 0);
        
        if (StringUtils.hasText(name)) {
            wrapper.like(Organization::getName, name);
        }
        
        if (StringUtils.hasText(code)) {
            wrapper.like(Organization::getCode, code);
        }
        
        if (StringUtils.hasText(status)) {
            wrapper.eq(Organization::getStatus, status);
        }
        
        wrapper.orderByAsc(Organization::getLevel)
               .orderByAsc(Organization::getSort)
               .orderByDesc(Organization::getId);
        
        return page(page, wrapper);
    }

    @Override
    public boolean isCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getCode, code)
               .eq(Organization::getDeleted, 0);
        
        if (excludeId != null) {
            wrapper.ne(Organization::getId, excludeId);
        }
        
        return count(wrapper) == 0;
    }

    /**
     * 计算组织层级和路径
     */
    private void calculateLevelAndPath(Organization organization) {
        if (organization.getParentId() == 0) {
            organization.setLevel(1);
            organization.setPath(String.valueOf(organization.getId()));
        } else {
            Organization parent = getById(organization.getParentId());
            if (parent != null) {
                organization.setLevel(parent.getLevel() + 1);
                organization.setPath(parent.getPath() + "," + organization.getId());
            } else {
                organization.setLevel(1);
                organization.setPath(String.valueOf(organization.getId()));
            }
        }
    }

    /**
     * 构建组织树
     */
    private List<Organization> buildTree(List<Organization> organizations) {
        if (CollectionUtils.isEmpty(organizations)) {
            return new ArrayList<>();
        }
        
        // 获取所有子组织
        List<Long> ids = organizations.stream()
                .map(Organization::getId)
                .collect(Collectors.toList());
        
        List<Organization> allChildren = list(new LambdaQueryWrapper<Organization>()
                .in(Organization::getParentId, ids)
                .eq(Organization::getDeleted, 0));
        
        // 递归构建树
        for (Organization org : organizations) {
            List<Organization> children = allChildren.stream()
                    .filter(child -> child.getParentId().equals(org.getId()))
                    .collect(Collectors.toList());
            
            org.setLeaf(CollectionUtils.isEmpty(children));
            org.setChildrenCount(children.size());
            
            if (!CollectionUtils.isEmpty(children)) {
                org.setChildren(buildTree(children));
            }
        }
        
        return organizations;
    }

    /**
     * 判断childId是否是parentId的后代
     */
    private boolean isDescendant(Long parentId, Long childId) {
        if (parentId.equals(childId)) {
            return true;
        }
        
        Organization parent = getById(parentId);
        if (parent == null || parent.getParentId() == 0) {
            return false;
        }
        
        return isDescendant(parent.getParentId(), childId);
    }

}