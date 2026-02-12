import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import type { Ref } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'auto'

export const useThemeStore = defineStore('theme', () => {
  // 主题模式
  const themeMode: Ref<ThemeMode> = ref('auto')

  // 是否是暗色主题
  const isDark: Ref<boolean> = ref(false)

  // 初始化主题
  const initTheme = () => {
    // 从 localStorage 读取保存的主题
    const savedTheme = localStorage.getItem('themeMode') as ThemeMode | null
    if (savedTheme && ['light', 'dark', 'auto'].includes(savedTheme)) {
      themeMode.value = savedTheme
    }

    // 应用主题
    applyTheme()
  }

  // 切换主题
  const toggleTheme = () => {
    if (themeMode.value === 'light') {
      themeMode.value = 'dark'
    } else if (themeMode.value === 'dark') {
      themeMode.value = 'auto'
    } else {
      themeMode.value = 'light'
    }
    
    saveTheme()
    applyTheme()
  }

  // 设置主题
  const setTheme = (mode: ThemeMode) => {
    themeMode.value = mode
    saveTheme()
    applyTheme()
  }

  // 保存主题到 localStorage
  const saveTheme = () => {
    localStorage.setItem('themeMode', themeMode.value)
  }

  // 应用主题
  const applyTheme = () => {
    let shouldBeDark = false

    if (themeMode.value === 'dark') {
      shouldBeDark = true
    } else if (themeMode.value === 'light') {
      shouldBeDark = false
    } else {
      // 自动模式：跟随系统
      shouldBeDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    }

    isDark.value = shouldBeDark

    // 设置 HTML 根元素的 class
    if (shouldBeDark) {
      document.documentElement.classList.add('dark')
    } else {
      document.documentElement.classList.remove('dark')
    }

    // 更新 meta theme-color
    const themeColorMeta = document.querySelector('meta[name="theme-color"]')
    if (themeColorMeta) {
      themeColorMeta.setAttribute('content', shouldBeDark ? '#141414' : '#ffffff')
    }
  }

  // 监听系统主题变化（仅在 auto 模式下）
  const watchSystemTheme = () => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    
    const handleChange = () => {
      if (themeMode.value === 'auto') {
        applyTheme()
      }
    }

    mediaQuery.addEventListener('change', handleChange)
    
    // 返回清理函数
    return () => {
      mediaQuery.removeEventListener('change', handleChange)
    }
  }

  // 监听主题模式变化
  watch(themeMode, () => {
    saveTheme()
    applyTheme()
  })

  return {
    themeMode,
    isDark,
    initTheme,
    toggleTheme,
    setTheme,
    saveTheme,
    applyTheme,
    watchSystemTheme
  }
})
