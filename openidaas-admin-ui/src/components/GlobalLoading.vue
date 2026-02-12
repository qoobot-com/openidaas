<template>
  <el-dialog
    v-model="visible"
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    width="400px"
    class="global-loading-dialog"
  >
    <div class="loading-content">
      <el-icon class="is-loading" :size="60">
        <Loading />
      </el-icon>
      <div class="loading-text">{{ text }}</div>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'

interface Props {
  modelValue: boolean
  text?: string
}

const props = withDefaults(defineProps<Props>(), {
  text: '加载中...'
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const visible = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})
</script>

<style lang="scss" scoped>
.global-loading-dialog {
  :deep(.el-dialog__header) {
    display: none;
  }

  :deep(.el-dialog__body) {
    padding: 40px 20px;
    text-align: center;
  }

  .loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    .el-icon {
      color: var(--el-color-primary);
      margin-bottom: 20px;
    }

    .loading-text {
      font-size: 16px;
      color: var(--el-text-color-regular);
    }
  }
}
</style>
