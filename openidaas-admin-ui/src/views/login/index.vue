<template>
  <div class="login-container">
    <!-- 背景动画效果 -->
    <div class="background-animation">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
      <div class="floating-shape shape-4"></div>
    </div>
    
    <el-form
      ref="loginFormRef"
      :model="loginForm"
      :rules="loginRules"
      class="login-form"
      autocomplete="on"
      label-position="left"
    >
      <div class="title-container">
        <div class="logo-wrapper">
          <div class="logo-animation">
            <svg viewBox="0 0 100 100" class="logo-svg">
              <circle cx="50" cy="50" r="45" fill="none" stroke="#409EFF" stroke-width="2"/>
              <path d="M30,50 L45,65 L70,35" fill="none" stroke="#409EFF" stroke-width="3" stroke-linecap="round"/>
            </svg>
          </div>
        </div>
        <h1 class="title">{{ title }}</h1>
        <p class="subtitle">安全可靠的身份认证平台</p>
      </div>

      <!-- 用户名/邮箱输入 -->
      <el-form-item prop="username">
        <div class="input-wrapper">
          <span class="input-icon">
            <el-icon><user /></el-icon>
          </span>
          <el-input
            ref="usernameRef"
            v-model="loginForm.username"
            placeholder="请输入用户名/邮箱/手机号"
            name="username"
            type="text"
            tabindex="1"
            autocomplete="on"
            class="animated-input"
            @focus="handleInputFocus"
            @blur="handleInputBlur"
          />
        </div>
        <div class="input-hint">支持用户名(3-20位字母数字)、邮箱或手机号登录</div>
      </el-form-item>

      <!-- 密码输入 -->
      <el-tooltip
        v-model:visible="capsTooltip"
        content="大写锁定已开启"
        placement="right"
        manual
      >
        <el-form-item prop="password">
          <div class="input-wrapper">
            <span class="input-icon">
              <el-icon><lock /></el-icon>
            </span>
            <el-input
              :key="passwordType"
              ref="passwordRef"
              v-model="loginForm.password"
              :type="passwordType"
              placeholder="请输入密码"
              name="password"
              tabindex="2"
              autocomplete="on"
              class="animated-input"
              @keyup.enter="handleLogin"
              @blur="capsTooltip = false"
              @keyup="checkCapslock"
              @focus="handleInputFocus"
            />
            <span class="show-pwd" @click="showPwd">
              <el-icon>
                <view v-if="passwordType === 'password'" />
                <hide v-else />
              </el-icon>
            </span>
          </div>
          <div class="input-hint">建议6-20位，包含字母和数字</div>
        </el-form-item>
      </el-tooltip>

      <!-- 验证码输入 -->
      <el-form-item v-if="showMFA" prop="mfaCode">
        <div class="input-wrapper">
          <span class="input-icon">
            <el-icon><key /></el-icon>
          </span>
          <el-input
            ref="mfaCodeRef"
            v-model="loginForm.mfaCode"
            placeholder="请输入验证码"
            name="mfaCode"
            type="text"
            tabindex="3"
            autocomplete="off"
            maxlength="6"
            class="animated-input"
            @focus="handleInputFocus"
          />
          <span class="refresh-captcha" @click="refreshCaptcha">
            <el-icon><refresh /></el-icon>
            看不清？换一张
          </span>
        </div>
      </el-form-item>

      <!-- 登录按钮 -->
      <el-button
        :loading="loading"
        type="primary"
        class="login-button animated-button"
        @click.prevent="handleLogin"
        :disabled="isButtonDisabled"
      >
        <span v-if="!loading">安全登录</span>
        <span v-else>登录中...</span>
      </el-button>

      <!-- 记住我 -->
      <div class="remember-me">
        <el-checkbox v-model="rememberMe">记住登录状态</el-checkbox>
      </div>

      <!-- 辅助链接 -->
      <div class="auth-links">
        <router-link to="/forgot-password" class="link-item">
          忘记密码？
        </router-link>
        <span class="divider">|</span>
        <router-link to="/register" class="link-item">
          立即注册
        </router-link>
      </div>

      <!-- 第三方登录 -->
      <div class="social-login">
        <p class="social-title">或使用以下方式登录</p>
        <div class="social-icons">
          <el-button circle class="social-btn" @click="handleSocialLogin('wechat')">
            <el-icon><chat-dot-round /></el-icon>
          </el-button>
          <el-button circle class="social-btn" @click="handleSocialLogin('google')">
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
          </el-button>
        </div>
      </div>

      <!-- 安全提示 -->
      <div class="security-tip" v-if="showSecurityTip">
        <el-alert
          title="检测到新设备登录"
          type="info"
          description="已发送验证邮件至您的邮箱，请查收"
          show-icon
          :closable="false"
        />
      </div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElForm, ElNotification, ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { User, Lock, View, Hide, Key, Refresh, ChatDotRound } from '@element-plus/icons-vue'
