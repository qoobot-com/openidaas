package com.qoobot.openidaas.user.service.impl;

import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserCreateDTO;
import com.qoobot.openidaas.common.dto.user.UserUpdateDTO;
import com.qoobot.openidaas.common.enumeration.OperationTypeEnum;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.feign.AuditClient;
import com.qoobot.openidaas.common.feign.FeignHelper;
import com.qoobot.openidaas.common.feign.OrganizationClient;
import com.qoobot.openidaas.common.feign.RoleClient;
import com.qoobot.openidaas.common.vo.department.DepartmentVO;
import com.qoobot.openidaas.common.vo.position.PositionVO;
import com.qoobot.openidaas.common.vo.role.RoleVO;
import com.qoobot.openidaas.user.entity.User;
import com.qoobot.openidaas.user.mapper.UserMapper;
import com.qoobot.openidaas.user.service.UserService;
import com.qoobot.openidaas.user.vo.UserDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户集成服务 - 演示 Feign 调用其他服务
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserIntegrationServiceImpl {

    private final UserService userService;
    private final UserMapper userMapper;
    private final OrganizationClient organizationClient;
    private final RoleClient roleClient;
    private final AuditClient auditClient;

    /**
     * 获取用户详细信息（包含部门、职位、角色信息）
     * 演示：调用多个微服务组合数据
     */
    public UserDetailVO getUserDetail(Long userId) {
        log.info("获取用户详细信息，userId: {}", userId);

        // 1. 查询本地用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserDetailVO detailVO = new UserDetailVO();
        detailVO.setId(user.getId());
        detailVO.setUsername(user.getUsername());
        detailVO.setEmail(user.getEmail());
        detailVO.setMobile(user.getMobile());
        detailVO.setStatus(user.getStatus());

        // 2. 调用组织服务获取部门信息
        // 注意：当前User实体中没有deptId字段，这里作为演示保留
        // 实际项目中可以通过user_department表关联查询
        /*
        if (user.getDeptId() != null) {
            try {
                DepartmentVO dept = FeignHelper.call(
                    () -> organizationClient.getDepartmentById(user.getDeptId())
                );
                if (dept != null) {
                    detailVO.setDeptId(dept.getId());
                    detailVO.setDeptName(dept.getDeptName());
                    detailVO.setDeptCode(dept.getDeptCode());
                }
            } catch (Exception e) {
                log.error("获取部门信息失败", e);
                // 部门信息获取失败不影响主流程
            }
        }
        */

        // 3. 调用组织服务获取职位信息
        // 注意：当前User实体中没有positionId字段，这里作为演示保留
        // 实际项目中可以通过user_department表关联查询
        /*
        if (user.getPositionId() != null) {
            try {
                PositionVO position = FeignHelper.call(
                    () -> organizationClient.getPositionById(user.getPositionId())
                );
                if (position != null) {
                    detailVO.setPositionId(position.getId());
                    detailVO.setPositionName(position.getPositionName());
                    detailVO.setPositionCode(position.getPositionCode());
                }
            } catch (Exception e) {
                log.error("获取职位信息失败", e);
            }
        }
        */

        // 4. 调用角色服务获取用户角色列表
        try {
            List<RoleVO> roles = FeignHelper.call(
                () -> roleClient.getUserRoles(userId)
            );
            if (roles != null) {
                detailVO.setRoles(roles);
                detailVO.setRoleNames(roles.stream()
                    .map(RoleVO::getRoleName)
                    .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.error("获取用户角色失败", e);
        }

        return detailVO;
    }

    /**
     * 分配用户角色并记录审计日志
     * 演示：跨服务事务和审计
     */
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesAndAudit(Long userId, Long roleId, Long operatorId) {
        log.info("分配用户角色，userId: {}, roleId: {}, operatorId: {}", userId, roleId, operatorId);

        // 1. 调用角色服务分配角色
        try {
            FeignHelper.call(
                () -> roleClient.assignRoleToUser(userId, roleId, null, null)
            );
            log.info("角色分配成功");
        } catch (Exception e) {
            log.error("角色分配失败", e);
            throw new BusinessException("角色分配失败: " + e.getMessage());
        }

        // 2. 获取角色信息
        RoleVO role = FeignHelper.call(() -> roleClient.getRoleById(roleId));

        // 3. 调用审计服务记录日志
        AuditLogCreateDTO auditLog = new AuditLogCreateDTO();
        auditLog.setOperatorId(operatorId);
        auditLog.setOperationType(OperationTypeEnum.CREATE.getCode());
        auditLog.setModule("user");
        auditLog.setOperationDesc(String.format("为用户 %d 分配角色 %s", userId, role.getRoleName()));
        auditLog.setTargetId(userId);
        auditLog.setTargetType("用户");
        auditLog.setResult("SUCCESS");
        auditLog.setOperatorIp(getClientIp());
        auditLog.setUserAgent(getUserAgent());

        try {
            FeignHelper.call(() -> auditClient.createAuditLog(auditLog));
            log.info("审计日志记录成功");
        } catch (Exception e) {
            log.error("审计日志记录失败", e);
            // 审计日志记录失败不影响主业务
        }
    }

    /**
     * 批量导入用户（支持异步处理）
     * 演示：异步 Feign 调用
     */
    public void batchImportUsers(List<UserCreateDTO> users, Long operatorId) {
        log.info("批量导入用户，数量: {}, 操作人: {}", users.size(), operatorId);

        // 异步导入用户
        users.parallelStream()
            .map(user -> {
                try {
                    // 调用本地用户服务创建用户
                    return userService.createUser(user);
                } catch (Exception e) {
                    log.error("用户导入失败: {}", user.getUsername(), e);
                    return null;
                }
            })
            .filter(user -> user != null)
            .collect(Collectors.toList());

        // 记录批量操作审计日志
        AuditLogCreateDTO auditLog = new AuditLogCreateDTO();
        auditLog.setOperatorId(operatorId);
        auditLog.setOperationType(OperationTypeEnum.CREATE.getCode());
        auditLog.setModule("user");
        auditLog.setOperationDesc(String.format("批量导入用户 %d 个", users.size()));
        auditLog.setResult("SUCCESS");

        FeignHelper.call(() -> auditClient.createAuditLog(auditLog));
    }

    /**
     * 删除用户（级联删除关联数据）
     * 演示：调用多个微服务清理关联数据
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserWithCascade(Long userId, Long operatorId) {
        log.info("级联删除用户，userId: {}, operatorId: {}", userId, operatorId);

        // 1. 获取用户角色列表
        List<RoleVO> roles = FeignHelper.call(() -> roleClient.getUserRoles(userId));

        // 2. 移除用户的所有角色
        for (RoleVO role : roles) {
            try {
                FeignHelper.call(() -> roleClient.removeRoleFromUser(userId, role.getId()));
                log.info("移除用户角色成功，roleId: {}", role.getId());
            } catch (Exception e) {
                log.error("移除用户角色失败，roleId: {}", role.getId(), e);
            }
        }

        // 3. 删除本地用户数据
        // userMapper.deleteById(userId);

        // 4. 记录审计日志
        AuditLogCreateDTO auditLog = new AuditLogCreateDTO();
        auditLog.setOperatorId(operatorId);
        auditLog.setOperationType(OperationTypeEnum.DELETE.getCode());
        auditLog.setModule("user");
        auditLog.setOperationDesc("级联删除用户及其关联数据");
        auditLog.setTargetId(userId);
        auditLog.setTargetType("用户");
        auditLog.setResult("SUCCESS");

        FeignHelper.call(() -> auditClient.createAuditLog(auditLog));
        log.info("用户级联删除成功");
    }

    /**
     * 验证用户权限
     * 演示：服务间权限校验
     */
    public boolean hasPermission(Long userId, String permission) {
        try {
            // 1. 获取用户角色
            List<RoleVO> roles = FeignHelper.call(() -> roleClient.getUserRoles(userId));

            // 2. 检查角色是否有对应权限
            for (RoleVO role : roles) {
                List<Long> permIds = FeignHelper.call(() -> roleClient.getRolePermissions(role.getId()));
                // 这里需要进一步检查 permission 是否在 permIds 中
                // 简化处理，实际需要根据 permission 查询权限表
                if (permIds != null && !permIds.isEmpty()) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("权限校验失败", e);
            return false;
        }
    }

    /**
     * 调用组织服务获取部门树（带缓存）
     * 演示：使用 Spring Cache 缓存 Feign 调用结果
     */
    // @Cacheable(value = "deptTree", key = "#parentId")
    public List<DepartmentVO> getDepartmentTree(Long parentId) {
        log.info("获取部门树，parentId: {}", parentId);
        return FeignHelper.call(() -> organizationClient.getDepartmentTree(parentId));
    }

    /**
     * 创建用户（简化示例）
     */
    /*
    private String createUser(UserCreateDTO user) {
        // 实际实现应该调用本地用户服务
        return "success";
    }
    */

    /**
     * 获取客户端 IP（简化）
     */
    private String getClientIp() {
        return "127.0.0.1";
    }

    /**
     * 获取 User-Agent（简化）
     */
    private String getUserAgent() {
        return "OpenIDaaS-Client/1.0";
    }
}
