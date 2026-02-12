// 验证工具函数
export const isExternal = (path: string): boolean => {
  return /^(https?:|mailto:|tel:)/.test(path)
}

export const isValidUsername = (str: string): boolean => {
  const validMap = ['admin', 'editor']
  return validMap.includes(str.trim())
}

export const isValidURL = (url: string): boolean => {
  const reg = /^(https?|ftp):\/\/([a-zA-Z0-9.-]+(:[a-zA-Z0-9.&%$-]+)*@)*((25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9][0-9]?)(\.(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}|([a-zA-Z0-9-]+\.)*[a-zA-Z0-9-]+\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(:[0-9]+)*(\/($|[a-zA-Z0-9.,?'\\+&%$#=~_-]+))*$/
  return reg.test(url)
}

export const isValidEmail = (email: string): boolean => {
  const reg = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
  return reg.test(email.toLowerCase())
}

export const isValidPhone = (phone: string): boolean => {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

export const isValidPassword = (password: string): boolean => {
  // 至少8位，包含大小写字母、数字和特殊字符
  const reg = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/
  return reg.test(password)
}

export const isArray = (arg: any): boolean => {
  if (typeof Array.isArray === 'undefined') {
    return Object.prototype.toString.call(arg) === '[object Array]'
  }
  return Array.isArray(arg)
}

export const isString = (str: any): boolean => {
  return typeof str === 'string' || str instanceof String
}

export const isNumber = (val: any): boolean => {
  return typeof val === 'number' && !isNaN(val)
}

export const isFunction = (fn: any): boolean => {
  return typeof fn === 'function'
}

export const isObject = (obj: any): boolean => {
  return obj !== null && typeof obj === 'object'
}

export const isEmpty = (val: any): boolean => {
  if (val === null || val === undefined || val === '') {
    return true
  }
  if (isArray(val) || isString(val)) {
    return val.length === 0
  }
  if (isObject(val)) {
    return Object.keys(val).length === 0
  }
  return false
}