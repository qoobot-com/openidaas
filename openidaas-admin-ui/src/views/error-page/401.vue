<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">401</div>
      <div class="error-description">抱歉，您没有权限访问该页面</div>
      <div class="error-actions">
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="goBack">返回上一页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style lang="scss" scoped>
.error-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background-color: #f5f7fa;
  
  .error-content {
    text-align: center;
    
    .error-code {
      font-size: 72px;
      font-weight: bold;
      color: #E6A23C;
      margin-bottom: 20px;
    }
    
    .error-description {
      font-size: 20px;
      color: #606266;
      margin-bottom: 30px;
    }
    
    .error-actions {
      .el-button {
        margin: 0 10px;
      }
    }
  }
}
</style>