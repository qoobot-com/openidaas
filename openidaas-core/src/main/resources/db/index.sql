-- ============================================================================
-- OpenIDaaS 数据库索引创建脚本
-- 创建时间: 2026-02-08
-- ============================================================================

-- 设置时区为 UTC
SET TIME ZONE 'UTC';

-- ============================================================================
-- 租户表索引
-- ============================================================================

-- 租户表查询优化索引
CREATE INDEX idx_tenants_domain ON tenants(domain) WHERE domain IS NOT NULL;
CREATE INDEX idx_tenants_status ON tenants(status);
CREATE INDEX idx_tenants_created_at ON tenants(created_at DESC);

-- ============================================================================
-- 用户表索引
-- ============================================================================

-- 用户表查询优化索引
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_username ON users(username) WHERE deleted_at IS NULL;
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_users_phone ON users(phone) WHERE phone IS NOT NULL AND deleted_at IS NULL;
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_last_login_at ON users(last_login_at DESC);
CREATE INDEX idx_users_created_at ON users(created_at DESC);
CREATE INDEX idx_users_tenant_status ON users(tenant_id, status);
CREATE INDEX idx_users_tenant_mfa_enabled ON users(tenant_id, mfa_enabled);
CREATE INDEX idx_users_email_verified ON users(email_verified) WHERE email_verified = FALSE;
CREATE INDEX idx_users_phone_verified ON users(phone_verified) WHERE phone_verified = FALSE;

-- 全文搜索索引 (PostgreSQL GIN)
CREATE INDEX idx_users_fulltext ON users USING GIN(
    to_tsvector('simple',
        COALESCE(username, '') || ' ' ||
        COALESCE(email, '') || ' ' ||
        COALESCE(phone, '')
    )
);

-- ============================================================================
-- 角色表索引
-- ============================================================================

-- 角色表查询优化索引
CREATE INDEX idx_roles_tenant_id ON roles(tenant_id);
CREATE INDEX idx_roles_name ON roles(name);
CREATE INDEX idx_roles_code ON roles(code);

-- ============================================================================
-- 用户角色关联表索引
-- ============================================================================

-- 用户角色关联表查询优化索引
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
CREATE INDEX idx_user_roles_assigned_at ON user_roles(assigned_at DESC);
CREATE INDEX idx_user_roles_expires_at ON user_roles(expires_at) WHERE expires_at IS NOT NULL;

-- 复合索引：用户的有效角色
CREATE INDEX idx_user_roles_active ON user_roles(user_id, role_id, expires_at)
WHERE (expires_at IS NULL OR expires_at > CURRENT_TIMESTAMP);

-- ============================================================================
-- OAuth2 客户端表索引
-- ============================================================================

-- OAuth2 客户端表查询优化索引
CREATE INDEX idx_oauth2_clients_client_id ON oauth2_clients(client_id);
CREATE INDEX idx_oauth2_clients_client_name ON oauth2_clients(client_name);
CREATE INDEX idx_oauth2_clients_created_at ON oauth2_clients(created_at DESC);

-- ============================================================================
-- Token 表索引
-- ============================================================================

-- Token 表查询优化索引
CREATE INDEX idx_tokens_user_id ON tokens(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_tokens_client_id ON tokens(client_id);
CREATE INDEX idx_tokens_token_value ON tokens(token_value) WHERE token_value IS NOT NULL;
CREATE INDEX idx_tokens_expires_at ON tokens(expires_at);
CREATE INDEX idx_tokens_token_type ON tokens(token_type);
CREATE INDEX idx_tokens_status ON tokens(status);
CREATE INDEX idx_tokens_tenant_id ON tokens(tenant_id);
CREATE INDEX idx_tokens_user_status ON tokens(user_id, status) WHERE user_id IS NOT NULL;
CREATE INDEX idx_tokens_client_expires_status ON tokens(client_id, expires_at, status);

-- 过期 token 索引 (用于清理任务)
CREATE INDEX idx_tokens_expired ON tokens(expires_at, status) WHERE status = 'ACTIVE' AND expires_at < CURRENT_TIMESTAMP;

-- ============================================================================
-- 审计日志表索引
-- ============================================================================

-- 审计日志表查询优化索引 (分区表索引需要在分区上创建)
CREATE INDEX idx_audit_logs_tenant_id ON audit_logs(tenant_id);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id) WHERE user_id IS NOT NULL;
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_resource ON audit_logs(resource) WHERE resource IS NOT NULL;
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
CREATE INDEX idx_audit_logs_result ON audit_logs(result);
CREATE INDEX idx_audit_logs_ip_address ON audit_logs(ip_address) WHERE ip_address IS NOT NULL;

-- 复合索引：租户审计日志查询
CREATE INDEX idx_audit_logs_tenant_created ON audit_logs(tenant_id, created_at DESC);

-- 复合索引：用户审计日志查询
CREATE INDEX idx_audit_logs_user_created ON audit_logs(user_id, created_at DESC) WHERE user_id IS NOT NULL;

-- 复合索引：操作和资源查询
CREATE INDEX idx_audit_logs_action_resource ON audit_logs(action, resource) WHERE resource IS NOT NULL;

