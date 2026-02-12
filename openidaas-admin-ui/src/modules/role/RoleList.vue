<template>
  <div class="role-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-form :inline="true" :model="queryParams">
          <el-form-item label="角色名称">
            <el-input v-model="queryParams.roleName" placeholder="请输入角色名称" clearable />
          </el-form-item>
          <el-form-item label="角色类型">
            <el-select v-model="queryParams.roleType" placeholder="请选择角色类型" clearable style="width: 150px">
              <el-option label="系统角色" :value="1" />
              <el-option label="业务角色" :value="2" />
              <el-option label="数据角色" :value="3" />
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
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="roleName" label="角色名称" width="200" />
        <el-table-column label="角色类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTypeTag(row.roleType)" size="small">
              {{ getRoleTypeLabel(row.roleType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userCount" label="用户数" width="100" />
        <el-table-column prop="permissionCount" label="权限数" width="100" />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAssignPermissions(row)">分配权限</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 角色表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色类型" prop="roleType">
          <el-select v-model="formData.roleType" placeholder="请选择角色类型" style="width: 100%">
            <el-option label="系统角色" :value="1" />
            <el-option label="业务角色" :value="2" />
            <el-option label="数据角色" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级角色">
          <el-select v-model="formData.parentId" placeholder="请选择上级角色" clearable style="width: 100%">
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
              :disabled="role.id === formData.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="enabled" />
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
import { roleApi, type RoleVO } from '@/api/role'

const formRef = ref<FormInstance>()
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const isEdit = ref(false)
const tableData = ref<RoleVO[]>([])
const roleOptions = ref<RoleVO[]>([])

const queryParams = reactive({
  roleName: '',
  roleType: undefined as number | undefined
})

const formData = reactive({
  id: 0,
  roleCode: '',
  roleName: '',
  roleType: 2,
  parentId: undefined as number | undefined,
  sortOrder: 0,
  enabled: true,
  description: ''
})

const enabled = computed({
  get: () => formData.enabled,
  set: (val) => {
    formData.enabled = val
  }
})

const formRules: FormRules = {
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' }
  ],
  roleType: [
    { required: true, message: '请选择角色类型', trigger: 'change' }
  ]
}

const getRoleTypeLabel = (type: number) => {
  const map: Record<number, string> = {
    1: '系统角色',
    2: '业务角色',
    3: '数据角色'
  }
  return map[type] || '未知'
}

const getRoleTypeTag = (type: number) => {
  const map: Record<number, string> = {
    1: 'warning',
    2: 'primary',
    3: 'success'
  }
  return map[type] || 'info'
}

// 加载角色列表
const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await roleApi.getRoleList(queryParams.roleType)
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  } finally {
    loading.value = false
  }
}

// 加载角色选项
const loadRoleOptions = async () => {
  try {
    roleOptions.value = await roleApi.getRoleList()
  } catch (error) {
    ElMessage.error('加载角色选项失败')
  }
}

// 搜索
const handleQuery = () => {
  loadData()
}

// 重置
const handleReset = () => {
  Object.assign(queryParams, {
    roleName: '',
    roleType: undefined
  })
  loadData()
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  dialogTitle.value = '新增角色'
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row: RoleVO) => {
  isEdit.value = true
  dialogTitle.value = '编辑角色'
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 分配权限
const handleAssignPermissions = (row: RoleVO) => {
  // 跳转到权限分配页面
  // 这里可以跳转到 RoleAssign 组件
  ElMessage.info('请使用角色权限分配页面')
}

// 删除
const handleDelete = async (row: RoleVO) => {
  if (row.isBuiltin) {
    ElMessage.warning('系统内置角色不能删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除角色 "${row.roleName}" 吗？`, '提示', {
      type: 'warning'
    })
    await roleApi.deleteRole(row.id)
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
        await roleApi.updateRole(formData)
        ElMessage.success('更新成功')
      } else {
        await roleApi.createRole(formData)
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
    roleCode: '',
    roleName: '',
    roleType: 2,
    parentId: undefined,
    sortOrder: 0,
    enabled: true,
    description: ''
  })
}

onMounted(() => {
  loadData()
  loadRoleOptions()
})
</script>

<style lang="scss" scoped>
.role-list {
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
