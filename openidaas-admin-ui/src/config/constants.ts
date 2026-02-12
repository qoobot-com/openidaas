// 常量配置
export const CONSTANTS = {
  // 存储键名
  STORAGE_KEYS: {
    ACCESS_TOKEN: 'access_token',
    REFRESH_TOKEN: 'refresh_token',
    USER_INFO: 'user_info',
    LANGUAGE: 'language',
    THEME: 'theme',
    SIDEBAR_STATUS: 'sidebar_status'
  },
  
  // 路由常量
  ROUTE_NAMES: {
    LOGIN: 'Login',
    DASHBOARD: 'Dashboard',
    USER_LIST: 'UserList',
    ROLE_LIST: 'RoleList'
  },
  
  // 状态常量
  USER_STATUS: {
    ACTIVE: 'ACTIVE',
    LOCKED: 'LOCKED',
    DISABLED: 'DISABLED',
    DELETED: 'DELETED'
  },
  
  // 性别常量
  GENDER: {
    UNKNOWN: 0,
    MALE: 1,
    FEMALE: 2
  },
  
  // 分页常量
  PAGINATION: {
    DEFAULT_PAGE_SIZE: 10,
    PAGE_SIZES: [10, 20, 50, 100],
    DEFAULT_PAGE: 1
  },
  
  // 文件上传常量
  UPLOAD: {
    MAX_SIZE: 10 * 1024 * 1024, // 10MB
    ACCEPTED_TYPES: ['image/jpeg', 'image/png', 'image/gif'],
    ACCEPTED_EXTENSIONS: ['.jpg', '.jpeg', '.png', '.gif']
  }
}