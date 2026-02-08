package com.qoobot.openidaas.user.dto;

import com.qoobot.openidaas.user.entity.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 角色数据传输对象
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Long tenantId;
    private RoleType type;
    private RoleScope scope;
    private Integer level;
    private DataScope dataScope;
    private Boolean isSystem;
    private Set<PermissionDTO> permissions;
    private Integer userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
