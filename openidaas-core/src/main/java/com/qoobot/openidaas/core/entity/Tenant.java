package com.qoobot.openidaas.core.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 租户实体类
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Entity
@Table(name = "tenants", indexes = {
    @Index(name = "idx_tenant_code", columnList = "code", unique = true),
    @Index(name = "idx_tenant_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Tenant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "租户编码不能为空")
    @Size(max = 50, message = "租户编码长度不能超过50")
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @NotBlank(message = "租户名称不能为空")
    @Size(max = 200, message = "租户名称长度不能超过200")
    @Column(nullable = false)
    private String name;
    
    @Size(max = 500, message = "租户描述长度不能超过500")
    @Column(length = 500)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;
    
    @Size(max = 100, message = "联系人长度不能超过100")
    @Column(name = "contact_person", length = 100)
    private String contactPerson;
    
    @Size(max = 20, message = "联系电话长度不能超过20")
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Email(message = "联系邮箱格式不正确")
    @Size(max = 100, message = "联系邮箱长度不能超过100")
    @Column(name = "contact_email", length = 100)
    private String contactEmail;
    
    @Column(name = "logo_url")
    private String logoUrl;
    
    @Column(name = "max_users")
    @Builder.Default
    private Integer maxUsers = 100;
    
    @Column(name = "current_users")
    @Builder.Default
    private Integer currentUsers = 0;
    
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;
    
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<User> users = new HashSet<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * 租户状态枚举
     */
    public enum TenantStatus {
        ACTIVE("激活"),
        SUSPENDED("暂停"),
        EXPIRED("已过期"),
        DELETED("已删除");
        
        private final String description;
        
        TenantStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
