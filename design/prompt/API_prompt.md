# IDaaS系统 API接口开发提示词

## 项目基础信息

- **项目名称**: openidaas
- **项目描述**: 企业级身份即服务(IDaaS)系统
- **架构模式**: 前后端分离架构
- **技术栈**:
  - 后端: Spring Boot 3.5.10 + JDK 21
  - 前端: Vue 3
  - 数据库: MySQL 8.0+
- **项目坐标**:
  - groupId: com.qoobot
  - artifactId: openidaas
  - version: 10.3.2
- **GitHub仓库**: https://github.com/qoobot-com/openidaas
- **编码格式**: UTF-8
- **数据库配置**:
  - 地址: localhost:3306
  - 数据库名: open_idaas
  - 用户名/密码: root/root123

---

## API开发规范要求

### 1. 通用设计原则

所有API接口开发必须遵循以下原则：

- **RESTful架构**: 使用标准HTTP方法(GET/POST/PUT/DELETE)和资源路径设计
- **统一响应格式**: 所有接口返回统一JSON格式的响应对象
- **版本管理**: API路径包含版本号，如 `/api/v1/xxx`
- **幂等性保证**: PUT/DELETE操作保证幂等性
- **安全第一**: 所有接口必须进行权限校验和参数验证
- **审计日志**: 关键操作必须记录审计日志
- **国际化支持**: 所有错误消息支持i18n
- **文档完整**: 接口使用Swagger/OpenAPI 3.0规范注解

### 2. 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1234567890,
  "traceId": "abc123xyz"
}
```

**响应码规范**:
- 200: 成功
- 400: 请求参数错误
- 401: 未认证
- 403: 无权限
- 404: 资源不存在
- 409: 资源冲突
- 429: 请求过于频繁
- 500: 服务器内部错误

### 3. 分页查询规范

所有列表查询接口支持分页:

```json
{
  "page": 1,
  "size": 20,
  "total": 100,
  "pages": 5,
  "records": []
}
```

---

## 核心模块API接口规范

---

## 模块一: 身份生命周期管理 (Identity Lifecycle Management)

### 1.1 用户账户管理 (User Account Management)

#### 1.1.1 用户注册
- **接口路径**: `POST /api/v1/identity/users/register`
- **功能描述**: 支持邮箱验证、手机号验证、邀请码注册
- **请求参数**:
```json
{
  "registerType": "EMAIL|PHONE|INVITE",
  "email": "user@example.com",
  "phone": "+8613800138000",
  "password": "Complex@Pass123",
  "inviteCode": "ABC123XYZ",
  "verifyCode": "123456",
  "termsAccepted": true
}
```
- **业务规则**:
  - 密码必须符合复杂度要求: 8位以上,包含大小写字母、数字、特殊字符
  - 验证码有效期10分钟
  - 同一IP注册频率限制
  - 邀请码验证有效性

#### 1.1.2 用户档案管理
- **接口路径**: `GET/PUT /api/v1/identity/users/{userId}/profile`
- **功能描述**: 个人资料、头像、联系方式、自定义属性
- **请求参数**:
```json
{
  "nickname": "张三",
  "avatar": "https://cdn.example.com/avatar.jpg",
  "email": "user@example.com",
  "phone": "+8613800138000",
  "departmentId": "DEPT001",
  "position": "软件工程师",
  "level": "P5",
  "customAttributes": {
    "employeeId": "EMP001",
    "hireDate": "2024-01-01"
  }
}
```

#### 1.1.3 账户状态管理
- **接口路径**: `PUT /api/v1/identity/users/{userId}/status`
- **功能描述**: 激活/停用/锁定/删除账户状态流转
- **状态枚举**: ACTIVE, INACTIVE, LOCKED, DELETED, PENDING
- **业务规则**:
  - 状态变更需要审批流程(停用/删除)
  - 锁定账户需要原因记录
  - 删除账户需进行数据备份

#### 1.1.4 密码管理
- **接口路径**: `POST /api/v1/identity/users/{userId}/password`
- **功能描述**: 修改密码、重置密码、密码策略验证
- **请求参数**:
```json
{
  "operation": "CHANGE|RESET|VALIDATE",
  "oldPassword": "OldPass123",
  "newPassword": "NewPass456",
  "verifyCode": "123456"
}
```
- **密码策略**:
  - 复杂度验证
  - 定期更换提醒(90天)
  - 历史密码检测(不能使用最近5次密码)

#### 1.1.5 用户列表查询
- **接口路径**: `GET /api/v1/identity/users`
- **功能描述**: 多条件查询用户列表
- **查询参数**: page, size, keyword, status, departmentId, roleId, createTimeStart, createTimeEnd
- **支持字段**: 模糊搜索(姓名/邮箱/手机号), 状态筛选, 部门筛选, 角色筛选

### 1.2 角色权限管理 (RBAC/ABAC)

#### 1.2.1 角色管理
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/identity/roles`
- **功能描述**: 创建层级角色、复合角色
- **请求参数**:
```json
{
  "roleCode": "ROLE_MANAGER",
  "roleName": "经理",
  "description": "部门经理角色",
  "parentId": "ROLE_DIRECTOR",
  "type": "STANDARD|COMPOSITE",
  "level": 3,
  "status": "ACTIVE",
  "permissions": ["user:read", "user:write", "approval:execute"]
}
```
- **业务规则**:
  - 角色编码全局唯一
  - 支持角色继承(父子关系)
  - 复合角色可包含多个子角色

