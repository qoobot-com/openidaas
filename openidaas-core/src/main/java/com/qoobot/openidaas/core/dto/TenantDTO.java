package com.qoobot.openidaas.core.dto;

import com.qoobot.openidaas.core.entity.Tenant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 租户DTO
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDTO {
    
    private Long id;
    private String code;
    private String name;
    private String description;
    private Tenant.TenantStatus status;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String logoUrl;
    private Integer maxUsers;
    private Integer currentUsers;
    private LocalDateTime expiredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为DTO
     */
    public static TenantDTO fromEntity(Tenant tenant) {
        return TenantDTO.builder()
                .id(tenant.getId())
                .code(tenant.getCode())
                .name(tenant.getName())
                .description(tenant.getDescription())
                .status(tenant.getStatus())
                .contactPerson(tenant.getContactPerson())
                .contactPhone(tenant.getContactPhone())
                .contactEmail(tenant.getContactEmail())
                .logoUrl(tenant.getLogoUrl())
                .maxUsers(tenant.getMaxUsers())
                .currentUsers(tenant.getCurrentUsers())
                .expiredAt(tenant.getExpiredAt())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(tenant.getUpdatedAt())
                .build();
    }
}
