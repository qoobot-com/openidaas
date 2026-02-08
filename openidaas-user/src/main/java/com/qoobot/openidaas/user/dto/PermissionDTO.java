package com.qoobot.openidaas.user.dto;

import com.qoobot.openidaas.user.entity.ResourceType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限数据传输对象
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Long parentId;
    private ResourceType resourceType;
    private String resourcePath;
    private String httpMethod;
    private Integer sortOrder;
    private String icon;
    private Boolean isVisible;
    private List<PermissionDTO> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