#### 1.2.2 权限管理
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/identity/permissions`
- **功能描述**: 细粒度权限定义,与应用绑定
- **请求参数**:
```json
{
  "permissionCode": "user:create",
  "permissionName": "创建用户",
  "resource": "USER",
  "action": "CREATE",
  "appId": "APP001",
  "description": "允许创建新用户"
}
```
- **权限类型**: CRUD, APPROVAL, REPORT, CONFIG, EXPORT, IMPORT

#### 1.2.3 动态权限授予
- **接口路径**: `POST /api/v1/identity/roles/{roleId}/dynamic-permissions`
- **功能描述**: 基于时间/地点/设备的临时权限授予
- **请求参数**:
```json
{
  "userId": "USER001",
  "permissionCode": "approval:execute",
  "validFrom": "2024-01-01T00:00:00Z",
  "validTo": "2024-01-02T23:59:59Z",
  "location": "北京",
  "deviceFingerprint": "xxx-xxx-xxx",
  "reason": "临时审批权限"
}
```

#### 1.2.4 权限继承
- **接口路径**: `GET /api/v1/identity/users/{userId}/effective-permissions`
- **功能描述**: 计算用户有效权限(角色继承+部门继承+项目继承)
- **返回数据**: 包含直接权限、继承权限、临时权限

### 1.3 组织架构管理

#### 1.3.1 部门树管理
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/identity/departments`
- **功能描述**: 多层级组织结构,支持虚拟组织
- **请求参数**:
```json
{
  "deptCode": "DEPT001",
  "deptName": "研发中心",
  "parentId": "ROOT",
  "type": "ACTUAL|VIRTUAL",
  "leaderId": "USER001",
  "sort": 1,
  "status": "ACTIVE"
}
```

#### 1.3.2 职位管理
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/identity/positions`
- **功能描述**: 职务/职级/汇报关系管理
- **请求参数**:
```json
{
  "positionCode": "POS001",
  "positionName": "软件工程师",
  "level": "P5",
  "levelName": "高级工程师",
  "reportToPositionId": "POS002",
  "description": "负责核心模块开发"
}
```

#### 1.3.3 动态组管理
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/identity/dynamic-groups`
- **功能描述**: 基于属性的动态用户组
- **请求参数**:
```json
{
  "groupCode": "GROUP_BEIJING",
  "groupName": "北京员工",
  "rules": [
    {
      "field": "city",
      "operator": "EQ",
      "value": "北京"
    }
  ],
  "refreshInterval": 3600
}
```

#### 1.3.4 委派授权
- **接口路径**: `POST /api/v1/identity/delegations`
- **功能描述**: 部门管理员代理权限
- **请求参数**:
```json
{
  "delegatorId": "USER001",
  "delegateId": "USER002",
  "departmentId": "DEPT001",
  "permissions": ["user:approve", "resource:allocate"],
  "validFrom": "2024-01-01T00:00:00Z",
  "validTo": "2024-01-07T23:59:59Z",
  "reason": "出差期间代理"
}
```

---

## 模块二: 认证服务 (Authentication)

### 2.1 多因子认证 (MFA)

#### 2.1.1 MFA配置
- **接口路径**: `POST/GET /api/v1/auth/mfa/setup`
- **功能描述**: 配置MFA认证方式
- **请求参数**:
```json
{
  "userId": "USER001",
  "mfaType": "SMS|TOTP|HARDWARE_TOKEN|BIOMETRIC|CERTIFICATE",
  "deviceName": "我的手机",
  "phoneNumber": "+8613800138000",
  "secret": "JBSWY3DPEHPK3PXP"
}
```

#### 2.1.2 TOTP验证
- **接口路径**: `POST /api/v1/auth/mfa/verify-totp`
- **功能描述**: 验证TOTP动态验证码
- **请求参数**:
```json
{
  "userId": "USER001",
  "code": "123456"
}
```

#### 2.1.3 硬件令牌验证
- **接口路径**: `POST /api/v1/auth/mfa/verify-token`
- **功能描述**: YubiKey、RSA SecurID验证
- **请求参数**:
```json
{
  "userId": "USER001",
  "tokenType": "YUBIKEY|RSA_SECURID",
  "tokenValue": "ccccccfgrgjvlfhlhrkhrkhjck"
}
```

#### 2.1.4 生物识别注册
- **接口路径**: `POST /api/v1/auth/mfa/register-biometric`
- **功能描述**: 指纹/Face ID注册
- **请求参数**:
```json
{
  "userId": "USER001",
  "biometricType": "FINGERPRINT|FACE_ID",
  "biometricTemplate": "base64_encoded_template",
  "deviceInfo": {
    "deviceId": "xxx",
    "deviceName": "iPhone 15 Pro"
  }
}
```

