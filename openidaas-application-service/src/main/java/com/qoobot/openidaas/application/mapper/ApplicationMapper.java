package com.qoobot.openidaas.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.application.entity.Application;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 应用数据访问接口
 */
@Mapper
public interface ApplicationMapper extends BaseMapper<Application> {

    /**
     * 根据应用密钥查询
     */
    @Select("SELECT * FROM applications WHERE app_key = #{appKey} AND deleted = 0")
    Application selectByAppKey(@Param("appKey") String appKey);

    /**
     * 根据所有者ID分页查询
     */
    IPage<Application> selectByOwnerId(@Param("ownerId") Long ownerId, Page<Application> page);
}
