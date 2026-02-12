package com.qoobot.openidaas.user.service.converter;

import com.qoobot.openidaas.common.vo.user.UserVO;
import com.qoobot.openidaas.user.entity.User;
import com.qoobot.openidaas.user.entity.UserProfile;
import com.qoobot.openidaas.user.entity.UserDepartment;
import com.qoobot.openidaas.user.entity.UserRole;
import com.qoobot.openidaas.user.mapper.UserDepartmentMapper;
import com.qoobot.openidaas.user.mapper.UserProfileMapper;
import com.qoobot.openidaas.user.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户转换器
 *
 * @author QooBot
 */
@Component
@RequiredArgsConstructor
public class UserConverter {

    private final UserProfileMapper userProfileMapper;
    private final UserDepartmentMapper userDepartmentMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 转换为UserVO
     *
     * @param user 用户实体
     * @return UserVO
     */
    public UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setEmail(user.getEmail());
        vo.setMobile(user.getMobile());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }

    /**
     * 转换为详细信息VO
     *
     * @param user 用户实体
     * @return UserVO
     */
    public UserVO toDetailVO(User user) {
        UserVO vo = toVO(user);

        // 查询用户档案
        UserProfile profile = userProfileMapper.selectById(user.getId());
        if (profile != null) {
            vo.setRealName(profile.getFullName());
            vo.setNickname(profile.getNickname());
            vo.setGender(profile.getGender());
            // vo.setBirthday(profile.getBirthDate()); // UserVO中没有birthday字段
            vo.setAvatar(profile.getAvatarUrl());
            // vo.setEmployeeId(profile.getEmployeeId()); // UserVO中没有employeeId字段
            // vo.setHireDate(profile.getHireDate()); // UserVO中没有hireDate字段
        }

        // 查询用户部门
        List<UserDepartment> departments = userDepartmentMapper.selectByUserId(user.getId());
        if (departments != null && !departments.isEmpty()) {
            List<com.qoobot.openidaas.common.vo.user.UserVO.DepartmentVO> deptVOs = departments.stream().map(ud -> {
                com.qoobot.openidaas.common.vo.user.UserVO.DepartmentVO deptVO = new com.qoobot.openidaas.common.vo.user.UserVO.DepartmentVO();
                deptVO.setId(ud.getDeptId());
                // deptVO.setPositionId(ud.getPositionId()); // DepartmentVO中没有positionId字段
                // deptVO.setIsPrimary(ud.getIsPrimary() == 1); // DepartmentVO中没有isPrimary字段
                // deptVO.setStartDate(ud.getStartDate()); // DepartmentVO中没有startDate字段
                // deptVO.setEndDate(ud.getEndDate()); // DepartmentVO中没有endDate字段
                return deptVO;
            }).collect(Collectors.toList());
            vo.setDepartments(deptVOs);
        }

        // 查询用户角色
        List<UserRole> roles = userRoleMapper.selectByUserId(user.getId());
        if (roles != null && !roles.isEmpty()) {
            List<com.qoobot.openidaas.common.vo.user.UserVO.RoleVO> roleVOs = roles.stream().map(ur -> {
                com.qoobot.openidaas.common.vo.user.UserVO.RoleVO roleVO = new com.qoobot.openidaas.common.vo.user.UserVO.RoleVO();
                roleVO.setId(ur.getRoleId());
                // roleVO.setScopeType(ur.getScopeType()); // RoleVO中没有scopeType字段
                // roleVO.setScopeId(ur.getScopeId()); // RoleVO中没有scopeId字段
                // roleVO.setExpireTime(ur.getExpireTime()); // RoleVO中没有expireTime字段
                return roleVO;
            }).collect(Collectors.toList());
            vo.setRoles(roleVOs);
        }

        return vo;
    }
}
