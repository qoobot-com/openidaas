package com.qoobot.openidaas.user.entity;

import com.qoobot.openidaas.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户档案实体类
 * 
 * 存储用户详细个人信息
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Entity
@Table(name = "user_profiles", indexes = {
    @Index(name = "idx_user_id", columnList = "userId")
})
@SQLDelete(sql = "UPDATE user_profiles SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "id_number", length = 50)
    private String idNumber;

    @Column(name = "id_type", length = 20)
    private String idType;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "province", length = 100)
    private String province;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "timezone", length = 50)
    @Builder.Default
    private String timezone = "Asia/Shanghai";

    @Column(name = "language", length = 20)
    @Builder.Default
    private String language = "zh-CN";

    @Column(name = "website", length = 200)
    private String website;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests;

    @Column(name = "linkedin", length = 200)
    private String linkedin;

    @Column(name = "twitter", length = 200)
    private String twitter;

    @Column(name = "wechat", length = 100)
    private String wechat;

    @Column(name = "qq", length = 20)
    private String qq;

    @Column(name = "emergency_contact", length = 100)
    private String emergencyContact;

    @Column(name = "emergency_phone", length = 20)
    private String emergencyPhone;

    @Column(name = "emergency_relationship", length = 50)
    private String emergencyRelationship;

    @Column(name = "education", columnDefinition = "TEXT")
    private String education;

    @Column(name = "experience", columnDefinition = "TEXT")
    private String experience;

    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;

    @Column(name = "certifications", columnDefinition = "TEXT")
    private String certifications;

    @Column(name = "custom_fields", columnDefinition = "JSONB")
    private String customFields;

    @Column(name = "last_profile_update")
    private LocalDateTime lastProfileUpdate;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
}
