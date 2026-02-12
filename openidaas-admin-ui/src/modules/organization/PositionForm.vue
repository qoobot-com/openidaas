<template>
  <div class="position-form-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑职位' : '新增职位' }}</span>
          <div>
            <el-button @click="handleCancel">取消</el-button>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
          </div>
        </div>
      </template>

      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="职位编码" prop="positionCode">
          <el-input v-model="formData.positionCode" placeholder="请输入职位编码" style="width: 400px" />
        </el-form-item>
        <el-form-item label="职位名称" prop="positionName">
          <el-input v-model="formData.positionName" placeholder="请输入职位名称" style="width: 400px" />
        </el-form-item>
        <el-form-item label="所属部门" prop="deptId">
          <el-select v-model="formData.deptId" placeholder="请选择部门" clearable style="width: 400px">
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.deptName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="职级" prop="level">
          <el-input-number v-model="formData.level" :min="1" :max="100" style="width: 400px" />
        </el-form-item>
        <el-form-item label="岗位等级" prop="jobGrade">
          <el-input v-model="formData.jobGrade" placeholder="例如：P6, T3等" style="width: 400px" />
        </el-form-item>
        <el-form-item label="汇报给" prop="reportsTo">
          <el-select v-model="formData.reportsTo" placeholder="请选择上级职位" clearable style="width: 400px">
            <el-option
              v-for="pos in positionOptions"
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
import { organizationApi, type Position, type DepartmentVO } from '@/api/organization'

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const isEdit = ref(false)
const departmentOptions = ref<DepartmentVO[]>([])
const positionOptions = ref<Position[]>([])

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

// 加载部门选项
const loadDepartments = async () => {
  try {
    departmentOptions.value = await organizationApi.getDepartmentTree()
  } catch (error) {
    ElMessage.error('加载部门列表失败')
  }
}

// 加载职位选项
const loadPositions = async () => {
  try {
    positionOptions.value = await organizationApi.getPositions()
  } catch (error) {
    ElMessage.error('加载职位列表失败')
  }
}

// 加载详情（编辑模式）
const loadDetail = async (id: number) => {
  try {
    const detail = await organizationApi.getPositionById(id)
    Object.assign(formData, detail)
  } catch (error) {
    ElMessage.error('加载职位详情失败')
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
      router.push('/organization/position')
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

// 取消
const handleCancel = () => {
  router.push('/organization/position')
}

onMounted(async () => {
  await loadDepartments()
  await loadPositions()

  const id = route.params.id as string
  if (id && id !== 'new') {
    isEdit.value = true
    await loadDetail(parseInt(id))
  }
})
</script>

<style lang="scss" scoped>
.position-form-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
