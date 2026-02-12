<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑部门' : '新增部门'"
    width="600px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item label="部门名称" prop="deptName">
        <el-input v-model="form.deptName" placeholder="请输入部门名称" />
      </el-form-item>
      <el-form-item label="部门编码" prop="deptCode">
        <el-input v-model="form.deptCode" placeholder="请输入部门编码" :disabled="isEdit" />
      </el-form-item>
      <el-form-item label="上级部门" prop="parentId">
        <el-tree-select
          v-model="form.parentId"
          :data="departmentTree"
          :props="{ label: 'deptName', value: 'id', children: 'children' }"
          placeholder="请选择上级部门"
          clearable
          check-strictly
        />
      </el-form-item>
      <el-form-item label="排序" prop="sortOrder">
        <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
      </el-form-item>
      <el-form-item label="部门负责人" prop="managerId">
        <el-select v-model="form.managerId" placeholder="请选择负责人" clearable filterable>
          <el-option
            v-for="user in userList"
            :key="user.id"
            :label="user.profile?.fullName || user.username"
            :value="user.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="部门描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入部门描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { organizationApi } from '@/api/organization'
import { userApi } from '@/api/user'
import type { DepartmentUpdateDTO, DepartmentVO, UserVO } from '@/api/user'

interface Props {
  modelValue: boolean
  data?: DepartmentVO
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'success': []
}>()

const formRef = ref<FormInstance>()
const visible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const departmentTree = ref<DepartmentVO[]>([])
const userList = ref<UserVO[]>([])

const form = reactive<DepartmentUpdateDTO>({
  id: 0,
  deptName: '',
  deptCode: '',
  parentId: undefined,
  sortOrder: 0,
  managerId: undefined,
  status: 1,
  description: ''
})

const rules: FormRules = {
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ],
  deptCode: [
    { required: true, message: '请输入部门编码', trigger: 'blur' },
    { pattern: /^[A-Z0-9_]+$/, message: '部门编码只能包含大写字母、数字和下划线', trigger: 'blur' }
  ]
}

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

watch(() => props.data, (data) => {
  if (data) {
    isEdit.value = true
    Object.assign(form, data)
  } else {
    isEdit.value = false
    resetForm()
  }
}, { immediate: true })

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(form, {
    id: 0,
    deptName: '',
    deptCode: '',
    parentId: undefined,
    sortOrder: 0,
    managerId: undefined,
    status: 1,
    description: ''
  })
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true

    if (isEdit.value) {
      await organizationApi.updateDepartment(form)
      ElMessage.success('更新成功')
    } else {
      await organizationApi.createDepartment({
        deptName: form.deptName,
        deptCode: form.deptCode,
        parentId: form.parentId,
        sortOrder: form.sortOrder,
        managerId: form.managerId,
        description: form.description
      })
      ElMessage.success('创建成功')
    }

    emit('success')
    handleClose()
  } catch (error: any) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '创建失败'))
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  visible.value = false
  resetForm()
}

// 加载部门树和用户列表
const loadData = async () => {
  try {
    const [deptTreeRes, userListRes] = await Promise.all([
      organizationApi.getDepartmentTree(),
      userApi.getUsers({ page: 1, size: 1000 })
    ])
    departmentTree.value = deptTreeRes.data
    userList.value = userListRes.data.content
  } catch (error) {
    console.error('加载数据失败:', error)
  }
}

loadData()
</script>
