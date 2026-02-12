<template>
  <div class="system-config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基础配置" name="basic">
          <el-form :model="basicConfig" label-width="120px">
            <el-form-item label="系统名称">
              <el-input v-model="basicConfig.systemName" />
            </el-form-item>
            <el-form-item label="系统描述">
              <el-input v-model="basicConfig.description" type="textarea" />
            </el-form-item>
            <el-form-item label="默认语言">
              <el-select v-model="basicConfig.defaultLanguage">
                <el-option label="中文" value="zh" />
                <el-option label="English" value="en" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicConfig">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="安全配置" name="security">
          <el-form :model="securityConfig" label-width="120px">
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securityConfig.minPasswordLength" :min="6" :max="32" />
            </el-form-item>
            <el-form-item label="密码复杂度">
              <el-checkbox-group v-model="securityConfig.passwordComplexity">
                <el-checkbox label="uppercase">大写字母</el-checkbox>
                <el-checkbox label="lowercase">小写字母</el-checkbox>
                <el-checkbox label="number">数字</el-checkbox>
                <el-checkbox label="special">特殊字符</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="登录失败次数">
              <el-input-number v-model="securityConfig.maxLoginAttempts" :min="1" :max="10" />
            </el-form-item>
            <el-form-item label="账户锁定时间(分钟)">
              <el-input-number v-model="securityConfig.lockoutDuration" :min="1" :max="1440" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveSecurityConfig">保存</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="邮件配置" name="email">
          <el-form :model="emailConfig" label-width="120px">
            <el-form-item label="SMTP服务器">
              <el-input v-model="emailConfig.smtpHost" />
            </el-form-item>
            <el-form-item label="SMTP端口">
              <el-input-number v-model="emailConfig.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="发件人邮箱">
              <el-input v-model="emailConfig.senderEmail" />
            </el-form-item>
            <el-form-item label="发件人密码">
              <el-input v-model="emailConfig.senderPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveEmailConfig">保存</el-button>
              <el-button @click="testEmailConfig">测试连接</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('basic')

const basicConfig = reactive({
  systemName: 'IDaaS管理系统',
  description: '企业级身份即服务管理平台',
  defaultLanguage: 'zh'
})

const securityConfig = reactive({
  minPasswordLength: 8,
  passwordComplexity: ['uppercase', 'lowercase', 'number', 'special'],
  maxLoginAttempts: 5,
  lockoutDuration: 30
})

const emailConfig = reactive({
  smtpHost: 'smtp.example.com',
  smtpPort: 465,
  senderEmail: 'noreply@example.com',
  senderPassword: ''
})

const saveBasicConfig = () => {
  ElMessage.success('基础配置保存成功')
}

const saveSecurityConfig = () => {
  ElMessage.success('安全配置保存成功')
}

const saveEmailConfig = () => {
  ElMessage.success('邮件配置保存成功')
}

const testEmailConfig = () => {
  ElMessage.info('正在测试邮件连接...')
  // 模拟测试连接
  setTimeout(() => {
    ElMessage.success('邮件连接测试成功')
  }, 2000)
}
</script>

<style lang="scss" scoped>
.system-config-container {
  padding: 20px;
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>