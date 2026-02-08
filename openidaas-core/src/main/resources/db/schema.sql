-- ============================================================================
-- OpenIDaaS 核心数据库表结构
-- 数据库: PostgreSQL 15+
-- 字符集: UTF8
-- 时区: UTC
-- 创建时间: 2026-02-08
-- ============================================================================

-- 设置时区为 UTC
SET TIME ZONE 'UTC';

-- ============================================================================
-- 枚举类型定义
-- ============================================================================

-- 用户状态枚举
CREATE TYPE user_status AS ENUM ('ACTIVE', 'INACTIVE', 'LOCKED', 'DELETED');

-- 租户状态枚举
CREATE TYPE tenant_status AS ENUM ('ACTIVE', 'INACTIVE', 'SUSPENDED');

-- Token 状态枚举
CREATE TYPE token_status AS ENUM ('ACTIVE', 'REVOKED', 'EXPIRED');

-- ============================================================================
-- 租户相关表
-- ============================================================================

-- 租户表
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(128) NOT NULL,
    code VARCHAR(64) NOT NULL,
    domain VARCHAR(255),
    status tenant_status NOT NULL DEFAULT 'ACTIVE',
    config JSONB DEFAULT '{}'::jsonb,
    billing_info JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);

-- ============================================================================
-- 用户相关表
-- ============================================================================

-- 用户表
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(128),
    phone VARCHAR(32),
    password VARCHAR(255) NOT NULL,
    status user_status NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    login_count INTEGER DEFAULT 0,
    deleted_at TIMESTAMP,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    mfa_secret VARCHAR(255),
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE
);

-- 角色表
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(32) NOT NULL,
    description TEXT,
    permissions JSONB DEFAULT '[]'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 用户角色关联表
CREATE TABLE user_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID,
    expires_at TIMESTAMP
);

-- ============================================================================
-- 认证相关表
-- ============================================================================

-- OAuth2 客户端表
CREATE TABLE oauth2_clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id VARCHAR(255) UNIQUE NOT NULL,
    client_secret VARCHAR(255) NOT NULL,
    client_name VARCHAR(255) NOT NULL,
    client_authentication_methods JSONB NOT NULL,
    authorization_grant_types JSONB NOT NULL,
    redirect_uris JSONB NOT NULL,
    scopes JSONB NOT NULL,
    require_proof_key BOOLEAN DEFAULT FALSE,
    require_authorization_consent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Token 表
CREATE TABLE tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token_value TEXT NOT NULL,
    token_type VARCHAR(32) NOT NULL,
    user_id UUID,
    client_id UUID NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status token_status NOT NULL DEFAULT 'ACTIVE',
    tenant_id UUID NOT NULL
);

-- ============================================================================
-- 审计相关表
-- ============================================================================

-- 审计日志表 (按月分区)
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID,
    action VARCHAR(64) NOT NULL,
    resource VARCHAR(255),
    ip_address VARCHAR(45),
    user_agent TEXT,
    details JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    result VARCHAR(32) NOT NULL,
    duration_ms INTEGER
) PARTITION BY RANGE (created_at);

-- ============================================================================
-- 会话管理表
-- ============================================================================

-- 用户会话表
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    tenant_id UUID NOT NULL,
    session_token VARCHAR(255) UNIQUE NOT NULL,
    ip_address VARCHAR(45),
    user_agent TEXT,
    device_type VARCHAR(32),
    device_id VARCHAR(128),
    location JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(32) DEFAULT 'ACTIVE'
);

-- ============================================================================
-- 权限管理表
-- ============================================================================

-- 权限表
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(128) NOT NULL,
    resource VARCHAR(128) NOT NULL,
    action VARCHAR(32) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 密码安全表
-- ============================================================================

-- 密码历史表
CREATE TABLE password_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    password VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 密码重置令牌表
CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 双因素认证表
-- ============================================================================

-- 备用验证码表
CREATE TABLE backup_codes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    code VARCHAR(64) NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- 外键约束
-- ============================================================================