import { computed } from 'vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref<InstanceType<typeof ElForm>>()
const usernameRef = ref<HTMLInputElement>()
const passwordRef = ref<HTMLInputElement>()
const mfaCodeRef = ref<HTMLInputElement>()

const title = 'IDaaS管理系统'
const loading = ref(false)
const passwordType = ref('password')
const capsTooltip = ref(false)
const showMFA = ref(false)
const rememberMe = ref(false)
const showSecurityTip = ref(false)
const loginAttempts = ref(0)

// 计算属性
const isButtonDisabled = computed(() => {
  return loading.value || 
         !loginForm.username.trim() || 
         !loginForm.password.trim()
})

const loginForm = reactive({
  username: '',
  password: '',
  mfaCode: ''
})

const validateUsername = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入用户名、邮箱或手机号'))
  } else if (/^[a-zA-Z0-9_]{3,20}$/.test(value)) {
    // 支持3-20位的字母数字下划线组合
    callback()
  } else if (/^([\w\.\-]+)@([\w\-]+\.)+([\w]{2,})$/.test(value)) {
    // 邮箱格式
    callback()
  } else if (/^1[3-9]\d{9}$/.test(value)) {
    // 手机号格式
    callback()
  } else {
    callback(new Error('用户名支持3-20位字母数字组合，或有效的邮箱/手机号'))
  }
}

const validatePassword = (_rule: any, value: string, callback: any) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else if (value.length > 20) {
    callback(new Error('密码长度不能超过20位'))
  } else {
    callback()
  }
}

const loginRules = {
  username: [
    { validator: validateUsername, trigger: 'blur' },
    { required: true, message: '请输入邮箱或手机号', trigger: 'blur' }
  ],
  password: [
    { validator: validatePassword, trigger: 'blur' },
    { required: true, message: '请输入密码', trigger: 'blur' }
  ],
  mfaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码格式不正确', trigger: 'blur' }
  ]
}

const checkCapslock = (e: KeyboardEvent) => {
  const { key } = e
  capsTooltip.value = !!(key && key.length === 1 && key >= 'A' && key <= 'Z')
}

const showPwd = () => {
  if (passwordType.value === 'password') {
    passwordType.value = ''
  } else {
    passwordType.value = 'password'
  }
  nextTick(() => {
    passwordRef.value?.focus()
  })
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login({
          username: loginForm.username,
          password: loginForm.password,
          mfaCode: loginForm.mfaCode || undefined
        })
        
        // 登录成功处理
        ElNotification.success({
          title: '登录成功',
          message: '正在为您跳转...',
          duration: 1500
        })
        
        // 记住登录状态
        if (rememberMe.value) {
          localStorage.setItem('rememberMe', 'true')
        }
        
        // 登录成功后的跳转
        const redirect = route.query.redirect as string || '/'
        setTimeout(() => {
          router.push({ path: redirect })
        }, 1500)
        
      } catch (error: any) {
        console.error('登录失败:', error)
        loginAttempts.value++
        
        // 根据错误类型显示不同的提示
        let errorMessage = ''
        let errorTitle = '登录失败'
        
        switch (error.code) {
          case 'ACCOUNT_NOT_FOUND':
            errorMessage = '账号不存在，请检查输入或立即注册'
            break
          case 'INVALID_CREDENTIALS':
            const remainingAttempts = Math.max(0, 5 - loginAttempts.value)
            errorMessage = `密码错误，您还有${remainingAttempts}次尝试机会`
            if (remainingAttempts <= 2) {
              errorMessage += ' 或 忘记密码？'
            }
            break
          case 'ACCOUNT_LOCKED':
            errorMessage = '账号因多次输错密码已锁定，请15分钟后重试或联系客服'
            break
          case 'MFA_REQUIRED':
            showMFA.value = true
            nextTick(() => {
              mfaCodeRef.value?.focus()
            })
            return
          case 'INVALID_MFA_CODE':
            errorMessage = '验证码错误，请重新输入'
            break
          case 'NETWORK_ERROR':
            errorMessage = '网络连接失败，请检查网络后重试'
            break
          default:
            errorMessage = error.message || '登录失败，请稍后重试'
        }
        
        ElNotification.error({
          title: errorTitle,
          message: errorMessage,
          duration: 4000
        })
      } finally {
        loading.value = false
      }
    }
  })
}

// 新增方法
const handleInputFocus = (event: FocusEvent) => {
  const target = event.target as HTMLElement
  target.parentElement?.classList.add('focused')
}

const handleInputBlur = (event: FocusEvent) => {
  const target = event.target as HTMLElement
  target.parentElement?.classList.remove('focused')
}

