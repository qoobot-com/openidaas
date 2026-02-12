import { http } from '@/utils/http'

/**
 * MFA因子类型
 */
export type MFAType = 'TOTP' | 'SMS' | 'EMAIL' | 'BACKUP_CODE' | 'HARDWARE_TOKEN' | 'BIOMETRIC'

/**
 * MFA状态
 */
export enum MFAStatus {
  ACTIVE = 1,
  PENDING = 2,
  DISABLED = 3,
  DELETED = 4
}

/**
 * TOTP设置响应
 */
export interface TOTPSetupResponse {
  factorId: number
  secret: string
  otpAuthURI: string
  qrCode: string
  remainingSeconds: number
}

/**
 * 激活TOTP请求
 */
export interface ActivateTOTPRequest {
  secret: string
  code: string
}

/**
 * 发送短信验证码请求
 */
export interface SendSMSRequest {
  phoneNumber: string
}

/**
 * 发送邮箱验证码请求
 */
export interface SendEmailRequest {
  email: string
}

/**
 * 备用码响应
 */
export interface BackupCodesResponse {
  factorId: number
  codes: string[]
  count: number
}

/**
 * MFA因子信息
 */
export interface MFAFactor {
  id: number
  factorType: MFAType
  factorName: string
  isPrimary: boolean
  status: MFAStatus
  statusText: string
  lastUsedAt?: string
  verificationCount: number
}

/**
 * MFA偏好设置
 */
export interface MFAPreferences {
  userId: number
  mfaEnabled: boolean
  factors: MFAFactor[]
}

/**
 * MFA API
 */
export const mfaApi = {
  /**
   * 生成TOTP设置信息
   */
  generateTOTPSetup: (userId: number) =>
    http.post<TOTPSetupResponse>('/api/auth/mfa/setup/totp', null, {
      headers: { 'X-User-Id': userId.toString() }
    }),

  /**
   * 验证并激活TOTP
   */
  activateTOTP: (userId: number, data: ActivateTOTPRequest) =>
    http.post('/api/auth/mfa/activate/totp', data, {
      headers: { 'X-User-Id': userId.toString() }
    }),

  /**
   * 发送短信验证码
   */
  sendSMSCode: (userId: number, data: SendSMSRequest) =>
    http.post('/api/auth/mfa/send-sms', data, {
      headers: { 'X-User-Id': userId.toString() }
    }),

  /**
   * 发送邮箱验证码
   */
  sendEmailCode: (userId: number, data: SendEmailRequest) =>
    http.post('/api/auth/mfa/send-email', data, {
      headers: { 'X-User-Id': userId.toString() }
    }),

  /**
   * 生成备用码
   */
  generateBackupCodes: (userId: number, count = 10) =>
    http.post<BackupCodesResponse>(`/api/auth/mfa/backup-codes?count=${count}`, null, {
      headers: { 'X-User-Id': userId.toString() }
    }),

  /**
   * 获取MFA偏好设置
   */
  getMFAPreferences: (userId: number) =>
    http.get<MFAPreferences>('/api/auth/mfa/preferences', {
      headers: { 'X-User-Id': userId.toString() }
    }),

  /**
   * 禁用MFA因子
   */
  disableMFAFactor: (userId: number, factorId: number) =>
    http.delete(`/api/auth/mfa/factors/${factorId}`, {
      headers: { 'X-User-Id': userId.toString() }
    }),

  /**
   * 设置主MFA方式
   */
  setPrimaryMFA: (userId: number, factorId: number) =>
    http.put(`/api/auth/mfa/factors/${factorId}/primary`, null, {
      headers: { 'X-User-Id': userId.toString() }
    })
}
