package com.qoobot.openidaas.organization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qoobot.openidaas.organization.entity.Organization;

import java.util.List;

/**
 * 组织服务接口
 *
 * @author Qoobot
 * @version 1.0.0
 */
public interface OrganizationService extends IService<Organization> {

    /**
     * 创建组织
     *
     * @param organization 组织信息
     * @return 组织ID
     */
    Long createOrganization(Organization organization);

    /**
     * 更新组织
     *
     * @param organization 组织信息
     * @return 是否成功
     */
    boolean updateOrganization(Organization organization);

    /**
     * 批量删除组织
     *
     * @param ids 组织ID列表
     * @return 是否成功
     */
    boolean batchDeleteOrganizations(List<Long> ids);

    /**
     * 启用组织
     *
     * @param id 组织ID
     * @return 是否成功
     */
    boolean enableOrganization(Long id);

    /**
     * 禁用组织
     *
     * @param id 组织ID
     * @return 是否成功
     */
    boolean disableOrganization(Long id);

    /**
     * 获取所有组织（分页）
     *
     * @param page 分页参数
     * @param name 组织名称搜索条件
     * @param code 组织编码搜索条件
     * @param status 状态搜索条件
     * @return 组织分页结果
     */
    com.baomidou.mybatisplus.extension.plugins.pagination.Page<Organization> getOrganizations(
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Organization> page,
            String name, String code, String status);

    /**
     * 删除组织
     *
     * @param id 组织ID
     * @return 是否成功
     */
    boolean deleteOrganization(Long id);

    /**
     * 获取组织详情
     *
     * @param id 组织ID
     * @return 组织信息
     */
    Organization getOrganizationById(Long id);

    /**
     * 获取组织树
     *
     * @param parentId 父组织ID，null表示获取根组织
     * @return 组织树
     */
    List<Organization> getOrganizationTree(Long parentId);

    /**
     * 获取用户所属组织
     *
     * @param userId 用户ID
     * @return 组织列表
     */
    List<Organization> getUserOrganizations(Long userId);

    /**
     * 移动组织
     *
     * @param id 组织ID
     * @param newParentId 新父组织ID
     * @return 是否成功
     */
    boolean moveOrganization(Long id, Long newParentId);

    /**
     * 获取组织路径
     *
     * @param id 组织ID
     * @return 组织路径
     */
    String getOrganizationPath(Long id);

    /**
     * 验证组织编码唯一性
     *
     * @param code 组织编码
     * @param excludeId 排除的组织ID
     * @return 是否唯一
     */
    boolean isCodeUnique(String code, Long excludeId);

}