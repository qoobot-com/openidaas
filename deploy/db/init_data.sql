/*
 * IDaaS系统初始化数据脚本
 * 用于插入默认用户、组织结构等基础数据
 * 
 * 注意：此脚本应在 schema.sql 执行后运行
 */

USE `open_idaas`;

-- ==================== 初始化租户 ====================
INSERT INTO `tenants` (`tenant_code`, `tenant_name`, `contact_email`, `status`) VALUES
('DEFAULT', '默认租户', 'admin@openidaas.com', 1)
ON DUPLICATE KEY UPDATE `tenant_name` = `tenant_name`;

-- ==================== 初始化组织架构 ====================

-- 插入根部门
INSERT INTO `departments` (`dept_code`, `dept_name`, `parent_id`, `level_path`, `level_depth`, `sort_order`, `status`) VALUES
('ROOT', 'QooBot科技', 0, '1', 1, 0, 1)
ON DUPLICATE KEY UPDATE `dept_name` = VALUES(`dept_name`);

-- 获取根部门ID（假设ID为1，如果已存在则查询）
SET @root_dept_id = (SELECT id FROM departments WHERE dept_code = 'ROOT' LIMIT 1);

-- 插入一级部门
INSERT INTO `departments` (`dept_code`, `dept_name`, `parent_id`, `level_path`, `level_depth`, `sort_order`, `status`) VALUES
('TECH', '技术中心', @root_dept_id, CONCAT(@root_dept_id, '/1'), 2, 1, 1),
('PRODUCT', '产品中心', @root_dept_id, CONCAT(@root_dept_id, '/2'), 2, 2, 1),
('HR', '人力资源部', @root_dept_id, CONCAT(@root_dept_id, '/3'), 2, 3, 1),
('FINANCE', '财务部', @root_dept_id, CONCAT(@root_dept_id, '/4'), 2, 4, 1),
('SALES', '销售部', @root_dept_id, CONCAT(@root_dept_id, '/5'), 2, 5, 1)
ON DUPLICATE KEY UPDATE `dept_name` = VALUES(`dept_name`);

-- 获取技术中心部门ID
SET @tech_dept_id = (SELECT id FROM departments WHERE dept_code = 'TECH' LIMIT 1);
SET @product_dept_id = (SELECT id FROM departments WHERE dept_code = 'PRODUCT' LIMIT 1);
SET @hr_dept_id = (SELECT id FROM departments WHERE dept_code = 'HR' LIMIT 1);
SET @finance_dept_id = (SELECT id FROM departments WHERE dept_code = 'FINANCE' LIMIT 1);
SET @sales_dept_id = (SELECT id FROM departments WHERE dept_code = 'SALES' LIMIT 1);

-- 插入技术中心下的二级部门
INSERT INTO `departments` (`dept_code`, `dept_name`, `parent_id`, `level_path`, `level_depth`, `sort_order`, `status`) VALUES
('TECH_DEV', '研发部', @tech_dept_id, CONCAT(@tech_dept_id, '/1'), 3, 1, 1),
('TECH_TEST', '测试部', @tech_dept_id, CONCAT(@tech_dept_id, '/2'), 3, 2, 1),
('TECH_OPS', '运维部', @tech_dept_id, CONCAT(@tech_dept_id, '/3'), 3, 3, 1)
ON DUPLICATE KEY UPDATE `dept_name` = VALUES(`dept_name`);

-- 获取二级部门ID
SET @dev_dept_id = (SELECT id FROM departments WHERE dept_code = 'TECH_DEV' LIMIT 1);
SET @test_dept_id = (SELECT id FROM departments WHERE dept_code = 'TECH_TEST' LIMIT 1);
SET @ops_dept_id = (SELECT id FROM departments WHERE dept_code = 'TECH_OPS' LIMIT 1);

