package com.qoobot.openidaas.user.service;

import com.qoobot.openidaas.user.dto.DepartmentDTO;
import com.qoobot.openidaas.user.entity.Department;
import com.qoobot.openidaas.user.entity.DepartmentStatus;
import com.qoobot.openidaas.user.exception.*;
import com.qoobot.openidaas.user.mapper.UserMapper;
import com.qoobot.openidaas.user.repository.DepartmentRepository;
import com.qoobot.openidaas.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门服务类
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * 创建部门
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentDTO createDepartment(DepartmentDTO dto) {
        log.info("Creating department: {}", dto.getName());

        // 检查编码是否已存在
        if (dto.getCode() != null && departmentRepository.findByCode(dto.getCode()).isPresent()) {
            throw new DepartmentAlreadyExistsException("Department code already exists: " + dto.getCode());
        }

        // 设置层级
        if (dto.getParentId() != null) {
            Department parent = departmentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new DepartmentNotFoundException("Parent department not found"));
            dto.setLevel(parent.getLevel() + 1);
        } else {
            dto.setLevel(1);
        }

        Department department = userMapper.toEntity(dto);
        Department saved = departmentRepository.save(department);

        log.info("Department created successfully: {}", saved.getId());
        return userMapper.toDTO(saved);
    }

    /**
     * 更新部门
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "departments", allEntries = true)
    public DepartmentDTO updateDepartment(Long id, DepartmentDTO dto) {
        log.info("Updating department: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + id));

        department.setName(dto.getName());
        department.setCode(dto.getCode());
        department.setDescription(dto.getDescription());
        department.setLeaderId(dto.getLeaderId());
        department.setLeaderName(dto.getLeaderName());
        department.setPhone(dto.getPhone());
        department.setEmail(dto.getEmail());
        department.setAddress(dto.getAddress());
        department.setStatus(dto.getStatus());
        department.setSortOrder(dto.getSortOrder());

        Department updated = departmentRepository.save(department);
        return userMapper.toDTO(updated);
    }

    /**
     * 删除部门
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "departments", allEntries = true)
    public void deleteDepartment(Long id) {
        log.info("Deleting department: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + id));

        // 检查是否有子部门
        if (departmentRepository.existsByParentId(id)) {
            throw new DepartmentHasChildrenException("Cannot delete department with children");
        }

        // 检查是否有用户
        if (userRepository.existsByDepartmentId(id)) {
            throw new DepartmentHasUsersException("Cannot delete department with users");
        }

        departmentRepository.deleteById(id);
    }

    /**
     * 获取部门详情
     */
    @Cacheable(value = "departments", key = "#id")
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + id));
        return userMapper.toDTO(department);
    }

    /**
     * 获取部门树
     */
    public List<DepartmentDTO> getDepartmentTree(Long tenantId) {
        List<Department> allDepts = departmentRepository.findDepartmentTree(tenantId);
        List<DepartmentDTO> rootDepts = allDepts.stream()
                .filter(d -> d.getParentId() == null)
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        buildDepartmentTree(rootDepts, allDepts);
        return rootDepts;
    }

    /**
     * 构建部门树
     */
    private void buildDepartmentTree(List<DepartmentDTO> parentDepts, List<Department> allDepts) {
        for (DepartmentDTO parent : parentDepts) {
            List<DepartmentDTO> children = allDepts.stream()
                    .filter(d -> parent.getId().equals(d.getParentId()))
                    .map(userMapper::toDTO)
                    .collect(Collectors.toList());

            if (!children.isEmpty()) {
                parent.setChildren(children);
                buildDepartmentTree(children, allDepts);
            }
        }
    }

    /**
     * 根据租户获取部门列表
     */
    public List<DepartmentDTO> getDepartmentsByTenant(Long tenantId) {
        List<Department> departments = departmentRepository.findByTenantId(tenantId);
        return departments.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 移动部门
     */
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "departments", allEntries = true)
    public void moveDepartment(Long deptId, Long newParentId) {
        log.info("Moving department {} to parent {}", deptId, newParentId);

        Department department = departmentRepository.findById(deptId)
                .orElseThrow(() -> new DepartmentNotFoundException("Department not found: " + deptId));

        // 检查是否移动到自己的子部门
        if (isDescendant(deptId, newParentId)) {
            throw new InvalidDepartmentMoveException("Cannot move department to its descendant");
        }

        if (newParentId != null) {
            Department newParent = departmentRepository.findById(newParentId)
                    .orElseThrow(() -> new DepartmentNotFoundException("Parent department not found"));
            department.setParentId(newParentId);
            department.setLevel(newParent.getLevel() + 1);
        } else {
            department.setParentId(null);
            department.setLevel(1);
        }

        departmentRepository.save(department);

        // 更新子部门层级
        updateChildrenLevel(deptId, department.getLevel());
    }

    /**
     * 检查是否是后代部门
     */
    private boolean isDescendant(Long deptId, Long ancestorId) {
        if (ancestorId == null) {
            return false;
        }

        Department current = departmentRepository.findById(ancestorId).orElse(null);
        while (current != null && current.getParentId() != null) {
            if (current.getParentId().equals(deptId)) {
                return true;
            }
            current = departmentRepository.findById(current.getParentId()).orElse(null);
        }
        return false;
    }

    /**
     * 更新子部门层级
     */
    private void updateChildrenLevel(Long parentId, int parentLevel) {
        List<Department> children = departmentRepository.findByParentId(parentId);
        for (Department child : children) {
            child.setLevel(parentLevel + 1);
            departmentRepository.save(child);
            updateChildrenLevel(child.getId(), child.getLevel());
        }
    }
}
