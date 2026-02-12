package com.qoobot.openidaas.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.application.entity.SamlServiceProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * SAML服务提供商数据访问接口
 */
@Mapper
public interface SamlServiceProviderMapper extends BaseMapper<SamlServiceProvider> {

    /**
     * 根据SP实体ID查询
     */
    @Select("SELECT * FROM saml_service_providers WHERE sp_entity_id = #{spEntityId} AND deleted = 0")
    SamlServiceProvider selectBySpEntityId(@Param("spEntityId") String spEntityId);

    /**
     * 根据应用ID查询
     */
    @Select("SELECT * FROM saml_service_providers WHERE app_id = #{appId} AND deleted = 0")
    SamlServiceProvider selectByAppId(@Param("appId") Long appId);
}
