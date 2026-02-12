-- ============================================
-- OpenIDaaS 数据库索引优化脚本
-- ============================================
-- 执行前请先备份数据库
-- ============================================

USE open_idaas;

-- ============================================
-- 1. users表索引优化
-- ============================================

-- 用户状态索引(用于查询活跃用户、锁定用户等)
CREATE INDEX idx_users_status ON users(status);

-- 邮箱索引(用于邮箱登录、找回密码)
CREATE INDEX idx_users_email ON users(email);

-- 手机号索引(用于手机号登录)
CREATE INDEX idx_users_mobile ON users(mobile);

-- 用户名和状态复合索引(用于登录验证时的用户查询)
CREATE INDEX idx_users_username_status ON users(username, status);

-- 最后登录时间索引(用于查询活跃用户)
CREATE INDEX idx_users_last_login ON users(last_login_time DESC);

-- 创建时间索引(用于查询新注册用户)
CREATE INDEX idx_users_created_at ON users(created_at DESC);

-- ============================================
-- 2. user_roles表索引优化
-- ============================================

-- 用户ID索引(用于查询用户的所有角色)
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);

-- 角色ID索引(用于查询拥有该角色的所有用户)
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- 用户和角色复合索引(用于判断用户是否拥有某个角色)
CREATE INDEX idx_user_roles_user_role ON user_roles(user_id, role_id);

-- 过期时间索引(用于查询即将过期的角色)
CREATE INDEX idx_user_roles_expire ON user_roles(expire_time)
    WHERE expire_time IS NOT NULL;

-- 作用域索引(用于按作用域查询权限)
CREATE INDEX idx_user_roles_scope ON user_roles(scope_type, scope_id);

-- ============================================
-- 3. user_departments表索引优化
-- ============================================

-- 用户ID索引
CREATE INDEX idx_user_departments_user_id ON user_departments(user_id);

-- 部门ID索引
CREATE INDEX idx_user_departments_dept_id ON user_departments(department_id);

-- 主部门索引
CREATE INDEX idx_user_departments_primary ON user_departments(user_id, is_primary);

-- ============================================
-- 4. departments表索引优化
-- ============================================

-- 部门状态索引
CREATE INDEX idx_departments_status ON departments(status);

-- 父部门ID索引(用于查询子部门)
CREATE INDEX idx_departments_parent_id ON departments(parent_id);

-- 层级路径索引(用于查询某个层级的所有子部门)
CREATE INDEX idx_departments_level_path ON departments(level_path(255));

-- 部门负责人索引
CREATE INDEX idx_departments_manager_id ON departments(manager_id);

-- ============================================
-- 5. roles表索引优化
-- ============================================

-- 角色类型索引(用于查询系统角色/自定义角色)
CREATE INDEX idx_roles_type ON roles(role_type);

-- 角色状态索引
CREATE INDEX idx_roles_status ON roles(status);

-- ============================================
-- 6. permissions表索引优化
-- ============================================

-- 权限类型索引(用于查询菜单权限、API权限等)
CREATE INDEX idx_permissions_type ON permissions(permission_type);

-- 资源类型索引
CREATE INDEX idx_permissions_resource ON permissions(resource_type);

-- 操作类型索引
CREATE INDEX idx_permissions_operation ON permissions(operation);

-- ============================================
-- 7. role_permissions表索引优化
-- ============================================

-- 角色ID索引
CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);

-- 权限ID索引
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- 角色和权限复合索引
CREATE INDEX idx_role_permissions_role_perm ON role_permissions(role_id, permission_id);

-- ============================================
-- 8. audit_logs表索引优化
-- ============================================

-- 用户ID索引
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);

-- 操作类型索引
CREATE INDEX idx_audit_logs_operation ON audit_logs(operation);

-- 创建时间索引(用于按时间范围查询)
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(created_at DESC);

-- 用户和时间复合索引(用于查询特定用户的操作记录)
CREATE INDEX idx_audit_logs_user_time ON audit_logs(user_id, created_at DESC);

-- IP地址索引(用于查询特定IP的操作)
CREATE INDEX idx_audit_logs_ip ON audit_logs(ip);

