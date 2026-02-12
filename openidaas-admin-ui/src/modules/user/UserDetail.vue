<template>
  <div class="user-detail-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户详情</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="用户名">{{ userDetail?.username }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userDetail?.email }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ userDetail?.mobile }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(userDetail?.status)">
            {{ getStatusText(userDetail?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ userDetail?.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ userDetail?.updatedAt }}</el-descriptions-item>
      </el-descriptions>
      
      <div v-if="userDetail?.profile" style="margin-top: 20px;">
        <h3>个人信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="姓名">{{ userDetail.profile.fullName }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ userDetail.profile.nickname }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ getGenderText(userDetail.profile.gender) }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ userDetail.profile.birthDate }}</el-descriptions-item>
          <el-descriptions-item label="员工编号">{{ userDetail.profile.employeeId }}</el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div v-if="userDetail?.departments && userDetail.departments.length > 0" style="margin-top: 20px;">
        <h3>部门信息</h3>
        <el-table :data="userDetail.departments" border>
          <el-table-column prop="deptName" label="部门名称" />
          <el-table-column prop="positionName" label="职位" />
          <el-table-column prop="isPrimary" label="是否主部门">
            <template #default="{ row }">
              <el-tag :type="row.isPrimary ? 'success' : 'info'">
                {{ row.isPrimary ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-if="userDetail?.roles && userDetail.roles.length > 0" style="margin-top: 20px;">
        <h3>角色信息</h3>
        <el-table :data="userDetail.roles" border>
          <el-table-column prop="roleName" label="角色名称" />
          <el-table-column prop="expireTime" label="过期时间" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { UserDetail } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userDetail = ref<UserDetail | null>(null)
const loading = ref(false)

const userId = Number(route.params.id)

const getUserDetail = async () => {
  try {
    loading.value = true
    const user = await userStore.getUserById(userId)
    userDetail.value = user
  } catch (error) {
    console.error('获取用户详情失败:', error)
    ElMessage.error('获取用户详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.go(-1)
}

const getStatusType = (status?: string) => {
  const statusMap: Record<string, any> = {
    ACTIVE: 'success',
    LOCKED: 'warning',
    DISABLED: 'danger',
    DELETED: 'info'
  }
  return statusMap[status || ''] || 'info'
}

const getStatusText = (status?: string) => {
  const statusMap: Record<string, string> = {
    ACTIVE: '正常',
    LOCKED: '锁定',
    DISABLED: '禁用',
    DELETED: '删除'
  }
  return statusMap[status || ''] || status || ''
}

const getGenderText = (gender?: number) => {
  const genderMap: Record<number, string> = {
    0: '未知',
    1: '男',
    2: '女'
  }
  return genderMap[gender || 0] || '未知'
}

onMounted(() => {
  if (userId) {
    getUserDetail()
  }
})
</script>

<style lang="scss" scoped>
.user-detail-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  h3 {
    margin: 20px 0 10px 0;
    color: #303133;
  }
}
</style>