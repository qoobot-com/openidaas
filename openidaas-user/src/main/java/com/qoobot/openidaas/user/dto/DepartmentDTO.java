package com.qoobot.openidaas.user.dto;

import com.qoobot.openidaas.user.entity.DepartmentStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门数据传输对象
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDTO {

    private Long id;
    private String name;
    private String code;
    private String description;
    private Long tenantId;
    private Long parentId;
    private String parentName;
    private List<DepartmentDTO> children;
    private Integer level;
    private Integer sortOrder;
    private Long leaderId;
    private String leaderName;
    private String phone;
    private String email;
    private String address;
    private DepartmentStatus status;
    private String externalId;
    private Long userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
