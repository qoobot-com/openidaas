<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="statistic-card">
          <div class="statistic-content">
            <div class="statistic-number">1,234</div>
            <div class="statistic-title">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistic-card">
          <div class="statistic-content">
            <div class="statistic-number">56</div>
            <div class="statistic-title">在线用户</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistic-card">
          <div class="statistic-content">
            <div class="statistic-number">89%</div>
            <div class="statistic-title">系统健康度</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="statistic-card">
          <div class="statistic-content">
            <div class="statistic-number">24</div>
            <div class="statistic-title">待处理任务</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>系统监控</span>
            </div>
          </template>
          <div class="chart-container">
            <div class="placeholder-chart">
              系统监控图表区域
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
            </div>
          </template>
          <div class="activity-list">
            <div class="activity-item" v-for="i in 5" :key="i">
              <div class="activity-content">
                <div class="activity-title">用户登录活动 {{ i }}</div>
                <div class="activity-time">2024-01-{{ 10 + i }} 10:{{ 30 + i }}:00</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
// Dashboard 组件
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
}

.statistic-card {
  .statistic-content {
    text-align: center;
    
    .statistic-number {
      font-size: 32px;
      font-weight: bold;
      color: #409EFF;
      margin-bottom: 10px;
    }
    
    .statistic-title {
      font-size: 14px;
      color: #606266;
    }
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  
  .placeholder-chart {
    color: #909399;
    font-size: 16px;
  }
}

.activity-list {
  .activity-item {
    padding: 12px 0;
    border-bottom: 1px solid #EBEEF5;
    
    &:last-child {
      border-bottom: none;
    }
    
    .activity-content {
      .activity-title {
        font-size: 14px;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .activity-time {
        font-size: 12px;
        color: #909399;
      }
    }
  }
}
</style>