-- 角色表
CREATE TABLE IF NOT EXISTS `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称',
    `role_type` INT NOT NULL DEFAULT 2 COMMENT '角色类型：1-系统角色，2-自定义角色',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父角色ID',
    `description` VARCHAR(500) COMMENT '角色描述',
    `is_builtin` INT NOT NULL DEFAULT 0 COMMENT '是否内置角色',
    `enabled` INT NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `created_by` VARCHAR(50) COMMENT '创建人',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) COMMENT '更新人',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_role_code` (`role_code`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS `permissions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `perm_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    `perm_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `perm_type` VARCHAR(20) NOT NULL COMMENT '权限类型：menu, button, api',
    `path` VARCHAR(255) COMMENT '权限路径',
    `method` VARCHAR(10) COMMENT '请求方法',
    `description` VARCHAR(500) COMMENT '权限描述',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID',
    `icon` VARCHAR(50) COMMENT '图标',
    `component` VARCHAR(255) COMMENT '组件路径',
    `external_link` INT NOT NULL DEFAULT 0 COMMENT '是否外链',
    `hidden` INT NOT NULL DEFAULT 0 COMMENT '是否隐藏',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `enabled` INT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `is_builtin` INT NOT NULL DEFAULT 0 COMMENT '是否内置权限',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_perm_code` (`perm_code`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permissions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `perm_id` BIGINT NOT NULL COMMENT '权限ID',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_role_perm` (`role_id`, `perm_id`),
    INDEX `idx_role_id` (`role_id`),
    INDEX `idx_perm_id` (`perm_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `scope_type` INT COMMENT '作用域类型',
    `scope_id` BIGINT COMMENT '作用域ID',
    `granted_by` BIGINT COMMENT '授予人',
    `grant_reason` VARCHAR(255) COMMENT '授予原因',
    `grant_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '授予时间',
    `expire_time` TIMESTAMP NULL COMMENT '过期时间',
    `is_temporary` INT NOT NULL DEFAULT 0 COMMENT '是否临时角色',
    UNIQUE KEY `uk_user_role_scope` (`user_id`, `role_id`, `scope_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 初始化测试数据
INSERT INTO `roles` (`role_code`, `role_name`, `role_type`, `parent_id`, `description`, `is_builtin`, `enabled`, `sort_order`) VALUES
('SUPER_ADMIN', '超级管理员', 1, 0, '系统超级管理员，拥有所有权限', 1, 1, 1),
('ADMIN', '管理员', 1, 0, '系统管理员', 1, 1, 2),
('USER', '普通用户', 1, 0, '普通用户', 1, 1, 3),
('DEPT_MANAGER', '部门经理', 2, 0, '部门经理角色', 0, 1, 4),
('TEST_ROLE', '测试角色', 2, 0, '用于测试的角色', 0, 1, 5);

INSERT INTO `permissions` (`perm_code`, `perm_name`, `perm_type`, `path`, `method`, `description`, `enabled`, `is_builtin`) VALUES
('user:view', '查看用户', 'button', '/api/users', 'GET', '查看用户列表', 1, 1),
('user:create', '创建用户', 'button', '/api/users', 'POST', '创建用户', 1, 1),
('user:update', '更新用户', 'button', '/api/users', 'PUT', '更新用户', 1, 1),
('user:delete', '删除用户', 'button', '/api/users', 'DELETE', '删除用户', 1, 1),
('role:view', '查看角色', 'button', '/api/roles', 'GET', '查看角色列表', 1, 1),
('role:create', '创建角色', 'button', '/api/roles', 'POST', '创建角色', 1, 1),
('role:update', '更新角色', 'button', '/api/roles', 'PUT', '更新角色', 1, 1),
('role:delete', '删除角色', 'button', '/api/roles', 'DELETE', '删除角色', 1, 1);

INSERT INTO `role_permissions` (`role_id`, `perm_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8),
(2, 1), (2, 2), (2, 3), (2, 5), (2, 6), (2, 7),
(3, 1);

INSERT INTO `user_roles` (`user_id`, `role_id`, `scope_type`, `scope_id`) VALUES
(1, 1, 1, 1),
(2, 2, 1, 1),
(3, 3, 1, 1);
