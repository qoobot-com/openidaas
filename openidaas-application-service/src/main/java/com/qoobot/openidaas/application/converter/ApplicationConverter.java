package com.qoobot.openidaas.application.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoobot.openidaas.application.entity.Application;
import com.qoobot.openidaas.application.entity.OAuth2Client;
import com.qoobot.openidaas.application.entity.SamlServiceProvider;
import com.qoobot.openidaas.common.dto.application.*;
import com.qoobot.openidaas.common.enumeration.ApplicationTypeEnum;
import com.qoobot.openidaas.common.enumeration.StatusEnum;
import com.qoobot.openidaas.common.vo.application.ApplicationVO;
import com.qoobot.openidaas.common.vo.application.OAuth2ClientVO;
import com.qoobot.openidaas.common.vo.application.SamlSpVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用实体转换器
 */
@Component
public class ApplicationConverter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * DTO转实体
     */
    public Application toEntity(ApplicationCreateDTO dto) {
        Application entity = new Application();
        BeanUtils.copyProperties(dto, entity);
        entity.setStatus(StatusEnum.ENABLED.getCode());
        return entity;
    }

    /**
     * 实体转VO
     */
    public ApplicationVO toVO(Application entity) {
        ApplicationVO vo = new ApplicationVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setAppTypeDesc(ApplicationTypeEnum.fromCode(entity.getAppType()).getDescription());
        vo.setStatusDesc(StatusEnum.fromCode(entity.getStatus()).getDescription());
        return vo;
    }

    /**
     * OAuth2客户端DTO转实体
     */
    public OAuth2Client toEntity(OAuth2ClientCreateDTO dto) throws Exception {
        OAuth2Client entity = new OAuth2Client();
        BeanUtils.copyProperties(dto, entity);

        // 转换List为JSON字符串
        if (dto.getGrantTypes() != null) {
            entity.setGrantTypes(objectMapper.writeValueAsString(dto.getGrantTypes()));
        }
        if (dto.getScopes() != null) {
            entity.setScopes(objectMapper.writeValueAsString(dto.getScopes()));
        }

        // 设置默认值
        if (entity.getAccessTokenValidity() == null) {
            entity.setAccessTokenValidity(3600);
        }
        if (entity.getRefreshTokenValidity() == null) {
            entity.setRefreshTokenValidity(2592000);
        }
        if (entity.getAutoApprove() == null) {
            entity.setAutoApprove(false);
        }

        return entity;
    }

    /**
     * OAuth2客户端实体转VO
     */
    public OAuth2ClientVO toOAuth2ClientVO(OAuth2Client entity) {
        OAuth2ClientVO vo = new OAuth2ClientVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * SAML SP DTO转实体
     */
    public SamlServiceProvider toEntity(SamlSpCreateDTO dto) {
        SamlServiceProvider entity = new SamlServiceProvider();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }

    /**
     * SAML SP实体转VO
     */
    public SamlSpVO toSamlSpVO(SamlServiceProvider entity) {
        SamlSpVO vo = new SamlSpVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    /**
     * JSON字符串转List
     */
    public List<String> jsonToList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
