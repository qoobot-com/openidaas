package com.qoobot.openidaas.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RegisteredClient 实体
 * 
 * OAuth2 客户端实体类
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Entity
@Table(name = "registered_clients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisteredClientEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 256)
    private String clientId;
    
    @Column(nullable = false)
    private String clientSecret;
    
    @Column(length = 1000)
    private String clientName;
    
    @Column(length = 2000)
    private String redirectUris;
    
    @Column(length = 1000)
    private String scopes;
    
    @Column(length = 500)
    private String grantTypes;
    
    @Column(length = 500)
    private String clientAuthenticationMethods;
    
    @Column(length = 2000)
    private String clientSettings;
    
    @Column(length = 2000)
    private String tokenSettings;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
