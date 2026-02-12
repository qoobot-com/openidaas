/*
 * IDaaS系统数据库初始化脚本
 * 数据库版本：MySQL 8.0+
 * 字符集：utf8mb4
 * 排序规则：utf8mb4_unicode_ci
 */

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `open_idaas` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `open_idaas`;

-- ==================== 身份数据域 ====================

-- 用户基本信息表
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(64) NOT NULL COMMENT '用户名（3-20位字母数字下划线）',
  `email` varchar(128) DEFAULT NULL COMMENT '邮箱地址',
  `mobile` varchar(32) DEFAULT NULL COMMENT '手机号码',
  `password_hash` varchar(255) NOT NULL COMMENT '密码哈希值（BCrypt）',
  `password_salt` varchar(64) NOT NULL COMMENT '密码盐值',
  `password_updated_at` timestamp NULL DEFAULT NULL COMMENT '密码最后更新时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '账户状态：1-正常，2-锁定，3-停用，4-删除',
  `failed_login_attempts` int DEFAULT '0' COMMENT '连续登录失败次数',
  `last_login_time` timestamp NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(64) DEFAULT NULL COMMENT '最后登录IP',
  `pwd_reset_required` tinyint DEFAULT '0' COMMENT '是否需要重置密码：0-否，1-是',
  `pwd_reset_time` timestamp NULL DEFAULT NULL COMMENT '密码重置时间',
  `pwd_reset_by` bigint DEFAULT NULL COMMENT '密码重置操作人',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `created_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `updated_by` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_last_login` (`last_login_time`),
  KEY `idx_pwd_reset` (`pwd_reset_required`),
  CONSTRAINT `fk_users_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_users_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_users_pwd_reset_by` FOREIGN KEY (`pwd_reset_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户基本信息表';

-- 用户扩展属性表
CREATE TABLE `user_attributes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `attr_key` varchar(64) NOT NULL COMMENT '属性键',
  `attr_value` longtext COMMENT '属性值',
  `attr_type` enum('STRING','INTEGER','BOOLEAN','DATE','JSON') DEFAULT 'STRING' COMMENT '属性类型',
  `is_sensitive` tinyint DEFAULT '0' COMMENT '是否敏感属性',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_attr` (`user_id`,`attr_key`),
  KEY `idx_attr_key` (`attr_key`),
  CONSTRAINT `fk_user_attributes_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户扩展属性表';

-- 用户档案表
CREATE TABLE `user_profiles` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `avatar_url` varchar(512) DEFAULT NULL COMMENT '头像URL',
  `full_name` varchar(128) DEFAULT NULL COMMENT '真实姓名',
  `nickname` varchar(64) DEFAULT NULL COMMENT '昵称',
  `gender` tinyint DEFAULT NULL COMMENT '性别：1-男，2-女，0-未知',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `id_card` varchar(32) DEFAULT NULL COMMENT '身份证号（加密存储）',
  `employee_id` varchar(64) DEFAULT NULL COMMENT '员工编号',
  `hire_date` date DEFAULT NULL COMMENT '入职日期',
  `emergency_contact` varchar(64) DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(32) DEFAULT NULL COMMENT '紧急联系电话',
  `data_masked` tinyint DEFAULT '0' COMMENT '数据是否已脱敏：0-否，1-是',
  `masked_fields` text COMMENT '脱敏字段记录（JSON格式）',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_hire_date` (`hire_date`),
  KEY `idx_data_masked` (`data_masked`),
  CONSTRAINT `fk_user_profiles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户档案信息表';

-- ==================== 组织架构数据域 ====================

-- 部门表
CREATE TABLE `departments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dept_code` varchar(64) NOT NULL COMMENT '部门编码（大写字母数字下划线）',
  `dept_name` varchar(128) NOT NULL COMMENT '部门名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID，0表示根部门',
  `level_path` varchar(512) NOT NULL COMMENT '层级路径，如：1/2/3',
  `level_depth` int DEFAULT '1' COMMENT '层级深度',
  `sort_order` int DEFAULT '0' COMMENT '排序序号',
  `manager_id` bigint DEFAULT NULL COMMENT '部门经理ID',
  `description` text COMMENT '部门描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，2-禁用',
  `org_chart_url` varchar(512) DEFAULT NULL COMMENT '组织架构图URL',
  `budget_center` varchar(64) DEFAULT NULL COMMENT '预算中心代码',
  `cost_center` varchar(64) DEFAULT NULL COMMENT '成本中心代码',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dept_code` (`dept_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level_path` (`level_path`),
  KEY `idx_status` (`status`),
  KEY `idx_manager_id` (`manager_id`),
  KEY `idx_budget_center` (`budget_center`),
  CONSTRAINT `fk_departments_manager_id` FOREIGN KEY (`manager_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_departments_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_departments_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `chk_dept_code_format` CHECK (`dept_code` REGEXP '^[A-Z][A-Z0-9_]*$'),
  CONSTRAINT `chk_level_depth` CHECK (`level_depth` BETWEEN 1 AND 10)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门信息表';

-- 职位表
CREATE TABLE `positions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `position_code` varchar(64) NOT NULL COMMENT '职位编码（大写字母数字下划线）',
  `position_name` varchar(128) NOT NULL COMMENT '职位名称',
  `dept_id` bigint NOT NULL COMMENT '所属部门ID',
  `level` tinyint DEFAULT NULL COMMENT '职级等级（1-20）',
  `job_grade` varchar(32) DEFAULT NULL COMMENT '职级',
  `reports_to` bigint DEFAULT NULL COMMENT '汇报对象职位ID',
  `description` text COMMENT '职位描述',
  `is_manager` tinyint DEFAULT '0' COMMENT '是否管理岗位',
  `min_salary` decimal(10,2) DEFAULT NULL COMMENT '最低薪资',
  `max_salary` decimal(10,2) DEFAULT NULL COMMENT '最高薪资',
  `headcount_limit` int DEFAULT '1' COMMENT '编制人数限制',
  `current_headcount` int DEFAULT '0' COMMENT '当前在职人数',
  `competency_model` text COMMENT '胜任力模型（JSON格式）',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_position_code` (`position_code`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_reports_to` (`reports_to`),
  KEY `idx_level` (`level`),
  KEY `idx_is_manager` (`is_manager`),
  CONSTRAINT `fk_positions_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `departments` (`id`),
  CONSTRAINT `fk_positions_reports_to` FOREIGN KEY (`reports_to`) REFERENCES `positions` (`id`),
  CONSTRAINT `fk_positions_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_positions_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`),
  CONSTRAINT `chk_position_code_format` CHECK (`position_code` REGEXP '^[A-Z][A-Z0-9_]*$'),
  CONSTRAINT `chk_level_range` CHECK (`level` BETWEEN 1 AND 20),
  CONSTRAINT `chk_salary_range` CHECK (`min_salary` <= `max_salary`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='职位信息表';

-- 用户部门关系表
CREATE TABLE `user_departments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `position_id` bigint DEFAULT NULL COMMENT '职位ID',
  `is_primary` tinyint DEFAULT '1' COMMENT '是否主部门',
  `start_date` date DEFAULT NULL COMMENT '开始日期',
  `end_date` date DEFAULT NULL COMMENT '结束日期',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_dept_period` (`user_id`,`dept_id`,`start_date`,`end_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dept_id` (`dept_id`),
  CONSTRAINT `fk_user_departments_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_departments_dept_id` FOREIGN KEY (`dept_id`) REFERENCES `departments` (`id`),
  CONSTRAINT `fk_user_departments_position_id` FOREIGN KEY (`position_id`) REFERENCES `positions` (`id`),
  CONSTRAINT `fk_user_departments_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户部门关系表';

-- ==================== 权限数据域 ====================

-- 角色表
CREATE TABLE `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(64) NOT NULL COMMENT '角色编码',
  `role_name` varchar(128) NOT NULL COMMENT '角色名称',
  `role_type` tinyint DEFAULT '1' COMMENT '角色类型：1-系统角色，2-自定义角色',
  `parent_id` bigint DEFAULT '0' COMMENT '父角色ID',
  `description` text COMMENT '角色描述',
  `is_builtin` tinyint DEFAULT '0' COMMENT '是否内置角色',
  `enabled` tinyint DEFAULT '1' COMMENT '是否启用：1-启用，0-禁用',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_role_type` (`role_type`),
  CONSTRAINT `fk_roles_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_roles_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色信息表';

-- 权限表
CREATE TABLE `permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `perm_code` varchar(128) NOT NULL COMMENT '权限编码',
  `perm_name` varchar(128) NOT NULL COMMENT '权限名称',
  `resource_type` varchar(64) DEFAULT NULL COMMENT '资源类型：MENU/API/DATA/OPERATION',
  `resource_id` varchar(128) DEFAULT NULL COMMENT '资源标识符',
  `action` varchar(64) DEFAULT NULL COMMENT '操作类型：READ/WRITE/DELETE/ADMIN/EXECUTE',
  `description` text COMMENT '权限描述',
  `is_builtin` tinyint DEFAULT '0' COMMENT '是否内置权限',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_resource_action` (`resource_type`,`resource_id`,`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限信息表';

-- 角色权限关联表
CREATE TABLE `role_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `perm_id` bigint NOT NULL COMMENT '权限ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`,`perm_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_perm_id` (`perm_id`),
  CONSTRAINT `fk_role_permissions_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permissions_perm_id` FOREIGN KEY (`perm_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_permissions_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 用户角色关联表
CREATE TABLE `user_roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `scope_type` tinyint DEFAULT '1' COMMENT '作用域类型：1-全局，2-部门，3-项目，4-应用',
  `scope_id` bigint DEFAULT NULL COMMENT '作用域ID',
  `granted_by` bigint DEFAULT NULL COMMENT '授权人ID',
  `grant_reason` text COMMENT '授权原因',
  `grant_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `expire_time` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `is_temporary` tinyint DEFAULT '0' COMMENT '是否临时授权',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role_scope` (`user_id`,`role_id`,`scope_type`,`scope_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_scope` (`scope_type`,`scope_id`),
  KEY `idx_expire_time` (`expire_time`),
  CONSTRAINT `fk_user_roles_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_roles_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_roles_granted_by` FOREIGN KEY (`granted_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ==================== 认证数据域 ====================

-- 认证令牌表
CREATE TABLE `auth_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `token_type` tinyint NOT NULL COMMENT '令牌类型：1-Access Token，2-Refresh Token，3-ID Token，4-Device Token',
  `token_value` varchar(512) NOT NULL COMMENT '令牌值',
  `client_id` varchar(128) DEFAULT NULL COMMENT '客户端ID',
  `scope` text COMMENT '权限范围',
  `audience` text COMMENT '受众',
  `expires_at` timestamp NOT NULL COMMENT '过期时间',
  `issued_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '签发时间',
  `revoked` tinyint DEFAULT '0' COMMENT '是否已撤销：0-未撤销，1-已撤销',
  `revoke_reason` varchar(255) DEFAULT NULL COMMENT '撤销原因',
  `revoked_at` timestamp NULL DEFAULT NULL COMMENT '撤销时间',
  `last_used_at` timestamp NULL DEFAULT NULL COMMENT '最后使用时间',
  PRIMARY KEY (`id`),
  KEY `idx_token_value` (`token_value`(255)),
  KEY `idx_user_expires` (`user_id`,`expires_at`),
  KEY `idx_client_user` (`client_id`,`user_id`),
  KEY `idx_revoked` (`revoked`),
  CONSTRAINT `fk_auth_tokens_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='认证令牌表';

-- 多因子认证表
CREATE TABLE `mfa_factors` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `factor_type` tinyint NOT NULL COMMENT '认证因素类型：1-短信，2-邮箱，3-TOTP，4-硬件令牌，5-生物识别',
  `factor_value` varchar(255) DEFAULT NULL COMMENT '认证因素值（加密存储）',
  `secret_key` varchar(255) DEFAULT NULL COMMENT '密钥（加密存储）',
  `backup_codes` text COMMENT '备用验证码',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，2-禁用，3-待验证',
  `verified` tinyint DEFAULT '0' COMMENT '是否已验证',
  `verified_at` timestamp NULL DEFAULT NULL COMMENT '验证时间',
  `last_used_at` timestamp NULL DEFAULT NULL COMMENT '最后使用时间',
  `failed_attempts` int DEFAULT '0' COMMENT '失败尝试次数',
  `locked_until` timestamp NULL DEFAULT NULL COMMENT '锁定截止时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_factor` (`user_id`,`factor_type`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_mfa_factors_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多因子认证表';

-- 登录会话表
CREATE TABLE `login_sessions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(128) NOT NULL COMMENT '会话ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `client_ip` varchar(64) DEFAULT NULL COMMENT '客户端IP',
  `user_agent` text COMMENT '用户代理',
  `device_fingerprint` varchar(255) DEFAULT NULL COMMENT '设备指纹',
  `device_type` varchar(32) DEFAULT NULL COMMENT '设备类型',
  `device_os` varchar(64) DEFAULT NULL COMMENT '操作系统',
  `login_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `last_access_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后访问时间',
  `expire_time` timestamp NOT NULL COMMENT '过期时间',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-有效，2-过期，3-强制退出',
  `logout_time` timestamp NULL DEFAULT NULL COMMENT '登出时间',
  `logout_reason` varchar(255) DEFAULT NULL COMMENT '登出原因',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_user_expire` (`user_id`,`expire_time`),
  KEY `idx_status` (`status`),
  KEY `idx_device_fingerprint` (`device_fingerprint`),
  CONSTRAINT `fk_login_sessions_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录会话表';

-- ==================== 应用集成数据域 ====================

-- 应用信息表
CREATE TABLE `applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_key` varchar(128) NOT NULL COMMENT '应用密钥',
  `app_name` varchar(128) NOT NULL COMMENT '应用名称',
  `app_type` tinyint DEFAULT '1' COMMENT '应用类型：1-Web，2-移动，3-API',
  `redirect_uris` text COMMENT '重定向URI列表',
  `logo_url` varchar(512) DEFAULT NULL COMMENT 'Logo URL',
  `homepage_url` varchar(512) DEFAULT NULL COMMENT '主页URL',
  `description` text COMMENT '应用描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，2-禁用',
  `owner_id` bigint DEFAULT NULL COMMENT '所有者ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_key` (`app_key`),
  KEY `idx_app_name` (`app_name`),
  CONSTRAINT `fk_applications_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用信息表';

-- OAuth2客户端表
CREATE TABLE `oauth2_clients` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `client_id` varchar(128) NOT NULL COMMENT '客户端ID',
  `client_secret` varchar(255) NOT NULL COMMENT '客户端密钥',
  `app_id` bigint NOT NULL COMMENT '应用ID',
  `grant_types` text COMMENT '授权类型列表',
  `scopes` text COMMENT '权限范围',
  `access_token_validity` int DEFAULT '3600' COMMENT '访问令牌有效期（秒）',
  `refresh_token_validity` int DEFAULT '2592000' COMMENT '刷新令牌有效期（秒）',
  `auto_approve` tinyint DEFAULT '0' COMMENT '是否自动批准',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_client_id` (`client_id`),
  KEY `idx_app_id` (`app_id`),
  CONSTRAINT `fk_oauth2_clients_app_id` FOREIGN KEY (`app_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OAuth2客户端表';

-- SAML服务提供商表
CREATE TABLE `saml_service_providers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sp_entity_id` varchar(255) NOT NULL COMMENT 'SP实体ID',
  `app_id` bigint NOT NULL COMMENT '应用ID',
  `acs_url` varchar(512) NOT NULL COMMENT '断言消费服务URL',
  `certificate` text COMMENT '证书',
  `metadata_url` varchar(512) DEFAULT NULL COMMENT '元数据URL',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sp_entity_id` (`sp_entity_id`),
  KEY `idx_app_id` (`app_id`),
  CONSTRAINT `fk_saml_service_providers_app_id` FOREIGN KEY (`app_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SAML服务提供商表';

-- ==================== 联邦身份数据域 ====================

-- 外部系统映射表
CREATE TABLE `external_systems` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `system_code` varchar(64) NOT NULL COMMENT '系统编码',
  `system_name` varchar(128) NOT NULL COMMENT '系统名称',
  `system_type` tinyint DEFAULT NULL COMMENT '系统类型：1-HR系统，2-LDAP，3-其他',
  `scim_endpoint` varchar(512) DEFAULT NULL COMMENT 'SCIM端点',
  `api_key` varchar(255) DEFAULT NULL COMMENT 'API密钥',
  `sync_config` text COMMENT 'JSON格式的同步配置',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，2-禁用',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_code` (`system_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='外部系统映射表';

-- 用户同步记录表
CREATE TABLE `user_sync_records` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `external_system_id` bigint NOT NULL COMMENT '外部系统ID',
  `external_user_id` varchar(128) DEFAULT NULL COMMENT '外部用户ID',
  `sync_direction` tinyint DEFAULT NULL COMMENT '同步方向：1-出站，2-入站',
  `sync_status` tinyint DEFAULT '1' COMMENT '状态：1-成功，2-失败，3-待处理',
  `last_sync_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后同步时间',
  `error_message` text COMMENT '错误信息',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_external_system_id` (`external_system_id`),
  KEY `idx_external_mapping` (`external_system_id`,`external_user_id`),
  CONSTRAINT `fk_user_sync_records_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_sync_records_external_system_id` FOREIGN KEY (`external_system_id`) REFERENCES `external_systems` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户同步记录表';

-- 数据权限表
CREATE TABLE `data_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `perm_code` varchar(128) NOT NULL COMMENT '权限编码',
  `perm_name` varchar(128) NOT NULL COMMENT '权限名称',
  `resource_type` varchar(64) DEFAULT NULL COMMENT '资源类型：USER/DEPARTMENT/ROLE/APPLICATION',
  `resource_id` varchar(128) DEFAULT NULL COMMENT '资源标识符',
  `data_scope` tinyint DEFAULT '1' COMMENT '数据范围：1-本人，2-本部门，3-本部门及下属，4-全部',
  `filter_conditions` json COMMENT '过滤条件（JSON格式）',
  `description` text COMMENT '权限描述',
  `is_builtin` tinyint DEFAULT '0' COMMENT '是否内置权限',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_resource_type` (`resource_type`),
  KEY `idx_data_scope` (`data_scope`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据权限表';

-- 角色数据权限关联表
CREATE TABLE `role_data_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `data_perm_id` bigint NOT NULL COMMENT '数据权限ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_data_perm` (`role_id`,`data_perm_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_data_perm_id` (`data_perm_id`),
  CONSTRAINT `fk_role_data_perms_role_id` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_data_perms_perm_id` FOREIGN KEY (`data_perm_id`) REFERENCES `data_permissions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_role_data_perms_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色数据权限关联表';

-- 用户数据权限关联表
CREATE TABLE `user_data_permissions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `data_perm_id` bigint NOT NULL COMMENT '数据权限ID',
  `granted_by` bigint DEFAULT NULL COMMENT '授权人ID',
  `grant_reason` text COMMENT '授权原因',
  `grant_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '授权时间',
  `expire_time` timestamp NULL DEFAULT NULL COMMENT '过期时间',
  `is_temporary` tinyint DEFAULT '0' COMMENT '是否临时授权',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_data_perm` (`user_id`,`data_perm_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_data_perm_id` (`data_perm_id`),
  KEY `idx_expire_time` (`expire_time`),
  CONSTRAINT `fk_user_data_perms_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_data_perms_perm_id` FOREIGN KEY (`data_perm_id`) REFERENCES `data_permissions` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_data_perms_granted_by` FOREIGN KEY (`granted_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户数据权限关联表';

-- 操作日志表
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `log_type` tinyint NOT NULL COMMENT '日志类型：1-登录，2-权限变更，3-数据访问',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `client_ip` varchar(64) DEFAULT NULL COMMENT '客户端IP',
  `user_agent` text COMMENT '用户代理',
  `resource_type` varchar(64) DEFAULT NULL COMMENT '资源类型',
  `resource_id` varchar(128) DEFAULT NULL COMMENT '资源ID',
  `action` varchar(64) DEFAULT NULL COMMENT '操作动作',
  `request_params` text COMMENT '请求参数',
  `response_result` text COMMENT '响应结果',
  `log_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '日志时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`log_time`),
  KEY `idx_username_time` (`username`,`log_time`),
  KEY `idx_resource` (`resource_type`,`resource_id`),
  KEY `idx_log_time` (`log_time`),
  KEY `idx_log_type` (`log_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ==================== 系统配置数据域 ====================

-- 系统配置表
CREATE TABLE `system_configs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(128) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_type` varchar(32) DEFAULT 'STRING' COMMENT '配置类型：STRING/INTEGER/BOOLEAN/JSON',
  `description` text COMMENT '配置描述',
  `category` varchar(64) DEFAULT 'GENERAL' COMMENT '配置分类',
  `is_encrypted` tinyint DEFAULT '0' COMMENT '是否加密存储',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `created_by` bigint DEFAULT NULL,
  `updated_by` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_category` (`category`),
  KEY `idx_config_type` (`config_type`),
  CONSTRAINT `fk_system_configs_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_system_configs_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 安全事件表
CREATE TABLE `security_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `event_type` varchar(64) NOT NULL COMMENT '事件类型',
  `severity` tinyint DEFAULT '2' COMMENT '严重程度：1-低，2-中，3-高，4-紧急',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `username` varchar(64) DEFAULT NULL COMMENT '用户名',
  `ip_address` varchar(64) DEFAULT NULL COMMENT 'IP地址',
  `device_info` text COMMENT '设备信息',
  `event_data` json COMMENT '事件数据',
  `handled` tinyint DEFAULT '0' COMMENT '是否已处理',
  `handle_time` timestamp NULL DEFAULT NULL COMMENT '处理时间',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_event_type` (`event_type`),
  KEY `idx_severity_time` (`severity`,`created_at`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='安全事件表';

-- ==================== 多租户数据域 ====================

-- 租户信息表
CREATE TABLE `tenants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_code` varchar(64) NOT NULL COMMENT '租户编码',
  `tenant_name` varchar(128) NOT NULL COMMENT '租户名称',
  `admin_user_id` bigint DEFAULT NULL COMMENT '管理员用户ID',
  `contact_email` varchar(128) DEFAULT NULL COMMENT '联系邮箱',
  `contact_phone` varchar(32) DEFAULT NULL COMMENT '联系电话',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，2-禁用',
  `quota_config` json COMMENT '配额配置',
  `brand_config` json COMMENT '品牌定制配置',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_code` (`tenant_code`),
  KEY `idx_admin_user_id` (`admin_user_id`),
  CONSTRAINT `fk_tenants_admin_user_id` FOREIGN KEY (`admin_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户信息表';

-- 用户租户关系表
CREATE TABLE `user_tenants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `role_type` tinyint DEFAULT '1' COMMENT '租户内角色：1-普通用户，2-管理员，3-超级管理员',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tenant` (`user_id`,`tenant_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_tenant_id` (`tenant_id`),
  CONSTRAINT `fk_user_tenants_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_tenants_tenant_id` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户租户关系表';

-- ==================== 初始化数据 ====================

-- 插入系统内置权限
INSERT INTO `permissions` (`perm_code`, `perm_name`, `resource_type`, `action`, `description`, `is_builtin`) VALUES
('USER_READ', '用户读取', 'USER', 'READ', '查看用户信息权限', 1),
('USER_WRITE', '用户写入', 'USER', 'WRITE', '创建/修改用户信息权限', 1),
('USER_DELETE', '用户删除', 'USER', 'DELETE', '删除用户权限', 1),
('USER_DEPARTMENT_ASSIGN', '用户部门分配', 'USER', 'ASSIGN', '分配用户部门权限', 1),
('USER_ROLE_ASSIGN', '用户角色分配', 'USER', 'ASSIGN', '分配用户角色权限', 1),
('DEPARTMENT_MANAGE', '部门管理', 'DEPARTMENT', 'MANAGE', '部门增删改查权限', 1),
('POSITION_MANAGE', '职位管理', 'POSITION', 'MANAGE', '职位增删改查权限', 1),
('ROLE_READ', '角色读取', 'ROLE', 'READ', '查看角色信息权限', 1),
('ROLE_WRITE', '角色写入', 'ROLE', 'WRITE', '创建/修改角色权限', 1),
('PERMISSION_READ', '权限读取', 'PERMISSION', 'READ', '查看权限信息权限', 1),
('PERMISSION_WRITE', '权限写入', 'PERMISSION', 'WRITE', '分配权限权限', 1),
('ORG_READ', '组织读取', 'ORGANIZATION', 'READ', '查看组织架构权限', 1),
('ORG_WRITE', '组织写入', 'ORGANIZATION', 'WRITE', '管理组织架构权限', 1),
('PASSWORD_RESET', '密码重置', 'AUTH', 'RESET', '重置用户密码权限', 1),
('AUDIT_READ', '审计读取', 'AUDIT', 'READ', '查看审计日志权限', 1),
('SECURITY_EVENT_READ', '安全事件读取', 'SECURITY', 'READ', '查看安全事件权限', 1),
('SYSTEM_ADMIN', '系统管理', 'SYSTEM', 'ADMIN', '系统管理员权限', 1);

-- 插入数据权限
INSERT INTO `data_permissions` (`perm_code`, `perm_name`, `resource_type`, `data_scope`, `description`, `is_builtin`) VALUES
('DATA_USER_SELF', '用户数据本人可见', 'USER', 1, '只能查看自己的用户数据', 1),
('DATA_USER_DEPT', '用户数据本部门可见', 'USER', 2, '只能查看本部门用户的用户数据', 1),
('DATA_USER_SUB_DEPT', '用户数据本部门及下属可见', 'USER', 3, '可以查看本部门及下属部门的用户数据', 1),
('DATA_USER_ALL', '用户数据全部可见', 'USER', 4, '可以查看所有用户的用户数据', 1),
('DATA_DEPARTMENT_ALL', '部门数据全部可见', 'DEPARTMENT', 4, '可以查看所有部门数据', 1),
('DATA_ROLE_ALL', '角色数据全部可见', 'ROLE', 4, '可以查看所有角色数据', 1);

-- 插入系统角色
INSERT INTO `roles` (`role_code`, `role_name`, `role_type`, `description`, `is_builtin`) VALUES
('SUPER_ADMIN', '超级管理员', 1, '系统超级管理员，拥有所有权限', 1),
('ADMIN', '管理员', 1, '系统管理员，拥有大部分管理权限', 1),
('HR_ADMIN', '人事管理员', 1, '人力资源管理员，负责用户和组织管理', 1),
('DEPT_MANAGER', '部门经理', 1, '部门管理者，可管理部门内用户', 1),
('AUDITOR', '审计员', 1, '系统审计员，可查看审计日志', 1),
('USER', '普通用户', 1, '普通用户角色，基础权限', 1);

-- 为超级管理员分配所有权限
INSERT INTO `role_permissions` (`role_id`, `perm_id`)
SELECT r.id, p.id 
FROM `roles` r, `permissions` p 
WHERE r.role_code = 'SUPER_ADMIN' AND p.is_builtin = 1;

-- 为超级管理员分配所有数据权限
INSERT INTO `role_data_permissions` (`role_id`, `data_perm_id`)
SELECT r.id, dp.id 
FROM `roles` r, `data_permissions` dp 
WHERE r.role_code = 'SUPER_ADMIN' AND dp.is_builtin = 1;

-- 为管理员分配常用权限
INSERT INTO `role_permissions` (`role_id`, `perm_id`)
SELECT r.id, p.id 
FROM `roles` r, `permissions` p 
WHERE r.role_code = 'ADMIN' 
AND p.perm_code IN ('USER_READ', 'USER_WRITE', 'DEPARTMENT_MANAGE', 'POSITION_MANAGE', 
                   'ROLE_READ', 'ROLE_WRITE', 'PERMISSION_READ', 'ORG_READ', 'ORG_WRITE',
                   'AUDIT_READ', 'SECURITY_EVENT_READ');

-- 为人事管理员分配HR相关权限
INSERT INTO `role_permissions` (`role_id`, `perm_id`)
SELECT r.id, p.id 
FROM `roles` r, `permissions` p 
WHERE r.role_code = 'HR_ADMIN' 
AND p.perm_code IN ('USER_READ', 'USER_WRITE', 'USER_DEPARTMENT_ASSIGN', 'USER_ROLE_ASSIGN',
                   'DEPARTMENT_MANAGE', 'POSITION_MANAGE', 'ORG_READ', 'ORG_WRITE');

-- 为部门经理分配部门管理权限
INSERT INTO `role_permissions` (`role_id`, `perm_id`)
SELECT r.id, p.id 
FROM `roles` r, `permissions` p 
WHERE r.role_code = 'DEPT_MANAGER' 
AND p.perm_code IN ('USER_READ', 'ORG_READ');

-- 为部门经理分配数据权限（本部门及下属）
INSERT INTO `role_data_permissions` (`role_id`, `data_perm_id`)
SELECT r.id, dp.id 
FROM `roles` r, `data_permissions` dp 
WHERE r.role_code = 'DEPT_MANAGER' 
AND dp.perm_code IN ('DATA_USER_SUB_DEPT', 'DATA_DEPARTMENT_ALL');

-- 为审计员分配审计权限
INSERT INTO `role_permissions` (`role_id`, `perm_id`)
SELECT r.id, p.id 
FROM `roles` r, `permissions` p 
WHERE r.role_code = 'AUDITOR' 
AND p.perm_code IN ('AUDIT_READ', 'SECURITY_EVENT_READ');

-- 为普通用户分配基础权限
INSERT INTO `role_permissions` (`role_id`, `perm_id`)
SELECT r.id, p.id 
FROM `roles` r, `permissions` p 
WHERE r.role_code = 'USER' 
AND p.perm_code IN ('USER_READ');

-- 为普通用户分配数据权限（仅本人）
INSERT INTO `role_data_permissions` (`role_id`, `data_perm_id`)
SELECT r.id, dp.id 
FROM `roles` r, `data_permissions` dp 
WHERE r.role_code = 'USER' 
AND dp.perm_code = 'DATA_USER_SELF';

-- 插入默认租户
INSERT INTO `tenants` (`tenant_code`, `tenant_name`, `status`) VALUES
('DEFAULT', '默认租户', 1);

-- 插入系统配置参数
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `category`) VALUES
('security.password.min_length', '8', 'INTEGER', '密码最小长度', 'SECURITY'),
('security.password.require_uppercase', 'true', 'BOOLEAN', '密码必须包含大写字母', 'SECURITY'),
('security.password.require_lowercase', 'true', 'BOOLEAN', '密码必须包含小写字母', 'SECURITY'),
('security.password.require_digit', 'true', 'BOOLEAN', '密码必须包含数字', 'SECURITY'),
('security.password.require_special', 'true', 'BOOLEAN', '密码必须包含特殊字符', 'SECURITY'),
('security.login.max_attempts', '5', 'INTEGER', '最大登录失败尝试次数', 'SECURITY'),
('security.login.lockout_duration', '1800', 'INTEGER', '账户锁定持续时间（秒）', 'SECURITY'),
('security.session.timeout', '1800', 'INTEGER', '会话超时时间（秒）', 'SECURITY'),
('security.mfa.enabled', 'true', 'BOOLEAN', '是否启用多因子认证', 'SECURITY'),
('audit.log.retention_days', '365', 'INTEGER', '审计日志保留天数', 'AUDIT'),
('organization.max_level_depth', '10', 'INTEGER', '组织架构最大层级深度', 'ORGANIZATION');

-- 重新启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 创建索引优化
CREATE INDEX `idx_users_status_created` ON `users` (`status`, `created_at`);
CREATE INDEX `idx_users_email_status` ON `users` (`email`, `status`);
CREATE INDEX `idx_users_mobile_status` ON `users` (`mobile`, `status`);
CREATE INDEX `idx_user_roles_user_scope` ON `user_roles` (`user_id`, `scope_type`, `scope_id`);
-- MySQL不支持在CREATE INDEX中使用WHERE条件，改为在查询时使用条件
CREATE INDEX `idx_auth_tokens_user_expire` ON `auth_tokens` (`user_id`, `expires_at`);
CREATE INDEX `idx_auth_tokens_revoked` ON `auth_tokens` (`revoked`);
CREATE INDEX `idx_audit_logs_user_time_desc` ON `audit_logs` (`user_id`, `log_time` DESC);
CREATE INDEX `idx_security_events_severity_time` ON `security_events` (`severity` DESC, `created_at` DESC);

COMMIT;