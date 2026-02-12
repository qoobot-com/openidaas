-- H2测试数据库初始化脚本
DROP TABLE IF EXISTS audit_logs;

CREATE TABLE audit_logs (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  operation_type VARCHAR(50) NOT NULL,
  operation_desc VARCHAR(200),
  module VARCHAR(50),
  sub_module VARCHAR(50),
  target_type VARCHAR(50),
  target_id BIGINT,
  target_name VARCHAR(100),
  request_url VARCHAR(500),
  request_method VARCHAR(10),
  request_params TEXT,
  response_result TEXT,
  operator_id BIGINT,
  operator_name VARCHAR(50),
  operator_ip VARCHAR(50),
  user_agent VARCHAR(500),
  operation_time TIMESTAMP NOT NULL,
  execution_time BIGINT,
  result VARCHAR(20),
  error_message VARCHAR(500),
  tenant_id BIGINT,
  app_id BIGINT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 插入测试数据
INSERT INTO audit_logs (
  operation_type, operation_desc, module, sub_module,
  target_type, target_id, target_name,
  operator_id, operator_name, operator_ip,
  operation_time, execution_time, result
) VALUES
('LOGIN', '用户登录', 'AUTH', 'LOGIN',
 'USER', 1, 'admin',
 1, 'admin', '192.168.1.100',
 TIMESTAMPADD(DAY, -1, CURRENT_TIMESTAMP), 120, 'SUCCESS'),
('CREATE', '创建用户', 'USER', 'USER_MANAGEMENT',
 'USER', 2, 'testuser',
 1, 'admin', '192.168.1.100',
 TIMESTAMPADD(HOUR, -2, CURRENT_TIMESTAMP), 350, 'SUCCESS'),
('UPDATE', '更新用户信息', 'USER', 'USER_MANAGEMENT',
 'USER', 2, 'testuser',
 1, 'admin', '192.168.1.100',
 TIMESTAMPADD(HOUR, -1, CURRENT_TIMESTAMP), 280, 'SUCCESS'),
('DELETE', '删除角色', 'ROLE', 'ROLE_MANAGEMENT',
 'ROLE', 5, 'temp_role',
 1, 'admin', '192.168.1.100',
 TIMESTAMPADD(MINUTE, -30, CURRENT_TIMESTAMP), 180, 'SUCCESS'),
('LOGIN', '用户登录失败', 'AUTH', 'LOGIN',
 'USER', 3, 'hacker',
 3, 'hacker', '192.168.1.200',
 TIMESTAMPADD(MINUTE, -10, CURRENT_TIMESTAMP), 85, 'FAILURE');