#### 2.1.5 证书认证
- **接口路径**: `POST /api/v1/auth/mfa/verify-certificate`
- **功能描述**: X.509证书、智能卡验证
- **请求参数**:
```json
{
  "certificate": "-----BEGIN CERTIFICATE-----\n...",
  "signature": "base64_signature"
}
```

### 2.2 社交登录 (Social Login)

#### 2.2.1 社交登录授权
- **接口路径**: `GET /api/v1/auth/social/{provider}/authorize`
- **功能描述**: 微信/钉钉/飞书、Google/Facebook/LinkedIn授权跳转
- **支持平台**: WECHAT, DINGTALK, FEISHU, GOOGLE, FACEBOOK, LINKEDIN

#### 2.2.2 社交登录回调
- **接口路径**: `GET/POST /api/v1/auth/social/{provider}/callback`
- **功能描述**: 处理社交登录回调
- **回调参数**: code, state

#### 2.2.3 企业微信扫码登录
- **接口路径**: `GET /api/v1/auth/wecom/qrcode`
- **功能描述**: 获取企业微信登录二维码
- **返回数据**: 二维码图片URL、qrcode_key

#### 2.2.4 OIDC Provider集成
- **接口路径**: `POST /api/v1/auth/oidc/providers`
- **功能描述**: 配置外部OIDC Provider
- **请求参数**:
```json
{
  "providerName": "公司内部IDP",
  "issuer": "https://idp.company.com",
  "clientId": "client_id",
  "clientSecret": "client_secret",
  "scopes": ["openid", "profile", "email"],
  "redirectUri": "https://idaas.example.com/callback"
}
```

#### 2.2.5 SAML Provider集成
- **接口路径**: `POST /api/v1/auth/saml/providers`
- **功能描述**: 配置外部SAML Identity Provider
- **请求参数**:
```json
{
  "providerName": "公司内部IDP",
  "metadataUrl": "https://idp.company.com/metadata",
  "spEntityId": "https://idaas.example.com",
  "acsUrl": "https://idaas.example.com/saml/acs",
  "sloUrl": "https://idaas.example.com/saml/slo"
}
```

### 2.3 自适应认证 (Adaptive Authentication)

#### 2.3.1 风险评分计算
- **接口路径**: `POST /api/v1/auth/risk-assessment`
- **功能描述**: 基于IP/设备/行为的风险评估
- **请求参数**:
```json
{
  "userId": "USER001",
  "ip": "1.2.3.4",
  "userAgent": "Mozilla/5.0...",
  "deviceFingerprint": "xxx-xxx-xxx",
  "location": {
    "country": "中国",
    "city": "北京",
    "coordinates": [116.4074, 39.9042]
  },
  "time": "2024-01-01T08:00:00Z",
  "behavior": {
    "loginAttempts": 1,
    "recentFailures": 0,
    "typicalLoginTime": true
  }
}
```
- **返回数据**: 风险等级(LOW/MEDIUM/HIGH/CRITICAL)、风险分数、建议措施

#### 2.3.2 上下文认证
- **接口路径**: `POST /api/v1/auth/context-verification`
- **功能描述**: 时间/地点/设备异常检测
- **业务规则**:
  - 非常用登录地触发二次验证
  - 异常登录时间触发风险提醒
  - 新设备触发邮箱/短信验证

#### 2.3.3 可信设备管理
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/auth/trusted-devices`
- **功能描述**: 可信设备白名单管理
- **请求参数**:
```json
{
  "userId": "USER001",
  "deviceName": "我的MacBook",
  "deviceFingerprint": "xxx-xxx-xxx",
  "platform": "macOS",
  "browser": "Chrome",
  "trustedUntil": "2024-12-31T23:59:59Z"
}
```

#### 2.3.4 AI风控
- **接口路径**: `POST /api/v1/auth/ai-detection`
- **功能描述**: 异常登录行为机器学习检测
- **检测场景**: 暴力破解、撞库、账号共享、异常行为模式

---

## 模块三: 授权与访问控制 (Authorization)

### 3.1 单点登录 (SSO)

#### 3.1.1 SAML 2.0 SSO
- **接口路径**: `GET /api/v1/sso/saml/{spId}/sso`
- **功能描述**: SAML 2.0 SP发起的SSO
- **支持协议**: SAML 2.0, SAML 1.1

#### 3.1.2 OIDC授权
- **接口路径**: `GET /api/v1/sso/oidc/authorize`
- **功能描述**: OIDC授权码模式授权
- **请求参数**: response_type=code, client_id, redirect_uri, scope, state

#### 3.1.3 OAuth2.0授权
- **接口路径**: `GET /api/v1/sso/oauth2/authorize`
- **功能描述**: OAuth2.0授权
- **支持模式**: Authorization Code, Implicit, Resource Owner Password, Client Credentials

#### 3.1.4 Token端点
- **接口路径**: `POST /api/v1/sso/oauth2/token`
- **功能描述**: 获取访问令牌
- **支持grant_type**: authorization_code, refresh_token, client_credentials, password

#### 3.1.5 用户信息端点
- **接口路径**: `GET /api/v1/sso/oauth2/userinfo`
- **功能描述**: 获取当前用户信息
- **认证方式**: Bearer Token

#### 3.1.6 单点登出
- **接口路径**: `GET/POST /api/v1/sso/logout`
- **功能描述**: 全局登出,撤销所有应用会话
- **支持协议**: SAML SLO, OIDC RP-Initiated Logout

### 3.2 API安全网关

#### 3.2.1 令牌管理
- **接口路径**: `POST/GET/PUT/DELETE /api/v1/gateway/tokens`
- **功能描述**: JWT/Opaque Token/Client Credentials管理
- **请求参数**:
```json
{
  "tokenType": "JWT|OPAQUE",
  "clientId": "client_001",
  "userId": "USER001",
  "scopes": ["read", "write"],
  "expiresIn": 3600,
  "refreshable": true
}
```

#### 3.2.2 Token验证
- **接口路径**: `POST /api/v1/gateway/tokens/validate`
- **功能描述**: 验证Token有效性
- **请求参数**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "requiredScopes": ["read"]
}
```

