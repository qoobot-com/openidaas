<template>
  <div class="virtual-table-container" ref="containerRef" @scroll="handleScroll">
    <div class="virtual-table-spacer" :style="{ height: `${totalHeight}px` }"></div>
    <div class="virtual-table-content" :style="{ transform: `translateY(${offsetY}px)` }">
      <el-table
        :data="visibleData"
        :height="height"
        :stripe="stripe"
        :border="border"
        v-bind="$attrs"
      >
        <slot></slot>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

interface Props {
  data: any[]
  itemHeight?: number
  height?: number | string
  stripe?: boolean
  border?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  itemHeight: 54,
  height: 600,
  stripe: true,
  border: true
})

const containerRef = ref<HTMLElement>()
const scrollTop = ref(0)

const totalHeight = computed(() => props.data.length * props.itemHeight)

const visibleCount = computed(() => {
  const containerHeight = typeof props.height === 'number' ? props.height : 600
  return Math.ceil(containerHeight / props.itemHeight) + 5
})

const startIndex = computed(() => {
  return Math.max(0, Math.floor(scrollTop.value / props.itemHeight) - 2)
})

const endIndex = computed(() => {
  return Math.min(props.data.length, startIndex.value + visibleCount.value)
})

const offsetY = computed(() => {
  return startIndex.value * props.itemHeight
})

const visibleData = computed(() => {
  return props.data.slice(startIndex.value, endIndex.value)
})

const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement
  scrollTop.value = target.scrollTop
}

onMounted(() => {
  if (containerRef.value) {
    const containerHeight = typeof props.height === 'number' ? props.height : 600
    containerRef.value.style.height = typeof props.height === 'number'
      ? `${props.height}px`
      : props.height
  }
})
</script>

<style lang="scss" scoped>
.virtual-table-container {
  position: relative;
  overflow: auto;

  .virtual-table-spacer {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
  }

  .virtual-table-content {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
  }
}
</style>
