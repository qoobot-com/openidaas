package com.qoobot.openidaas.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qoobot.openidaas.application.converter.ApplicationConverter;
import com.qoobot.openidaas.application.entity.Application;
import com.qoobot.openidaas.application.entity.OAuth2Client;
import com.qoobot.openidaas.application.entity.SamlServiceProvider;
import com.qoobot.openidaas.application.mapper.ApplicationMapper;
import com.qoobot.openidaas.application.mapper.OAuth2ClientMapper;
import com.qoobot.openidaas.application.mapper.SamlServiceProviderMapper;
import com.qoobot.openidaas.application.service.ApplicationService;
import com.qoobot.openidaas.common.dto.application.*;
import com.qoobot.openidaas.common.enumeration.ApplicationTypeEnum;
import com.qoobot.openidaas.common.enumeration.StatusEnum;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.util.StringUtil;
import com.qoobot.openidaas.common.vo.application.ApplicationVO;
import com.qoobot.openidaas.common.vo.application.OAuth2ClientVO;
import com.qoobot.openidaas.common.vo.application.SamlSpVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 应用服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl extends ServiceImpl<ApplicationMapper, Application>
        implements ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final OAuth2ClientMapper oauth2ClientMapper;
    private final SamlServiceProviderMapper samlSpMapper;
    private final ApplicationConverter applicationConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createApplication(ApplicationCreateDTO dto) {
        log.info("创建应用，应用名称：{}", dto.getAppName());

        // 生成应用密钥
        String appKey = "APP_" + StringUtil.generateRandomString(32);

        Application entity = applicationConverter.toEntity(dto);
        entity.setAppKey(appKey);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        applicationMapper.insert(entity);

        log.info("应用创建成功，应用ID：{}，应用密钥：{}", entity.getId(), appKey);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApplication(ApplicationUpdateDTO dto) {
        log.info("更新应用，应用ID：{}", dto.getId());

        Application entity = applicationMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("应用不存在");
        }

        if (StringUtils.hasText(dto.getAppName())) {
            entity.setAppName(dto.getAppName());
        }
        if (dto.getAppType() != null) {
            entity.setAppType(dto.getAppType());
        }
        if (dto.getRedirectUris() != null) {
            entity.setRedirectUris(applicationConverter.jsonToList(dto.getRedirectUris().toString()).toString());
        }
        if (StringUtils.hasText(dto.getLogoUrl())) {
            entity.setLogoUrl(dto.getLogoUrl());
        }
        if (StringUtils.hasText(dto.getHomepageUrl())) {
            entity.setHomepageUrl(dto.getHomepageUrl());
        }
        if (StringUtils.hasText(dto.getDescription())) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }

        entity.setUpdatedAt(LocalDateTime.now());
        applicationMapper.updateById(entity);

        log.info("应用更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteApplication(Long appId) {
        log.info("删除应用，应用ID：{}", appId);

        Application entity = applicationMapper.selectById(appId);
        if (entity == null) {
            throw new BusinessException("应用不存在");
        }

        // 检查是否有关联的OAuth2客户端或SAML SP
        Long oauth2Count = oauth2ClientMapper.selectCount(
                new LambdaQueryWrapper<OAuth2Client>().eq(OAuth2Client::getAppId, appId)
        );
        if (oauth2Count > 0) {
            throw new BusinessException("应用存在关联的OAuth2客户端，无法删除");
        }

        Long samlCount = samlSpMapper.selectCount(
                new LambdaQueryWrapper<SamlServiceProvider>().eq(SamlServiceProvider::getAppId, appId)
        );
        if (samlCount > 0) {
            throw new BusinessException("应用存在关联的SAML服务提供商，无法删除");
        }

        applicationMapper.deleteById(appId);

        log.info("应用删除成功");
    }

    @Override
    public ApplicationVO getApplicationById(Long appId) {
        log.debug("获取应用详情，应用ID：{}", appId);

        Application entity = applicationMapper.selectById(appId);
        if (entity == null) {
            throw new BusinessException("应用不存在");
        }

        ApplicationVO vo = applicationConverter.toVO(entity);

        // 查询关联的OAuth2客户端
        OAuth2Client oauth2Client = oauth2ClientMapper.selectByAppId(appId);
        if (oauth2Client != null) {
            ApplicationVO.OAuth2ClientVO oauth2ClientVO = new ApplicationVO.OAuth2ClientVO();
            oauth2ClientVO.setId(oauth2Client.getId());
            oauth2ClientVO.setClientId(oauth2Client.getClientId());
            oauth2ClientVO.setGrantTypes(oauth2Client.getGrantTypes());
            oauth2ClientVO.setScopes(oauth2Client.getScopes());
            oauth2ClientVO.setAccessTokenValidity(oauth2Client.getAccessTokenValidity());
            oauth2ClientVO.setRefreshTokenValidity(oauth2Client.getRefreshTokenValidity());
            oauth2ClientVO.setAutoApprove(oauth2Client.getAutoApprove());
            vo.setOauth2Client(oauth2ClientVO);
        }

        // 查询关联的SAML SP
        SamlServiceProvider samlSp = samlSpMapper.selectByAppId(appId);
        if (samlSp != null) {
            ApplicationVO.SamlSpVO samlSpVO = new ApplicationVO.SamlSpVO();
            samlSpVO.setId(samlSp.getId());
            samlSpVO.setSpEntityId(samlSp.getSpEntityId());
            samlSpVO.setAcsUrl(samlSp.getAcsUrl());
            samlSpVO.setMetadataUrl(samlSp.getMetadataUrl());
            vo.setSamlSp(samlSpVO);
        }

        return vo;
    }

    @Override
    public ApplicationVO getApplicationByAppKey(String appKey) {
        log.debug("根据应用密钥获取应用，应用密钥：{}", appKey);

        Application entity = applicationMapper.selectByAppKey(appKey);
        if (entity == null) {
            throw new BusinessException("应用不存在");
        }

        return applicationConverter.toVO(entity);
    }

    @Override
    public IPage<ApplicationVO> queryApplications(ApplicationQueryDTO dto) {
        log.debug("查询应用列表，查询条件：{}", dto);

        Page<Application> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(dto.getAppName())) {
            wrapper.like(Application::getAppName, dto.getAppName());
        }
        if (dto.getAppType() != null) {
            wrapper.eq(Application::getAppType, dto.getAppType());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(Application::getStatus, dto.getStatus());
        }
        if (dto.getOwnerId() != null) {
            wrapper.eq(Application::getOwnerId, dto.getOwnerId());
        }

        wrapper.orderByDesc(Application::getCreatedAt);

        IPage<Application> resultPage = applicationMapper.selectPage(page, wrapper);

        // 转换为VO
        Page<ApplicationVO> voPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        List<ApplicationVO> voList = resultPage.getRecords().stream()
                .map(applicationConverter::toVO)
                .toList();
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOAuth2Client(OAuth2ClientCreateDTO dto) {
        log.info("创建OAuth2客户端，客户端ID：{}", dto.getClientId());

        // 检查应用是否存在
        Application app = applicationMapper.selectById(dto.getAppId());
        if (app == null) {
            throw new BusinessException("应用不存在");
        }

        // 检查客户端ID是否已存在
        OAuth2Client existClient = oauth2ClientMapper.selectByClientId(dto.getClientId());
        if (existClient != null) {
            throw new BusinessException("客户端ID已存在");
        }

        OAuth2Client entity;
        try {
            entity = applicationConverter.toEntity(dto);
        } catch (Exception e) {
            throw new BusinessException("500", "创建OAuth2客户端失败：JSON转换错误", e);
        }

        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        oauth2ClientMapper.insert(entity);

        log.info("OAuth2客户端创建成功，ID：{}", entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOAuth2Client(OAuth2ClientUpdateDTO dto) {
        log.info("更新OAuth2客户端，ID：{}", dto.getId());

        OAuth2Client entity = oauth2ClientMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("OAuth2客户端不存在");
        }

        if (StringUtils.hasText(dto.getClientSecret())) {
            entity.setClientSecret(dto.getClientSecret());
        }
        if (dto.getGrantTypes() != null) {
            entity.setGrantTypes(applicationConverter.jsonToList(dto.getGrantTypes().toString()).toString());
        }
        if (dto.getScopes() != null) {
            entity.setScopes(applicationConverter.jsonToList(dto.getScopes().toString()).toString());
        }
        if (dto.getAccessTokenValidity() != null) {
            entity.setAccessTokenValidity(dto.getAccessTokenValidity());
        }
        if (dto.getRefreshTokenValidity() != null) {
            entity.setRefreshTokenValidity(dto.getRefreshTokenValidity());
        }
        if (dto.getAutoApprove() != null) {
            entity.setAutoApprove(dto.getAutoApprove());
        }

        entity.setUpdatedAt(LocalDateTime.now());
        oauth2ClientMapper.updateById(entity);

        log.info("OAuth2客户端更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOAuth2Client(Long clientId) {
        log.info("删除OAuth2客户端，ID：{}", clientId);

        OAuth2Client entity = oauth2ClientMapper.selectById(clientId);
        if (entity == null) {
            throw new BusinessException("OAuth2客户端不存在");
        }

        oauth2ClientMapper.deleteById(clientId);

        log.info("OAuth2客户端删除成功");
    }

    @Override
    public OAuth2ClientVO getOAuth2ClientById(Long clientId) {
        log.debug("获取OAuth2客户端详情，ID：{}", clientId);

        OAuth2Client entity = oauth2ClientMapper.selectById(clientId);
        if (entity == null) {
            throw new BusinessException("OAuth2客户端不存在");
        }

        return applicationConverter.toOAuth2ClientVO(entity);
    }

    @Override
    public OAuth2ClientVO getOAuth2ClientByClientId(String clientId) {
        log.debug("根据客户端ID获取OAuth2客户端，客户端ID：{}", clientId);

        OAuth2Client entity = oauth2ClientMapper.selectByClientId(clientId);
        if (entity == null) {
            throw new BusinessException("OAuth2客户端不存在");
        }

        return applicationConverter.toOAuth2ClientVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSamlSp(SamlSpCreateDTO dto) {
        log.info("创建SAML服务提供商，SP实体ID：{}", dto.getSpEntityId());

        // 检查应用是否存在
        Application app = applicationMapper.selectById(dto.getAppId());
        if (app == null) {
            throw new BusinessException("应用不存在");
        }

        // 检查SP实体ID是否已存在
        SamlServiceProvider existSp = samlSpMapper.selectBySpEntityId(dto.getSpEntityId());
        if (existSp != null) {
            throw new BusinessException("SP实体ID已存在");
        }

        SamlServiceProvider entity = applicationConverter.toEntity(dto);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        samlSpMapper.insert(entity);

        log.info("SAML服务提供商创建成功，ID：{}", entity.getId());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSamlSp(SamlSpUpdateDTO dto) {
        log.info("更新SAML服务提供商，ID：{}", dto.getId());

        SamlServiceProvider entity = samlSpMapper.selectById(dto.getId());
        if (entity == null) {
            throw new BusinessException("SAML服务提供商不存在");
        }

        if (StringUtils.hasText(dto.getSpEntityId())) {
            entity.setSpEntityId(dto.getSpEntityId());
        }
        if (StringUtils.hasText(dto.getAcsUrl())) {
            entity.setAcsUrl(dto.getAcsUrl());
        }
        if (StringUtils.hasText(dto.getCertificate())) {
            entity.setCertificate(dto.getCertificate());
        }
        if (StringUtils.hasText(dto.getMetadataUrl())) {
            entity.setMetadataUrl(dto.getMetadataUrl());
        }

        entity.setUpdatedAt(LocalDateTime.now());
        samlSpMapper.updateById(entity);

        log.info("SAML服务提供商更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSamlSp(Long spId) {
        log.info("删除SAML服务提供商，ID：{}", spId);

        SamlServiceProvider entity = samlSpMapper.selectById(spId);
        if (entity == null) {
            throw new BusinessException("SAML服务提供商不存在");
        }

        samlSpMapper.deleteById(spId);

        log.info("SAML服务提供商删除成功");
    }

    @Override
    public SamlSpVO getSamlSpById(Long spId) {
        log.debug("获取SAML服务提供商详情，ID：{}", spId);

        SamlServiceProvider entity = samlSpMapper.selectById(spId);
        if (entity == null) {
            throw new BusinessException("SAML服务提供商不存在");
        }

        return applicationConverter.toSamlSpVO(entity);
    }

    @Override
    public OAuth2ClientVO getOAuth2ClientByAppId(Long appId) {
        log.debug("根据应用ID获取OAuth2客户端配置，应用ID：{}", appId);

        LambdaQueryWrapper<OAuth2Client> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OAuth2Client::getAppId, appId);
        OAuth2Client entity = oauth2ClientMapper.selectOne(wrapper);
        
        if (entity == null) {
            return null;
        }

        return applicationConverter.toOAuth2ClientVO(entity);
    }

    @Override
    public SamlSpVO getSamlSpByAppId(Long appId) {
        log.debug("根据应用ID获取SAML服务提供商配置，应用ID：{}", appId);

        LambdaQueryWrapper<SamlServiceProvider> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SamlServiceProvider::getAppId, appId);
        SamlServiceProvider entity = samlSpMapper.selectOne(wrapper);
        
        if (entity == null) {
            return null;
        }

        return applicationConverter.toSamlSpVO(entity);
    }
}
