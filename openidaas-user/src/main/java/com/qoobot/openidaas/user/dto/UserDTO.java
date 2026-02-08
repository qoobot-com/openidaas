package com.qoobot.openidaas.user.dto;

import com.qoobot.openidaas.user.entity.AccountType;
import com.qoobot.openidaas.user.entity.UserStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户数据传输对象
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String fullname;
    private String nickname;
    private String avatar;
    private String email;
    private String phone;
    private String countryCode;
    private UserStatus status;
    private AccountType accountType;
    private Long tenantId;
    private Long departmentId;
    private Long positionId;
    private String employeeId;
    private LocalDateTime hireDate;
    private String jobTitle;
    private String workLocation;
    private Long managerId;
    private String managerName;
    private String departmentName;
    private String roleName;
    private List<String> roleNames;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Integer loginCount;
    private Boolean mfaEnabled;
    private Boolean mustChangePassword;
    private LocalDateTime passwordExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}
