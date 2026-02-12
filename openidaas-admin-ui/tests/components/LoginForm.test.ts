import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import LoginForm from '@/views/login/index.vue'
import { useAuthStore } from '@/stores/auth'
import { createPinia, setActivePinia } from 'pinia'

describe('Login Form', () => {
  let wrapper: any
  
  beforeEach(() => {
    setActivePinia(createPinia())
    wrapper = mount(LoginForm, {
      global: {
        plugins: [createPinia()]
      }
    })
  })

  it('should render login form correctly', () => {
    expect(wrapper.find('.login-container')).toBeTruthy()
    expect(wrapper.find('input[placeholder="用户名"]')).toBeTruthy()
    expect(wrapper.find('input[placeholder="密码"]')).toBeTruthy()
    expect(wrapper.find('button[type="submit"]')).toBeTruthy()
  })

  it('should validate required fields', async () => {
    const submitButton = wrapper.find('button[type="submit"]')
    
    // 测试空表单提交
    await submitButton.trigger('click')
    
    // 应该显示验证错误
    expect(wrapper.findAll('.el-form-item__error')).toHaveLength(2)
  })

  it('should handle login submission', async () => {
    const authStore = useAuthStore()
    const loginSpy = vi.spyOn(authStore, 'login').mockResolvedValue({
      accessToken: 'test-token',
      refreshToken: 'test-refresh',
      expiresIn: 3600,
      tokenType: 'Bearer',
      userInfo: {
        id: 1,
        username: 'testuser',
        fullName: 'Test User',
        avatar: '',
        permissions: [],
        roles: []
      }
    })
    
    const usernameInput = wrapper.find('input[name="username"]')
    const passwordInput = wrapper.find('input[name="password"]')
    
    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')
    
    const submitButton = wrapper.find('button[type="submit"]')
    await submitButton.trigger('click')
    
    expect(loginSpy).toHaveBeenCalledWith({
      username: 'testuser',
      password: 'password123'
    })
  })

  it('should show error message on login failure', async () => {
    const authStore = useAuthStore()
    vi.spyOn(authStore, 'login').mockRejectedValue(new Error('Invalid credentials'))
    
    const usernameInput = wrapper.find('input[name="username"]')
    const passwordInput = wrapper.find('input[name="password"]')
    
    await usernameInput.setValue('wronguser')
    await passwordInput.setValue('wrongpass')
    
    const submitButton = wrapper.find('button[type="submit"]')
    await submitButton.trigger('click')
    
    // 等待异步操作完成
    await wrapper.vm.$nextTick()
    
    // 应该显示错误消息
    expect(wrapper.find('.el-message--error')).toBeTruthy()
  })
})