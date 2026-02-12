# OpenIDaaS 前端国际化指南

## 目录

- [概述](#概述)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [使用方法](#使用方法)
- [语言包结构](#语言包结构)
- [添加新语言](#添加新语言)
- [最佳实践](#最佳实践)
- [常见问题](#常见问题)

## 概述

OpenIDaaS 前端采用 **Vue I18n** 实现国际化，支持多语言切换。

### 支持的语言

| 语言 | 代码 | 状态 |
|-----|------|------|
| 简体中文 | `zh-CN` | ✅ 已完成 |
| 英语 | `en-US` | ✅ 已完成 |

### 特性

- ✅ 支持动态语言切换
- ✅ 语言设置持久化（localStorage）
- ✅ 自动回退到默认语言
- ✅ 支持日期、数字格式化
- ✅ 支持命名插值
- ✅ 支持复数形式
- ✅ 完整的语言包覆盖

## 技术栈

- **Vue I18n**: 9.x
- **Vue 3**: Composition API
- **TypeScript**: 类型安全

## 项目结构

```
src/
├── locales/
│   ├── index.ts           # i18n 配置和组合式函数
│   ├── zh-CN.ts          # 简体中文语言包
│   └── en-US.ts          # 英语语言包
├── components/
│   └── LanguageSwitcher.vue  # 语言切换组件
└── main.ts               # 主入口文件
```

## 使用方法

### 1. 基础使用

#### 在模板中使用

```vue
<template>
  <div>
    <!-- 基础翻译 -->
    <h1>{{ $t('login.title') }}</h1>

    <!-- 命名插值 -->
    <p>{{ $t('message.total', { count: 10 }) }}</p>

    <!-- 使用组合式 API -->
    <p>{{ t('common.add') }}</p>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from '@/locales'

const { t } = useI18n()
</script>
```

#### 在 Script 中使用

```typescript
import { useI18n } from '@/locales'

const { t, switchLanguage, locale } = useI18n()

// 翻译
const title = t('login.title')

// 切换语言
switchLanguage('en-US')

// 获取当前语言
const currentLang = locale.value
```

### 2. 语言切换组件

```vue
<template>
  <LanguageSwitcher />
</template>

<script setup lang="ts">
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
</script>
```

### 3. 初始化配置

```typescript
import { createApp } from 'vue'
import { createI18n } from 'vue-i18n'
import App from './App.vue'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'

const i18n = createI18n({
  legacy: false,
  locale: 'zh-CN',
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

const app = createApp(App)
app.use(i18n)
app.mount('#app')
```

## 语言包结构

### 语言包示例

```typescript
// zh-CN.ts
export default {
  // 模块划分
  route: {
    dashboard: '首页',
    user: '用户管理'
  },
  login: {
    title: '系统登录',
    username: '用户名'
  },
  common: {
    add: '新增',
    edit: '编辑',
    delete: '删除'
  }
}
```

### 命名规范

- 使用 **小写字母 + 连字符**
- 按功能模块划分
- 层级不超过 3 层

```typescript
// ✅ 推荐
login.title
user.username
common.add

// ❌ 不推荐
loginTitle
UserName
common_add
```

### 高级用法

#### 命名插值

```typescript
// 语言包
{
  message: {
    total: '共 {count} 条',
    greeting: '你好，{name}！'
  }
}

// 使用
$t('message.total', { count: 10 })  // "共 10 条"
$t('message.greeting', { name: 'John' })  // "你好，John！"
```

#### 列表插值

```typescript
// 语言包
{
  message: {
    items: '{0}、{1} 和 {2}'
  }
}

// 使用
$t('message.items', ['苹果', '香蕉', '橙子'])  // "苹果、香蕉 和 橙子"
```

#### 复数形式

```typescript
// 语言包
{
  message: {
    apple: 'no apples | {n} apple | {n} apples'
  }
}

// 使用
$t('message.apple', 0)  // "no apples"
$t('message.apple', 1)  // "1 apple"
$t('message.apple', 5)  // "5 apples"
```

#### 链式翻译

```typescript
// 语言包
{
  user: {
    name: '用户名',
    profile: {
      title: '个人资料'
    }
  }
}

// 使用
$t('user.name')           // "用户名"
$t('user.profile.title')  // "个人资料"
```

## 添加新语言

### 步骤 1: 创建语言文件

```typescript
// src/locales/ja-JP.ts
export default {
  route: {
    dashboard: 'ダッシュボード',
    user: 'ユーザー管理'
  },
  login: {
    title: 'システムログイン',
    username: 'ユーザー名'
  },
  // ... 其他翻译
}
```

### 步骤 2: 更新配置

```typescript
// src/locales/index.ts
import jaJP from './ja-JP'

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
  },
  {
    value: 'ja-JP',
    label: '日本語',
    icon: '🇯🇵'
  }
]

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS,
  'ja-JP': jaJP
}
```

### 步骤 3: 更新语言切换组件

```vue
<!-- LanguageSwitcher.vue -->
<script setup lang="ts">
const languages = [
  { value: 'zh-CN', label: '简体中文', icon: Location },
  { value: 'en-US', label: 'English', icon: Globe },
  { value: 'ja-JP', label: '日本語', icon: Location }
]
</script>
```

## 最佳实践

### 1. 命名规范

#### 模块划分

```typescript
// ✅ 推荐 - 按模块划分
export default {
  route: { ... },
  login: { ... },
  common: { ... },
  user: { ... },
  organization: { ... },
  role: { ... }
}

// ❌ 不推荐 - 所有翻译平铺
export default {
  dashboard: '首页',
  username: '用户名',
  add: '新增',
  // ... 所有翻译混在一起
}
```

#### 键名规范

```typescript
// ✅ 推荐
login.title
login.username
user.email
common.add

// ❌ 不推荐
loginTitle
userName
Email
CommonAdd
```

### 2. 避免硬编码

```vue
<!-- ✅ 推荐 -->
<template>
  <el-button>{{ t('common.add') }}</el-button>
  <el-alert>{{ t('message.success') }}</el-alert>
</template>

<!-- ❌ 不推荐 -->
<template>
  <el-button>新增</el-button>
  <el-alert>操作成功</el-alert>
</template>
```

### 3. 提取公共翻译

```typescript
// ✅ 推荐 - 提取公共翻译
export default {
  common: {
    add: '新增',
    edit: '编辑',
    delete: '删除',
    save: '保存',
    cancel: '取消'
  },
  user: {
    addUser: '新增用户',  // 复用 common.add
    editUser: '编辑用户'
  }
}

// ❌ 不推荐 - 重复翻译
export default {
  user: {
    add: '新增',
    edit: '编辑',
    addUser: '新增用户',
    editUser: '编辑用户'
  },
  role: {
    add: '新增',  // 重复
    edit: '编辑'  // 重复
  }
}
```

### 4. 使用类型安全

```typescript
// types/i18n.d.ts
declare module '@/locales' {
  export interface SupportedLocale {
    value: 'zh-CN' | 'en-US' | 'ja-JP'
    label: string
    icon: string
  }

  export const supportedLanguages: SupportedLocale[]
}

export type I18nKey =
  | 'route.dashboard'
  | 'login.title'
  | 'common.add'
  | 'user.username'
  // ... 其他键
```

### 5. 缓存翻译

```typescript
// ✅ 推荐 - 缓存翻译
const t = useI18n().t

// 在组件外部缓存
const USER_TITLE = t('user.title')

const showUserTitle = () => {
  console.log(USER_TITLE)
}

// ❌ 不推荐 - 每次调用翻译
const showUserTitle = () => {
  console.log(t('user.title'))
}
```

### 6. 处理缺失翻译

```typescript
// ✅ 推荐 - 提供回退
const title = te('user.title')
  ? t('user.title')
  : 'User Title'

// 或者使用默认值
const title = t('user.title', 'User Title')

// ❌ 不推荐 - 直接使用可能为空的翻译
const title = t('user.title')
```

## 常见问题

### Q1: 如何设置默认语言？

```typescript
// src/locales/index.ts
const i18n = createI18n({
  locale: 'zh-CN',  // 设置默认语言
  fallbackLocale: 'zh-CN'  // 设置回退语言
})
```

### Q2: 如何根据用户浏览器语言自动选择？

```typescript
// src/locales/index.ts
const browserLanguage = navigator.language
const supportedLanguages = ['zh-CN', 'en-US', 'ja-JP']

const matchedLanguage = supportedLanguages.find(lang =>
  browserLanguage.startsWith(lang)
) || 'zh-CN'

const i18n = createI18n({
  locale: matchedLanguage
})
```

### Q3: 如何在 API 请求中发送语言？

```typescript
import { useI18n } from '@/locales'

const { locale } = useI18n()

const api = axios.create({
  headers: {
    'Accept-Language': locale.value  // 发送当前语言
  }
})
```

### Q4: 如何翻译动态内容？

```typescript
// ✅ 推荐 - 使用命名插值
const message = t('message.greeting', { name: userName })

// 语言包
{
  message: {
    greeting: '你好，{name}！'
  }
}
```

### Q5: 如何处理复数形式？

```typescript
// ✅ 推荐 - 使用复数形式
const message = t('message.apple', appleCount)

// 语言包
{
  message: {
    apple: 'no apples | {n} apple | {n} apples'
  }
}
```

### Q6: 如何在路由守卫中使用国际化？

```typescript
import { useI18n } from '@/locales'

router.beforeEach((to) => {
  const { t } = useI18n()
  document.title = t(`route.${to.name}`)
})
```

### Q7: 如何翻译验证错误消息？

```typescript
import { useI18n } from '@/locales'

const { t } = useI18n()

const rules = {
  username: [
    {
      required: true,
      message: t('validation.usernameRequired')
    }
  ]
}
```

### Q8: 如何格式化日期和数字？

```typescript
import { useI18n } from '@/locales'

const { d, n } = useI18n()

// 格式化日期
const formattedDate = d(new Date(), 'YYYY-MM-DD')

// 格式化数字
const formattedNumber = n(1234567.89)
```

### Q9: 如何在 Element Plus 中使用国际化？

```typescript
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import { useI18n } from '@/locales'

const app = createApp(App)
const { locale } = useI18n()

// 响应式切换 Element Plus 语言
watch(locale, (newLang) => {
  app.use(ElementPlus, {
    locale: newLang === 'zh-CN' ? zhCn : en
  })
})
```

### Q10: 如何处理 RTL（从右到左）语言？

```vue
<script setup lang="ts">
import { watch } from 'vue'
import { useI18n } from '@/locales'

const { locale } = useI18n()

const rtlLanguages = ['ar', 'he', 'fa']

watch(locale, (lang) => {
  const isRtl = rtlLanguages.some(rtl => lang.startsWith(rtl))
  document.documentElement.dir = isRtl ? 'rtl' : 'ltr'
  document.documentElement.lang = lang
})
</script>
```

---

## 附录

### 参考资源

- [Vue I18n 官方文档](https://vue-i18n.intlify.dev/)
- [Vue I18n API](https://vue-i18n.intlify.dev/api/)
- [MDN - Internationalization](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Intl)
- [ICU Message Format](http://userguide.icu-project.org/formatparse/messages)

### 语言代码标准

- [ISO 639-1](https://en.wikipedia.org/wiki/ISO_639-1) - 语言代码
- [ISO 3166-1](https://en.wikipedia.org/wiki/ISO_3166-1) - 国家代码

### 支持的语言列表

| 语言 | 代码 | 状态 |
|-----|------|------|
| 简体中文 | zh-CN | ✅ |
| 繁体中文 | zh-TW | ⏳ 待实现 |
| 英语 | en-US | ✅ |
| 日语 | ja-JP | ⏳ 待实现 |
| 韩语 | ko-KR | ⏳ 待实现 |
| 法语 | fr-FR | ⏳ 待实现 |
| 德语 | de-DE | ⏳ 待实现 |
| 西班牙语 | es-ES | ⏳ 待实现 |
| 俄语 | ru-RU | ⏳ 待实现 |
| 阿拉伯语 | ar-SA | ⏳ 待实现 |

---

**文档版本**: 1.0  
**最后更新**: 2024-01-15
