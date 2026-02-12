# IDaaS Admin UI

基于 Vue 3 + TypeScript + Element Plus 的企业级身份管理平台前端系统。

## 🚀 技术栈

- **核心框架**: Vue 3.4 + TypeScript 5.0
- **构建工具**: Vite 5.0
- **UI组件库**: Element Plus 2.4
- **状态管理**: Pinia 2.1
- **路由管理**: Vue Router 4.2
- **HTTP客户端**: Axios 1.6
- **测试框架**: Vitest + Vue Test Utils
- **代码规范**: ESLint + Prettier

## 📁 项目结构

```
src/
├── api/                    # API接口层
│   ├── auth.ts            # 认证相关接口
│   ├── user.ts            # 用户管理接口
│   ├── organization.ts    # 组织架构接口
│   ├── role.ts            # 角色权限接口
│   ├── application.ts     # 应用管理接口
│   ├── audit.ts           # 审计日志接口
│   └── system.ts          # 系统管理接口
├── assets/                # 静态资源
├── components/            # 通用组件
│   ├── Layout.vue         # 主布局组件
│   ├── Sidebar.vue        # 侧边栏组件
│   ├── Navbar.vue         # 顶部导航组件
│   ├── Pagination.vue     # 分页组件
│   └── ...
├── composables/           # Composition API逻辑复用
│   ├── useAuth.ts         # 认证逻辑
│   ├── usePermission.ts   # 权限控制
│   ├── useTable.ts        # 表格操作
│   └── useForm.ts         # 表单操作
├── layouts/               # 页面布局
├── locales/               # 国际化配置
├── modules/               # 业务模块
│   ├── user/              # 用户管理模块
│   ├── organization/      # 组织架构模块
│   ├── role/              # 角色权限模块
│   ├── application/       # 应用管理模块
│   ├── audit/             # 审计日志模块
│   └── system/            # 系统管理模块
├── pages/                 # 页面组件
├── plugins/               # 插件配置
├── router/                # 路由配置
├── stores/                # 状态管理
├── styles/                # 样式文件
├── types/                 # TypeScript类型定义
├── utils/                 # 工具函数
└── views/                 # 视图组件
```

## 🛠️ 开发环境搭建

### 环境要求

- Node.js >= 18.0.0
- npm >= 8.0.0 或 yarn >= 1.22.0

### 安装依赖

```bash
# 使用 npm
npm install

# 使用 yarn
yarn install
```

### 启动开发服务器

```bash
# 开发环境
npm run dev

# 生产环境构建
npm run build

# 预览生产构建
npm run preview

# 运行单元测试
npm run test:unit

# 运行端到端测试
npm run test:e2e

# 代码检查
npm run lint
```

## 🔧 配置说明

### 环境变量

项目支持多环境配置，在项目根目录下创建对应的环境文件：

```bash
# 开发环境
.env.development

# 生产环境
.env.production

# 测试环境
.env.test
```

主要环境变量配置：

```bash
# 应用配置
VITE_APP_TITLE=IDaaS管理系统
VITE_APP_SHORT_NAME=IDaaS
VITE_PORT=3000

# API配置
VITE_API_BASE_URL=http://localhost:8080

# 认证配置
VITE_JWT_SECRET_KEY=your_jwt_secret_key
VITE_TOKEN_EXPIRES_IN=3600

# 其他配置
VITE_APP_DEBUG=true
VITE_APP_MOCK=false
```

### 代理配置

在 `vite.config.ts` 中配置开发环境代理：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '/api')
    }
  }
}
```

## 🎯 核心功能

### 1. 认证授权
- JWT Token 认证机制
- 多因子认证支持
- 权限路由守卫
- 自动刷新令牌

### 2. 用户管理
- 用户增删改查
- 批量导入导出
- 用户状态管理
- 个人信息维护

### 3. 组织架构
- 部门树形管理
- 职位体系管理
- 组织架构可视化

### 4. 角色权限
- RBAC权限模型
- 权限分配管理
- 角色继承体系

### 5. 系统监控
- 操作日志审计
- 系统状态监控
- 性能指标展示

## 📱 响应式设计

项目采用响应式设计，支持多种设备：

- 桌面端 (≥1200px)
- 平板端 (768px-1199px)
- 移动端 (<768px)

## 🔒 安全特性

- XSS防护
- CSRF防护
- 输入验证
- 权限控制
- 数据加密

## 🧪 测试策略

### 单元测试
```bash
npm run test:unit
```

### 端到端测试
```bash
npm run test:e2e
```

### 测试覆盖率
```bash
npm run test:coverage
```

## 📦 部署指南

### 构建生产版本
```bash
npm run build
```

### Docker部署
```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 服务器部署
```bash
# 构建项目
npm run build

# 上传到服务器
scp -r dist/* user@server:/var/www/html/

# 配置Nginx
server {
    listen 80;
    server_name your-domain.com;
    root /var/www/html;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://backend-server;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🤝 贡献指南

### 代码规范
- 遵循 ESLint 和 Prettier 规范
- 使用 TypeScript 类型检查
- 编写单元测试
- 提交前运行 lint 和 test

### Git 提交规范
```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

### Pull Request 流程
1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 License

MIT License

## 📞 技术支持

如有问题，请联系：
- 邮箱: dev@qoobot.com
- GitHub: https://github.com/qoobot-com/openidaas

## 🙏 致谢

感谢以下开源项目的支持：
- Vue.js
- Element Plus
- Vite
- Pinia
- TypeScript