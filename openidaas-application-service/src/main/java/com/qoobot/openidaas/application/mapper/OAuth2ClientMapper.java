package com.qoobot.openidaas.application.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qoobot.openidaas.application.entity.OAuth2Client;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * OAuth2客户端数据访问接口
 */
@Mapper
public interface OAuth2ClientMapper extends BaseMapper<OAuth2Client> {

    /**
     * 根据客户端ID查询
     */
    @Select("SELECT * FROM oauth2_clients WHERE client_id = #{clientId} AND deleted = 0")
    OAuth2Client selectByClientId(@Param("clientId") String clientId);

    /**
     * 根据应用ID查询
     */
    @Select("SELECT * FROM oauth2_clients WHERE app_id = #{appId} AND deleted = 0")
    OAuth2Client selectByAppId(@Param("appId") Long appId);
}
