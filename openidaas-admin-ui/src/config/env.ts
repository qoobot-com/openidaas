// 环境变量配置
export const ENV_CONFIG = {
  // API 配置
  API_BASE_URL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  API_TIMEOUT: parseInt(import.meta.env.VITE_API_TIMEOUT || '15000'),
  
  // 应用配置
  APP_TITLE: import.meta.env.VITE_APP_TITLE || 'IDaaS管理系统',
  APP_SHORT_NAME: import.meta.env.VITE_APP_SHORT_NAME || 'IDaaS',
  
  // 认证配置
  JWT_SECRET_KEY: import.meta.env.VITE_JWT_SECRET_KEY || 'openidaas_jwt_secret',
  TOKEN_EXPIRES_IN: parseInt(import.meta.env.VITE_TOKEN_EXPIRES_IN || '3600'),
  
  // 上传配置
  UPLOAD_BASE_URL: import.meta.env.VITE_UPLOAD_BASE_URL || 'http://localhost:8080',
  UPLOAD_MAX_SIZE: parseInt(import.meta.env.VITE_UPLOAD_MAX_SIZE || '10'), // MB
  
  // 调试配置
  DEBUG: import.meta.env.VITE_APP_DEBUG === 'true',
  MOCK: import.meta.env.VITE_APP_MOCK === 'true'
}

// 环境判断
export const isDevelopment = import.meta.env.DEV
export const isProduction = import.meta.env.PROD
export const isTest = import.meta.env.MODE === 'test'