#### 3.2.3 Token刷新
- **接口路径**: `POST /api/v1/gateway/tokens/refresh`
- **功能描述**: 刷新过期Token
- **请求参数**:
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

#### 3.2.4 访问控制
- **接口路径**: `POST /api/v1/gateway/access-control`
- **功能描述**: 基于角色/权限的API限流
- **请求参数**:
```json
{
  "clientId": "client_001",
  "apiPath": "/api/v1/users",
  "method": "GET",
  "ip": "1.2.3.4",
  "userId": "USER001"
}
```

#### 3.2.5 审计日志
- **接口路径**: `GET /api/v1/gateway/audit-logs`
- **功能描述**: API调用链追踪
- **查询参数**: client_id, user_id, api_path, start_time, end_time

#### 3.2.6 密钥管理
- **接口路径**: `GET/POST/PUT /api/v1/gateway/secrets`
- **功能描述**: API Key/Client Secret轮换
- **请求参数**:
```json
{
  "secretType": "API_KEY|CLIENT_SECRET",
  "clientId": "client_001",
  "rotateReason": "定期轮换"
}
```

### 3.3 动态权限计算

#### 3.3.1 权限计算
- **接口路径**: `POST /api/v1/authorization/evaluate`
- **功能描述**: 基于规则的权限实时决策
- **请求参数**:
```json
{
  "userId": "USER001",
  "resource": "USER",
  "action": "UPDATE",
  "context": {
    "time": "2024-01-01T08:00:00Z",
    "location": "北京",
    "device": "DESKTOP",
    "ip": "1.2.3.4"
  }
}
```

#### 3.3.2 上下文感知权限
- **接口路径**: `POST /api/v1/authorization/context-aware`
- **功能描述**: 时间/地点/设备权限动态调整
- **策略示例**:
  - 工作时间(9:00-18:00)允许敏感操作
  - 公司IP允许完整权限
  - 可信设备减少二次验证

#### 3.3.3 缓存管理
- **接口路径**: `POST /api/v1/authorization/cache/invalidate`
- **功能描述**: 权限缓存失效
- **请求参数**:
```json
{
  "cacheKeys": ["USER001:PERMISSIONS", "ROLE_ADMIN:PERMISSIONS"],
  "scope": "USER|ROLE|GLOBAL"
}
```

---

## 模块四: 目录与元数据管理

### 4.1 用户目录

#### 4.1.1 用户主数据管理
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/directory/users`
- **功能描述**: 用户主记录、去重合并
- **请求参数**:
```json
{
  "primaryId": "USER001",
  "source": "HR|MANUAL|SYNC",
  "attributes": {
    "userId": "USER001",
    "username": "zhangsan",
    "displayName": "张三",
    "email": "zhangsan@example.com",
    "phone": "+8613800138000",
    "employeeId": "EMP001"
  },
  "linkedIds": ["USER_HR_001", "USER_LDAP_001"]
}
```

#### 4.1.2 属性扩展
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/directory/user-attributes`
- **功能描述**: 自定义字段、扩展属性
- **请求参数**:
```json
{
  "attributeCode": "wechat",
  "attributeName": "微信号",
  "dataType": "STRING",
  "category": "CONTACT",
  "required": false,
  "indexed": true,
  "visible": true
}
```

#### 4.1.3 数据质量检查
- **接口路径**: `POST /api/v1/directory/data-quality/check`
- **功能描述**: 字段验证、数据清洗
- **请求参数**:
```json
{
  "userId": "USER001",
  "rules": ["EMAIL_FORMAT", "PHONE_FORMAT", "REQUIRED_FIELDS"]
}
```

#### 4.1.4 版本追溯
- **接口路径**: `GET /api/v1/directory/users/{userId}/history`
- **功能描述**: 属性变更历史查询
- **查询参数**: field, start_time, end_time

### 4.2 应用目录

#### 4.2.1 应用注册
- **接口路径**: `POST /api/v1/directory/applications`
- **功能描述**: OAuth2 Client、SAML SP注册
- **请求参数**:
```json
{
  "appId": "APP001",
  "appName": "企业OA",
  "appType": "WEB|MOBILE|DESKTOP|API",
  "protocol": "OAUTH2|SAML|OIDC",
  "redirectUris": ["https://oa.example.com/callback"],
  "scopes": ["profile", "email"],
  "grantTypes": ["authorization_code", "refresh_token"],
  "logo": "https://cdn.example.com/logo.png"
}
```

