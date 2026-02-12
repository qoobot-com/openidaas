<template>
  <el-dropdown trigger="click" @command="handleLanguageChange">
    <span class="language-switcher">
      <el-icon><component :is="currentLanguageIcon" /></el-icon>
      <span class="language-name">{{ currentLanguageName }}</span>
      <el-icon class="el-icon--right"><arrow-down /></el-icon>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="lang in languages"
          :key="lang.value"
          :command="lang.value"
          :class="{ 'is-active': currentLanguage === lang.value }"
        >
          <el-icon><component :is="lang.icon" /></el-icon>
          <span>{{ lang.label }}</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from '@/locales'
import { ArrowDown, Location, Globe } from '@element-plus/icons-vue'

const { locale, switchLanguage } = useI18n()

const languages = [
  {
    value: 'zh-CN',
    label: '简体中文',
    icon: Location
  },
  {
    value: 'en-US',
    label: 'English',
    icon: Globe
  }
]

const currentLanguage = computed(() => locale.value)

const currentLanguageName = computed(() => {
  const lang = languages.find(l => l.value === locale.value)
  return lang ? lang.label : 'Language'
})

const currentLanguageIcon = computed(() => {
  const lang = languages.find(l => l.value === locale.value)
  return lang ? lang.icon : Globe
})

const handleLanguageChange = (lang: string) => {
  switchLanguage(lang as 'zh-CN' | 'en-US')
}
</script>

<style scoped lang="scss">
.language-switcher {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  padding: 0 8px;
  height: 32px;
  border-radius: 4px;
  transition: all 0.3s;

  &:hover {
    background-color: var(--el-fill-color-light);
  }

  .language-name {
    font-size: 14px;
    color: var(--el-text-color-primary);
  }

  .el-icon {
    font-size: 16px;
    color: var(--el-text-color-regular);
  }
}

.el-dropdown-menu {
  .el-dropdown-item {
    display: flex;
    align-items: center;
    gap: 8px;

    &.is-active {
      color: var(--el-color-primary);
      background-color: var(--el-color-primary-light-9);
    }
  }
}
</style>
