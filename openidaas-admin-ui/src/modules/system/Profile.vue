<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <div class="user-info">
            <el-avatar :size="100" :src="userInfo.avatar">
              {{ userInfo.fullName?.charAt(0) || userInfo.username?.charAt(0) }}
            </el-avatar>
            <h3>{{ userInfo.fullName || userInfo.username }}</h3>
            <p class="user-role">{{ userInfo.roles?.join(', ') }}</p>
            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-label">登录次数</span>
                <span class="stat-value">256</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">最后登录</span>
                <span class="stat-value">{{ userInfo.lastLoginTime || '2024-01-15 14:30:25' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="基本信息" name="basic">
            <el-form :model="profileForm" label-width="100px">
              <el-form-item label="用户名">
                <el-input v-model="profileForm.username" disabled />
              </el-form-item>
              <el-form-item label="姓名">
                <el-input v-model="profileForm.fullName" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="profileForm.email" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="profileForm.mobile" />
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="profileForm.gender">
                  <el-radio :label="1">男</el-radio>
                  <el-radio :label="2">女</el-radio>
                  <el-radio :label="0">保密</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="生日">
                <el-date-picker
                  v-model="profileForm.birthDate"
                  type="date"
                  placeholder="选择日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="updateProfile">保存</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="安全设置" name="security">
            <el-form :model="securityForm" label-width="120px">
              <el-form-item label="当前密码">
                <el-input v-model="securityForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="securityForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认新密码">
                <el-input v-model="securityForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="changePassword">修改密码</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="权限信息" name="permissions">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="拥有角色">
                <el-tag v-for="role in userInfo.roles" :key="role" style="margin-right: 10px;">
                  {{ role }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="权限列表">
                <el-tag v-for="perm in userInfo.permissions" :key="perm" type="success" style="margin-right: 10px; margin-bottom: 5px;">
                  {{ perm }}
                </el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const activeTab = ref('basic')

const userInfo = ref({
  username: '',
  fullName: '',
  email: '',
  mobile: '',
  avatar: '',
  roles: [] as string[],
  permissions: [] as string[],
  lastLoginTime: ''
})

const profileForm = reactive({
  username: '',
  fullName: '',
  email: '',
  mobile: '',
  gender: 0,
  birthDate: ''
})

const securityForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const loadUserInfo = () => {
  const user = authStore.userInfo
  if (user) {
    userInfo.value = {
      username: user.username,
      fullName: user.fullName || '',
      email: '',
      mobile: '',
      avatar: user.avatar || '',
      roles: user.roles || [],
      permissions: user.permissions || [],
      lastLoginTime: ''
    }
    
    // 初始化表单数据
    profileForm.username = user.username
    profileForm.fullName = user.fullName || ''
  }
}

const updateProfile = () => {
  ElMessage.success('个人信息更新成功')
}

const changePassword = () => {
  if (securityForm.newPassword !== securityForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  if (securityForm.newPassword.length < 6) {
    ElMessage.error('密码长度不能少于6位')
    return
  }
  
  ElMessage.success('密码修改成功')
  securityForm.oldPassword = ''
  securityForm.newPassword = ''
  securityForm.confirmPassword = ''
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-container {
  padding: 20px;
  
  .user-info {
    text-align: center;
    
    h3 {
      margin: 20px 0 10px 0;
      color: #303133;
    }
    
    .user-role {
      color: #909399;
      margin-bottom: 30px;
    }
    
    .user-stats {
      .stat-item {
        display: flex;
        justify-content: space-between;
        padding: 10px 0;
        border-bottom: 1px solid #eee;
        
        .stat-label {
          color: #606266;
        }
        
        .stat-value {
          color: #303133;
          font-weight: 500;
        }
      }
    }
  }
}
</style>