#### 4.2.2 权限映射
- **接口路径**: `POST /api/v1/directory/applications/{appId}/permission-mapping`
- **功能描述**: 应用权限与IDaaS角色映射
- **请求参数**:
```json
{
  "roleCode": "ROLE_MANAGER",
  "appPermissions": ["oa:approve", "oa:report"],
  "mappings": [
    {
      "idaasPermission": "user:read",
      "appPermission": "oa:user:view"
    }
  ]
}
```

#### 4.2.3 SSO配置
- **接口路径**: `PUT /api/v1/directory/applications/{appId}/sso-config`
- **功能描述**: SSO参数、回调地址配置
- **请求参数**:
```json
{
  "ssoEnabled": true,
  "ssoProtocol": "SAML20",
  "samlConfig": {
    "spEntityId": "https://oa.example.com",
    "acsUrl": "https://oa.example.com/saml/acs",
    "attributeMapping": {
      "email": "emailAddress",
      "name": "fullName"
    }
  }
}
```

#### 4.2.4 应用生命周期
- **接口路径**: `PUT /api/v1/directory/applications/{appId}/lifecycle`
- **功能描述**: 应用启用/禁用/删除
- **状态**: ACTIVE, INACTIVE, DELETED

---

## 模块五: 联合身份

### 5.1 SAML 2.0集成

#### 5.1.1 SP集成配置
- **接口路径**: `POST /api/v1/federation/saml/service-providers`
- **功能描述**: 配置SAML SP (Salesforce、Workday、SAP等)
- **请求参数**:
```json
{
  "spId": "SP001",
  "spName": "Salesforce",
  "metadataUrl": "https://salesforce.example.com/metadata",
  "entityId": "https://salesforce.example.com",
  "acsUrl": "https://salesforce.example.com/saml/acs",
  "sloUrl": "https://salesforce.example.com/saml/slo",
  "attributeMapping": {
    "email": "Email",
    "firstName": "FirstName",
    "lastName": "LastName"
  }
}
```

#### 5.1.2 IDP配置
- **接口路径**: `POST /api/v1/federation/saml/identity-providers`
- **功能描述**: 配置外部IDP
- **请求参数**:
```json
{
  "idpId": "IDP001",
  "idpName": "公司内部IDP",
  "metadataUrl": "https://idp.company.com/metadata",
  "metadata": "base64_encoded_metadata"
}
```

#### 5.1.3 SAML断言解析
- **接口路径**: `POST /api/v1/federation/saml/parse-assertion`
- **功能描述**: SAML Assertion属性转换
- **请求参数**:
```json
{
  "samlResponse": "base64_encoded_saml_response"
}
```

#### 5.1.4 单点登出
- **接口路径**: `POST /api/v1/federation/saml/logout`
- **功能描述**: 全局登出流程

### 5.2 OIDC/OAuth2.0

