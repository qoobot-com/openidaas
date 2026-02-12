// 认证相关类型
export interface LoginRequest {
  username: string
  password: string
  mfaCode?: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  tokenType: string
  userInfo: UserInfo
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface RefreshTokenResponse {
  accessToken: string
  expiresIn: number
}

export interface MFASetupRequest {
  factorType: 'SMS' | 'EMAIL' | 'TOTP' | 'HARDWARE_TOKEN'
  phoneNumber?: string
  email?: string
}

export interface MFASetupResponse {
  factorId: number
  qrCode?: string
  secret?: string
}

export interface UserInfo {
  id: number
  username: string
  fullName?: string
  avatar?: string
  permissions: string[]
  roles: string[]
}

export interface ResetPasswordRequest {
  userId: number
  newPassword: string
  confirmPassword: string
  resetReason?: string
}

export interface ResetPasswordResponse {
  userId: number
  resetTime: string
  resetBy: number
  message: string
}