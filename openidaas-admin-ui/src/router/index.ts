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
    path: '/organization',
    component: Layout,
    redirect: '/organization/departments',
    name: 'Organization',
    meta: {
      title: '组织管理',
      icon: 'office-building',
      permissions: ['organization:manage']
    },
    children: [
      {
        path: 'departments',
        component: () => import('@/modules/organization/DepartmentTree.vue'),
        name: 'DepartmentManagement',
        meta: {
          title: '部门管理',
          icon: 'location',
          permissions: ['organization:department']
        }
      },
      {
        path: 'positions',
        component: () => import('@/modules/organization/PositionList.vue'),
        name: 'PositionManagement',
        meta: {
          title: '职位管理',
          icon: 'user-filled',
          permissions: ['organization:position']
        }
      }
    ]
  },
  {
    path: '/role',
    component: Layout,
    redirect: '/role/list',
    name: 'Role',
    meta: {
      title: '角色管理',
      icon: 'tickets',
      permissions: ['role:manage']
    },
    children: [
      {
        path: 'list',
        component: () => import('@/modules/role/RoleList.vue'),
        name: 'RoleList',
        meta: {
          title: '角色列表',
          icon: 'list',
          permissions: ['role:list']
        }
      },
      {
        path: 'create',
        component: () => import('@/modules/role/RoleForm.vue'),
        name: 'RoleCreate',
        meta: {
          title: '创建角色',
          hidden: true,
          activeMenu: '/role/list'
        }
      },
      {
        path: 'edit/:id(\\d+)',
        component: () => import('@/modules/role/RoleForm.vue'),
        name: 'RoleEdit',
        meta: {
          title: '编辑角色',
          hidden: true,
          activeMenu: '/role/list'
        },
        props: true
      }
    ]
  },
  {
    path: '/audit',
    component: Layout,
    redirect: '/audit/logs',
    name: 'Audit',
    meta: {
      title: '审计管理',
      icon: 'document',
      permissions: ['audit:manage']
    },
    children: [
      {
        path: 'logs',
        component: () => import('@/modules/audit/AuditLogList.vue'),
        name: 'AuditLogs',
        meta: {
          title: '操作日志',
          icon: 'tickets',
          permissions: ['audit:log']
        }
      },
      {
        path: 'events',
        component: () => import('@/modules/audit/SecurityEventList.vue'),
        name: 'SecurityEvents',
        meta: {
          title: '安全事件',
          icon: 'warning',
          permissions: ['audit:event']
        }
      },
      {
        path: 'statistics',
        component: () => import('@/modules/audit/AuditStatistics.vue'),
        name: 'AuditStatistics',
        meta: {
          title: '统计分析',
          icon: 'data-analysis',
          permissions: ['audit:statistics']
        }
      }
    ]
  },
  {
    path: '/application',
    component: Layout,
    redirect: '/application/list',
    name: 'Application',
    meta: {
      title: '应用管理',
      icon: 'platform',
      permissions: ['application:manage']
    },
    children: [
      {
        path: 'list',
        name: 'ApplicationList',
        component: () => import('@/modules/application/ApplicationList.vue'),
        meta: {
          title: '应用列表',
          icon: 'list',
          permissions: ['application:list']
        }
      },
      {
        path: 'create',
        component: () => import('@/modules/application/ApplicationForm.vue'),
        name: 'ApplicationCreate',
        meta: {
          title: '新增应用',
          hidden: true,
          activeMenu: '/application/list'
        }
      },
      {
        path: 'edit/:id(\\d+)',
        component: () => import('@/modules/application/ApplicationForm.vue'),
        name: 'ApplicationEdit',
        meta: {
          title: '编辑应用',
          hidden: true,
          activeMenu: '/application/list'
        },
        props: true
      },
      {
        path: 'detail/:id(\\d+)',
        component: () => import('@/modules/application/AppDetail.vue'),
        name: 'ApplicationDetail',
        meta: {
          title: '应用详情',
          hidden: true,
          activeMenu: '/application/list'
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
  }
]

// 动态路由

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