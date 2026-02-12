<template>
  <el-dialog
    v-model="dialogVisible"
    :title="formData.id ? '编辑应用' : '新增应用'"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="应用名称" prop="appName">
        <el-input v-model="form.appName" placeholder="请输入应用名称" />
      </el-form-item>

      <el-form-item label="应用类型" prop="appType">
        <el-select v-model="form.appType" placeholder="请选择应用类型">
          <el-option label="Web应用" :value="1" />
          <el-option label="移动应用" :value="2" />
          <el-option label="API应用" :value="3" />
          <el-option label="桌面应用" :value="4" />
          <el-option label="服务应用" :value="5" />
        </el-select>
      </el-form-item>

      <el-form-item label="重定向URI">
        <el-input
          v-model="redirectUriText"
          type="textarea"
          :rows="3"
          placeholder="每行一个URI"
        />
      </el-form-item>

      <el-form-item label="Logo URL">
        <el-input v-model="form.logoUrl" placeholder="请输入Logo URL" />
      </el-form-item>

      <el-form-item label="主页URL">
        <el-input v-model="form.homepageUrl" placeholder="请输入主页URL" />
      </el-form-item>

      <el-form-item label="应用描述">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入应用描述"
        />
      </el-form-item>

      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">启用</el-radio>
          <el-radio :label="2">禁用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { applicationApi } from '@/api/application'
import type { FormInstance, FormRules } from 'element-plus'

interface Props {
  visible: boolean
  formData: any
}

interface Emits {
  (e: 'update:visible', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const form = ref<any>({
  appName: '',
  appType: 1,
  redirectUris: [],
  logoUrl: '',
  homepageUrl: '',
  description: '',
  status: 1
})

const redirectUriText = computed({
  get: () => form.value.redirectUris?.join('\n') || '',
  set: (val: string) => {
    form.value.redirectUris = val.split('\n').filter(u => u.trim())
  }
})

const rules: FormRules = {
  appName: [{ required: true, message: '请输入应用名称', trigger: 'blur' }],
  appType: [{ required: true, message: '请选择应用类型', trigger: 'change' }]
}

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

watch(
  () => props.formData,
  (val) => {
    if (val && val.id) {
      form.value = { ...val }
    } else {
      form.value = {
        appName: '',
        appType: 1,
        redirectUris: [],
        logoUrl: '',
        homepageUrl: '',
        description: '',
        status: 1
      }
    }
  },
  { immediate: true }
)

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.value.id) {
          await applicationApi.updateApplication(form.value)
          ElMessage.success('更新成功')
        } else {
          await applicationApi.createApplication(form.value)
          ElMessage.success('创建成功')
        }
        dialogVisible.value = false
        emit('success')
      } catch (error) {
        ElMessage.error('操作失败')
      }
    }
  })
}

const handleClose = () => {
  formRef.value?.resetFields()
}
</script>
