package com.qoobot.openidaas.user.controller;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 分配角色请求
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
public class AssignRolesRequest {

    @NotEmpty(message = "角色列表不能为空")
    private List<Long> roleIds;
}
