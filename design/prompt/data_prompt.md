# IDaaS系统数据架构设计提示词

## 数据架构总体要求

作为资深架构师，请为基于MySQL数据库的IDaaS系统设计完整的企业级数据架构。系统需要支撑大规模用户并发访问，满足高可用、高性能、高安全性的数据管理需求，同时确保数据一致性、完整性和可扩展性。

## 核心数据域设计

### 1. 身份数据域（Identity Data Domain）

**用户核心数据表：**
```sql
-- 用户基本信息表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(128) UNIQUE COMMENT '邮箱',
    mobile VARCHAR(32) UNIQUE COMMENT '手机号',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    status TINYINT DEFAULT 1 COMMENT '账户状态：1-正常，2-锁定，3-停用，4-删除',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_mobile (mobile)
);

-- 用户扩展属性表
CREATE TABLE user_attributes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    attr_key VARCHAR(64) NOT NULL,
    attr_value TEXT,
    attr_type VARCHAR(32) DEFAULT 'STRING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_attr (user_id, attr_key)
);

-- 用户档案表
CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY,
    avatar_url VARCHAR(512),
    full_name VARCHAR(128),
    nickname VARCHAR(64),
    gender TINYINT COMMENT '性别：1-男，2-女，0-未知',
    birth_date DATE,
    department_id BIGINT,
    position_id BIGINT,
    hire_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### 2. 组织架构数据域（Organization Data Domain）

**组织结构表：**
```sql
-- 部门表
CREATE TABLE departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    dept_code VARCHAR(64) UNIQUE NOT NULL,
    dept_name VARCHAR(128) NOT NULL,
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID，0表示根部门',
    level_path VARCHAR(512) COMMENT '层级路径，如：1/2/3',
    sort_order INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id),
    INDEX idx_level_path (level_path)
);

-- 职位表
CREATE TABLE positions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    position_code VARCHAR(64) UNIQUE NOT NULL,
    position_name VARCHAR(128) NOT NULL,
    dept_id BIGINT NOT NULL,
    level TINYINT COMMENT '职级等级',
    reports_to BIGINT COMMENT '汇报对象职位ID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (dept_id) REFERENCES departments(id),
    FOREIGN KEY (reports_to) REFERENCES positions(id)
);

-- 用户部门关系表
CREATE TABLE user_departments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    is_primary TINYINT DEFAULT 1 COMMENT '是否主部门',
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (dept_id) REFERENCES departments(id),
    UNIQUE KEY uk_user_dept (user_id, dept_id)
);
```

### 3. 权限数据域（Authorization Data Domain）

**权限模型表：**
```sql
-- 角色表
CREATE TABLE roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code VARCHAR(64) UNIQUE NOT NULL,
    role_name VARCHAR(128) NOT NULL,
    role_type TINYINT DEFAULT 1 COMMENT '角色类型：1-系统角色，2-自定义角色',
    parent_id BIGINT DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
);

-- 权限表
CREATE TABLE permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    perm_code VARCHAR(128) UNIQUE NOT NULL,
    perm_name VARCHAR(128) NOT NULL,
    resource_type VARCHAR(64) COMMENT '资源类型：MENU/API/DATA',
    resource_id VARCHAR(128) COMMENT '资源标识',
    action VARCHAR(64) COMMENT '操作类型：READ/WRITE/DELETE/ADMIN',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 角色权限关联表
CREATE TABLE role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id BIGINT NOT NULL,
    perm_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (perm_id) REFERENCES permissions(id) ON DELETE CASCADE,
    UNIQUE KEY uk_role_perm (role_id, perm_id)
);

-- 用户角色关联表
CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    scope_type TINYINT DEFAULT 1 COMMENT '作用域：1-全局，2-部门，3-项目',
    scope_id BIGINT COMMENT '作用域ID',
    granted_by BIGINT COMMENT '授权人ID',
    grant_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expire_time TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (granted_by) REFERENCES users(id),
    UNIQUE KEY uk_user_role_scope (user_id, role_id, scope_type, scope_id)
);
```

### 4. 认证数据域（Authentication Data Domain）

**认证相关表：**
```sql
-- 认证令牌表
CREATE TABLE auth_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_type TINYINT NOT NULL COMMENT '令牌类型：1-Access Token，2-Refresh Token，3-ID Token',
    token_value VARCHAR(512) NOT NULL,
    client_id VARCHAR(128),
    scope TEXT,
    expires_at TIMESTAMP NOT NULL,
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    revoked TINYINT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_token_value (token_value),
    INDEX idx_user_expires (user_id, expires_at),
    INDEX idx_client_user (client_id, user_id)
);

-- 多因子认证表
CREATE TABLE mfa_factors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    factor_type TINYINT NOT NULL COMMENT '认证因素类型：1-短信，2-邮箱，3-TOTP，4-硬件令牌',
    factor_value VARCHAR(255),
    secret_key VARCHAR(255),
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用，2-禁用',
    verified TINYINT DEFAULT 0 COMMENT '是否已验证',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_factor (user_id, factor_type)
);

