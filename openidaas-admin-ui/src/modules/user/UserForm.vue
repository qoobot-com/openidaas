<template>
  <div class="user-form-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑用户' : '新增用户' }}</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="formData.username" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="mobile">
              <el-input v-model="formData.mobile" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="formData.status" style="width: 100%;">
                <el-option label="正常" value="ACTIVE" />
                <el-option label="锁定" value="LOCKED" />
                <el-option label="禁用" value="DISABLED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="formData.password" type="password" show-password />
        </el-form-item>
        
        <el-divider>个人信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="profile.fullName">
              <el-input v-model="formData.profile.fullName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="昵称" prop="profile.nickname">
              <el-input v-model="formData.profile.nickname" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="性别" prop="profile.gender">
              <el-radio-group v-model="formData.profile.gender">
                <el-radio :label="1">男</el-radio>
                <el-radio :label="2">女</el-radio>
                <el-radio :label="0">保密</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生日" prop="profile.birthDate">
              <el-date-picker
                v-model="formData.profile.birthDate"
                type="date"
                placeholder="选择日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%;"
              />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="员工编号" prop="profile.employeeId">
          <el-input v-model="formData.profile.employeeId" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { CreateUserRequest, UpdateUserRequest, UserProfile } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const isEdit = ref(false)
const userId = ref(0)

const formData = reactive({
  username: '',
  email: '',
  mobile: '',
  status: 'ACTIVE',
  password: '',
  profile: {
    fullName: '',
    nickname: '',
    gender: 0,
    birthDate: '',
    employeeId: ''
  } as UserProfile
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  mobile: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const loadUserDetail = async () => {
  try {
    const user = await userStore.getUserById(userId.value)
    if (user) {
      formData.username = user.username
      formData.email = user.email || ''
      formData.mobile = user.mobile || ''
      formData.status = user.status
      if (user.profile) {
        Object.assign(formData.profile, user.profile)
      }
    }
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          const updateData: UpdateUserRequest = {
            email: formData.email,
            mobile: formData.mobile,
            profile: formData.profile,
            status: formData.status as any
          }
          await userStore.updateUser(userId.value, updateData)
          ElMessage.success('用户更新成功')
        } else {
          const createData: CreateUserRequest = {
            username: formData.username,
            email: formData.email,
            password: formData.password,
            profile: formData.profile
          }
          await userStore.createUser(createData)
          ElMessage.success('用户创建成功')
        }
        goBack()
      } catch (error) {
        console.error('保存用户失败:', error)
        ElMessage.error('保存用户失败')
      }
    }
  })
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const goBack = () => {
  router.go(-1)
}

onMounted(() => {
  if (route.params.id) {
    isEdit.value = true
    userId.value = Number(route.params.id)
    loadUserDetail()
  }
})
</script>

<style lang="scss" scoped>
.user-form-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>