#### 5.2.1 Client管理
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/federation/oidc/clients`
- **功能描述**: OIDC Client管理
- **请求参数**:
```json
{
  "clientId": "client_001",
  "clientName": "移动应用",
  "clientType": "CONFIDENTIAL|PUBLIC",
  "redirectUris": ["myapp://callback"],
  "grantTypes": ["authorization_code", "refresh_token"],
  "scopes": ["openid", "profile", "email"],
  "tokenEndpointAuth": "client_secret_post|none"
}
```

#### 5.2.2 授权码模式
- **接口路径**: `GET /api/v1/federation/oidc/authorize`
- **功能描述**: Web应用安全认证

#### 5.2.3 客户端凭证模式
- **接口路径**: `POST /api/v1/federation/oidc/token`
- **功能描述**: 服务间调用认证
- **grant_type**: client_credentials

#### 5.2.4 PKCE
- **接口路径**: `POST /api/v1/federation/oidc/token`
- **功能描述**: 移动应用安全增强
- **额外参数**: code_verifier, code_challenge, code_challenge_method

#### 5.2.5 Scope控制
- **接口路径**: `GET /api/v1/federation/oidc/scopes`
- **功能描述**: 精细化权限控制

### 5.3 SCIM 2.0同步

#### 5.3.1 SCIM服务端
- **接口路径**: `/api/v1/scim/v2/Users`
- **功能描述**: SCIM 2.0标准接口(遵循RFC7643/7644)
- **支持操作**: GET, POST, PUT, PATCH, DELETE

#### 5.3.2 用户同步
- **接口路径**: `POST /api/v1/sync/users`
- **功能描述**: HR系统→IDaaS→应用系统
- **请求参数**:
```json
{
  "source": "HR_SYSTEM",
  "syncMode": "FULL|INCREMENTAL",
  "users": [
    {
      "externalId": "HR001",
      "userName": "zhangsan",
      "name": {
        "givenName": "张",
        "familyName": "三"
      },
      "emails": [
        {
          "value": "zhangsan@example.com",
          "type": "work",
          "primary": true
        }
      ],
      "active": true
    }
  ]
}
```

#### 5.3.3 组同步
- **接口路径**: `POST /api/v1/sync/groups`
- **功能描述**: 组织架构同步
- **请求参数**:
```json
{
  "source": "HR_SYSTEM",
  "groups": [
    {
      "externalId": "DEPT001",
      "displayName": "研发部",
      "members": ["zhangsan", "lisi"]
    }
  ]
}
```

#### 5.3.4 双向同步
- **接口路径**: `POST /api/v1/sync/bidirectional`
- **功能描述**: 支持本地修改回写

#### 5.3.5 事件驱动
- **接口路径**: `POST /api/v1/sync/events/webhook`
- **功能描述**: 实时变更通知
- **事件类型**: USER_CREATED, USER_UPDATED, USER_DELETED, GROUP_CREATED, GROUP_UPDATED, GROUP_DELETED

---

## 模块六: 身份治理 (Identity Governance)

### 6.1 访问审查

#### 6.1.1 权限盘点
- **接口路径**: `GET /api/v1/governance/access-review/user/{userId}`
- **功能描述**: 用户权限全景视图
- **返回数据**: 直接角色、继承角色、临时权限、应用权限

#### 6.1.2 定期审查
- **接口路径**: `POST /api/v1/governance/access-review/campaigns`
- **功能描述**: 权限合理性审核
- **请求参数**:
```json
{
  "campaignName": "Q1权限审查",
  "reviewers": ["USER001", "USER002"],
  "scope": {
    "departments": ["DEPT001"],
    "roles": ["ROLE_MANAGER"]
  },
  "reviewPeriod": {
    "startDate": "2024-01-01",
    "endDate": "2024-03-31"
  },
  "dueDate": "2024-04-30"
}
```

#### 6.1.3 异常检测
- **接口路径**: `GET /api/v1/governance/access-review/anomalies`
- **功能描述**: 特权账户滥用监控
- **查询参数**: user_id, risk_level, time_range

#### 6.1.4 审批流程
- **接口路径**: `POST/GET/PUT /api/v1/governance/approvals`
- **功能描述**: 权限申请/变更审批
- **请求参数**:
```json
{
  "requestType": "GRANT|REVOKE|MODIFY",
  "requesterId": "USER001",
  "targetUserId": "USER002",
  "resourceType": "ROLE|PERMISSION",
  "resourceId": "ROLE_MANAGER",
  "reason": "项目需要",
  "validFrom": "2024-01-01T00:00:00Z",
  "validTo": "2024-06-30T23:59:59Z"
}
```

### 6.2 合规报告

#### 6.2.1 GDPR报告
- **接口路径**: `GET /api/v1/governance/compliance/gdpr`
- **功能描述**: 数据主体权利响应
- **报告类型**: DATA_ACCESS, DATA_DELETE, DATA_PORTABILITY

#### 6.2.2 SOX报告
- **接口路径**: `GET /api/v1/governance/compliance/sox`
- **功能描述**: 权限分离、审计轨迹
- **报告内容**: 权限分离矩阵、变更审计日志

#### 6.2.3 等保2.0报告
- **接口路径**: `GET /api/v1/governance/compliance/iso27001`
- **功能描述**: 身份认证安全要求
- **检查项**: 密码策略、MFA覆盖、会话管理、日志审计

#### 6.2.4 SOC2报告
- **接口路径**: `GET /api/v1/governance/compliance/soc2`
- **功能描述**: 访问控制合规证明
- **信任原则**: Security, Availability, Processing Integrity

### 6.3 权限分析

#### 6.3.1 权限可视化
- **接口路径**: `GET /api/v1/governance/analytics/permission-graph`
- **功能描述**: 用户-权限关系图
- **返回数据**: Graphviz/JSON格式的关系图

#### 6.3.2 权限冗余
- **接口路径**: `GET /api/v1/governance/analytics/redundancy`
- **功能描述**: 重复权限识别
- **分析维度**: 角色间重复、用户间重复

#### 6.3.3 最小权限
- **接口路径**: `POST /api/v1/governance/analytics/baseline`
- **功能描述**: 权限基线建立
- **请求参数**:
```json
{
  "roleCode": "ROLE_DEVELOPER",
  "actualPermissions": ["user:read", "user:write", "user:delete"],
  "baselinePermissions": ["user:read"]
}
```

#### 6.3.4 权限预测
- **接口路径**: `POST /api/v1/governance/analytics/predict`
- **功能描述**: 基于AI的权限推荐
- **请求参数**:
```json
{
  "userId": "USER001",
  "department": "研发中心",
  "position": "软件工程师",
  "projects": ["PROJECT_A", "PROJECT_B"]
}
```

---

## 模块七: 安全与隐私

### 7.1 隐私保护

#### 7.1.1 数据加密
- **接口路径**: `POST /api/v1/security/encrypt`
- **功能描述**: 静态/传输/内存数据加密
- **请求参数**:
```json
{
  "data": "敏感数据",
  "encryptType": "AES256|RSA",
  "keyId": "key_001"
}
```

#### 7.1.2 数据解密
- **接口路径**: `POST /api/v1/security/decrypt`
- **功能描述**: 数据解密

#### 7.1.3 数据脱敏
- **接口路径**: `POST /api/v1/security/masking`
- **功能描述**: 敏感信息脱敏展示
- **请求参数**:
```json
{
  "data": {
    "phone": "+8613800138000",
    "idCard": "110101199001011234",
    "email": "user@example.com"
  },
  "maskingRules": {
    "phone": "keep_prefix:3",
    "idCard": "keep_prefix:6",
    "email": "mask_middle"
  }
}
```

#### 7.1.4 数据删除
- **接口路径**: `POST /api/v1/privacy/data-delete`
- **功能描述**: GDPR被遗忘权实现
- **请求参数**:
```json
{
  "userId": "USER001",
  "deleteReason": "用户要求删除",
  "retentionBackup": true
}
```

#### 7.1.5 同意管理
- **接口路径**: `GET/POST /api/v1/privacy/consents`
- **功能描述**: 隐私政策同意收集
- **请求参数**:
```json
{
  "userId": "USER001",
  "consentType": "PRIVACY_POLICY|TERMS_OF_SERVICE|MARKETING",
  "version": "v2.0",
  "accepted": true,
  "acceptedAt": "2024-01-01T00:00:00Z"
}
```

### 7.2 安全监控

#### 7.2.1 威胁检测
- **接口路径**: `GET /api/v1/security/threats`
- **功能描述**: 异常登录、批量撞库
- **威胁类型**: BRUTE_FORCE, CREDENTIAL_STUFFING, ACCOUNT_TAKEOVER, DATA_EXFILTRATION

#### 7.2.2 行为分析
- **接口路径**: `GET /api/v1/security/behavior/{userId}`
- **功能描述**: 用户行为基线建立
- **返回数据**: 登录习惯、操作模式、访问路径

#### 7.2.3 设备指纹
- **接口路径**: `POST /api/v1/security/device-fingerprint`
- **功能描述**: 设备唯一标识生成
- **请求参数**:
```json
{
  "userAgent": "Mozilla/5.0...",
  "screenResolution": "1920x1080",
  "timezone": "Asia/Shanghai",
  "language": "zh-CN",
  "plugins": ["Chrome PDF Plugin"],
  "canvas": "canvas_hash"
}
```

#### 7.2.4 IP黑白名单
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/security/ip-rules`
- **功能描述**: 访问控制策略
- **请求参数**:
```json
{
  "ip": "1.2.3.4",
  "type": "WHITELIST|BLACKLIST",
  "reason": "公司IP",
  "effectiveFrom": "2024-01-01T00:00:00Z",
  "effectiveTo": "2024-12-31T23:59:59Z"
}
```

