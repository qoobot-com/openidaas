import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { constantRoutes, asyncRoutes } from '@/router'

// 过滤有权限的路由
function filterAsyncRoutes(routes: any[], permissions: string[]) {
  const res: any[] = []
  
  routes.forEach(route => {
    const tmp = { ...route }
    if (hasPermission(permissions, tmp)) {
      if (tmp.children) {
        tmp.children = filterAsyncRoutes(tmp.children, permissions)
      }
      res.push(tmp)
    }
  })
  
  return res
}

function hasPermission(permissions: string[], route: any) {
  if (route.meta && route.meta.permissions) {
    return permissions.some(permission => route.meta.permissions.includes(permission))
  } else {
    return true
  }
}

export const usePermissionStore = defineStore('permission', () => {
  // 状态
  const routes = ref<any[]>([])
  const addRoutes = ref<any[]>([])

  // 计算属性
  const allRoutes = computed(() => [...constantRoutes, ...addRoutes.value])

  // Actions
  const generateRoutes = (permissions: string[]) => {
    return new Promise(resolve => {
      let accessedRoutes
      if (permissions.includes('admin')) {
        accessedRoutes = asyncRoutes || []
      } else {
        accessedRoutes = filterAsyncRoutes(asyncRoutes, permissions)
      }
      routes.value = constantRoutes.concat(accessedRoutes)
      addRoutes.value = accessedRoutes
      resolve(accessedRoutes)
    })
  }

  return {
    // 状态
    routes,
    addRoutes,
    
    // 计算属性
    allRoutes,
    
    // Actions
    generateRoutes
  }
})