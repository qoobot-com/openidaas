package com.qoobot.openidaas.user.mapper;

import com.qoobot.openidaas.user.dto.*;
import com.qoobot.openidaas.user.entity.*;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户实体映射接口
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Mapper(
    componentModel = "spring",
    uses = {},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    /**
     * 实体转DTO
     */
    @Mapping(target = "managerName", ignore = true)
    @Mapping(target = "departmentName", ignore = true)
    @Mapping(target = "roleName", ignore = true)
    @Mapping(target = "roleNames", ignore = true)
    UserDTO toDTO(User user);

    /**
     * DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "salt", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDTO dto);

    /**
     * 创建请求转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "salt", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "status", ignore = true)
    User toEntity(CreateUserRequest request);

    /**
     * 部门实体转DTO
     */
    @Mapping(target = "parentName", ignore = true)
    @Mapping(target = "userCount", ignore = true)
    DepartmentDTO toDTO(Department department);

    /**
     * DTO转部门实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Department toEntity(DepartmentDTO dto);

    /**
     * 角色实体转DTO
     */
    @Mapping(target = "userCount", ignore = true)
    RoleDTO toDTO(Role role);

    /**
     * DTO转角色实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    @Mapping(target = "userRoles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Role toEntity(RoleDTO dto);

    /**
     * 权限实体转DTO
     */
    PermissionDTO toDTO(Permission permission);

    /**
     * DTO转权限实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Permission toEntity(PermissionDTO dto);

    /**
     * 列表转换
     */
    List<UserDTO> toUserDTOList(List<User> users);
    List<DepartmentDTO> toDepartmentDTOList(List<Department> departments);
    List<RoleDTO> toRoleDTOList(List<Role> roles);
    List<PermissionDTO> toPermissionDTOList(List<Permission> permissions);

    /**
     * Set转换
     */
    Set<PermissionDTO> toPermissionDTOSet(Set<Permission> permissions);

    /**
     * 默认方法：获取用户角色名称列表
     */
    @AfterMapping
    default void setRoleNames(@MappingTarget UserDTO dto, User user) {
        if (user.getUserRoles() != null) {
            List<String> roleNames = user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toList());
            dto.setRoleNames(roleNames);
            
            if (!roleNames.isEmpty()) {
                dto.setRoleName(roleNames.get(0));
            }
        }
    }
}
