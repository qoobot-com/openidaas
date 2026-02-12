-- 用户表
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  email VARCHAR(128),
  mobile VARCHAR(32),
  password_hash VARCHAR(255) NOT NULL,
  password_salt VARCHAR(64) NOT NULL,
  password_updated_at TIMESTAMP,
  status TINYINT NOT NULL DEFAULT 1,
  failed_login_attempts INT DEFAULT 0,
  last_login_time TIMESTAMP,
  last_login_ip VARCHAR(64),
  pwd_reset_required TINYINT DEFAULT 0,
  pwd_reset_time TIMESTAMP,
  pwd_reset_by BIGINT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by BIGINT,
  updated_by BIGINT
);

-- 用户档案表
CREATE TABLE IF NOT EXISTS user_profiles (
  user_id BIGINT PRIMARY KEY,
  avatar_url VARCHAR(512),
  full_name VARCHAR(128),
  nickname VARCHAR(64),
  gender TINYINT,
  birth_date DATE,
  id_card VARCHAR(32),
  employee_id VARCHAR(64),
  hire_date DATE,
  emergency_contact VARCHAR(64),
  emergency_phone VARCHAR(32),
  data_masked TINYINT DEFAULT 0,
  masked_fields TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户属性表
CREATE TABLE IF NOT EXISTS user_attributes (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  attr_key VARCHAR(64) NOT NULL,
  attr_value TEXT,
  attr_type VARCHAR(20) DEFAULT 'STRING',
  is_sensitive TINYINT DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 部门表
CREATE TABLE IF NOT EXISTS departments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  dept_code VARCHAR(64) NOT NULL,
  dept_name VARCHAR(128) NOT NULL,
  parent_id BIGINT DEFAULT 0,
  level_path VARCHAR(512) NOT NULL,
  level_depth INT DEFAULT 1,
  sort_order INT DEFAULT 0,
  manager_id BIGINT,
  description TEXT,
  status TINYINT DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 职位表
CREATE TABLE IF NOT EXISTS positions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  position_code VARCHAR(64) NOT NULL,
  position_name VARCHAR(128) NOT NULL,
  dept_id BIGINT NOT NULL,
  level TINYINT,
  job_grade VARCHAR(32),
  reports_to BIGINT,
  description TEXT,
  is_manager TINYINT DEFAULT 0,
  min_salary DECIMAL(10,2),
  max_salary DECIMAL(10,2),
  headcount_limit INT DEFAULT 1,
  current_headcount INT DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户部门关系表
CREATE TABLE IF NOT EXISTS user_departments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  dept_id BIGINT NOT NULL,
  position_id BIGINT,
  is_primary TINYINT DEFAULT 1,
  start_date DATE,
  end_date DATE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色表
CREATE TABLE IF NOT EXISTS roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(128) NOT NULL,
  role_type TINYINT DEFAULT 1,
  parent_id BIGINT DEFAULT 0,
  description TEXT,
  is_builtin TINYINT DEFAULT 0,
  status TINYINT DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户角色关系表
CREATE TABLE IF NOT EXISTS user_roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  scope_type TINYINT DEFAULT 1,
  scope_id BIGINT,
  granted_by BIGINT,
  grant_reason TEXT,
  grant_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expire_time TIMESTAMP,
  is_temporary TINYINT DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 初始化测试数据
INSERT INTO departments (id, dept_code, dept_name, parent_id, level_path, level_depth, status) VALUES
(1, 'ROOT', 'QooBot科技', 0, '1', 1, 1),
(2, 'TECH', '技术中心', 1, '1/2', 2, 1);

INSERT INTO positions (id, position_code, position_name, dept_id, level, job_grade, is_manager) VALUES
(1, 'CTO', '首席技术官', 1, 20, 'P20', 1),
(2, 'SENIOR_DEV', '高级开发工程师', 2, 14, 'P14', 0);

INSERT INTO roles (id, role_code, role_name, role_type, is_builtin, status) VALUES
(1, 'ADMIN', '管理员', 1, 1, 1),
(2, 'USER', '普通用户', 1, 1, 1);

INSERT INTO users (id, username, email, mobile, password_hash, password_salt, status) VALUES
(1, 'admin', 'admin@test.com', '13800000001', '$2a$12$test_hash', 'salt1', 1),
(2, 'testuser', 'test@test.com', '13800000002', '$2a$12$test_hash', 'salt2', 1);

INSERT INTO user_profiles (user_id, full_name, nickname, gender, employee_id) VALUES
(1, '系统管理员', 'Admin', 0, 'EMP001'),
(2, '测试用户', 'Test', 1, 'EMP002');

INSERT INTO user_departments (user_id, dept_id, position_id, is_primary) VALUES
(1, 1, 1, 1),
(2, 2, 2, 1);

INSERT INTO user_roles (user_id, role_id, scope_type, grant_time) VALUES
(1, 1, 1, CURRENT_TIMESTAMP),
(2, 2, 1, CURRENT_TIMESTAMP);