-- 插入研发部下的三级部门
INSERT INTO `departments` (`dept_code`, `dept_name`, `parent_id`, `level_path`, `level_depth`, `sort_order`, `status`) VALUES
('DEV_BACKEND', '后端开发组', @dev_dept_id, CONCAT(@dev_dept_id, '/1'), 4, 1, 1),
('DEV_FRONTEND', '前端开发组', @dev_dept_id, CONCAT(@dev_dept_id, '/2'), 4, 2, 1),
('DEV_MOBILE', '移动开发组', @dev_dept_id, CONCAT(@dev_dept_id, '/3'), 4, 3, 1)
ON DUPLICATE KEY UPDATE `dept_name` = VALUES(`dept_name`);

-- ==================== 初始化职位 ====================

-- 技术中心职位
INSERT INTO `positions` (`position_code`, `position_name`, `dept_id`, `level`, `job_grade`, `is_manager`, `description`) VALUES
('CTO', '首席技术官', @tech_dept_id, 20, 'P20', 1, '公司技术负责人'),
('ARCHITECT', '系统架构师', @dev_dept_id, 18, 'P18', 0, '系统架构设计'),
('TECH_LEAD', '技术主管', @dev_dept_id, 16, 'P16', 1, '技术团队负责人'),
('SENIOR_DEV', '高级开发工程师', @dev_dept_id, 14, 'P14', 0, '高级开发人员'),
('DEV_ENGINEER', '开发工程师', @dev_dept_id, 12, 'P12', 0, '软件开发人员'),
('JUNIOR_DEV', '初级开发工程师', @dev_dept_id, 10, 'P10', 0, '初级开发人员')
ON DUPLICATE KEY UPDATE `position_name` = VALUES(`position_name`);

-- 人力资源部职位
INSERT INTO `positions` (`position_code`, `position_name`, `dept_id`, `level`, `job_grade`, `is_manager`, `description`) VALUES
('HR_DIRECTOR', '人力资源总监', @hr_dept_id, 18, 'P18', 1, '人力资源负责人'),
('HR_MANAGER', '人事经理', @hr_dept_id, 16, 'P16', 1, '人事经理'),
('HR_SPECIALIST', '人事专员', @hr_dept_id, 12, 'P12', 0, '人事专员')
ON DUPLICATE KEY UPDATE `position_name` = VALUES(`position_name`);

-- 获取职位ID
SET @cto_position_id = (SELECT id FROM positions WHERE position_code = 'CTO' LIMIT 1);
SET @architect_position_id = (SELECT id FROM positions WHERE position_code = 'ARCHITECT' LIMIT 1);
SET @hr_director_id = (SELECT id FROM positions WHERE position_code = 'HR_DIRECTOR' LIMIT 1);

-- ==================== 初始化用户 ====================

-- 生成BCrypt密码哈希 (密码: Admin@123)
-- $2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
SET @default_password_hash = '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
SET @default_salt = 'default_salt';

-- 插入系统管理员
INSERT INTO `users` (`username`, `email`, `password_hash`, `password_salt`, `status`) VALUES
('admin', 'admin@openidaas.com', @default_password_hash, @default_salt, 1)
ON DUPLICATE KEY UPDATE `password_hash` = VALUES(`password_hash`);

SET @admin_user_id = (SELECT id FROM users WHERE username = 'admin' LIMIT 1);

-- 插入管理员档案
INSERT INTO `user_profiles` (`user_id`, `full_name`, `nickname`, `gender`, `employee_id`, `hire_date`) VALUES
(@admin_user_id, '系统管理员', 'Admin', 0, 'EMP001', CURDATE())
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

-- 插入其他测试用户
INSERT INTO `users` (`username`, `email`, `mobile`, `password_hash`, `password_salt`, `status`) VALUES
('hr_admin', 'hr_admin@openidaas.com', '13800138001', @default_password_hash, @default_salt, 1),
('dept_manager', 'dept_manager@openidaas.com', '13800138002', @default_password_hash, @default_salt, 1),
('developer', 'developer@openidaas.com', '13800138003', @default_password_hash, @default_salt, 1),
('auditor', 'auditor@openidaas.com', '13800138004', @default_password_hash, @default_salt, 1),
('test_user', 'test_user@openidaas.com', '13800138005', @default_password_hash, @default_salt, 1)
ON DUPLICATE KEY UPDATE `password_hash` = VALUES(`password_hash`);

