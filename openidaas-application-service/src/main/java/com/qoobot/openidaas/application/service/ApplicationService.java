package com.qoobot.openidaas.application.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openidaas.common.dto.application.*;
import com.qoobot.openidaas.common.vo.application.ApplicationVO;
import com.qoobot.openidaas.common.vo.application.OAuth2ClientVO;
import com.qoobot.openidaas.common.vo.application.SamlSpVO;

/**
 * 应用服务接口
 */
public interface ApplicationService {

    /**
     * 创建应用
     */
    Long createApplication(ApplicationCreateDTO dto);

    /**
     * 更新应用
     */
    void updateApplication(ApplicationUpdateDTO dto);

    /**
     * 删除应用
     */
    void deleteApplication(Long appId);

    /**
     * 获取应用详情
     */
    ApplicationVO getApplicationById(Long appId);

    /**
     * 根据应用密钥获取应用
     */
    ApplicationVO getApplicationByAppKey(String appKey);

    /**
     * 分页查询应用
     */
    IPage<ApplicationVO> queryApplications(ApplicationQueryDTO dto);

    /**
     * 创建OAuth2客户端
     */
    Long createOAuth2Client(OAuth2ClientCreateDTO dto);

    /**
     * 更新OAuth2客户端
     */
    void updateOAuth2Client(OAuth2ClientUpdateDTO dto);

    /**
     * 删除OAuth2客户端
     */
    void deleteOAuth2Client(Long clientId);

    /**
     * 获取OAuth2客户端详情
     */
    OAuth2ClientVO getOAuth2ClientById(Long clientId);

    /**
     * 根据客户端ID获取OAuth2客户端
     */
    OAuth2ClientVO getOAuth2ClientByClientId(String clientId);

    /**
     * 创建SAML服务提供商
     */
    Long createSamlSp(SamlSpCreateDTO dto);

    /**
     * 更新SAML服务提供商
     */
    void updateSamlSp(SamlSpUpdateDTO dto);

    /**
     * 删除SAML服务提供商
     */
    void deleteSamlSp(Long spId);

    /**
     * 获取SAML服务提供商详情
     */
    SamlSpVO getSamlSpById(Long spId);

    /**
     * 根据应用ID获取OAuth2客户端配置
     */
    OAuth2ClientVO getOAuth2ClientByAppId(Long appId);

    /**
     * 根据应用ID获取SAML服务提供商配置
     */
    SamlSpVO getSamlSpByAppId(Long appId);
}
