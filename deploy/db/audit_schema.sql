/*
 * 审计日志数据库初始化脚本
 * 数据库版本：MySQL 8.0+
 * 字符集：utf8mb4
 * 排序规则：utf8mb4_unicode_ci
 */

USE `open_idaas`;

-- ==================== 审计日志数据域 ====================

-- 审计日志表
DROP TABLE IF EXISTS `audit_logs`;
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型：CREATE/READ/UPDATE/DELETE/EXPORT/IMPORT/APPROVE/REJECT/LOGIN/LOGOUT',
  `operation_desc` varchar(200) DEFAULT NULL COMMENT '操作描述',
  `module` varchar(50) DEFAULT NULL COMMENT '操作模块',
  `sub_module` varchar(50) DEFAULT NULL COMMENT '操作子模块',
  `target_type` varchar(50) DEFAULT NULL COMMENT '目标对象类型',
  `target_id` bigint DEFAULT NULL COMMENT '目标对象ID',
  `target_name` varchar(100) DEFAULT NULL COMMENT '目标对象名称',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_params` text COMMENT '请求参数（JSON格式）',
  `response_result` text COMMENT '响应结果（JSON格式）',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人名称',
  `operator_ip` varchar(50) DEFAULT NULL COMMENT '操作人IP地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理信息',
  `operation_time` datetime NOT NULL COMMENT '操作时间',
  `execution_time` bigint DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `result` varchar(20) DEFAULT NULL COMMENT '操作结果：SUCCESS/FAILURE/PARTIAL',
  `error_message` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `app_id` bigint DEFAULT NULL COMMENT '应用ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_operation_time` (`operation_time`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_operator_id_time` (`operator_id`, `operation_time`),
  KEY `idx_tenant_id` (`tenant_id`),
  KEY `idx_tenant_id_time` (`tenant_id`, `operation_time`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_module` (`module`),
  KEY `idx_result` (`result`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';

-- 插入示例数据
INSERT INTO `audit_logs` (
  `operation_type`, `operation_desc`, `module`, `sub_module`,
  `target_type`, `target_id`, `target_name`,
  `operator_id`, `operator_name`, `operator_ip`,
  `operation_time`, `execution_time`, `result`
) VALUES
('LOGIN', '用户登录', 'AUTH', 'LOGIN',
 'USER', 1, 'admin',
 1, 'admin', '192.168.1.100',
 NOW() - INTERVAL 1 DAY, 120, 'SUCCESS'),
('CREATE', '创建用户', 'USER', 'USER_MANAGEMENT',
 'USER', 2, 'testuser',
 1, 'admin', '192.168.1.100',
 NOW() - INTERVAL 2 HOUR, 350, 'SUCCESS'),
('UPDATE', '更新用户信息', 'USER', 'USER_MANAGEMENT',
 'USER', 2, 'testuser',
 1, 'admin', '192.168.1.100',
 NOW() - INTERVAL 1 HOUR, 280, 'SUCCESS'),
('DELETE', '删除角色', 'ROLE', 'ROLE_MANAGEMENT',
 'ROLE', 5, 'temp_role',
 1, 'admin', '192.168.1.100',
 NOW() - INTERVAL 30 MINUTE, 180, 'SUCCESS'),
('LOGIN', '用户登录失败', 'AUTH', 'LOGIN',
 'USER', 3, 'hacker',
 3, 'hacker', '192.168.1.200',
 NOW() - INTERVAL 10 MINUTE, 85, 'FAILURE');
