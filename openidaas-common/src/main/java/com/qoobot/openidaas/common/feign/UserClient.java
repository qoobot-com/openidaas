package com.qoobot.openidaas.common.feign;

import com.qoobot.openidaas.common.dto.user.UserQueryDTO;
import com.qoobot.openidaas.common.vo.PageResultVO;
import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.vo.ResultVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务 Feign 客户端
 *
 * @author QooBot
 */
@FeignClient(
        name = "user-service",
        path = "/api/users",
        configuration = com.qoobot.openidaas.common.config.FeignConfig.class
)
public interface UserClient {

    /**
     * 根据用户 ID 获取用户信息
     */
    @GetMapping("/{id}")
    ResultVO<Object> getUserById(@PathVariable("id") Long id);

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/by-username/{username}")
    ResultVO<Object> getUserByUsername(@PathVariable("username") String username);

    /**
     * 查询用户列表（分页）
     */
    @GetMapping
    ResultVO<PageResultVO<Object>> getUsers(@RequestBody UserQueryDTO queryDTO);

    /**
     * 创建用户
     */
    @PostMapping
    ResultVO<Object> createUser(@RequestBody UserCreateDTO createDTO);

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    ResultVO<Object> updateUser(@PathVariable("id") Long id, @RequestBody UserUpdateDTO updateDTO);

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    ResultVO<Void> deleteUser(@PathVariable("id") Long id);

    /**
     * 锁定用户
     */
    @PostMapping("/{id}/lock")
    ResultVO<Void> lockUser(@PathVariable("id") Long id);

    /**
     * 解锁用户
     */
    @PostMapping("/{id}/unlock")
    ResultVO<Void> unlockUser(@PathVariable("id") Long id);

    /**
     * 验证用户密码
     */
    @PostMapping("/{id}/verify-password")
    ResultVO<Boolean> verifyPassword(@PathVariable("id") Long id, @RequestBody String password);
}
