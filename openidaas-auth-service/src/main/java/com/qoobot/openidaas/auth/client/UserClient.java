package com.qoobot.openidaas.auth.client;

import com.qoobot.openidaas.auth.dto.UserInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务 Feign 客户端
 */
@FeignClient(
    name = "openidaas-user-service",
    path = "/users",
    contextId = "authUserClient"
)
public interface UserClient {

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/username/{username}")
    UserInfoDTO getUserByUsername(@PathVariable("username") String username);

    /**
     * 根据 ID 获取用户信息
     */
    @GetMapping("/{id}")
    UserInfoDTO getUserById(@PathVariable("id") Long id);

    /**
     * 根据邮箱获取用户信息
     */
    @GetMapping("/email")
    UserInfoDTO getUserByEmail(@RequestParam("email") String email);

    /**
     * 根据手机号获取用户信息
     */
    @GetMapping("/phone")
    UserInfoDTO getUserByPhone(@RequestParam("phone") String phone);
}