-- 获取用户ID
SET @hr_admin_id = (SELECT id FROM users WHERE username = 'hr_admin' LIMIT 1);
SET @dept_manager_id = (SELECT id FROM users WHERE username = 'dept_manager' LIMIT 1);
SET @developer_id = (SELECT id FROM users WHERE username = 'developer' LIMIT 1);
SET @auditor_id = (SELECT id FROM users WHERE username = 'auditor' LIMIT 1);
SET @test_user_id = (SELECT id FROM users WHERE username = 'test_user' LIMIT 1);

-- 插入用户档案
INSERT INTO `user_profiles` (`user_id`, `full_name`, `nickname`, `gender`, `employee_id`, `hire_date`) VALUES
(@hr_admin_id, '人事管理员', 'HR Admin', 2, 'EMP002', CURDATE()),
(@dept_manager_id, '部门经理', 'Manager', 1, 'EMP003', CURDATE()),
(@developer_id, '开发工程师', 'Dev', 1, 'EMP004', CURDATE()),
(@auditor_id, '审计员', 'Auditor', 0, 'EMP005', CURDATE()),
(@test_user_id, '测试用户', 'Test User', 1, 'EMP006', CURDATE())
ON DUPLICATE KEY UPDATE `full_name` = VALUES(`full_name`);

-- ==================== 关联用户与部门职位 ====================

-- 管理员关联到根部门（CTO职位）
INSERT INTO `user_departments` (`user_id`, `dept_id`, `position_id`, `is_primary`, `start_date`) VALUES
(@admin_user_id, @root_dept_id, @cto_position_id, 1, CURDATE())
ON DUPLICATE KEY UPDATE `position_id` = VALUES(`position_id`);

-- HR管理员关联到人力资源部
INSERT INTO `user_departments` (`user_id`, `dept_id`, `position_id`, `is_primary`, `start_date`) VALUES
(@hr_admin_id, @hr_dept_id, @hr_director_id, 1, CURDATE())
ON DUPLICATE KEY UPDATE `position_id` = VALUES(`position_id`);

-- 部门经理关联到研发部（技术主管职位）
SET @tech_lead_position_id = (SELECT id FROM positions WHERE position_code = 'TECH_LEAD' LIMIT 1);
INSERT INTO `user_departments` (`user_id`, `dept_id`, `position_id`, `is_primary`, `start_date`) VALUES
(@dept_manager_id, @dev_dept_id, @tech_lead_position_id, 1, CURDATE())
ON DUPLICATE KEY UPDATE `position_id` = VALUES(`position_id`);

-- 开发人员关联到研发部
SET @dev_engineer_id = (SELECT id FROM positions WHERE position_code = 'DEV_ENGINEER' LIMIT 1);
INSERT INTO `user_departments` (`user_id`, `dept_id`, `position_id`, `is_primary`, `start_date`) VALUES
(@developer_id, @dev_dept_id, @dev_engineer_id, 1, CURDATE())
ON DUPLICATE KEY UPDATE `position_id` = VALUES(`position_id`);

-- 审计员关联到根部门
SET @senior_dev_id = (SELECT id FROM positions WHERE position_code = 'SENIOR_DEV' LIMIT 1);
INSERT INTO `user_departments` (`user_id`, `dept_id`, `position_id`, `is_primary`, `start_date`) VALUES
(@auditor_id, @root_dept_id, @senior_dev_id, 1, CURDATE())
ON DUPLICATE KEY UPDATE `position_id` = VALUES(`position_id`);

-- 测试用户关联到研发部
INSERT INTO `user_departments` (`user_id`, `dept_id`, `position_id`, `is_primary`, `start_date`) VALUES
(@test_user_id, @dev_dept_id, @dev_engineer_id, 1, CURDATE())
ON DUPLICATE KEY UPDATE `position_id` = VALUES(`position_id`);

