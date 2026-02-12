<template>
  <div class="role-form-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑角色' : '新增角色' }}</span>
          <div>
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
          </div>
        </div>
      </template>

      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="请输入角色编码" style="width: 400px" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名称" style="width: 400px" />
        </el-form-item>
        <el-form-item label="角色类型" prop="roleType">
          <el-select v-model="formData.roleType" placeholder="请选择角色类型" style="width: 400px">
            <el-option label="系统角色" :value="1" />
            <el-option label="业务角色" :value="2" />
            <el-option label="数据角色" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级角色">
          <el-select v-model="formData.parentId" placeholder="请选择上级角色" clearable style="width: 400px">
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
          <el-input-number v-model="formData.sortOrder" :min="0" style="width: 400px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="enabled" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" rows="4" style="width: 400px" />
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { roleApi, type RoleVO } from '@/api/role'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const isEdit = ref(false)
const roleOptions = ref<RoleVO[]>([])

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

// 加载角色选项
const loadRoleOptions = async () => {
  try {
    roleOptions.value = await roleApi.getRoleList()
  } catch (error) {
    ElMessage.error('加载角色列表失败')
  }
}

// 加载详情（编辑模式）
const loadDetail = async (id: number) => {
  try {
    const detail = await roleApi.getRoleById(id)
    Object.assign(formData, detail)
  } catch (error) {
    ElMessage.error('加载角色详情失败')
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
      router.push('/role/list')
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

// 取消
const handleCancel = () => {
  router.push('/role/list')
}

onMounted(async () => {
  await loadRoleOptions()

  const id = route.params.id as string
  if (id && id !== 'new') {
    isEdit.value = true
    await loadDetail(parseInt(id))
  }
})
</script>

<style lang="scss" scoped>
.role-form-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
