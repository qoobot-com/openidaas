package com.qoobot.openidaas.user.dto;

import com.qoobot.openidaas.user.entity.UserStatus;
import lombok.Data;

/**
 * 用户搜索请求DTO
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
public class UserSearchRequest {

    private String keyword;
    private UserStatus status;
    private Long tenantId;
    private Long departmentId;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