const refreshCaptcha = () => {
  // 刷新验证码逻辑
  ElMessage.info('验证码已刷新')
}

const handleSocialLogin = (provider: string) => {
  ElMessage.info(`正在使用${provider === 'wechat' ? '微信' : 'Google'}登录`) 
  // 社交登录逻辑
}

onMounted(() => {
  // 检查是否记住登录状态
  const remember = localStorage.getItem('rememberMe')
  if (remember === 'true') {
    rememberMe.value = true
  }
  
  // 检查是否已登录
  if (authStore.isAuthenticated) {
    router.push({ path: '/' })
  }
  
  // 模拟安全提示（实际项目中应根据真实情况判断）
  // showSecurityTip.value = true
})
</script>

<style lang="scss" scoped>
// 导入动画库
@import "animate.css";

.login-container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  
  // 背景动画效果
  .background-animation {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    overflow: hidden;
    z-index: 1;
    
    .floating-shape {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.1);
      animation: float 15s infinite ease-in-out;
      
      &.shape-1 {
        width: 80px;
        height: 80px;
        top: 10%;
        left: 5%;
        animation-delay: 0s;
      }
      
      &.shape-2 {
        width: 120px;
        height: 120px;
        top: 70%;
        right: 10%;
        animation-delay: 2s;
      }
      
      &.shape-3 {
        width: 60px;
        height: 60px;
        bottom: 20%;
        left: 15%;
        animation-delay: 4s;
      }
      
      &.shape-4 {
        width: 100px;
        height: 100px;
        top: 40%;
        right: 20%;
        animation-delay: 6s;
      }
    }
  }
  
  .login-form {
    position: relative;
    width: 450px;
    max-width: 95%;
    padding: 45px 40px;
    background: rgba(255, 255, 255, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 20px;
    box-shadow: 0 25px 50px rgba(0, 0, 0, 0.2);
    z-index: 2;
    animation: fadeInUp 0.8s ease-out;
    
    @media (max-width: 768px) {
      width: 90%;
      padding: 35px 25px;
      margin: 20px;
    }
    
    @media (max-width: 480px) {
      padding: 30px 20px;
      width: 95%;
    }
  }
  
  // 标题区域
  .title-container {
    text-align: center;
    margin-bottom: 35px;
    
    .logo-wrapper {
      margin-bottom: 20px;
      
      .logo-animation {
        width: 70px;
        height: 70px;
        margin: 0 auto;
        animation: pulse 2s infinite;
        
        .logo-svg {
          width: 100%;
          height: 100%;
          
          circle {
            animation: drawCircle 3s ease-in-out forwards;
            stroke: #409eff;
            stroke-width: 2;
            fill: none;
          }
          
          path {
            animation: drawPath 3s 1s ease-in-out forwards;
            stroke: #409eff;
            stroke-width: 3;
            fill: none;
            stroke-linecap: round;
          }
        }
      }
    }
    
    .title {
      font-size: 26px;
      font-weight: 700;
      color: #303133;
      margin: 0 0 8px 0;
      letter-spacing: 1px;
    }
    
    .subtitle {
      font-size: 14px;
      color: #909399;
      margin: 0;
      font-weight: 400;
    }
  }
  
  // 输入框包装器
  .input-wrapper {
    position: relative;
    transition: all 0.3s ease;
    margin-bottom: 5px;
    
    &.focused {
      transform: translateY(-2px);
    }
    
    .input-icon {
      position: absolute;
      left: 15px;
      top: 50%;
      transform: translateY(-50%);
      color: #909399;
      z-index: 2;
      transition: color 0.3s ease;
      font-size: 18px;
    }
    
    .animated-input {
      width: 100%;
      
      :deep(.el-input__wrapper) {
        width: 100%;
        padding-left: 45px;
        padding-right: 45px;
        border-radius: 10px;
        border: 1px solid #dcdfe6;
        transition: all 0.3s ease;
        background: #fff;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
        
        &:hover {
          border-color: #c0c4cc;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        
        &.is-focus {
          border-color: #409eff;
          box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
        }
      }
      
      :deep(.el-input__inner) {
        height: 48px;
        font-size: 15px;
        color: #606266;
        
        &::placeholder {
          color: #c0c4cc;
        }
      }
    }
    
    .show-pwd, .refresh-captcha {
      position: absolute;
      right: 15px;
      top: 50%;
      transform: translateY(-50%);
      cursor: pointer;
      color: #909399;
      transition: all 0.3s ease;
      z-index: 2;
      display: flex;
      align-items: center;
      gap: 5px;
      font-size: 13px;
      
      &:hover {
        color: #409eff;
      }
    }
  }
  
  // 输入提示
  .input-hint {
    font-size: 12px;
    color: #909399;
    margin-top: 6px;
    padding-left: 45px;
    line-height: 1.4;
  }
  
  // 登录按钮
  .login-button {
    width: 100%;
    height: 48px;
    font-size: 16px;
    font-weight: 600;
    border-radius: 10px;
    margin: 25px 0 20px 0;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    letter-spacing: 1px;
    box-sizing: border-box;
    
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: -100%;
      width: 100%;
      height: 100%;
      background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
      transition: left 0.5s;
    }
    
    &:hover:not(:disabled)::before {
      left: 100%;
    }
    
    &:disabled {
      opacity: 0.7;
      cursor: not-allowed;
    }
  }
  
  // 记住我
  .remember-me {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
    
    :deep(.el-checkbox__label) {
      color: #666;
      font-size: 14px;
    }
  }
  
  // 辅助链接
  .auth-links {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 15px;
    margin-bottom: 30px;
    
    .link-item {
      color: #409eff;
      text-decoration: none;
      font-size: 14px;
      transition: color 0.3s ease;
      
      &:hover {
        color: #66b1ff;
        text-decoration: underline;
      }
    }
    
    .divider {
      color: #c0c4cc;
      font-size: 14px;
    }
  }
  
  // 社交登录
  .social-login {
    text-align: center;
    
    .social-title {
      font-size: 14px;
      color: #909399;
      margin: 0 0 20px 0;
      position: relative;
      
      &::before,
      &::after {
        content: '';
        position: absolute;
        top: 50%;
        width: 30%;
        height: 1px;
        background: #e4e7ed;
      }
      
      &::before {
        left: 0;
      }
      
      &::after {
        right: 0;
      }
    }
    
    .social-icons {
      display: flex;
      justify-content: center;
      gap: 15px;
      
      .social-btn {
        width: 45px;
        height: 45px;
        border-radius: 50%;
        border: 2px solid #e4e7ed;
        transition: all 0.3s ease;
        
        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
          border-color: #409eff;
        }
      }
    }
  }
  
  // 安全提示
  .security-tip {
    margin-top: 20px;
    
    :deep(.el-alert) {
      border-radius: 10px;
    }
  }
}

