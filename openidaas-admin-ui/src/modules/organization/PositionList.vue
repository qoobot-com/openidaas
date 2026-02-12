<template>
  <div class="position-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>职位管理</span>
          <el-button type="primary" @click="handleAdd">新增职位</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="职位名称">
            <el-input v-model="queryParams.positionName" placeholder="请输入职位名称" clearable />
          </el-form-item>
          <el-form-item label="部门">
            <el-select v-model="queryParams.deptId" placeholder="请选择部门" clearable style="width: 200px">
              <el-option
                v-for="dept in departmentOptions"
                :key="dept.id"
                :label="dept.deptName"
                :value="dept.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="tableData" stripe>
        <el-table-column prop="positionCode" label="职位编码" width="150" />
        <el-table-column prop="positionName" label="职位名称" width="200" />
        <el-table-column prop="deptName" label="所属部门" width="200" />
        <el-table-column prop="level" label="职级" width="100" />
        <el-table-column prop="jobGrade" label="岗位等级" width="120" />
        <el-table-column label="是否管理岗" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isManager === 1 ? 'success' : 'info'" size="small">
              {{ row.isManager === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userCount" label="人员数" width="100" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 职位表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="职位编码" prop="positionCode">
          <el-input v-model="formData.positionCode" placeholder="请输入职位编码" />
        </el-form-item>
        <el-form-item label="职位名称" prop="positionName">
          <el-input v-model="formData.positionName" placeholder="请输入职位名称" />
        </el-form-item>
        <el-form-item label="所属部门" prop="deptId">
          <el-select v-model="formData.deptId" placeholder="请选择部门" clearable style="width: 100%">
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职级" prop="level">
          <el-input-number v-model="formData.level" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="岗位等级" prop="jobGrade">
          <el-input v-model="formData.jobGrade" placeholder="例如：P6, T3等" />
        </el-form-item>
        <el-form-item label="汇报给" prop="reportsTo">
          <el-select v-model="formData.reportsTo" placeholder="请选择上级职位" clearable style="width: 100%">
            <el-option
              v-for="pos in tableData"
              :key="pos.id"
              :label="pos.positionName"
              :value="pos.id"
              :disabled="pos.id === formData.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否管理岗">
          <el-switch v-model="isManager" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { organizationApi, type Position, type DepartmentVO } from '@/api/organization'

const formRef = ref<FormInstance>()
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const isEdit = ref(false)
const tableData = ref<Position[]>([])
const departmentOptions = ref<DepartmentVO[]>([])

const queryParams = reactive({
  positionName: '',
  deptId: undefined as number | undefined
})

const formData = reactive({
  id: 0,
  positionCode: '',
  positionName: '',
  deptId: undefined as number | undefined,
  level: 1,
  jobGrade: '',
  reportsTo: undefined as number | undefined,
  isManager: 0,
  description: ''
})

const isManager = computed({
  get: () => formData.isManager === 1,
  set: (val) => {
    formData.isManager = val ? 1 : 0
  }
})

const formRules: FormRules = {
  positionCode: [
    { required: true, message: '请输入职位编码', trigger: 'blur' }
  ],
  positionName: [
    { required: true, message: '请输入职位名称', trigger: 'blur' }
  ]
}

// 加载职位列表
const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await organizationApi.getPositions(queryParams.deptId)
  } catch (error) {
    ElMessage.error('加载职位列表失败')
  } finally {
    loading.value = false
  }
}

// 加载部门选项
const loadDepartments = async () => {
  try {
    departmentOptions.value = await organizationApi.getDepartmentTree()
  } catch (error) {
    ElMessage.error('加载部门列表失败')
  }
}

// 搜索
const handleQuery = () => {
  loadData()
}

// 重置
const handleReset = () => {
  Object.assign(queryParams, {
    positionName: '',
    deptId: undefined
  })
  loadData()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增职位'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: Position) => {
  isEdit.value = true
  dialogTitle.value = '编辑职位'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row: Position) => {
  try {
    await ElMessageBox.confirm(`确认删除职位 "${row.positionName}" 吗？`, '提示', {
      type: 'warning'
    })
    await organizationApi.deletePosition(row.id)
    ElMessage.success('删除成功')
    await loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await organizationApi.updatePosition(formData)
        ElMessage.success('更新成功')
      } else {
        await organizationApi.createPosition(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      await loadData()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: 0,
    positionCode: '',
    positionName: '',
    deptId: undefined,
    level: 1,
    jobGrade: '',
    reportsTo: undefined,
    isManager: 0,
    description: ''
  })
}

onMounted(() => {
  loadData()
  loadDepartments()
})
</script>

<style lang="scss" scoped>
.position-list {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .search-bar {
    margin-bottom: 20px;
  }
}
</style>