-- 登录会话表
CREATE TABLE login_sessions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(128) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    client_ip VARCHAR(64),
    user_agent TEXT,
    device_fingerprint VARCHAR(255),
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_access_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expire_time TIMESTAMP NOT NULL,
    status TINYINT DEFAULT 1 COMMENT '状态：1-有效，2-过期，3-强制退出',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_session_id (session_id),
    INDEX idx_user_expire (user_id, expire_time)
);
```

### 5. 应用集成数据域（Application Integration Data Domain）

**应用管理表：**
```sql
-- 应用信息表
CREATE TABLE applications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    app_key VARCHAR(128) UNIQUE NOT NULL,
    app_name VARCHAR(128) NOT NULL,
    app_type TINYINT DEFAULT 1 COMMENT '应用类型：1-Web，2-移动，3-API',
    redirect_uris TEXT,
    logo_url VARCHAR(512),
    homepage_url VARCHAR(512),
    description TEXT,
    status TINYINT DEFAULT 1,
    owner_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES users(id),
    INDEX idx_app_key (app_key)
);

-- OAuth2客户端表
CREATE TABLE oauth2_clients (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    client_id VARCHAR(128) UNIQUE NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    app_id BIGINT NOT NULL,
    grant_types TEXT COMMENT '授权类型列表',
    scopes TEXT COMMENT '权限范围',
    access_token_validity INT DEFAULT 3600,
    refresh_token_validity INT DEFAULT 2592000,
    auto_approve TINYINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE,
    INDEX idx_client_id (client_id)
);

-- SAML服务提供商表
CREATE TABLE saml_service_providers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    sp_entity_id VARCHAR(255) UNIQUE NOT NULL,
    app_id BIGINT NOT NULL,
    acs_url VARCHAR(512) NOT NULL,
    certificate TEXT,
    metadata_url VARCHAR(512),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (app_id) REFERENCES applications(id) ON DELETE CASCADE
);
```

## 数据架构设计要点

### 6. 联邦身份数据域（Federation Data Domain）

**SCIM同步表：**
```sql
-- 外部系统映射表
CREATE TABLE external_systems (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    system_code VARCHAR(64) UNIQUE NOT NULL,
    system_name VARCHAR(128) NOT NULL,
    system_type TINYINT COMMENT '系统类型：1-HR系统，2-LDAP，3-其他',
    scim_endpoint VARCHAR(512),
    api_key VARCHAR(255),
    sync_config TEXT COMMENT 'JSON格式的同步配置',
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 用户同步记录表
CREATE TABLE user_sync_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    external_system_id BIGINT NOT NULL,
    external_user_id VARCHAR(128),
    sync_direction TINYINT COMMENT '同步方向：1-出站，2-入站',
    sync_status TINYINT DEFAULT 1 COMMENT '状态：1-成功，2-失败，3-待处理',
    last_sync_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (external_system_id) REFERENCES external_systems(id),
    INDEX idx_external_mapping (external_system_id, external_user_id)
);
```

### 7. 安全审计数据域（Security Audit Data Domain）

**审计日志表：**
```sql
-- 操作日志表
CREATE TABLE audit_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    log_type TINYINT NOT NULL COMMENT '日志类型：1-登录，2-权限变更，3-数据访问',
    user_id BIGINT,
    client_ip VARCHAR(64),
    user_agent TEXT,
    resource_type VARCHAR(64),
    resource_id VARCHAR(128),
    action VARCHAR(64),
    request_params TEXT,
    response_result TEXT,
    log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_time (user_id, log_time),
    INDEX idx_resource (resource_type, resource_id),
    INDEX idx_log_time (log_time)
);

-- 安全事件表
CREATE TABLE security_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_type VARCHAR(64) NOT NULL,
    severity TINYINT DEFAULT 2 COMMENT '严重程度：1-低，2-中，3-高，4-紧急',
    user_id BIGINT,
    ip_address VARCHAR(64),
    device_info TEXT,
    event_data JSON,
    handled TINYINT DEFAULT 0,
    handle_time TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_event_type (event_type),
    INDEX idx_severity_time (severity, created_at)
);
```

### 8. 多租户数据域（Multi-tenant Data Domain）

**租户管理表：**
```sql
-- 租户信息表
CREATE TABLE tenants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_code VARCHAR(64) UNIQUE NOT NULL,
    tenant_name VARCHAR(128) NOT NULL,
    admin_user_id BIGINT,
    contact_email VARCHAR(128),
    contact_phone VARCHAR(32),
    status TINYINT DEFAULT 1,
    quota_config JSON COMMENT '配额配置',
    brand_config JSON COMMENT '品牌定制配置',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_user_id) REFERENCES users(id),
    INDEX idx_tenant_code (tenant_code)
);

-- 用户租户关系表
CREATE TABLE user_tenants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    role_type TINYINT DEFAULT 1 COMMENT '租户内角色：1-普通用户，2-管理员，3-超级管理员',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_tenant (user_id, tenant_id)
);
```

## 数据架构优化策略

### 性能优化设计

**索引优化：**
- 关键查询字段建立复合索引
- 避免过多索引影响写入性能
- 定期分析索引使用情况

**分区策略：**
- 大表按时间或租户进行分区
- 审计日志表按月分区
- 用户会话表按过期时间分区

**读写分离：**
- 主库处理写操作
- 从库处理读操作
- 关键业务数据强一致性读取

### 数据安全设计

**敏感数据保护：**
- 密码等敏感字段加密存储
- 个人信息脱敏处理
- 数据传输全程HTTPS加密

**访问控制：**
- 数据库用户权限最小化
- 应用层数据访问控制
- 操作审计日志完整记录

### 高可用设计

**备份策略：**
- 每日全量备份
- 每小时增量备份
- 异地灾备存储

**故障恢复：**
- 主从自动切换
- 数据损坏检测
- 快速恢复机制

请基于以上数据架构要求，输出完整的数据架构设计方案，包括：
1. 数据库ER图设计
2. 分库分表策略
3. 数据同步与一致性保障方案
4. 性能优化具体实施方案
5. 数据安全与合规性设计