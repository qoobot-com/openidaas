<template>
  <div class="scroll-container" ref="scrollContainer" @wheel.prevent="handleScroll">
    <div class="scroll-wrapper" ref="scrollWrapper" :style="{ left: left + 'px' }">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const scrollContainer = ref<HTMLElement>()
const scrollWrapper = ref<HTMLElement>()
const left = ref(0)

const handleScroll = (e: WheelEvent) => {
  const eventDelta = e.deltaX || -e.deltaY * 40
  const $container = scrollContainer.value
  const $wrapper = scrollWrapper.value
  
  if ($container && $wrapper) {
    const containerWidth = $container.offsetWidth
    const wrapperWidth = $wrapper.offsetWidth
    
    if (eventDelta > 0) {
      left.value = Math.max(left.value - eventDelta, containerWidth - wrapperWidth)
    } else {
      left.value = Math.min(left.value - eventDelta, 0)
    }
  }
}

const moveToTarget = (currentTag: HTMLElement) => {
  const $container = scrollContainer.value
  const $wrapper = scrollWrapper.value
  
  if (!$container || !$wrapper) return
  
  const containerWidth = $container.offsetWidth
  const wrapperWidth = $wrapper.offsetWidth
  
  if (wrapperWidth <= containerWidth) {
    left.value = 0
    return
  }
  
  const tagOffsetLeft = currentTag.offsetLeft
  const tagWidth = currentTag.offsetWidth
  
  if (tagOffsetLeft < -left.value) {
    // 标签在可视区域左侧
    left.value = -tagOffsetLeft
  } else if (tagOffsetLeft + tagWidth > -left.value + containerWidth) {
    // 标签在可视区域右侧
    left.value = -(tagOffsetLeft + tagWidth - containerWidth)
  }
}

defineExpose({
  moveToTarget
})

onMounted(() => {
  // 组件挂载后的逻辑
})

onBeforeUnmount(() => {
  // 组件卸载前的清理工作
})
</script>

<style lang="scss" scoped>
.scroll-container {
  white-space: nowrap;
  position: relative;
  overflow: hidden;
  width: 100%;

  .scroll-wrapper {
    position: absolute;
    transition: left 0.3s ease;
  }
}
</style>