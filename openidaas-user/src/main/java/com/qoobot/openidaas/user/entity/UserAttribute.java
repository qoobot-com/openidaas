package com.qoobot.openidaas.user.entity;

import com.qoobot.openidaas.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 用户扩展属性实体
 * 
 * 支持自定义字段扩展
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_attributes", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_key", columnList = "key")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAttribute extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    @Column(name = "data_type", length = 50)
    @Builder.Default
    private String dataType = "STRING";

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "is_encrypted")
    @Builder.Default
    private Boolean isEncrypted = false;
}
