<template>
  <div class="audit-log-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>审计日志</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleExport">导出日志</el-button>
            <el-button type="danger" @click="handleCleanup">清理过期日志</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索条件 -->
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operationType" placeholder="请选择操作类型" clearable>
            <el-option label="创建" value="CREATE" />
            <el-option label="读取" value="READ" />
            <el-option label="更新" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="导出" value="EXPORT" />
            <el-option label="导入" value="IMPORT" />
            <el-option label="登录" value="LOGIN" />
            <el-option label="退出" value="LOGOUT" />
          </el-select>
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="searchForm.module" placeholder="请选择模块" clearable>
            <el-option label="用户管理" value="USER" />
            <el-option label="角色管理" value="ROLE" />
            <el-option label="权限管理" value="PERMISSION" />
            <el-option label="部门管理" value="DEPARTMENT" />
            <el-option label="应用管理" value="APPLICATION" />
            <el-option label="认证授权" value="AUTH" />
            <el-option label="系统配置" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作结果">
          <el-select v-model="searchForm.result" placeholder="请选择操作结果" clearable>
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAILURE" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="searchForm.operatorName" placeholder="请输入操作人" clearable />
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

      <!-- 统计卡片 -->
      <div class="statistics-cards">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="statistic-item">
                <div class="statistic-value">{{ statistics.totalOperations || 0 }}</div>
                <div class="statistic-label">总操作次数</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="statistic-item success">
                <div class="statistic-value">{{ statistics.successCount || 0 }}</div>
                <div class="statistic-label">成功操作</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="statistic-item danger">
                <div class="statistic-value">{{ statistics.failureCount || 0 }}</div>
                <div class="statistic-label">失败操作</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="hover">
              <div class="statistic-item">
                <div class="statistic-value">{{ failureRate }}%</div>
                <div class="statistic-label">失败率</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 操作类型分布图表 -->
      <div class="chart-container" v-if="showCharts">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <div class="chart-header">操作类型分布</div>
              </template>
              <div ref="operationTypeChartRef" style="height: 300px"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header>
                <div class="chart-header">模块操作分布</div>
              </template>
              <div ref="moduleChartRef" style="height: 300px"></div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 日志表格 -->
      <div class="table-container">
        <el-table
          :data="tableData"
          style="width: 100%"
          v-loading="loading"
          border
          @row-click="handleRowClick"
        >
          <el-table-column prop="id" label="日志ID" width="80" fixed />
          <el-table-column prop="operationType" label="操作类型" width="100">
            <template #default="scope">
              <el-tag :type="getOperationTypeTag(scope.row.operationType)">
                {{ getOperationTypeLabel(scope.row.operationType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="operationDesc" label="操作描述" width="200" show-overflow-tooltip />
          <el-table-column prop="module" label="模块" width="120" />
          <el-table-column prop="targetType" label="目标类型" width="120" />
          <el-table-column prop="targetName" label="目标名称" width="150" show-overflow-tooltip />
          <el-table-column prop="operatorName" label="操作人" width="120" />
          <el-table-column prop="operatorIp" label="操作IP" width="140" />
          <el-table-column prop="operationTime" label="操作时间" width="180" />
          <el-table-column prop="executionTime" label="耗时(ms)" width="100" align="right">
            <template #default="scope">
              <span :class="getExecutionTimeClass(scope.row.executionTime)">
                {{ scope.row.executionTime || '-' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="result" label="结果" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.result === 'SUCCESS' ? 'success' : 'danger'">
                {{ scope.row.result === 'SUCCESS' ? '成功' : '失败' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="scope">
              <el-button type="primary" link size="small" @click.stop="handleViewDetail(scope.row)">
                查看详情
              </el-button>
              <el-button type="danger" link size="small" @click.stop="handleDelete(scope.row)">
                删除
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
      </div>
    </el-card>

    <!-- 审计日志详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="审计日志详情"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-descriptions v-if="currentLog" :column="2" border>
        <el-descriptions-item label="日志ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">
          <el-tag :type="getOperationTypeTag(currentLog.operationType)">
            {{ getOperationTypeLabel(currentLog.operationType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="操作描述">{{ currentLog.operationDesc || '-' }}</el-descriptions-item>
        <el-descriptions-item label="模块">{{ currentLog.module || '-' }}</el-descriptions-item>
        <el-descriptions-item label="子模块">{{ currentLog.subModule || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标类型">{{ currentLog.targetType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标ID">{{ currentLog.targetId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标名称">{{ currentLog.targetName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ currentLog.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作人ID">{{ currentLog.operatorId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ currentLog.operatorIp || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户代理">{{ currentLog.userAgent || '-' }}</el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ currentLog.operationTime }}</el-descriptions-item>
        <el-descriptions-item label="执行耗时">{{ currentLog.executionTime || '-' }} ms</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请求URL" :span="2">
          {{ currentLog.requestUrl || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="操作结果" :span="2">
          <el-tag :type="currentLog.result === 'SUCCESS' ? 'success' : 'danger'">
            {{ currentLog.result === 'SUCCESS' ? '成功' : '失败' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="currentLog.errorMessage">
          <el-text type="danger">{{ currentLog.errorMessage }}</el-text>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 请求参数 -->
      <el-divider content-position="left">请求参数</el-divider>
      <div class="code-block">
        <pre>{{ formatJson(currentLog?.requestParams) || '-' }}</pre>
      </div>

      <!-- 响应结果 -->
      <el-divider content-position="left">响应结果</el-divider>
      <div class="code-block">
        <pre>{{ formatJson(currentLog?.responseResult) || '-' }}</pre>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 导出对话框 -->
    <el-dialog v-model="exportDialogVisible" title="导出审计日志" width="600px">
      <el-form :model="exportForm" label-width="100px">
        <el-form-item label="开始时间" required>
          <el-date-picker
            v-model="exportForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
        <el-form-item label="结束时间" required>
          <el-date-picker
            v-model="exportForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExport" :loading="exporting">导出</el-button>
      </template>
    </el-dialog>

    <!-- 清理过期日志对话框 -->
    <el-dialog v-model="cleanupDialogVisible" title="清理过期审计日志" width="600px">
      <el-alert
        title="警告：此操作将永久删除指定时间之前的所有审计日志，请谨慎操作！"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      />
      <el-form :model="cleanupForm" label-width="120px">
        <el-form-item label="保留天数" required>
          <el-input-number
            v-model="cleanupForm.retentionDays"
            :min="1"
            :max="365"
            placeholder="请输入保留天数"
          />
          <span style="margin-left: 10px">天（将删除{{ retentionDate }}之前的所有日志）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cleanupDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmCleanup" :loading="cleaning">清理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditApi } from '@/api/audit'
import type { AuditLog, AuditStatistics, PageResponse, AuditLogQuery } from '@/types/audit'
import * as echarts from 'echarts'

// 响应式数据
const loading = ref(false)
const tableData = ref<AuditLog[]>([])
const statistics = ref<AuditStatistics>({
  totalOperations: 0,
  successCount: 0,
  failureCount: 0,
  operationTypeDistribution: {},
  moduleDistribution: {},
  topUsers: {}
})
const currentLog = ref<AuditLog | null>(null)
const detailDialogVisible = ref(false)
const exportDialogVisible = ref(false)
const cleanupDialogVisible = ref(false)
const exporting = ref(false)
const cleaning = ref(false)
const showCharts = ref(true)
const dateRange = ref<[string, string] | null>(null)

// 搜索表单
const searchForm = reactive<AuditLogQuery>({
  operationType: '',
  module: '',
  result: '',
  operatorName: '',
  page: 1,
  size: 20
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 导出表单
const exportForm = reactive({
  startTime: '',
  endTime: ''
})

// 清理表单
const cleanupForm = reactive({
  retentionDays: 90
})

// 图表引用
const operationTypeChartRef = ref<HTMLElement | null>(null)
const moduleChartRef = ref<HTMLElement | null>(null)
let operationTypeChart: echarts.ECharts | null = null
let moduleChart: echarts.ECharts | null = null

// 计算属性
const failureRate = computed(() => {
  if (!statistics.value.totalOperations) return 0
  return ((statistics.value.failureCount / statistics.value.totalOperations) * 100).toFixed(2)
})

const retentionDate = computed(() => {
  const date = new Date()
  date.setDate(date.getDate() - cleanupForm.retentionDays)
  return date.toLocaleString('zh-CN')
})

// 操作类型标签颜色
const getOperationTypeTag = (type: string) => {
  const tagMap: Record<string, string> = {
    CREATE: 'success',
    READ: 'info',
    UPDATE: 'warning',
    DELETE: 'danger',
    EXPORT: 'primary',
    IMPORT: 'primary',
    LOGIN: 'success',
    LOGOUT: 'info'
  }
  return tagMap[type] || 'info'
}

// 操作类型标签文字
const getOperationTypeLabel = (type: string) => {
  const labelMap: Record<string, string> = {
    CREATE: '创建',
    READ: '读取',
    UPDATE: '更新',
    DELETE: '删除',
    EXPORT: '导出',
    IMPORT: '导入',
    LOGIN: '登录',
    LOGOUT: '退出'
  }
  return labelMap[type] || type
}

// 执行时间样式
const getExecutionTimeClass = (time: number) => {
  if (!time) return ''
  if (time < 100) return 'fast'
  if (time < 500) return 'normal'
  if (time < 1000) return 'slow'
  return 'very-slow'
}

// 格式化JSON
const formatJson = (json: string | null | undefined) => {
  if (!json) return null
  try {
    return JSON.stringify(JSON.parse(json), null, 2)
  } catch {
    return json
  }
}

// 查询审计日志
const queryAuditLogs = async () => {
  loading.value = true
  try {
    const query: AuditLogQuery = {
      ...searchForm,
      startTime: dateRange.value?.[0],
      endTime: dateRange.value?.[1],
      page: pagination.currentPage,
      size: pagination.pageSize
    }
    const response = await auditApi.queryAuditLogs(query)
    tableData.value = response.content
    pagination.total = response.totalElements
  } catch (error) {
    ElMessage.error('查询审计日志失败')
  } finally {
    loading.value = false
  }
}

// 查询统计数据
const queryStatistics = async () => {
  try {
    const now = new Date()
    const startTime = dateRange.value?.[0] || new Date(now.setMonth(now.getMonth() - 1)).toISOString().slice(0, 19).replace('T', ' ')
    const endTime = dateRange.value?.[1] || new Date().toISOString().slice(0, 19).replace('T', ' ')

    const data = await auditApi.getStatistics(startTime, endTime)
    statistics.value = data

    // 更新图表
    updateCharts()
  } catch (error) {
    ElMessage.error('查询统计数据失败')
  }
}

// 更新图表
const updateCharts = () => {
  // 操作类型分布图
  if (operationTypeChartRef.value) {
    if (!operationTypeChart) {
      operationTypeChart = echarts.init(operationTypeChartRef.value)
    }
    const operationTypeData = Object.entries(statistics.value.operationTypeDistribution).map(([key, value]) => ({
      name: key,
      value
    }))
    operationTypeChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '操作类型',
          type: 'pie',
          radius: '60%',
          data: operationTypeData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }

  // 模块分布图
  if (moduleChartRef.value) {
    if (!moduleChart) {
      moduleChart = echarts.init(moduleChartRef.value)
    }
    const moduleData = Object.entries(statistics.value.moduleDistribution).map(([key, value]) => ({
      name: key,
      value
    }))
    moduleChart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [
        {
          name: '模块',
          type: 'pie',
          radius: '60%',
          data: moduleData,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }
      ]
    })
  }
}

// 搜索
const handleSearch = () => {
  pagination.currentPage = 1
  queryAuditLogs()
  queryStatistics()
}

// 重置
const handleReset = () => {
  searchForm.operationType = ''
  searchForm.module = ''
  searchForm.result = ''
  searchForm.operatorName = ''
  dateRange.value = null
  pagination.currentPage = 1
  queryAuditLogs()
  queryStatistics()
}

// 查看详情
const handleViewDetail = (row: AuditLog) => {
  currentLog.value = row
  detailDialogVisible.value = true
}

// 行点击
const handleRowClick = (row: AuditLog) => {
  handleViewDetail(row)
}

// 删除
const handleDelete = (row: AuditLog) => {
  ElMessageBox.confirm('确定要删除这条审计日志吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await auditApi.deleteAuditLog(row.id)
      ElMessage.success('删除成功')
      queryAuditLogs()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 导出日志
const handleExport = () => {
  // 设置默认时间范围为当前搜索条件的时间范围
  if (dateRange.value) {
    exportForm.startTime = dateRange.value[0]
    exportForm.endTime = dateRange.value[1]
  } else {
    const now = new Date()
    const oneMonthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
    exportForm.startTime = oneMonthAgo.toISOString().slice(0, 19).replace('T', ' ')
    exportForm.endTime = now.toISOString().slice(0, 19).replace('T', ' ')
  }
  exportDialogVisible.value = true
}

// 确认导出
const confirmExport = async () => {
  if (!exportForm.startTime || !exportForm.endTime) {
    ElMessage.warning('请选择时间范围')
    return
  }

  exporting.value = true
  try {
    const blob = await auditApi.exportAuditLogs(exportForm.startTime, exportForm.endTime)
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `审计日志_${exportForm.startTime}_至_${exportForm.endTime}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
    exportDialogVisible.value = false
  } catch (error) {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}

// 清理过期日志
const handleCleanup = () => {
  cleanupDialogVisible.value = true
}

// 确认清理
const confirmCleanup = async () => {
  const beforeTime = retentionDate.value.slice(0, 19).replace(/\//g, '-')

  ElMessageBox.confirm(`确定要删除${beforeTime}之前的所有审计日志吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定清理',
    cancelButtonText: '取消',
    type: 'error',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    cleaning.value = true
    try {
      const count = await auditApi.cleanupExpiredLogs(beforeTime)
      ElMessage.success(`成功清理 ${count} 条过期日志`)
      cleanupDialogVisible.value = false
      queryAuditLogs()
      queryStatistics()
    } catch (error) {
      ElMessage.error('清理失败')
    } finally {
      cleaning.value = false
    }
  })
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  queryAuditLogs()
}

const handleCurrentChange = (page: number) => {
  pagination.currentPage = page
  queryAuditLogs()
}

// 初始化
onMounted(() => {
  queryAuditLogs()
  queryStatistics()

  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    operationTypeChart?.resize()
    moduleChart?.resize()
  })
})
</script>

<style lang="scss" scoped>
.audit-log-container {
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

  .statistics-cards {
    margin-bottom: 20px;

    .statistic-item {
      text-align: center;

      .statistic-value {
        font-size: 32px;
        font-weight: bold;
        color: #303133;
      }

      .statistic-label {
        margin-top: 10px;
        font-size: 14px;
        color: #909399;
      }

      &.success .statistic-value {
        color: #67c23a;
      }

      &.danger .statistic-value {
        color: #f56c6c;
      }
    }
  }

  .chart-container {
    margin-bottom: 20px;

    .chart-header {
      font-weight: bold;
    }
  }

  .table-container {
    margin-top: 20px;
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
    }
  }

  .fast {
    color: #67c23a;
    font-weight: bold;
  }

  .normal {
    color: #e6a23c;
  }

  .slow {
    color: #f56c6c;
  }

  .very-slow {
    color: #f56c6c;
    font-weight: bold;
  }
}
</style>
