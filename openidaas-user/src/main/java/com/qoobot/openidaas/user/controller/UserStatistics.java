package com.qoobot.openidaas.user.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计信息
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {

    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long lockedUsers;
    private Long departments;
    private Long roles;
}
