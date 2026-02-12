<template>
  <div class="audit-log-detail">
    <el-page-header @back="handleBack" title="返回">
      <template #content>
        <span class="text-large font-600">审计日志详情</span>
      </template>
    </el-page-header>

    <el-card v-loading="loading" style="margin-top: 20px">
      <template #header>
        <div class="card-header">
          <span>日志信息</span>
          <div class="header-actions">
            <el-button type="danger" @click="handleDelete">删除日志</el-button>
          </div>
        </div>
      </template>

      <template v-if="logDetail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="日志ID" :span="2">
            <el-text type="primary">{{ logDetail.id }}</el-text>
          </el-descriptions-item>
          <el-descriptions-item label="操作类型">
            <el-tag :type="getOperationTypeTag(logDetail.operationType)" size="large">
              {{ getOperationTypeLabel(logDetail.operationType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作结果">
            <el-tag :type="logDetail.result === 'SUCCESS' ? 'success' : 'danger'" size="large">
              {{ logDetail.result === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作描述" :span="2">
            {{ logDetail.operationDesc || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="模块">{{ logDetail.module || '-' }}</el-descriptions-item>
          <el-descriptions-item label="子模块">{{ logDetail.subModule || '-' }}</el-descriptions-item>
          <el-descriptions-item label="目标类型">{{ logDetail.targetType || '-' }}</el-descriptions-item>
          <el-descriptions-item label="目标ID">{{ logDetail.targetId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="目标名称" :span="2">
            <el-text type="primary">{{ logDetail.targetName || '-' }}</el-text>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">操作人信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="操作人">
            <el-icon><User /></el-icon>
            {{ logDetail.operatorName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="操作人ID">
            {{ logDetail.operatorId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="操作IP">
            <el-icon><Monitor /></el-icon>
            {{ logDetail.operatorIp || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="用户代理">
            <el-icon><Chrome /></el-icon>
            {{ logDetail.userAgent || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">请求信息</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="请求方法">
            <el-tag>{{ logDetail.requestMethod || '-' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="操作时间">
            <el-icon><Clock /></el-icon>
            {{ logDetail.operationTime }}
          </el-descriptions-item>
          <el-descriptions-item label="请求URL" :span="2">
            <el-text type="info" style="word-break: break-all">
              {{ logDetail.requestUrl || '-' }}
            </el-text>
          </el-descriptions-item>
          <el-descriptions-item label="执行耗时">
            <span :class="getExecutionTimeClass(logDetail.executionTime)">
              {{ logDetail.executionTime || 0 }} ms
            </span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ logDetail.createdAt }}</el-descriptions-item>
        </el-descriptions>

        <!-- 错误信息 -->
        <template v-if="logDetail.errorMessage">
          <el-divider content-position="left">
            <el-text type="danger">错误信息</el-text>
          </el-divider>
          <el-alert type="error" :closable="false" show-icon>
            {{ logDetail.errorMessage }}
          </el-alert>
        </template>

        <!-- 请求参数 -->
        <el-divider content-position="left">
          <span>请求参数</span>
          <el-button
            text
            type="primary"
            size="small"
            @click="copyToClipboard(logDetail.requestParams)"
            style="margin-left: 10px"
          >
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-button>
        </el-divider>
        <div class="code-block">
          <pre v-if="logDetail.requestParams">{{ formatJson(logDetail.requestParams) }}</pre>
          <el-empty v-else description="暂无请求参数" :image-size="60" />
        </div>

        <!-- 响应结果 -->
        <el-divider content-position="left">
          <span>响应结果</span>
          <el-button
            text
            type="primary"
            size="small"
            @click="copyToClipboard(logDetail.responseResult)"
            style="margin-left: 10px"
          >
            <el-icon><CopyDocument /></el-icon>
            复制
          </el-button>
        </el-divider>
        <div class="code-block">
          <pre v-if="logDetail.responseResult">{{ formatJson(logDetail.responseResult) }}</pre>
          <el-empty v-else description="暂无响应结果" :image-size="60" />
        </div>

        <!-- 租户和应用信息 -->
        <el-divider content-position="left">租户与应用</el-divider>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="租户ID">
            {{ logDetail.tenantId || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="应用ID">
            {{ logDetail.appId || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
      <el-empty v-else description="日志详情加载中..." />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { User, Monitor, Clock, Chrome, CopyDocument } from '@element-plus/icons-vue'
import { auditApi } from '@/api/audit'
import type { AuditLog } from '@/types/audit'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const logDetail = ref<AuditLog | null>(null)

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
const getExecutionTimeClass = (time: number | null | undefined) => {
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

// 复制到剪贴板
const copyToClipboard = (text: string | null | undefined) => {
  if (!text) {
    ElMessage.warning('没有内容可复制')
    return
  }
  navigator.clipboard.writeText(text).then(() => {
    ElMessage.success('已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 返回
const handleBack = () => {
  router.back()
}

// 删除日志
const handleDelete = () => {
  if (!logDetail.value) return

  ElMessageBox.confirm('确定要删除这条审计日志吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'error',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      await auditApi.deleteAuditLog(logDetail.value!.id)
      ElMessage.success('删除成功')
      router.back()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 加载日志详情
const loadLogDetail = async () => {
  const logId = route.params.id as string
  if (!logId) {
    ElMessage.error('日志ID不存在')
    return
  }

  loading.value = true
  try {
    const log = await auditApi.getAuditLog(Number(logId))
    logDetail.value = log
  } catch (error) {
    ElMessage.error('加载日志详情失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLogDetail()
})
</script>

<style lang="scss" scoped>
.audit-log-detail {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
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
