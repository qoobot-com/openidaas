<template>
  <div class="global-empty">
    <el-empty
      :description="description"
      :image="image"
      :image-size="imageSize"
    >
      <template v-if="showAction" #extra>
        <el-button type="primary" @click="handleAction">{{ actionText }}</el-button>
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  type?: 'data' | 'search' | 'network' | 'permission' | 'custom'
  description?: string
  actionText?: string
  showAction?: boolean
  image?: string
  imageSize?: number
}

const props = withDefaults(defineProps<Props>(), {
  type: 'data',
  description: '',
  actionText: '返回首页',
  showAction: false
})

const emit = defineEmits<{
  action: []
}>()

const description = computed(() => {
  if (props.description) return props.description

  const descriptions: Record<string, string> = {
    data: '暂无数据',
    search: '未找到相关内容',
    network: '网络连接失败',
    permission: '暂无访问权限'
  }

  return descriptions[props.type] || '暂无数据'
})

const image = computed(() => {
  if (props.image) return props.image

  const images: Record<string, string> = {
    data: '',
    search: '',
    network: '',
    permission: ''
  }

  return images[props.type]
})

const handleAction = () => {
  emit('action')
}
</script>

<style lang="scss" scoped>
.global-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  padding: 40px 20px;
}
</style>
