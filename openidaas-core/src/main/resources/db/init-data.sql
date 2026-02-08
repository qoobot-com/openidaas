-- ============================================================================
-- OpenIDaaS 初始数据脚本
-- 创建时间: 2026-02-08
-- ============================================================================

-- 设置时区为 UTC
SET TIME ZONE 'UTC';

-- ============================================================================
-- 默认租户
-- ============================================================================

INSERT INTO tenants (id, name, code, domain, status, config, billing_info) VALUES
(
    '00000000-0000-0000-0000-000000000001',
    'Default Tenant',
    'default',
    'openidaas.example.com',
    'ACTIVE',
    '{
        "max_users": 10000,
        "max_sessions_per_user": 5,
        "password_policy": {
            "min_length": 8,
            "require_uppercase": true,
            "require_lowercase": true,
            "require_numbers": true,
            "require_special_chars": true,
            "password_history": 5,
            "password_expiration_days": 90
        },
        "session_policy": {
            "timeout_minutes": 30,
            "remember_me_days": 30,
            "max_concurrent_sessions": 5
        },
        "mfa_policy": {
            "enabled": false,
            "required_for_admin": true
        }
    }'::jsonb,
    '{
        "plan": "free",
        "max_users": 10000,
        "billing_cycle": "monthly",
        "currency": "USD"
    }'::jsonb
);

-- ============================================================================
-- 默认系统管理员
-- ============================================================================

-- 密码: Admin@123 (BCrypt 加密)
INSERT INTO users (id, tenant_id, username, email, phone, password, status, mfa_enabled, email_verified, phone_verified) VALUES
(
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'admin',
    'admin@openidaas.com',
    '+8613800000000',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ACTIVE',
    FALSE,
    TRUE,
    TRUE
);

-- ============================================================================
-- 默认角色
-- ============================================================================

-- 超级管理员角色
INSERT INTO roles (id, tenant_id, name, code, description, permissions) VALUES
(
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'Super Admin',
    'SUPER_ADMIN',
    '拥有系统所有权限',
    '[
        "user:create",
        "user:read",
        "user:update",
        "user:delete",
        "user:reset_password",
        "role:create",
        "role:read",
        "role:update",
        "role:delete",
        "role:assign",
        "tenant:create",
        "tenant:read",
        "tenant:update",
        "tenant:delete",
        "tenant:config",
        "oauth2:create",
        "oauth2:read",
        "oauth2:update",
        "oauth2:delete",
        "audit:read",
        "audit:export",
        "system:config",
        "system:monitor"
    ]'::jsonb
);

-- 管理员角色
INSERT INTO roles (id, tenant_id, name, code, description, permissions) VALUES
(
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    'Admin',
    'ADMIN',
    '拥有租户管理权限',
    '[
        "user:create",
        "user:read",
        "user:update",
        "user:delete",
        "role:create",
        "role:read",
        "role:update",
        "role:assign",
        "oauth2:read",
        "oauth2:update",
        "audit:read"
    ]'::jsonb
);

-- 普通用户角色
INSERT INTO roles (id, tenant_id, name, code, description, permissions) VALUES
(
    '00000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000001',
    'User',
    'USER',
    '普通用户权限',
    '[
        "user:read_self",
        "user:update_self",
        "user:change_password"
    ]'::jsonb
);

-- ============================================================================
-- 分配默认角色给系统管理员
-- ============================================================================

INSERT INTO user_roles (id, user_id, role_id, assigned_by) VALUES
(
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001'
);

-- ============================================================================
-- 默认 OAuth2 客户端
-- ============================================================================

-- Web 应用客户端
INSERT INTO oauth2_clients (
    id,
    client_id,
    client_secret,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    scopes,
    require_proof_key,
    require_authorization_consent
) VALUES
(
    '00000000-0000-0000-0000-000000000001',
    'openidaas-web-client',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'OpenIDaaS Web Client',
    '["client_secret_basic"]'::jsonb,
    '["authorization_code", "refresh_token"]'::jsonb,
    '["http://localhost:8080/login/oauth2/code/openidaas", "https://openidaas.example.com/login/oauth2/code/openidaas"]'::jsonb,
    '["openid", "profile", "email", "phone"]'::jsonb,
    FALSE,
    FALSE
);

-- 移动应用客户端 (使用 PKCE)
INSERT INTO oauth2_clients (
    id,
    client_id,
    client_secret,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    scopes,
    require_proof_key,
    require_authorization_consent
) VALUES
(
    '00000000-0000-0000-0000-000000000002',
    'openidaas-mobile-client',
    '',
    'OpenIDaaS Mobile Client',
    '["none"]'::jsonb,
    '["authorization_code", "refresh_token"]'::jsonb,
    '["openidaas://oauth/callback", "openidaas://oauth/authorize"]'::jsonb,
    '["openid", "profile", "email"]'::jsonb,
    TRUE,
    FALSE
);

-- 服务账号客户端 (使用 client_credentials)
INSERT INTO oauth2_clients (
    id,
    client_id,
    client_secret,
    client_name,
    client_authentication_methods,
    authorization_grant_types,
    redirect_uris,
    scopes,
    require_proof_key,
    require_authorization_consent
) VALUES
(
    '00000000-0000-0000-0000-000000000003',
    'openidaas-service-account',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'OpenIDaaS Service Account',
    '["client_secret_basic"]'::jsonb,
    '["client_credentials"]'::jsonb,
    '[]'::jsonb,
    '["service:access"]'::jsonb,
    FALSE,
    FALSE
);

