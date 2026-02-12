import { http } from '@/utils/http'

/**
 * 登录请求
 */
export interface LoginRequest {
  username: string
  password: string
  mfaCode?: string
}

/**
 * 登录响应
 */
export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType: string
  userInfo: {
    id: number
    username: string
    email?: string
  }
}

/**
 * 刷新令牌请求
 */
export interface RefreshTokenRequest {
  refreshToken: string
}

/**
 * 刷新令牌响应
 */
export interface RefreshTokenResponse {
  accessToken: string
  expiresIn: number
}

/**
 * MFA设置请求
 */
export interface MFASetupRequest {
  factorType: 'SMS' | 'EMAIL' | 'TOTP' | 'HARDWARE_TOKEN'
  phoneNumber?: string
  email?: string
}

/**
 * MFA设置响应
 */
export interface MFASetupResponse {
  factorId: number
  qrCode?: string
  secret?: string
}

/**
 * 重置密码请求
 */
export interface ResetPasswordRequest {
  userId: number
  oldPassword?: string
  newPassword: string
  confirmPassword: string
  resetReason?: string
}

/**
 * 认证API
 */
export const authApi = {
  /**
   * 用户登录
   */
  login: (data: LoginRequest) => http.post<LoginResponse>('/api/auth/login', data),

  /**
   * 用户登出
   */
  logout: () => http.post('/api/auth/logout'),

  /**
   * 刷新访问令牌
   */
  refreshToken: (data: RefreshTokenRequest) => http.post<RefreshTokenResponse>('/api/auth/refresh', data),

  /**
   * 设置多因子认证
   */
  setupMFA: (data: MFASetupRequest) => http.post<MFASetupResponse>('/api/auth/mfa/setup', data),

  /**
   * 重置密码
   */
  resetPassword: (data: ResetPasswordRequest) => http.post('/api/auth/reset-password', data)
}
