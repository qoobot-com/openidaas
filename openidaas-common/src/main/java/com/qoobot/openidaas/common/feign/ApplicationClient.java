package com.qoobot.openidaas.common.feign;

import com.qoobot.openidaas.common.dto.application.ApplicationCreateDTO;
import com.qoobot.openidaas.common.dto.application.ApplicationQueryDTO;
import com.qoobot.openidaas.common.dto.application.ApplicationUpdateDTO;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.application.ApplicationVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 应用服务 Feign 客户端
 *
 * @author QooBot
 */
@FeignClient(
        name = "application-service",
        configuration = com.qoobot.openidaas.common.config.FeignConfig.class
)
public interface ApplicationClient {

    /**
     * 查询应用列表（分页）
     */
    @PostMapping("/api/applications/query")
    ResultVO<PageResultVO<ApplicationVO>> queryApplications(@RequestBody ApplicationQueryDTO queryDTO);

    /**
     * 根据应用 ID 获取应用信息
     */
    @GetMapping("/api/applications/{id}")
    ResultVO<ApplicationVO> getApplicationById(@PathVariable("id") Long id);

    /**
     * 根据应用密钥获取应用信息
     */
    @GetMapping("/api/applications/by-appkey/{appKey}")
    ResultVO<ApplicationVO> getApplicationByAppKey(@PathVariable("appKey") String appKey);

    /**
     * 创建应用
     */
    @PostMapping("/api/applications")
    ResultVO<ApplicationVO> createApplication(@RequestBody ApplicationCreateDTO createDTO);

    /**
     * 更新应用信息
     */
    @PutMapping("/api/applications")
    ResultVO<ApplicationVO> updateApplication(@RequestBody ApplicationUpdateDTO updateDTO);

    /**
     * 删除应用
     */
    @DeleteMapping("/api/applications/{id}")
    ResultVO<Void> deleteApplication(@PathVariable("id") Long id);

    /**
     * 启用应用
     */
    @PostMapping("/api/applications/{id}/enable")
    ResultVO<Void> enableApplication(@PathVariable("id") Long id);

    /**
     * 禁用应用
     */
    @PostMapping("/api/applications/{id}/disable")
    ResultVO<Void> disableApplication(@PathVariable("id") Long id);
}
