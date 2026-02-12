-- ==================== 应用管理数据域 ====================

-- 应用信息表
CREATE TABLE IF NOT EXISTS `applications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `app_key` varchar(128) NOT NULL COMMENT '应用密钥',
  `app_name` varchar(128) NOT NULL COMMENT '应用名称',
  `app_type` tinyint DEFAULT '1' COMMENT '应用类型：1-Web，2-移动，3-API',
  `redirect_uris` text COMMENT '重定向URI列表（JSON）',
  `logo_url` varchar(512) DEFAULT NULL COMMENT 'Logo URL',
  `homepage_url` varchar(512) DEFAULT NULL COMMENT '主页URL',
  `description` text COMMENT '应用描述',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，2-禁用',
  `owner_id` bigint DEFAULT NULL COMMENT '所有者ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_key` (`app_key`),
  KEY `idx_app_name` (`app_name`),
  KEY `idx_app_type` (`app_type`),
  KEY `idx_status` (`status`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_deleted` (`deleted`),
  CONSTRAINT `fk_applications_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='应用信息表';

-- OAuth2客户端表
CREATE TABLE IF NOT EXISTS `oauth2_clients` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `client_id` varchar(128) NOT NULL COMMENT '客户端ID',
  `client_secret` varchar(255) NOT NULL COMMENT '客户端密钥（加密存储）',
  `app_id` bigint NOT NULL COMMENT '应用ID',
  `grant_types` text COMMENT '授权类型列表（JSON）',
  `scopes` text COMMENT '权限范围（JSON）',
  `access_token_validity` int DEFAULT '3600' COMMENT '访问令牌有效期（秒）',
  `refresh_token_validity` int DEFAULT '2592000' COMMENT '刷新令牌有效期（秒）',
  `auto_approve` tinyint DEFAULT '0' COMMENT '是否自动批准',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_client_id` (`client_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_deleted` (`deleted`),
  CONSTRAINT `fk_oauth2_clients_app_id` FOREIGN KEY (`app_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='OAuth2客户端表';

-- SAML服务提供商表
CREATE TABLE IF NOT EXISTS `saml_service_providers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sp_entity_id` varchar(255) NOT NULL COMMENT 'SP实体ID',
  `app_id` bigint NOT NULL COMMENT '应用ID',
  `acs_url` varchar(512) NOT NULL COMMENT '断言消费服务URL',
  `certificate` text COMMENT '证书（PEM格式）',
  `metadata_url` varchar(512) DEFAULT NULL COMMENT '元数据URL',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sp_entity_id` (`sp_entity_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_deleted` (`deleted`),
  CONSTRAINT `fk_saml_service_providers_app_id` FOREIGN KEY (`app_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='SAML服务提供商表';

-- 插入示例数据
INSERT INTO `applications` (`app_key`, `app_name`, `app_type`, `redirect_uris`, `logo_url`, `homepage_url`, `description`, `status`) VALUES
('APP_1234567890abcdef1234567890abcdef', '示例Web应用', 1, '["http://localhost:3000/callback", "https://example.com/callback"]', 'https://via.placeholder.com/100', 'https://example.com', '这是一个示例Web应用', 1);

INSERT INTO `oauth2_clients` (`client_id`, `client_secret`, `app_id`, `grant_types`, `scopes`, `access_token_validity`, `refresh_token_validity`, `auto_approve`) VALUES
('web-client-id', '$2a$10$encrypted_secret_placeholder', 1, '["authorization_code", "refresh_token"]', '["read", "write"]', 3600, 2592000, 0);