---

## 模块八: 多租户与运营管理

### 8.1 多租户架构

#### 8.1.1 租户管理
- **接口路径**: `GET/POST/PUT/DELETE /api/v1/tenant/tenants`
- **功能描述**: 多租户管理
- **请求参数**:
```json
{
  "tenantCode": "TENANT001",
  "tenantName": "示例公司",
  "domain": "example.openidaas.com",
  "plan": "ENTERPRISE",
  "isolationMode": "SCHEMA|DATABASE|ROW",
  "logo": "https://cdn.example.com/logo.png",
  "status": "ACTIVE"
}
```

#### 8.1.2 数据隔离配置
- **接口路径**: `PUT /api/v1/tenant/tenants/{tenantId}/isolation`
- **功能描述**: Schema/Database/Row级隔离配置

#### 8.1.3 资源配额
- **接口路径**: `PUT /api/v1/tenant/tenants/{tenantId}/quota`
- **功能描述**: 用户数/应用数/API调用限制
- **请求参数**:
```json
{
  "maxUsers": 1000,
  "maxApplications": 50,
  "maxApiCallsPerMonth": 1000000,
  "maxStorageGB": 100
}
```

#### 8.1.4 品牌定制
- **接口路径**: `PUT /api/v1/tenant/tenants/{tenantId}/branding`
- **功能描述**: 登录页面、邮件模板定制
- **请求参数**:
```json
{
  "theme": {
    "primaryColor": "#007AFF",
    "secondaryColor": "#5856D6",
    "logo": "https://cdn.example.com/logo.png"
  },
  "loginPage": {
    "backgroundImage": "https://cdn.example.com/bg.jpg",
    "customHtml": "<div>欢迎登录</div>"
  },
  "emailTemplates": {
    "welcome": "欢迎加入{{companyName}}...",
    "resetPassword": "点击链接重置密码..."
  }
}
```

#### 8.1.5 租户管理员
- **接口路径**: `POST /api/v1/tenant/tenants/{tenantId}/admins`
- **功能描述**: 独立管理员配置

### 8.2 运营分析

#### 8.2.1 用户统计
- **接口路径**: `GET /api/v1/analytics/users`
- **功能描述**: 活跃度、登录趋势
- **统计维度**: DAU/MAU、新增用户、流失用户、留存率

#### 8.2.2 应用统计
- **接口路径**: `GET /api/v1/analytics/applications`
- **功能描述**: SSO使用情况
- **统计维度**: 登录次数、活跃用户数、错误率

