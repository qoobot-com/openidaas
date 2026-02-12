<template>
  <el-dropdown @command="handleCommand" trigger="click">
    <div class="theme-switcher">
      <el-icon :size="20">
        <Sunny v-if="themeMode === 'light'" />
        <Moon v-else-if="themeMode === 'dark'" />
        <Monitor v-else />
      </el-icon>
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="light">
          <el-icon class="dropdown-icon"><Sunny /></el-icon>
          <span>{{ t('theme.light') }}</span>
        </el-dropdown-item>
        <el-dropdown-item command="dark">
          <el-icon class="dropdown-icon"><Moon /></el-icon>
          <span>{{ t('theme.dark') }}</span>
        </el-dropdown-item>
        <el-dropdown-item command="auto">
          <el-icon class="dropdown-icon"><Monitor /></el-icon>
          <span>{{ t('theme.auto') }}</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Sunny, Moon, Monitor } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'
import { useI18n } from '@/locales'

const themeStore = useThemeStore()
const { t } = useI18n()

const themeMode = computed(() => themeStore.themeMode)

const handleCommand = (command: 'light' | 'dark' | 'auto') => {
  themeStore.setTheme(command)
  ElMessage.success(t('theme.changed'))
}
</script>

<style lang="scss" scoped>
.theme-switcher {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background-color: var(--el-fill-color-light);
  }
}

.dropdown-icon {
  margin-right: 8px;
}
</style>
