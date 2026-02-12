import router from '@/router'
import { useAuthStore } from '@/stores/auth'
import { useTagsViewStore } from '@/stores/tagsView'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'

NProgress.configure({ showSpinner: false })

// 页面白名单
const whiteList = ['/login', '/404', '/401']

router.beforeEach(async (to, _from, next) => {
  NProgress.start()
  
  const authStore = useAuthStore()
  const tagsViewStore = useTagsViewStore()
  
  // 添加标签页
  if (to.name && !to.meta?.hidden) {
    tagsViewStore.addVisitedView(to)
    tagsViewStore.addCachedView(to)
  }
  
  // 白名单直接通过
  if (whiteList.includes(to.path)) {
    next()
    return
  }
  
  // 检查登录状态
  if (!authStore.isAuthenticated) {
    next(`/login?redirect=${to.path}`)
    NProgress.done()
    return
  }
  
  // 已登录且要跳转到登录页，则重定向到首页
  if (to.path === '/login') {
    next({ path: '/' })
    NProgress.done()
    return
  }
  
  // 检查权限
  if (to.meta?.permissions) {
    const requiredPermissions = to.meta.permissions as string[]
    const hasPermission = authStore.hasAnyPermission(requiredPermissions)
    
    if (!hasPermission) {
      next('/401')
      NProgress.done()
      return
    }
  }
  
  next()
})

router.afterEach(() => {
  NProgress.done()
})