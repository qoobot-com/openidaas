import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import './styles/index.scss'
import './styles/dark-theme.scss'
import './permission'
import i18n from './locales'
import errorHandler from './plugins/error-handler'
import registerElementPlus from './plugins/element-plus'
import registerDirectives from './plugins/directives'
import { useThemeStore } from './stores/theme'

const app = createApp(App)

// 注册Element Plus组件（按需导入）
registerElementPlus(app)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 注册自定义指令
registerDirectives(app)

const pinia = createPinia()
app.use(pinia)
app.use(router)
app.use(i18n)
app.use(errorHandler)

// 初始化主题
const themeStore = useThemeStore()
themeStore.initTheme()
themeStore.watchSystemTheme()

app.mount('#app')