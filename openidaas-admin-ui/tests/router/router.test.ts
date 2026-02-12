import { describe, it, expect, vi } from 'vitest'
import { createRouter, createWebHistory } from 'vue-router'
import routes from '@/router/routes'

describe('Router', () => {
  let router: ReturnType<typeof createRouter>

  beforeEach(() => {
    router = createRouter({
      history: createWebHistory(),
      routes
    })
  })

  it('should have all required routes', () => {
    const expectedRoutes = [
      '/dashboard',
      '/user/list',
      '/organization/departments',
      '/role/list',
      '/permission/tree',
      '/audit/statistics',
      '/audit/log/list',
      '/audit/security-events'
    ]

    expectedRoutes.forEach(path => {
      const route = router.resolve(path)
      expect(route.matched.length).toBeGreaterThan(0)
    })
  })

  it('should have route meta information', () => {
    const userRoute = router.resolve('/user/list')
    expect(userRoute.meta).toBeDefined()
    expect(userRoute.meta.title).toBeDefined()
  })

  it('should handle dynamic route parameters', () => {
    const route = router.resolve('/user/detail/1')
    expect(route.params.id).toBe('1')
  })

  it('should have 404 route', () => {
    const notFoundRoute = router.resolve('/non-existent-route')
    expect(notFoundRoute.matched[notFoundRoute.matched.length - 1].name).toBe('NotFound')
  })

  it('should have login route', () => {
    const route = router.resolve('/login')
    expect(route.matched.length).toBeGreaterThan(0)
  })
})