-- 审计日志 JSONB 字段索引
CREATE INDEX idx_audit_logs_details ON audit_logs USING GIN(details);

-- ============================================================================
-- 用户会话表索引
-- ============================================================================

-- 用户会话表查询优化索引
CREATE INDEX idx_user_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_user_sessions_tenant_id ON user_sessions(tenant_id);
CREATE INDEX idx_user_sessions_session_token ON user_sessions(session_token);
CREATE INDEX idx_user_sessions_last_activity_at ON user_sessions(last_activity_at DESC);
CREATE INDEX idx_user_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX idx_user_sessions_status ON user_sessions(status);
CREATE INDEX idx_user_sessions_device_id ON user_sessions(device_id) WHERE device_id IS NOT NULL;

-- 复合索引：用户活跃会话
CREATE INDEX idx_user_sessions_user_status ON user_sessions(user_id, status, last_activity_at)
WHERE status = 'ACTIVE';

-- 过期会话索引 (用于清理任务)
CREATE INDEX idx_user_sessions_expired ON user_sessions(expires_at, status)
WHERE status = 'ACTIVE' AND expires_at < CURRENT_TIMESTAMP;

-- ============================================================================
-- 权限表索引
-- ============================================================================

-- 权限表查询优化索引
CREATE INDEX idx_permissions_tenant_id ON permissions(tenant_id) WHERE tenant_id IS NOT NULL;
CREATE INDEX idx_permissions_code ON permissions(code);
CREATE INDEX idx_permissions_resource ON permissions(resource);
CREATE INDEX idx_permissions_action ON permissions(action);

-- 复合索引：资源和操作
CREATE INDEX idx_permissions_resource_action ON permissions(resource, action);

-- ============================================================================
-- 密码历史表索引
-- ============================================================================

-- 密码历史表查询优化索引
CREATE INDEX idx_password_history_user_id ON password_history(user_id);
CREATE INDEX idx_password_history_changed_at ON password_history(changed_at DESC);

-- 复合索引：用户最近密码历史
CREATE INDEX idx_password_history_user_changed ON password_history(user_id, changed_at DESC);

-- ============================================================================
-- 密码重置令牌表索引
-- ============================================================================

-- 密码重置令牌表查询优化索引
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens(expires_at);
CREATE INDEX idx_password_reset_tokens_created_at ON password_reset_tokens(created_at DESC);

-- 过期令牌索引 (用于清理任务)
CREATE INDEX idx_password_reset_tokens_expired ON password_reset_tokens(expires_at, used_at)
WHERE used_at IS NULL AND expires_at < CURRENT_TIMESTAMP;

-- ============================================================================
-- 备用验证码表索引
-- ============================================================================

-- 备用验证码表查询优化索引
CREATE INDEX idx_backup_codes_user_id ON backup_codes(user_id);
CREATE INDEX idx_backup_codes_code ON backup_codes(code);
CREATE INDEX idx_backup_codes_created_at ON backup_codes(created_at DESC);

-- 未使用的备用码索引
CREATE INDEX idx_backup_codes_unused ON backup_codes(user_id, used_at) WHERE used_at IS NULL;

-- ============================================================================
-- 性能优化：部分索引 (Partial Indexes)
-- ============================================================================

-- 只索引活跃用户
CREATE INDEX idx_users_active ON users(tenant_id, username)
WHERE status = 'ACTIVE' AND deleted_at IS NULL;

-- 只索引活跃租户
CREATE INDEX idx_tenants_active ON tenants(id, name)
WHERE status = 'ACTIVE' AND deleted_at IS NULL;

-- 只索引有效 token
CREATE INDEX idx_tokens_valid ON tokens(user_id, token_type, expires_at)
WHERE status = 'ACTIVE' AND expires_at > CURRENT_TIMESTAMP;

-- ============================================================================
-- 覆盖索引 (Covering Indexes) 用于常用查询
-- ============================================================================

-- 用户登录查询优化
CREATE INDEX idx_users_login_query ON users(tenant_id, username, status, password, id)
WHERE deleted_at IS NULL;

-- Token 验证查询优化
CREATE INDEX idx_tokens_validation ON tokens(token_value, token_type, status, expires_at, user_id, client_id)
WHERE status = 'ACTIVE';

-- ============================================================================
-- BRIN 索引 (用于大表)
-- ============================================================================

-- 审计日志表使用 BRIN 索引 (按时间范围分区,适合大量数据)
CREATE INDEX idx_audit_logs_created_at_brin ON audit_logs USING BRIN(created_at);

-- ============================================================================
-- 并发索引创建 (CONCURRENTLY)
-- 注意：生产环境建议使用 CONCURRENTLY 创建索引,避免锁表
-- ============================================================================
/*
-- 示例：使用 CONCURRENTLY 创建索引
CREATE INDEX CONCURRENTLY idx_users_username ON users(username);
CREATE INDEX CONCURRENTLY idx_tokens_user_id ON tokens(user_id);

-- 注意事项：
-- 1. CONCURRENTLY 不能在事务中使用
-- 2. 如果创建失败,会留下一个无效索引,需要手动删除
-- 3. 创建时间较长,但不会阻塞 DML 操作
*/

-- ============================================================================
-- 索引创建完成
-- ============================================================================
