<template>
  <div class="redirect-container">
    <div class="redirect-text">页面跳转中...</div>
  </div>
</template>

<script setup lang="ts">
import { onBeforeMount } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

onBeforeMount(() => {
  const { params, query } = route
  const { path } = params
  router.replace({ path: '/' + path, query })
})
</script>

<style lang="scss" scoped>
.redirect-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  
  .redirect-text {
    font-size: 18px;
    color: #606266;
  }
}
</style>