-- ============================================
-- 9. auth_tokens表索引优化
-- ============================================

-- 用户ID和Token复合索引
CREATE INDEX idx_auth_tokens_user_token ON auth_tokens(user_id, token(255));

-- 过期时间索引(用于清理过期Token)
CREATE INDEX idx_auth_tokens_expire ON auth_tokens(expire_at);

-- Token类型和用户ID复合索引
CREATE INDEX idx_auth_tokens_type_user ON auth_tokens(token_type, user_id);

-- Token本身索引(用于Token验证)
CREATE INDEX idx_auth_tokens_token ON auth_tokens(token(255));

-- ============================================
-- 10. user_mfa_factors表索引优化
-- ============================================

-- 用户ID索引
CREATE INDEX idx_user_mfa_factors_user_id ON user_mfa_factors(user_id);

-- MFA类型索引
CREATE INDEX idx_user_mfa_factors_type ON user_mfa_factors(mfa_type);

-- 状态索引
CREATE INDEX idx_user_mfa_factors_status ON user_mfa_factors(status);

-- 用户和类型复合索引
CREATE INDEX idx_user_mfa_factors_user_type ON user_mfa_factors(user_id, mfa_type);

-- ============================================
-- 11. applications表索引优化
-- ============================================

-- 应用状态索引
CREATE INDEX idx_applications_status ON applications(status);

-- 应用类型索引
CREATE INDEX idx_applications_type ON applications(app_type);

-- ============================================
-- 12. oauth2_clients表索引优化
-- ============================================

-- Client ID索引
CREATE INDEX idx_oauth2_clients_client_id ON oauth2_clients(client_id);

-- 状态索引
CREATE INDEX idx_oauth2_clients_status ON oauth2_clients(status);

-- ============================================
-- 13. mfa_logs表索引优化
-- ============================================

-- 用户ID索引
CREATE INDEX idx_mfa_logs_user_id ON mfa_logs(user_id);

-- MFA类型索引
CREATE INDEX idx_mfa_logs_type ON mfa_logs(mfa_type);

-- 创建时间索引
CREATE INDEX idx_mfa_logs_timestamp ON mfa_logs(created_at DESC);

-- 结果索引(成功/失败)
CREATE INDEX idx_mfa_logs_result ON mfa_logs(success);

-- ============================================
-- 14. 查看索引使用情况
-- ============================================

-- 查看索引大小
SELECT
    table_name,
    index_name,
    ROUND(stat_value * @@innodb_page_size / 1024 / 1024, 2) AS size_mb
FROM mysql.innodb_index_stats
WHERE database_name = 'open_idaas'
    AND stat_name = 'size'
ORDER BY size_mb DESC
LIMIT 20;

-- 查看索引使用情况
SELECT
    table_name,
    index_name,
    rows_read,
    rows_indexed
FROM (
    SELECT
        object_schema AS table_name,
        index_name,
        SUM(rows_read) AS rows_read,
        SUM(rows_indexed) AS rows_indexed
    FROM performance_schema.table_io_waits_summary_by_index_usage
    WHERE object_schema = 'open_idaas'
        AND index_name IS NOT NULL
    GROUP BY table_name, index_name
) AS idx_usage
ORDER BY rows_read DESC
LIMIT 20;

-- 查看未使用的索引
SELECT
    object_schema AS table_name,
    index_name
FROM performance_schema.table_io_waits_summary_by_index_usage
WHERE object_schema = 'open_idaas'
    AND index_name IS NOT NULL
    AND count_star = 0
    AND index_name != 'PRIMARY'
ORDER BY object_schema, index_name;

-- ============================================
-- 注意事项
-- ============================================
-- 1. 索引会占用额外存储空间,请根据实际业务需求选择性创建
-- 2. 索引会降低INSERT/UPDATE/DELETE性能,请权衡读写比例
-- 3. 定期监控索引使用情况,删除未使用的索引
-- 4. 对于大表,创建索引时建议使用ALGORITHM=INPLACE,LOCK=NONE
-- 5. 建议在业务低峰期执行索引创建操作
-- ============================================
