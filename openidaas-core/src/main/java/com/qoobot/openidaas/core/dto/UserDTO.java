package com.qoobot.openidaas.core.dto;

import com.qoobot.openidaas.core.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户DTO
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private User.UserStatus status;
    private User.UserType type;
    private TenantDTO tenant;
    private Set<String> roles;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 从实体转换为DTO
     */
    public static UserDTO fromEntity(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus())
                .type(user.getType())
                .tenant(user.getTenant() != null ? 
                    TenantDTO.fromEntity(user.getTenant()) : null)
                .roles(user.getRoles().stream()
                        .map(role -> role.getCode())
                        .collect(Collectors.toSet()))
                .lastLoginTime(user.getLastLoginTime())
                .lastLoginIp(user.getLastLoginIp())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
