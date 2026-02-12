import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'

// 语言包
const messages = {
  'zh-CN': zhCN,
  'en-US': enUS
}

// 支持的语言列表
export const supportedLanguages = [
  {
    value: 'zh-CN',
    label: '简体中文',
    icon: '🇨🇳'
  },
  {
    value: 'en-US',
    label: 'English',
    icon: '🇺🇸'
  }
]

// 从 localStorage 获取语言，默认简体中文
const savedLanguage = localStorage.getItem('language')
const defaultLanguage = savedLanguage && supportedLanguages.some(l => l.value === savedLanguage)
  ? savedLanguage
  : 'zh-CN'

// 创建 i18n 实例
const i18n = createI18n({
  legacy: false,
  locale: defaultLanguage,
  fallbackLocale: 'zh-CN',
  messages,
  globalInjection: true
})

// 导出 i18n 实例
export default i18n

/**
 * 组合式 API - 国际化
 */
export const useI18n = () => {
  const { t, locale, d, n, te, tm, rt } = i18n.global

  /**
   * 切换语言
   * @param lang 语言代码，如 'zh-CN' 或 'en-US'
   */
  const switchLanguage = (lang: string) => {
    const language = supportedLanguages.find(l => l.value === lang)
    if (language) {
      locale.value = lang
      localStorage.setItem('language', lang)
      // 更新 HTML lang 属性
      document.documentElement.lang = lang
    }
  }

  /**
   * 获取当前语言
   */
  const getCurrentLanguage = () => {
    return locale.value
  }

  /**
   * 获取支持的语言列表
   */
  const getSupportedLanguages = () => {
    return supportedLanguages
  }

  /**
   * 格式化日期
   * @param value 日期值
   * @param format 格式
   */
  const formatDate = (value: Date | number | string, format?: string) => {
    return d(value, format)
  }

  /**
   * 格式化数字
   * @param value 数字值
   */
  const formatNumber = (value: number) => {
    return n(value)
  }

  return {
    t,
    locale,
    d,
    n,
    te,
    tm,
    rt,
    switchLanguage,
    getCurrentLanguage,
    getSupportedLanguages,
    formatDate,
    formatNumber
  }
}