-- ============================================================================
-- 默认权限
-- ============================================================================

-- 用户管理权限
INSERT INTO permissions (code, name, resource, action, description) VALUES
('user:create', '创建用户', 'user', 'create', '创建新用户');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('user:read', '查看用户', 'user', 'read', '查看用户信息');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('user:update', '更新用户', 'user', 'update', '更新用户信息');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('user:delete', '删除用户', 'user', 'delete', '删除用户');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('user:reset_password', '重置密码', 'user', 'reset_password', '重置用户密码');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('user:read_self', '查看自己的信息', 'user', 'read_self', '查看自己的用户信息');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('user:update_self', '更新自己的信息', 'user', 'update_self', '更新自己的用户信息');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('user:change_password', '修改密码', 'user', 'change_password', '修改自己的密码');

-- 角色管理权限
INSERT INTO permissions (code, name, resource, action, description) VALUES
('role:create', '创建角色', 'role', 'create', '创建新角色');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('role:read', '查看角色', 'role', 'read', '查看角色信息');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('role:update', '更新角色', 'role', 'update', '更新角色信息');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('role:delete', '删除角色', 'role', 'delete', '删除角色');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('role:assign', '分配角色', 'role', 'assign', '给用户分配角色');

-- 租户管理权限
INSERT INTO permissions (code, name, resource, action, description) VALUES
('tenant:create', '创建租户', 'tenant', 'create', '创建新租户');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('tenant:read', '查看租户', 'tenant', 'read', '查看租户信息');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('tenant:update', '更新租户', 'tenant', 'update', '更新租户信息');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('tenant:delete', '删除租户', 'tenant', 'delete', '删除租户');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('tenant:config', '配置租户', 'tenant', 'config', '配置租户参数');

-- OAuth2 客户端权限
INSERT INTO permissions (code, name, resource, action, description) VALUES
('oauth2:create', '创建客户端', 'oauth2', 'create', '创建 OAuth2 客户端');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('oauth2:read', '查看客户端', 'oauth2', 'read', '查看 OAuth2 客户端');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('oauth2:update', '更新客户端', 'oauth2', 'update', '更新 OAuth2 客户端');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('oauth2:delete', '删除客户端', 'oauth2', 'delete', '删除 OAuth2 客户端');

-- 审计日志权限
INSERT INTO permissions (code, name, resource, action, description) VALUES
('audit:read', '查看审计日志', 'audit', 'read', '查看审计日志');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('audit:export', '导出审计日志', 'audit', 'export', '导出审计日志');

-- 系统配置权限
INSERT INTO permissions (code, name, resource, action, description) VALUES
('system:config', '系统配置', 'system', 'config', '配置系统参数');
INSERT INTO permissions (code, name, resource, action, description) VALUES
('system:monitor', '系统监控', 'system', 'monitor', '监控系统运行状态');

-- ============================================================================
-- 测试用户数据 (用于开发和测试)
-- ============================================================================

-- 创建测试用户
INSERT INTO users (id, tenant_id, username, email, phone, password, status, mfa_enabled, email_verified, phone_verified) VALUES
(
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000001',
    'testuser',
    'testuser@openidaas.com',
    '+8613800000001',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ACTIVE',
    FALSE,
    TRUE,
    TRUE
);

-- 为测试用户分配普通用户角色
INSERT INTO user_roles (id, user_id, role_id, assigned_by) VALUES
(
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000002',
    '00000000-0000-0000-0000-000000000003',
    '00000000-0000-0000-0000-000000000001'
);

-- ============================================================================
-- 初始审计日志
-- ============================================================================

INSERT INTO audit_logs (tenant_id, user_id, action, resource, ip_address, user_agent, details, result, duration_ms) VALUES
(
    '00000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'SYSTEM_INIT',
    'system',
    '127.0.0.1',
    'OpenIDaaS System',
    '{"message": "System initialization completed"}'::jsonb,
    'SUCCESS',
    0
);

-- ============================================================================
-- 初始密码历史
-- ============================================================================

INSERT INTO password_history (user_id, password) VALUES
(
    '00000000-0000-0000-0000-000000000001',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
);

-- ============================================================================
-- 数据初始化完成
-- ============================================================================

-- 显示初始化统计信息
SELECT '=== Initial Data Summary ===' AS info;

SELECT
    'Tenants' AS type,
    COUNT(*) AS count
FROM tenants
UNION ALL
SELECT
    'Users' AS type,
    COUNT(*) AS count
FROM users
UNION ALL
SELECT
    'Roles' AS type,
    COUNT(*) AS count
FROM roles
UNION ALL
SELECT
    'User Roles' AS type,
    COUNT(*) AS count
FROM user_roles
UNION ALL
SELECT
    'OAuth2 Clients' AS type,
    COUNT(*) AS count
FROM oauth2_clients
UNION ALL
SELECT
    'Permissions' AS type,
    COUNT(*) AS count
FROM permissions;

-- 默认管理员凭证
SELECT '=== Default Admin Credentials ===' AS info;
SELECT
    'Username: admin' AS credentials;
SELECT
    'Password: Admin@123' AS credentials;
SELECT
    'Email: admin@openidaas.com' AS credentials;
