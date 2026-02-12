// 存储工具函数
export const setStorage = (key: string, value: any, storage: Storage = localStorage): void => {
  try {
    storage.setItem(key, JSON.stringify(value))
  } catch (error) {
    console.error('存储数据失败:', error)
  }
}

export const getStorage = (key: string, storage: Storage = localStorage): any => {
  try {
    const item = storage.getItem(key)
    return item ? JSON.parse(item) : null
  } catch (error) {
    console.error('获取存储数据失败:', error)
    return null
  }
}

export const removeStorage = (key: string, storage: Storage = localStorage): void => {
  try {
    storage.removeItem(key)
  } catch (error) {
    console.error('删除存储数据失败:', error)
  }
}

export const clearStorage = (storage: Storage = localStorage): void => {
  try {
    storage.clear()
  } catch (error) {
    console.error('清空存储失败:', error)
  }
}

// 会话存储快捷方法
export const setSession = (key: string, value: any): void => {
  setStorage(key, value, sessionStorage)
}

export const getSession = (key: string): any => {
  return getStorage(key, sessionStorage)
}

export const removeSession = (key: string): void => {
  removeStorage(key, sessionStorage)
}