-- ==================== 关联用户与租户 ====================

SET @default_tenant_id = (SELECT id FROM tenants WHERE tenant_code = 'DEFAULT' LIMIT 1);

INSERT INTO `user_tenants` (`user_id`, `tenant_id`, `role_type`) VALUES
(@admin_user_id, @default_tenant_id, 3),  -- 超级管理员
(@hr_admin_id, @default_tenant_id, 2),   -- 管理员
(@dept_manager_id, @default_tenant_id, 2), -- 管理员
(@developer_id, @default_tenant_id, 1),  -- 普通用户
(@auditor_id, @default_tenant_id, 1),    -- 普通用户
(@test_user_id, @default_tenant_id, 1)   -- 普通用户
ON DUPLICATE KEY UPDATE `role_type` = VALUES(`role_type`);

-- ==================== 为用户分配角色 ====================

-- 获取角色ID
SET @super_admin_role_id = (SELECT id FROM roles WHERE role_code = 'SUPER_ADMIN' LIMIT 1);
SET @admin_role_id = (SELECT id FROM roles WHERE role_code = 'ADMIN' LIMIT 1);
SET @hr_admin_role_id = (SELECT id FROM roles WHERE role_code = 'HR_ADMIN' LIMIT 1);
SET @dept_manager_role_id = (SELECT id FROM roles WHERE role_code = 'DEPT_MANAGER' LIMIT 1);
SET @auditor_role_id = (SELECT id FROM roles WHERE role_code = 'AUDITOR' LIMIT 1);
SET @user_role_id = (SELECT id FROM roles WHERE role_code = 'USER' LIMIT 1);

-- 管理员分配超级管理员角色
INSERT INTO `user_roles` (`user_id`, `role_id`, `scope_type`, `grant_time`) VALUES
(@admin_user_id, @super_admin_role_id, 1, NOW())
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- HR管理员分配HR管理员角色
INSERT INTO `user_roles` (`user_id`, `role_id`, `scope_type`, `grant_time`) VALUES
(@hr_admin_id, @hr_admin_role_id, 1, NOW())
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- 部门经理分配部门经理角色
INSERT INTO `user_roles` (`user_id`, `role_id`, `scope_type`, `scope_id`, `grant_time`) VALUES
(@dept_manager_id, @dept_manager_role_id, 2, @dev_dept_id, NOW())
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- 开发人员分配普通用户角色
INSERT INTO `user_roles` (`user_id`, `role_id`, `scope_type`, `grant_time`) VALUES
(@developer_id, @user_role_id, 1, NOW())
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- 审计员分配审计员角色
INSERT INTO `user_roles` (`user_id`, `role_id`, `scope_type`, `grant_time`) VALUES
(@auditor_id, @auditor_role_id, 1, NOW())
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- 测试用户分配普通用户角色
INSERT INTO `user_roles` (`user_id`, `role_id`, `scope_type`, `grant_time`) VALUES
(@test_user_id, @user_role_id, 1, NOW())
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- ==================== 初始化应用 ====================

INSERT INTO `applications` (`app_key`, `app_name`, `app_type`, `homepage_url`, `description`, `status`) VALUES
('openidaas-admin', 'IDaaS管理后台', 1, 'http://localhost:3000', 'IDaaS系统管理控制台', 1),
('openidaas-portal', 'IDaaS用户门户', 1, 'http://localhost:3001', '用户自助服务门户', 1),
('demo-app-1', '示例应用1', 1, 'http://demo.example.com', '演示应用1', 1),
('demo-app-2', '示例应用2', 3, 'http://api.example.com', '演示API应用', 1)
ON DUPLICATE KEY UPDATE `app_name` = VALUES(`app_name`);

-- 获取应用ID
SET @admin_app_id = (SELECT id FROM applications WHERE app_key = 'openidaas-admin' LIMIT 1);
SET @portal_app_id = (SELECT id FROM applications WHERE app_key = 'openidaas-portal' LIMIT 1);

