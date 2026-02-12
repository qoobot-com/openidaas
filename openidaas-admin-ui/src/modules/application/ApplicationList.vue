<template>
  <div class="application-list">
    <el-card class="search-card">
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="应用名称">
          <el-input v-model="queryParams.appName" placeholder="请输入应用名称" clearable />
        </el-form-item>
        <el-form-item label="应用类型">
          <el-select v-model="queryParams.appType" placeholder="请选择" clearable>
            <el-option label="Web应用" :value="1" />
            <el-option label="移动应用" :value="2" />
            <el-option label="API应用" :value="3" />
            <el-option label="桌面应用" :value="4" />
            <el-option label="服务应用" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="primary" @click="handleCreate">新增应用</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="appName" label="应用名称" min-width="150" />
        <el-table-column prop="appTypeDesc" label="应用类型" width="120" />
        <el-table-column prop="appKey" label="应用密钥" min-width="200" show-overflow-tooltip />
        <el-table-column prop="statusDesc" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.statusDesc }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="所有者" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSearch"
        @current-change="handleSearch"
      />
    </el-card>

    <ApplicationForm
      v-model:visible="formVisible"
      :form-data="formData"
      @success="handleSearch"
    />

    <AppDetail v-model:visible="detailVisible" :app-data="selectedApp" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applicationApi } from '@/api/application'
import { Application } from '@/types/application'
import ApplicationForm from './ApplicationForm.vue'
import AppDetail from './AppDetail.vue'

const loading = ref(false)
const tableData = ref<Application[]>([])
const total = ref(0)

const queryParams = ref({
  appName: '',
  appType: undefined as number | undefined,
  status: undefined as number | undefined,
  ownerId: undefined,
  page: 1,
  size: 20
})

const formVisible = ref(false)
const detailVisible = ref(false)
const formData = ref<any>({})
const selectedApp = ref<Application | null>(null)

onMounted(() => {
  handleSearch()
})

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await applicationApi.queryApplications(queryParams.value)
    tableData.value = res.content
    total.value = res.totalElements
  } catch (error) {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryParams.value = {
    appName: '',
    appType: undefined,
    status: undefined,
    ownerId: undefined,
    page: 1,
    size: 20
  }
  handleSearch()
}

const handleCreate = () => {
  formData.value = {}
  formVisible.value = true
}

const handleView = (row: Application) => {
  selectedApp.value = row
  detailVisible.value = true
}

const handleEdit = (row: Application) => {
  formData.value = { ...row }
  formVisible.value = true
}

const handleDelete = (row: Application) => {
  ElMessageBox.confirm(`确定要删除应用"${row.appName}"吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await applicationApi.deleteApplication(row.id)
      ElMessage.success('删除成功')
      handleSearch()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}
</script>

<style scoped>
.application-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
}

.table-card {
  padding: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
