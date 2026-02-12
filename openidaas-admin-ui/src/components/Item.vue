<template>
  <i v-if="icon.includes('el-icon')" class="sub-el-icon" :class="icon" />
  <svg-icon v-else-if="icon" :icon-class="icon" />
  <span v-if="title">{{ title }}</span>
</template>

<script setup lang="ts">
defineProps({
  icon: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: ''
  }
})
</script>

<style scoped>
.sub-el-icon {
  color: currentColor;
  width: 1em;
  height: 1em;
}
</style>