// 动画定义
@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  25% {
    transform: translate(20px, -20px) rotate(90deg);
  }
  50% {
    transform: translate(-10px, -30px) rotate(180deg);
  }
  75% {
    transform: translate(-20px, 10px) rotate(270deg);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes drawCircle {
  0% {
    stroke-dasharray: 0 283;
    stroke-dashoffset: 283;
  }
  100% {
    stroke-dasharray: 283 283;
    stroke-dashoffset: 0;
  }
}

@keyframes drawPath {
  0% {
    stroke-dasharray: 0 100;
    stroke-dashoffset: 100;
  }
  100% {
    stroke-dasharray: 100 100;
    stroke-dashoffset: 0;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .login-container {
    padding: 15px;
    
    .login-form {
      width: 95%;
      padding: 35px 25px;
    }
    
    .title-container {
      .title {
        font-size: 24px;
      }
      
      .logo-animation {
        width: 60px;
        height: 60px;
      }
    }
    
    .input-wrapper {
      .animated-input {
        width: 100%;
        
        :deep(.el-input__wrapper) {
          width: 100%;
          padding-left: 40px;
          padding-right: 40px;
        }
      }
      
      .input-icon {
        left: 12px;
        font-size: 16px;
      }
      
      .show-pwd, .refresh-captcha {
        right: 12px;
        font-size: 12px;
      }
    }
    
    .input-hint {
      padding-left: 40px;
    }
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 10px;
    
    .login-form {
      padding: 30px 20px;
      width: 98%;
    }
    
    .title-container {
      margin-bottom: 30px;
      
      .title {
        font-size: 22px;
      }
      
      .subtitle {
        font-size: 13px;
      }
      
      .logo-animation {
        width: 55px;
        height: 55px;
      }
    }
    
    .input-wrapper {
      .animated-input {
        width: 100%;
        
        :deep(.el-input__wrapper) {
          width: 100%;
          padding-left: 35px;
          padding-right: 35px;
        }
        
        :deep(.el-input__inner) {
          height: 45px;
          font-size: 14px;
        }
      }
      
      .input-icon {
        left: 10px;
        font-size: 15px;
      }
    }
    
    .input-hint {
      padding-left: 35px;
      font-size: 11px;
    }
    
    .login-button {
      height: 45px;
      font-size: 15px;
    }
    
    .auth-links {
      flex-direction: column;
      gap: 8px;
      
      .divider {
        display: none;
      }
      
      .link-item {
        font-size: 13px;
      }
    }
    
    .social-login {
      .social-title {
        font-size: 13px;
        margin-bottom: 15px;
      }
      
      .social-icons {
        gap: 12px;
        
        .social-btn {
          width: 40px;
          height: 40px;
        }
      }
    }
  }
}
</style>