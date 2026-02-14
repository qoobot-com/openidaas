package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.core.domain.UserDepartment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户部门关联Mapper接口
 *
 * @author QooBot
 */
public interface UserDepartmentMapper extends BaseMapper<UserDepartment> {

    /**
     * 根据用户ID查找部门ID列表
     */
    @Select("SELECT dept_id FROM user_departments WHERE user_id = #{userId}")
    List<Long> findDeptIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据部门ID查找用户ID列表
     */
    @Select("SELECT user_id FROM user_departments WHERE dept_id = #{deptId}")
    List<Long> findUserIdsByDeptId(@Param("deptId") Long deptId);

    /**
     * 根据用户ID删除用户部门关联
     */
    @Delete("DELETE FROM user_departments WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据部门ID删除用户部门关联
     */
    @Delete("DELETE FROM user_departments WHERE dept_id = #{deptId}")
    int deleteByDeptId(@Param("deptId") Long deptId);

    /**
     * 删除指定用户和部门的关联
     */
    @Delete("DELETE FROM user_departments WHERE user_id = #{userId} AND dept_id = #{deptId}")
    int deleteByUserIdAndDeptId(@Param("userId") Long userId, @Param("deptId") Long deptId);

    /**
     * 批量插入用户部门关联
     * 【需在XML中实现】
     */
    int batchInsert(@Param("list") List<UserDepartment> userDepartments);
}
