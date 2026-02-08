package com.qoobot.openidaas.core.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 认证令牌实体类
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Entity
@Table(name = "auth_tokens", indexes = {
    @Index(name = "idx_token_user", columnList = "user_id"),
    @Index(name = "idx_token_tenant", columnList = "tenant_id"),
    @Index(name = "idx_token_refresh", columnList = "refresh_token"),
    @Index(name = "idx_token_expired_at", columnList = "expired_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String accessToken;
    
    @Column(name = "access_token_expired_at", nullable = false)
    private LocalDateTime accessTokenExpiredAt;
    
    @Column(length = 500)
    private String refreshToken;
    
    @Column(name = "refresh_token_expired_at")
    private LocalDateTime refreshTokenExpiredAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TokenType tokenType;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;
    
    @Column(name = "client_id")
    private String clientId;
    
    @Column(name = "device_id", length = 100)
    private String deviceId;
    
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    @Column(name = "ip_address", length = 50)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 令牌类型枚举
     */
    public enum TokenType {
        BEARER("Bearer"),
        REFRESH("Refresh"),
        ID_TOKEN("ID Token");
        
        private final String value;
        
        TokenType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}
