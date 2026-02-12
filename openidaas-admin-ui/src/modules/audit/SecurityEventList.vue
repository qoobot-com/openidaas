<template>
  <div class="security-event-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>安全事件</span>
          <div class="header-actions">
            <el-tag :type="getSeverityTag(severityFilter)" @click="toggleSeverityFilter">
              严重程度: {{ getSeverityLabel(severityFilter) }}
            </el-tag>
            <el-tag :type="handledFilter === 'all' ? 'info' : 'success'" @click="toggleHandledFilter">
              处理状态: {{ handledFilter === 'all' ? '全部' : handledFilter === 'handled' ? '已处理' : '未处理' }}
            </el-tag>
          </div>
        </div>
      </template>

      <!-- 搜索条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="事件类型">
          <el-select v-model="searchForm.eventType" placeholder="请选择事件类型" clearable>
            <el-option label="登录失败" value="LOGIN_FAILED" />
            <el-option label="密码错误" value="PASSWORD_ERROR" />
            <el-option label="异常登录" value="ABNORMAL_LOGIN" />
            <el-option label="越权访问" value="UNAUTHORIZED_ACCESS" />
            <el-option label="数据泄露" value="DATA_LEAK" />
            <el-option label="暴力破解" value="BRUTE_FORCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="searchForm.ipAddress" placeholder="请输入IP地址" clearable />
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 统计概览 -->
      <div class="statistics-overview">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="statistic-item">
              <div class="statistic-value emergency">{{ statistics.emergency || 0 }}</div>
              <div class="statistic-label">紧急事件</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="statistic-item">
              <div class="statistic-value high">{{ statistics.high || 0 }}</div>
              <div class="statistic-label">高危事件</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="statistic-item">
              <div class="statistic-value medium">{{ statistics.medium || 0 }}</div>
              <div class="statistic-label">中危事件</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="statistic-item">
              <div class="statistic-value low">{{ statistics.low || 0 }}</div>
              <div class="statistic-label">低危事件</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- 事件表格 -->
      <el-table
        :data="tableData"
        style="width: 100%"
        v-loading="loading"
        border
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="事件ID" width="80" fixed />
        <el-table-column prop="eventType" label="事件类型" width="150">
          <template #default="scope">
            <el-tag>{{ getEventTypeLabel(scope.row.eventType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="scope">
            <el-tag :type="getSeverityTag(scope.row.severity)" effect="dark">
              {{ getSeverityLabel(scope.row.severity) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="ipAddress" label="IP地址" width="140" />
        <el-table-column prop="deviceInfo" label="设备信息" width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="发生时间" width="180" />
        <el-table-column prop="handled" label="处理状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.handled ? 'success' : 'warning'">
              {{ scope.row.handled ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button
              v-if="!scope.row.handled"
              type="primary"
              link
              size="small"
              @click.stop="handleMarkHandled(scope.row)"
            >
              标记处理
            </el-button>
            <el-button type="info" link size="small" @click.stop="handleViewDetail(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 安全事件详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="安全事件详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentEvent" :column="2" border>
        <el-descriptions-item label="事件ID">{{ currentEvent.id }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">
          <el-tag>{{ getEventTypeLabel(currentEvent.eventType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="严重程度">
          <el-tag :type="getSeverityTag(currentEvent.severity)" effect="dark">
            {{ getSeverityLabel(currentEvent.severity) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="currentEvent.handled ? 'success' : 'warning'">
            {{ currentEvent.handled ? '已处理' : '未处理' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户名">{{ currentEvent.username || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ currentEvent.userId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="IP地址" :span="2">
          <el-text type="danger">{{ currentEvent.ipAddress || '-' }}</el-text>
        </el-descriptions-item>
        <el-descriptions-item label="设备信息" :span="2">
          {{ currentEvent.deviceInfo || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="发生时间" :span="2">
          {{ currentEvent.createdAt }}
        </el-descriptions-item>
        <el-descriptions-item label="处理时间" :span="2">
          {{ currentEvent.handleTime || '未处理' }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 事件数据 -->
      <el-divider content-position="left">事件数据</el-divider>
      <div class="code-block">
        <pre>{{ formatJson(currentEvent?.eventData) || '-' }}</pre>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          v-if="!currentEvent?.handled"
          type="primary"
          @click="handleMarkHandled(currentEvent)"
        >
          标记为已处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { securityEventApi } from '@/api/audit'
import type { SecurityEvent } from '@/types/audit'

// 响应式数据
const loading = ref(false)
const tableData = ref<SecurityEvent[]>([])
const currentEvent = ref<SecurityEvent | null>(null)
const detailDialogVisible = ref(false)
const dateRange = ref<[string, string] | null>(null)
const severityFilter = ref<number | null>(null)
const handledFilter = ref<'all' | 'handled' | 'unhandled'>('all')

// 搜索表单
const searchForm = reactive({
  eventType: '',
  username: '',
  ipAddress: ''
})

// 统计数据
const statistics = reactive({
  emergency: 0,
  high: 0,
  medium: 0,
  low: 0
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 事件类型标签
const getEventTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    LOGIN_FAILED: '登录失败',
    PASSWORD_ERROR: '密码错误',
    ABNORMAL_LOGIN: '异常登录',
    UNAUTHORIZED_ACCESS: '越权访问',
    DATA_LEAK: '数据泄露',
    BRUTE_FORCE: '暴力破解'
  }
  return labelMap[type] || type
}

// 严重程度标签
const getSeverityLabel = (severity: number) => {
  const labelMap: Record<number, string> = {
    1: '低',
    2: '中',
    3: '高',
    4: '紧急'
  }
  return labelMap[severity] || '未知'
}

// 严重程度标签颜色
const getSeverityTag = (severity: number | null) => {
  if (severity === null) return 'info'
  const tagMap: Record<number, string> = {
    1: 'success',
    2: 'warning',
    3: 'danger',
    4: 'danger'
  }
  return tagMap[severity] || 'info'
}

// 格式化JSON
const formatJson = (data: any) => {
  if (!data) return null
  try {
    return JSON.stringify(data, null, 2)
  } catch {
    return data
  }
}

// 切换严重程度过滤
const toggleSeverityFilter = () => {
  const filters = [null, 4, 3, 2, 1]
  const currentIndex = filters.indexOf(severityFilter.value)
  severityFilter.value = filters[(currentIndex + 1) % filters.length]
  queryEvents()
}

// 切换处理状态过滤
const toggleHandledFilter = () => {
  const filters: Array<'all' | 'handled' | 'unhandled'> = ['all', 'handled', 'unhandled']
  const currentIndex = filters.indexOf(handledFilter.value)
  handledFilter.value = filters[(currentIndex + 1) % filters.length]
  queryEvents()
}

// 查询安全事件
const queryEvents = async () => {
  loading.value = true
  try {
    const query = {
      eventType: searchForm.eventType || undefined,
      username: searchForm.username || undefined,
      ipAddress: searchForm.ipAddress || undefined,
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1],
      severity: severityFilter.value || undefined,
      handled: handledFilter.value === 'all' ? undefined : (handledFilter.value === 'handled'),
      page: pagination.currentPage,
      size: pagination.pageSize
    }

    const response = await securityEventApi.querySecurityEvents(query)
    tableData.value = response.content
    pagination.total = response.totalElements

    // 查询统计数据
    const now = new Date()
    const oneMonthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
    const startTime = dateRange.value?.[0] || oneMonthAgo.toISOString().slice(0, 19).replace('T', ' ')
    const endTime = dateRange.value?.[1] || now.toISOString().slice(0, 19).replace('T', ' ')

    const stats = await securityEventApi.getSecurityStatistics(startTime, endTime)
    statistics.emergency = stats.emergency || 0
    statistics.high = stats.high || 0
    statistics.medium = stats.medium || 0
    statistics.low = stats.low || 0
  } catch (error) {
    ElMessage.error('查询安全事件失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  queryEvents()
}

// 重置
const handleReset = () => {
  searchForm.eventType = ''
  searchForm.username = ''
  searchForm.ipAddress = ''
  dateRange.value = null
  severityFilter.value = null
  handledFilter.value = 'all'
  pagination.currentPage = 1
  queryEvents()
}

// 查看详情
const handleViewDetail = (row: SecurityEvent) => {
  currentEvent.value = row
  detailDialogVisible.value = true
}

// 行点击
const handleRowClick = (row: SecurityEvent) => {
  handleViewDetail(row)
}

// 标记为已处理
const handleMarkHandled = async (row: SecurityEvent) => {
  try {
    await securityEventApi.markAsHandled(row.id)
    ElMessage.success('已标记为已处理')
    queryEvents()
    detailDialogVisible.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  queryEvents()
}

const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  queryEvents()
}

// 初始化
onMounted(() => {
  queryEvents()
})
</script>

<style lang="scss" scoped>
.security-event-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .search-form {
    margin-bottom: 20px;
  }

  .statistics-overview {
    margin-bottom: 20px;
    padding: 20px;
    background-color: #f5f7fa;
    border-radius: 4px;

    .statistic-item {
      text-align: center;

      .statistic-value {
        font-size: 32px;
        font-weight: bold;
        margin-bottom: 10px;

        &.emergency {
          color: #f56c6c;
        }

        &.high {
          color: #e6a23c;
        }

        &.medium {
          color: #409eff;
        }

        &.low {
          color: #67c23a;
        }
      }

      .statistic-label {
        font-size: 14px;
        color: #909399;
      }
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .code-block {
    background-color: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
    max-height: 400px;
    overflow: auto;

    pre {
      margin: 0;
      white-space: pre-wrap;
      word-wrap: break-word;
      font-size: 12px;
      line-height: 1.5;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    }
  }
}
</style>
