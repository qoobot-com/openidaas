package com.qoobot.openidaas.application.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qoobot.openidaas.application.service.ApplicationService;
import com.qoobot.openidaas.common.dto.application.*;
import com.qoobot.openidaas.common.vo.application.ApplicationVO;
import com.qoobot.openidaas.common.vo.application.OAuth2ClientVO;
import com.qoobot.openidaas.common.vo.application.SamlSpVO;
import com.qoobot.openidaas.common.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 应用管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "应用管理", description = "应用注册和管理API")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @Operation(summary = "创建应用")
    public ResultVO<Long> createApplication(@Valid @RequestBody ApplicationCreateDTO dto) {
        Long appId = applicationService.createApplication(dto);
        return ResultVO.success(appId);
    }

    /**
     * 更新应用 - 符合RESTful规范，使用请求体
     */
    @PutMapping
    @Operation(summary = "更新应用")
    public ResultVO<Void> updateApplication(@Valid @RequestBody ApplicationUpdateDTO dto) {
        applicationService.updateApplication(dto);
        return ResultVO.success();
    }

    /**
     * 删除应用 - 符合RESTful规范，使用路径参数
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除应用")
    public ResultVO<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResultVO.success();
    }

    /**
     * 获取应用详情 - 符合OpenAPI规范，使用路径参数{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取应用详情")
    public ResultVO<ApplicationVO> getApplication(@PathVariable Long id) {
        ApplicationVO vo = applicationService.getApplicationById(id);
        return ResultVO.success(vo);
    }

    @GetMapping("/app-key/{appKey}")
    @Operation(summary = "根据应用密钥获取应用")
    public ResultVO<ApplicationVO> getApplicationByAppKey(@PathVariable String appKey) {
        ApplicationVO vo = applicationService.getApplicationByAppKey(appKey);
        return ResultVO.success(vo);
    }

    /**
     * 分页查询应用 - 符合OpenAPI规范，使用GET方法
     */
    @GetMapping
    @Operation(summary = "分页查询应用")
    public ResultVO<IPage<ApplicationVO>> queryApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String appName) {
        ApplicationQueryDTO dto = new ApplicationQueryDTO();
        dto.setAppName(appName);
        // 使用MyBatis Plus分页参数
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<ApplicationVO> pageParam =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        IPage<ApplicationVO> pageResult = applicationService.queryApplications(dto);
        return ResultVO.success(pageResult);
    }

    /**
     * OAuth2客户端管理
     */
    @PostMapping("/oauth2/clients")
    @Operation(summary = "创建OAuth2客户端")
    public ResultVO<Long> createOAuth2Client(@Valid @RequestBody OAuth2ClientCreateDTO dto) {
        Long clientId = applicationService.createOAuth2Client(dto);
        return ResultVO.success(clientId);
    }

    @PutMapping("/oauth2/clients")
    @Operation(summary = "更新OAuth2客户端")
    public ResultVO<Void> updateOAuth2Client(@Valid @RequestBody OAuth2ClientUpdateDTO dto) {
        applicationService.updateOAuth2Client(dto);
        return ResultVO.success();
    }

    @DeleteMapping("/oauth2/clients/{clientId}")
    @Operation(summary = "删除OAuth2客户端")
    public ResultVO<Void> deleteOAuth2Client(@PathVariable Long clientId) {
        applicationService.deleteOAuth2Client(clientId);
        return ResultVO.success();
    }

    @GetMapping("/oauth2/clients/{clientId}")
    @Operation(summary = "获取OAuth2客户端详情")
    public ResultVO<OAuth2ClientVO> getOAuth2Client(@PathVariable Long clientId) {
        OAuth2ClientVO vo = applicationService.getOAuth2ClientById(clientId);
        return ResultVO.success(vo);
    }

    @GetMapping("/oauth2/clients/client-id/{clientId}")
    @Operation(summary = "根据客户端ID获取OAuth2客户端")
    public ResultVO<OAuth2ClientVO> getOAuth2ClientByClientId(@PathVariable String clientId) {
        OAuth2ClientVO vo = applicationService.getOAuth2ClientByClientId(clientId);
        return ResultVO.success(vo);
    }

    /**
     * 获取应用的OAuth2配置
     */
    @GetMapping("/{id}/oauth2")
    @Operation(summary = "获取应用的OAuth2配置")
    public ResultVO<OAuth2ClientVO> getApplicationOAuth2Config(@PathVariable Long id) {
        OAuth2ClientVO vo = applicationService.getOAuth2ClientByAppId(id);
        return ResultVO.success(vo);
    }

    /**
     * SAML服务提供商管理
     */
    @PostMapping("/saml/sp")
    @Operation(summary = "创建SAML服务提供商")
    public ResultVO<Long> createSamlSp(@Valid @RequestBody SamlSpCreateDTO dto) {
        Long spId = applicationService.createSamlSp(dto);
        return ResultVO.success(spId);
    }

    @PutMapping("/saml/sp")
    @Operation(summary = "更新SAML服务提供商")
    public ResultVO<Void> updateSamlSp(@Valid @RequestBody SamlSpUpdateDTO dto) {
        applicationService.updateSamlSp(dto);
        return ResultVO.success();
    }

    @DeleteMapping("/saml/sp/{spId}")
    @Operation(summary = "删除SAML服务提供商")
    public ResultVO<Void> deleteSamlSp(@PathVariable Long spId) {
        applicationService.deleteSamlSp(spId);
        return ResultVO.success();
    }

    @GetMapping("/saml/sp/{spId}")
    @Operation(summary = "获取SAML服务提供商详情")
    public ResultVO<SamlSpVO> getSamlSp(@PathVariable Long spId) {
        SamlSpVO vo = applicationService.getSamlSpById(spId);
        return ResultVO.success(vo);
    }

    /**
     * 获取应用的SAML配置
     */
    @GetMapping("/{id}/saml")
    @Operation(summary = "获取应用的SAML配置")
    public ResultVO<SamlSpVO> getApplicationSamlConfig(@PathVariable Long id) {
        SamlSpVO vo = applicationService.getSamlSpByAppId(id);
        return ResultVO.success(vo);
    }
}
