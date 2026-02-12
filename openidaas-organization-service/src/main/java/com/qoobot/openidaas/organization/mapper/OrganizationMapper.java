package com.qoobot.openidaas.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.organization.entity.Organization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 组织Mapper接口
 *
 * @author Qoobot
 * @version 1.0.0
 */
@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {

    /**
     * 根据父ID查询子组织
     *
     * @param parentId 父组织ID
     * @return 子组织列表
     */
    @Select("SELECT * FROM sys_organization WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort ASC, id ASC")
    List<Organization> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询组织树
     *
     * @param rootId 根组织ID
     * @return 组织树
     */
    List<Organization> selectOrganizationTree(@Param("rootId") Long rootId);

    /**
     * 查询用户所属组织
     *
     * @param userId 用户ID
     * @return 组织列表
     */
    @Select("SELECT o.* FROM sys_organization o " +
            "JOIN sys_user_organization uo ON o.id = uo.organization_id " +
            "WHERE uo.user_id = #{userId} AND o.deleted = 0 AND uo.deleted = 0")
    List<Organization> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询组织下的用户数量
     *
     * @param organizationId 组织ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM sys_user_organization WHERE organization_id = #{organizationId} AND deleted = 0")
    Integer selectUserCountByOrganizationId(@Param("organizationId") Long organizationId);

}