-- 租户外键
ALTER TABLE users ADD CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
ALTER TABLE roles ADD CONSTRAINT fk_roles_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

-- 用户外键
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_assigned_by FOREIGN KEY (assigned_by) REFERENCES users(id) ON DELETE SET NULL;
ALTER TABLE user_sessions ADD CONSTRAINT fk_user_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE password_history ADD CONSTRAINT fk_password_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE password_reset_tokens ADD CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE backup_codes ADD CONSTRAINT fk_backup_codes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- 角色外键
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

-- Token 外键
ALTER TABLE tokens ADD CONSTRAINT fk_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE tokens ADD CONSTRAINT fk_tokens_client FOREIGN KEY (client_id) REFERENCES oauth2_clients(id) ON DELETE CASCADE;
ALTER TABLE tokens ADD CONSTRAINT fk_tokens_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

-- 审计日志外键
ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_logs_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
ALTER TABLE audit_logs ADD CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL;

-- 会话外键
ALTER TABLE user_sessions ADD CONSTRAINT fk_user_sessions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

-- 权限外键
ALTER TABLE permissions ADD CONSTRAINT fk_permissions_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

-- ============================================================================
-- 唯一约束
-- ============================================================================

CREATE UNIQUE INDEX idx_tenants_name ON tenants(name) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX idx_tenants_code ON tenants(code) WHERE deleted_at IS NULL;

CREATE UNIQUE INDEX idx_users_tenant_username ON users(tenant_id, username) WHERE deleted_at IS NULL;
CREATE UNIQUE INDEX idx_users_tenant_email ON users(tenant_id, email) WHERE email IS NOT NULL AND deleted_at IS NULL;
CREATE UNIQUE INDEX idx_users_tenant_phone ON users(tenant_id, phone) WHERE phone IS NOT NULL AND deleted_at IS NULL;

CREATE UNIQUE INDEX idx_roles_tenant_code ON roles(tenant_id, code);

CREATE UNIQUE INDEX idx_user_roles_user_role ON user_roles(user_id, role_id) WHERE expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP;

CREATE UNIQUE INDEX idx_permissions_tenant_code ON permissions(tenant_id, code) WHERE tenant_id IS NOT NULL;
CREATE UNIQUE INDEX idx_permissions_global_code ON permissions(code) WHERE tenant_id IS NULL;

-- ============================================================================
-- 默认值更新触发器
-- ============================================================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为需要自动更新 updated_at 的表创建触发器
CREATE TRIGGER update_tenants_updated_at BEFORE UPDATE ON tenants
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_roles_updated_at BEFORE UPDATE ON roles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_oauth2_clients_updated_at BEFORE UPDATE ON oauth2_clients
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_permissions_updated_at BEFORE UPDATE ON permissions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- 软删除触发器
-- ============================================================================

CREATE OR REPLACE FUNCTION handle_user_soft_delete()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status != 'DELETED' AND NEW.status = 'DELETED' THEN
        NEW.deleted_at = CURRENT_TIMESTAMP;
    ELSIF OLD.status = 'DELETED' AND NEW.status != 'DELETED' THEN
        NEW.deleted_at = NULL;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER handle_users_soft_delete BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION handle_user_soft_delete();

-- ============================================================================
-- 注释
-- ============================================================================

COMMENT ON TABLE tenants IS '租户表';
COMMENT ON TABLE users IS '用户表';
COMMENT ON TABLE roles IS '角色表';
COMMENT ON TABLE user_roles IS '用户角色关联表';
COMMENT ON TABLE oauth2_clients IS 'OAuth2 客户端表';
COMMENT ON TABLE tokens IS 'Token 表';
COMMENT ON TABLE audit_logs IS '审计日志表';
COMMENT ON TABLE user_sessions IS '用户会话表';
COMMENT ON TABLE permissions IS '权限表';
COMMENT ON TABLE password_history IS '密码历史表';
COMMENT ON TABLE password_reset_tokens IS '密码重置令牌表';
COMMENT ON TABLE backup_codes IS '备用验证码表';
