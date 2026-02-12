/**
 * 应用相关类型定义
 */

/**
 * 应用类型枚举
 */
export enum ApplicationTypeEnum {
  WEB = 1,
  MOBILE = 2,
  API = 3,
  DESKTOP = 4,
  SERVICE = 5
}

/**
 * 状态枚举
 */
export enum StatusEnum {
  ENABLED = 1,
  DISABLED = 2
}

/**
 * 应用信息
 */
export interface Application {
  id: number
  appKey: string
  appName: string
  appType: ApplicationTypeEnum
  appTypeDesc: string
  redirectUris?: string
  logoUrl?: string
  homepageUrl?: string
  description?: string
  status: StatusEnum
  statusDesc: string
  ownerId?: number
  ownerName?: string
  oauth2Client?: {
    id: number
    clientId: string
    grantTypes?: string
    scopes?: string
    accessTokenValidity?: number
    refreshTokenValidity?: number
    autoApprove?: boolean
  }
  samlSp?: {
    id: number
    spEntityId: string
    acsUrl: string
    metadataUrl?: string
  }
  createdAt: string
  updatedAt: string
}

/**
 * 应用创建请求
 */
export interface ApplicationCreate {
  appName: string
  appType: ApplicationTypeEnum
  redirectUris?: string[]
  logoUrl?: string
  homepageUrl?: string
  description?: string
  ownerId?: number
}

/**
 * 应用更新请求
 */
export interface ApplicationUpdate {
  id: number
  appName?: string
  appType?: ApplicationTypeEnum
  redirectUris?: string[]
  logoUrl?: string
  homepageUrl?: string
  description?: string
  status?: StatusEnum
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
  first: boolean
  last: boolean
}
