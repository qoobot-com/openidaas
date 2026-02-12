package com.qoobot.openidaas.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.user.entity.UserDepartment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 用户部门关系Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface UserDepartmentMapper extends BaseMapper<UserDepartment> {

    /**
     * 查询用户的主部门
     *
     * @param userId 用户ID
     * @return 主部门信息
     */
    UserDepartment selectPrimaryByUserId(Long userId);

    /**
     * 查询用户的所有部门
     *
     * @param userId 用户ID
     * @return 部门列表
     */
    List<UserDepartment> selectByUserId(Long userId);

    /**
     * 删除用户的所有部门关系
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(Long userId);
}
