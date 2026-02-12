package com.qoobot.openidaas.organization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.organization.entity.Position;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 职位Mapper接口
 *
 * @author QooBot
 */
@Mapper
public interface PositionMapper extends BaseMapper<Position> {

    /**
     * 根据部门ID查询职位列表
     *
     * @param deptId 部门ID
     * @return 职位列表
     */
    List<Position> selectByDeptId(@Param("deptId") Long deptId);

    /**
     * 根据职位编码查询职位
     *
     * @param positionCode 职位编码
     * @return 职位信息
     */
    Position selectByCode(@Param("positionCode") String positionCode);

    /**
     * 查询职位下的用户数量
     *
     * @param positionId 职位ID
     * @return 用户数量
     */
    @Select("SELECT COUNT(*) FROM user_profiles WHERE position_id = #{positionId}")
    int countUsers(@Param("positionId") Long positionId);
}
