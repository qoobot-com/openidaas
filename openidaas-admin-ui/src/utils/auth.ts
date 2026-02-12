// 认证工具函数
const TOKEN_KEY = 'access_token'

export const getToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY)
}

export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token)
}

export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY)
}

export const clearAuth = (): void => {
  removeToken()
  // 清除其他认证相关信息
  localStorage.removeItem('refresh_token')
  sessionStorage.clear()
}