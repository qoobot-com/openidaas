import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import Layout from '@/components/Layout.vue'

// 静态路由
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/redirect',
    component: Layout,
    meta: { hidden: true },
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/redirect/index.vue')
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    meta: { hidden: true }
  },
  {
    path: '/404',
    component: () => import('@/views/error-page/404.vue'),
    meta: { hidden: true }
  },
  {
    path: '/401',
    component: () => import('@/views/error-page/401.vue'),
    meta: { hidden: true }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        component: () => import('@/modules/system/Dashboard.vue'),
        name: 'Dashboard',
        meta: { 
          title: '首页', 
          icon: 'dashboard', 
          affix: true,
          permissions: ['dashboard:view']
        }
      }
    ]
  },
  {
    path: '/profile',
    component: Layout,
    redirect: '/profile/index',
    meta: { hidden: true },
    children: [
      {
        path: 'index',
        component: () => import('@/modules/system/Profile.vue'),
        name: 'Profile',
        meta: { title: '个人中心', icon: 'user' }
      }
    ]
  },
  {
    path: '/application',
    component: Layout,
    meta: { title: '应用管理', icon: 'Platform' },
    children: [
      {
        path: 'list',
        name: 'ApplicationList',
        component: () => import('@/modules/application/ApplicationList.vue'),
        meta: { title: '应用列表', icon: 'List' }
      }
    ]
  }
]

// 动态路由
export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/user',
    component: Layout,
    redirect: '/user/list',
    name: 'User',
    meta: {
      title: '用户管理',
      icon: 'user',
      permissions: ['user:manage']
    },
    children: [
      {
        path: 'list',
        component: () => import('@/modules/user/UserList.vue'),
        name: 'UserList',
        meta: { 
          title: '用户列表', 
          icon: 'list',
          permissions: ['user:list']
        }
      },
      {
        path: 'detail/:id(\\d+)',
        component: () => import('@/modules/user/UserDetail.vue'),
        name: 'UserDetail',
        meta: { 
          title: '用户详情', 
          hidden: true,
          activeMenu: '/user/list'
        },
        props: true
      },
      {
        path: 'create',
        component: () => import('@/modules/user/UserForm.vue'),
        name: 'UserCreate',
        meta: { 
          title: '新增用户', 
          hidden: true,
          activeMenu: '/user/list'
        }
      },
      {
        path: 'edit/:id(\\d+)',
        component: () => import('@/modules/user/UserForm.vue'),
        name: 'UserEdit',
        meta: { 
          title: '编辑用户', 
          hidden: true,
          activeMenu: '/user/list'
        },
        props: true
      }
    ]
  },
  {
    path: '/system',
    component: Layout,
    redirect: '/system/config',
    name: 'System',
    meta: {
      title: '系统管理',
      icon: 'setting',
      permissions: ['system:manage']
    },
    children: [
      {
        path: 'config',
        component: () => import('@/modules/system/SystemConfig.vue'),
        name: 'SystemConfig',
        meta: { 
          title: '系统配置', 
          icon: 'config',
          permissions: ['system:config']
        }
      }
    ]
  },
  {
    path: '/application',
    component: Layout,
    meta: { title: '应用管理', icon: 'Platform' },
    children: [
      {
        path: 'list',
        name: 'ApplicationList',
        component: () => import('@/modules/application/ApplicationList.vue'),
        meta: { title: '应用列表', icon: 'List' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory('/'),
  routes: constantRoutes,
  scrollBehavior: () => ({ left: 0, top: 0 })
})

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  
  // 白名单路径
  const whiteList = ['/login', '/404', '/401']
  
  if (whiteList.includes(to.path)) {
    next()
    return
  }
  
  // 检查是否已登录
  if (!authStore.isAuthenticated) {
    next(`/login?redirect=${to.path}`)
    return
  }
  
  // 检查权限
  if (to.meta?.permissions) {
    const hasPermission = authStore.hasAnyPermission(to.meta.permissions as string[])
    if (!hasPermission) {
      next('/401')
      return
    }
  }
  
  next()
})

// 重置路由
export function resetRouter() {
  // Vue Router 4 不再需要手动重置matcher
  // 这里可以添加其他重置逻辑
}

export default router