-- 插入OAuth2客户端配置
INSERT INTO `oauth2_clients` (`client_id`, `client_secret`, `app_id`, `grant_types`, `scopes`, `auto_approve`) VALUES
('admin-client', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', @admin_app_id, 
 'authorization_code,password,refresh_token,client_credentials', 'openid,profile,email', 0),
('portal-client', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', @portal_app_id,
 'authorization_code,refresh_token', 'openid,profile,email', 0)
ON DUPLICATE KEY UPDATE `client_secret` = VALUES(`client_secret`);

-- ==================== 初始化外部系统映射 ====================

INSERT INTO `external_systems` (`system_code`, `system_name`, `system_type`, `status`) VALUES
('LDAP_CORP', '企业LDAP', 2, 1),
('HR_SYSTEM', 'HR系统', 1, 1),
('ACTIVE_DIRECTORY', 'AD域', 2, 1)
ON DUPLICATE KEY UPDATE `system_name` = VALUES(`system_name`);

-- ==================== 额外系统配置 ====================

INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `category`) VALUES
('system.name', 'QooBot IDaaS', 'STRING', '系统名称', 'GENERAL'),
('system.logo', '/assets/logo.png', 'STRING', '系统Logo路径', 'GENERAL'),
('system.version', '1.0.0', 'STRING', '系统版本', 'GENERAL'),
('token.access_validity', '7200', 'INTEGER', '访问令牌有效期（秒）', 'AUTH'),
('token.refresh_validity', '604800', 'INTEGER', '刷新令牌有效期（秒）', 'AUTH'),
('password.reset_token_validity', '3600', 'INTEGER', '密码重置令牌有效期（秒）', 'AUTH'),
('session.concurrent_limit', '3', 'INTEGER', '同一用户最大并发会话数', 'SECURITY'),
('notification.email.enabled', 'true', 'BOOLEAN', '启用邮件通知', 'NOTIFICATION'),
('notification.sms.enabled', 'true', 'BOOLEAN', '启用短信通知', 'NOTIFICATION'),
('backup.auto_enabled', 'true', 'BOOLEAN', '启用自动备份', 'MAINTENANCE'),
('backup.retention_days', '30', 'INTEGER', '备份保留天数', 'MAINTENANCE')
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`);

-- ==================== 插入审计日志（初始化记录） ====================

INSERT INTO `audit_logs` (`log_type`, `user_id`, `username`, `action`, `resource_type`, `log_time`) VALUES
(3, @admin_user_id, 'admin', '系统初始化', 'SYSTEM', NOW()),
(2, @admin_user_id, 'admin', '创建用户', 'USER', NOW()),
(2, @admin_user_id, 'admin', '创建角色', 'ROLE', NOW()),
(2, @admin_user_id, 'admin', '创建部门', 'DEPARTMENT', NOW()),
(2, @admin_user_id, 'admin', '创建权限', 'PERMISSION', NOW());

-- ==================== 显示初始化摘要 ====================
SELECT '========================================' AS '';
SELECT 'IDaaS系统数据初始化完成！' AS '';
SELECT '========================================' AS '';
SELECT CONCAT('总用户数: ', COUNT(*)) AS '用户统计' FROM users;
SELECT CONCAT('总部门数: ', COUNT(*)) AS '部门统计' FROM departments;
SELECT CONCAT('总职位数: ', COUNT(*)) AS '职位统计' FROM positions;
SELECT CONCAT('总角色数: ', COUNT(*)) AS '角色统计' FROM roles;
SELECT CONCAT('总权限数: ', COUNT(*)) AS '权限统计' FROM permissions;
SELECT CONCAT('总应用数: ', COUNT(*)) AS '应用统计' FROM applications;
SELECT '========================================' AS '';
SELECT '默认管理员账户:' AS '';
SELECT '  用户名: admin' AS '';
SELECT '  密码: Admin@123' AS '';
SELECT '  邮箱: admin@openidaas.com' AS '';
SELECT '========================================' AS '';

COMMIT;
