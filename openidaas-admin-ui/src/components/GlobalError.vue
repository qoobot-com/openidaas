<template>
  <div class="global-error">
    <el-result
      :icon="icon"
      :title="title"
      :sub-title="subTitle"
    >
      <template #extra>
        <el-space>
          <el-button type="primary" @click="handleRetry" v-if="showRetry">
            重试
          </el-button>
          <el-button @click="handleBack" v-if="showBack">
            返回
          </el-button>
          <el-button @click="handleHome" v-if="showHome">
            返回首页
          </el-button>
        </el-space>
      </template>

      <template #icon v-if="customIcon">
        <component :is="customIcon" :size="64" />
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Warning,
  CircleClose,
  Connection,
  Lock
} from '@element-plus/icons-vue'

interface Props {
  type?: 'network' | 'permission' | 'error' | 'custom'
  title?: string
  subTitle?: string
  showRetry?: boolean
  showBack?: boolean
  showHome?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'error',
  title: '',
  subTitle: '',
  showRetry: true,
  showBack: false,
  showHome: false
})

const emit = defineEmits<{
  retry: []
  back: []
}>()

const router = useRouter()

const title = computed(() => {
  if (props.title) return props.title

  const titles: Record<string, string> = {
    network: '网络连接失败',
    permission: '无访问权限',
    error: '操作失败',
    custom: ''
  }

  return titles[props.type] || '操作失败'
})

const subTitle = computed(() => {
  if (props.subTitle) return props.subTitle

  const subTitles: Record<string, string> = {
    network: '请检查网络连接后重试',
    permission: '您没有访问该页面的权限',
    error: '发生了一些错误',
    custom: ''
  }

  return subTitles[props.type] || '发生了一些错误'
})

const icon = computed(() => {
  const icons: Record<string, any> = {
    network: 'error',
    permission: 'warning',
    error: 'error',
    custom: ''
  }

  return icons[props.type] || 'error'
})

const customIcon = computed(() => {
  const icons: Record<string, any> = {
    network: Connection,
    permission: Lock,
    error: CircleClose,
    custom: null
  }

  return icons[props.type]
})

const handleRetry = () => {
  emit('retry')
}

const handleBack = () => {
  emit('back')
  router.back()
}

const handleHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.global-error {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px 20px;
}
</style>
