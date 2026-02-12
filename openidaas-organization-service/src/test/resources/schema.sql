-- 部门表
CREATE TABLE IF NOT EXISTS `departments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `dept_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '部门编码',
    `dept_name` VARCHAR(100) NOT NULL COMMENT '部门名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID',
    `level_path` VARCHAR(500) COMMENT '层级路径',
    `level_depth` INT NOT NULL DEFAULT 1 COMMENT '层级深度',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `manager_id` BIGINT COMMENT '部门经理ID',
    `description` VARCHAR(500) COMMENT '部门描述',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，2-禁用',
    `created_by` VARCHAR(50) COMMENT '创建人',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_by` VARCHAR(50) COMMENT '更新人',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_dept_code` (`dept_code`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_level_path` (`level_path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 职位表
CREATE TABLE IF NOT EXISTS `positions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `position_code` VARCHAR(50) NOT NULL UNIQUE COMMENT '职位编码',
    `position_name` VARCHAR(100) NOT NULL COMMENT '职位名称',
    `dept_id` BIGINT COMMENT '所属部门ID',
    `level` INT NOT NULL DEFAULT 1 COMMENT '职级等级',
    `job_grade` VARCHAR(50) COMMENT '职级',
    `reports_to` BIGINT COMMENT '汇报对象职位ID',
    `is_manager` INT NOT NULL DEFAULT 0 COMMENT '是否管理岗位',
    `description` VARCHAR(500) COMMENT '职位描述',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_position_code` (`position_code`),
    INDEX `idx_dept_id` (`dept_id`),
    INDEX `idx_reports_to` (`reports_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='职位表';

-- 用户部门关联表
CREATE TABLE IF NOT EXISTS `user_departments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `dept_id` BIGINT NOT NULL COMMENT '部门ID',
    `position_id` BIGINT COMMENT '职位ID',
    `is_primary` INT NOT NULL DEFAULT 1 COMMENT '是否主要部门',
    `start_date` DATE COMMENT '入职日期',
    `end_date` DATE COMMENT '离职日期',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_user_dept_position` (`user_id`, `dept_id`, `position_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户部门关联表';

-- 初始化测试数据
-- 创建部门树
INSERT INTO `departments` (`dept_code`, `dept_name`, `parent_id`, `level_path`, `level_depth`, `sort_order`, `description`, `status`) VALUES
('ROOT', 'QooBot科技', 0, '/ROOT/', 1, 1, '根公司', 1),
('TECH', '技术中心', 1, '/ROOT/TECH/', 2, 10, '技术中心', 1),
('DEV', '研发部', 2, '/ROOT/TECH/DEV/', 3, 11, '研发部', 1),
('BACKEND', '后端组', 3, '/ROOT/TECH/DEV/BACKEND/', 4, 12, '后端开发组', 1),
('FRONTEND', '前端组', 3, '/ROOT/TECH/DEV/FRONTEND/', 4, 13, '前端开发组', 1),
('TEST', '测试部', 2, '/ROOT/TECH/TEST/', 3, 20, '测试部', 1),
('PRODUCT', '产品中心', 1, '/ROOT/PRODUCT/', 2, 30, '产品中心', 1),
('HR', '人力资源部', 1, '/ROOT/HR/', 2, 40, '人力资源部', 1);

-- 创建职位
INSERT INTO `positions` (`position_code`, `position_name`, `dept_id`, `level`, `job_grade`, `is_manager`, `description`) VALUES
('CTO', '首席技术官', 2, 20, 'P20', 1, 'CTO'),
('ARCHITECT', '系统架构师', 3, 18, 'P18', 0, '系统架构师'),
('TECH_LEAD', '技术主管', 3, 16, 'P16', 1, '技术主管'),
('SENIOR_DEV', '高级开发', 3, 14, 'P14', 0, '高级开发'),
('DEV', '开发工程师', 3, 12, 'P12', 0, '开发工程师'),
('JUNIOR_DEV', '初级开发', 4, 10, 'P10', 0, '初级开发'),
('TEST_LEAD', '测试主管', 6, 16, 'P16', 1, '测试主管'),
('TESTER', '测试工程师', 6, 12, 'P12', 0, '测试工程师'),
('PM', '产品经理', 7, 14, 'P14', 0, '产品经理'),
('HR_DIRECTOR', 'HR总监', 8, 18, 'P18', 1, 'HR总监'),
('HR_MANAGER', 'HR经理', 8, 16, 'P16', 1, 'HR经理'),
('HR_SPECIALIST', 'HR专员', 8, 12, 'P12', 0, 'HR专员');

-- 创建用户部门关联（用于测试删除限制）
INSERT INTO `user_departments` (`user_id`, `dept_id`, `position_id`, `is_primary`, `start_date`) VALUES
(1, 1, 1, 1, CURDATE()),
(2, 3, 4, 1, CURDATE()),
(3, 4, 6, 1, CURDATE());