#### 8.2.3 安全统计
- **接口路径**: `GET /api/v1/analytics/security`
- **功能描述**: 风险事件统计
- **统计维度**: 异常登录、MFA使用率、密码强度分布

#### 8.2.4 计费管理
- **接口路径**: `GET /api/v1/analytics/billing`
- **功能描述**: 按用户数/调用量计费
- **返回数据**: 当前用量、费用明细、账单

---

## 通用模块规范

### 系统配置管理

- **接口路径**: `GET/PUT /api/v1/system/config`
- **功能描述**: 系统全局配置
- **配置项**: 密码策略、MFA策略、会话配置、安全策略

### 日志查询

- **接口路径**: `GET /api/v1/system/logs`
- **功能描述**: 操作日志、登录日志、审计日志
- **查询参数**: log_type, user_id, start_time, end_time, keyword

### 健康检查

- **接口路径**: `GET /api/v1/system/health`
- **功能描述**: 系统健康状态检查
- **检查项**: 数据库、Redis、消息队列、外部服务

### 接口版本

- **接口路径**: `GET /api/v1/system/version`
- **功能描述**: 获取当前系统版本信息

---

## 数据库表设计要点

根据上述API接口,数据库应包含以下核心表:

1. **用户相关**: `sys_user`, `sys_user_profile`, `sys_user_attribute`, `sys_user_status_history`
2. **角色权限**: `sys_role`, `sys_permission`, `sys_role_permission`, `sys_user_role`, `sys_dynamic_group`
3. **组织架构**: `sys_department`, `sys_position`, `sys_user_department`, `sys_delegation`
4. **认证相关**: `sys_mfa_config`, `sys_mfa_device`, `sys_social_account`, `sys_trusted_device`
5. **SSO相关**: `sys_oauth2_client`, `sys_saml_sp`, `sys_saml_idp`, `sys_oidc_provider`
6. **Token管理**: `sys_access_token`, `sys_refresh_token`, `sys_client_secret`
7. **应用目录**: `sys_application`, `sys_app_permission_mapping`, `sys_app_sso_config`
8. **联合身份**: `sys_scim_user`, `sys_scim_group`, `sys_sync_task`, `sys_sync_log`
9. **身份治理**: `sys_access_review`, `sys_approval_request`, `sys_gdpr_request`, `sys_consent`
10. **安全监控**: `sys_security_event`, `sys_threat_log`, `sys_behavior_log`, `sys_ip_rule`
11. **多租户**: `sys_tenant`, `sys_tenant_quota`, `sys_tenant_branding`
12. **系统管理**: `sys_config`, `sys_audit_log`, `sys_operation_log`

---

## 开发注意事项

1. **安全性**
   - 所有接口必须进行权限校验
   - 敏感数据传输必须加密
   - 密码必须加密存储(BCrypt)
   - SQL注入防护(使用参数化查询)

2. **性能优化**
   - 列表查询必须分页
   - 权限计算结果缓存
   - 数据库索引优化
   - 异步处理耗时操作

3. **可扩展性**
   - 支持水平扩展(无状态设计)
   - 微服务拆分准备
   - 插件化架构(MFA提供者、SSO协议)

4. **监控告警**
   - 接口响应时间监控
   - 错误率监控
   - 业务指标监控(登录数、注册数)
   - 异常事件告警

5. **测试覆盖**
   - 单元测试覆盖率>80%
   - 接口测试(Swagger/Postman)
   - 性能测试(压力测试)
   - 安全测试(渗透测试)

---

## 技术栈建议

### 后端框架
- Spring Boot 3.5.10
- Spring Security (认证授权)
- Spring Data JPA (数据访问)
- Spring Cache (缓存)
- Spring Validation (参数验证)
- Spring Scheduler (定时任务)

### 安全组件
- Spring Security SAML (SAML集成)
- Nimbus JOSE + JWT (JWT处理)
- BCrypt (密码加密)
- Apache Shiro (可选:更灵活的权限控制)

### 数据库
- MySQL 8.0+ (主数据库)
- Redis (缓存、会话)
- Liquibase/Flyway (数据库版本管理)

### 工具类库
- Lombok (减少样板代码)
- MapStruct (对象映射)
- Hutool (工具类库)
- Apache Commons (通用工具)

### 文档和测试
- SpringDoc OpenAPI 3 (Swagger文档)
- JUnit 5 (单元测试)
- Mockito (Mock测试)
- TestContainers (集成测试)

---

## API文档规范

每个接口必须包含以下Swagger注解:

```java
@Tag(name = "用户管理", description = "用户账户管理相关接口")
@Operation(summary = "用户注册", description = "支持邮箱验证、手机号验证、邀请码注册")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "注册成功"),
    @ApiResponse(responseCode = "400", description = "参数错误"),
    @ApiResponse(responseCode = "409", description = "用户已存在")
})
```

---

## 总结

本API提示词文档详细定义了《IDaaS系统》的核心功能模块和接口规范,包括:
- 8大核心功能模块
- 100+个API接口定义
- 统一的响应格式和错误码规范
- 数据库表设计要点
- 开发注意事项和技术栈建议

开发者应严格遵循本规范进行API开发,确保系统的安全性、可扩展性和可维护性。
