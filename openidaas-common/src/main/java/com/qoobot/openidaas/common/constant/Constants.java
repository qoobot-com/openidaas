package com.qoobot.openidaas.common.constant;

/**
 * 系统常量类
 *
 * @author QooBot
 */
public class Constants {

    /**
     * JWT相关常量
     */
    public static class Jwt {
        /** JWT令牌前缀 */
        public static final String TOKEN_PREFIX = "Bearer ";
        /** JWT头部名称 */
        public static final String HEADER_AUTHORIZATION = "Authorization";
        /** JWT用户ID声明 */
        public static final String CLAIM_USER_ID = "user_id";
        /** JWT用户名声明 */
        public static final String CLAIM_USERNAME = "username";
        /** JWT租户ID声明 */
        public static final String CLAIM_TENANT_ID = "tenant_id";
        /** JWT角色声明 */
        public static final String CLAIM_ROLES = "roles";
    }

    /**
     * Redis相关常量
     */
    public static class Redis {
        /** 访问令牌前缀 */
        public static final String ACCESS_TOKEN_PREFIX = "access_token:";
        /** 刷新令牌前缀 */
        public static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
        /** 用户信息前缀 */
        public static final String USER_INFO_PREFIX = "user_info:";
        /** 验证码前缀 */
        public static final String CAPTCHA_PREFIX = "captcha:";
        /** 登录失败次数前缀 */
        public static final String LOGIN_FAIL_COUNT_PREFIX = "login_fail_count:";
        
        /** 默认过期时间（秒） */
        public static final long DEFAULT_EXPIRE = 3600L;
        /** 访问令牌过期时间（秒） */
        public static final long ACCESS_TOKEN_EXPIRE = 3600L;
        /** 刷新令牌过期时间（秒） */
        public static final long REFRESH_TOKEN_EXPIRE = 86400L;
        /** 验证码过期时间（秒） */
        public static final long CAPTCHA_EXPIRE = 300L;
    }

    /**
     * 缓存相关常量
     */
    public static class Cache {
        /** 用户缓存名称 */
        public static final String USER_CACHE = "userCache";
        /** 角色缓存名称 */
        public static final String ROLE_CACHE = "roleCache";
        /** 权限缓存名称 */
        public static final String PERMISSION_CACHE = "permissionCache";
        /** 部门缓存名称 */
        public static final String DEPARTMENT_CACHE = "departmentCache";
        /** 字典缓存名称 */
        public static final String DICT_CACHE = "dictCache";
    }

    /**
     * 系统参数常量
     */
    public static class SysParam {
        /** 系统名称 */
        public static final String SYS_NAME = "sys.name";
        /** 系统版本 */
        public static final String SYS_VERSION = "sys.version";
        /** 系统管理员邮箱 */
        public static final String SYS_ADMIN_EMAIL = "sys.admin.email";
        /** 默认密码 */
        public static final String SYS_DEFAULT_PASSWORD = "sys.default.password";
        /** 密码最小长度 */
        public static final String SYS_PASSWORD_MIN_LENGTH = "sys.password.min.length";
        /** 密码最大长度 */
        public static final String SYS_PASSWORD_MAX_LENGTH = "sys.password.max.length";
        /** 登录失败锁定次数 */
        public static final String SYS_LOGIN_FAIL_LOCK_COUNT = "sys.login.fail.lock.count";
        /** 登录失败锁定时间（分钟） */
        public static final String SYS_LOGIN_FAIL_LOCK_TIME = "sys.login.fail.lock.time";
    }

    /**
     * 正则表达式常量
     */
    public static class Regex {
        /** 手机号正则 */
        public static final String MOBILE = "^1[3-9]\\d{9}$";
        /** 邮箱正则 */
        public static final String EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        /** 用户名正则（字母开头，允许字母数字下划线，3-20位） */
        public static final String USERNAME = "^[a-zA-Z][a-zA-Z0-9_]{2,19}$";
        /** 密码正则（至少包含字母和数字，8-20位） */
        public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,20}$";
    }

    /**
     * HTTP状态码常量
     */
    public static class HttpStatus {
        /** 成功 */
        public static final int SUCCESS = 200;
        /** 参数错误 */
        public static final int BAD_REQUEST = 400;
        /** 未授权 */
        public static final int UNAUTHORIZED = 401;
        /** 禁止访问 */
        public static final int FORBIDDEN = 403;
        /** 资源不存在 */
        public static final int NOT_FOUND = 404;
        /** 服务器内部错误 */
        public static final int INTERNAL_SERVER_ERROR = 500;
    }

    /**
     * 日期时间格式常量
     */
    public static class DateTime {
        /** 标准日期时间格式 */
        public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";
        /** 标准日期格式 */
        public static final String DATE_FORMAT = "yyyy-MM-dd";
        /** 标准时间格式 */
        public static final String TIME_FORMAT = "HH:mm:ss";
        /** 时间戳格式 */
        public static final String TIMESTAMP_FORMAT = "yyyyMMddHHmmss";
    }
}