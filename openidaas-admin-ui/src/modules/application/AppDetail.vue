<template>
  <el-dialog
    v-model="dialogVisible"
    title="应用详情"
    width="700px"
  >
    <el-descriptions :column="2" border v-if="appData">
      <el-descriptions-item label="应用ID">{{ appData.id }}</el-descriptions-item>
      <el-descriptions-item label="应用密钥">
        {{ appData.appKey }}
        <el-button link type="primary" @click="copyAppKey">复制</el-button>
      </el-descriptions-item>
      <el-descriptions-item label="应用名称">{{ appData.appName }}</el-descriptions-item>
      <el-descriptions-item label="应用类型">{{ appData.appTypeDesc }}</el-descriptions-item>
      <el-descriptions-item label="状态" :span="2">
        <el-tag :type="appData.status === 1 ? 'success' : 'danger'">
          {{ appData.statusDesc }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="重定向URI" :span="2">
        {{ appData.redirectUris || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="Logo URL" :span="2">
        {{ appData.logoUrl || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="主页URL" :span="2">
        {{ appData.homepageUrl || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="应用描述" :span="2">
        {{ appData.description || '无' }}
      </el-descriptions-item>
      <el-descriptions-item label="所有者">{{ appData.ownerName || '无' }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ appData.createdAt }}</el-descriptions-item>
    </el-descriptions>

    <template v-if="appData?.oauth2Client">
      <el-divider content-position="left">OAuth2 配置</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="客户端ID">{{ appData.oauth2Client.clientId }}</el-descriptions-item>
        <el-descriptions-item label="自动批准">
          {{ appData.oauth2Client.autoApprove ? '是' : '否' }}
        </el-descriptions-item>
        <el-descriptions-item label="访问令牌有效期">
          {{ appData.oauth2Client.accessTokenValidity }} 秒
        </el-descriptions-item>
        <el-descriptions-item label="刷新令牌有效期">
          {{ appData.oauth2Client.refreshTokenValidity }} 秒
        </el-descriptions-item>
        <el-descriptions-item label="授权类型" :span="2">
          {{ appData.oauth2Client.grantTypes || '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="权限范围" :span="2">
          {{ appData.oauth2Client.scopes || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </template>

    <template v-if="appData?.samlSp">
      <el-divider content-position="left">SAML 配置</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="SP实体ID" :span="2">
          {{ appData.samlSp.spEntityId }}
        </el-descriptions-item>
        <el-descriptions-item label="ACS URL" :span="2">
          {{ appData.samlSp.acsUrl }}
        </el-descriptions-item>
        <el-descriptions-item label="元数据URL" :span="2">
          {{ appData.samlSp.metadataUrl || '无' }}
        </el-descriptions-item>
      </el-descriptions>
    </template>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Application } from '@/types/application'

interface Props {
  visible: boolean
  appData: Application | null
}

interface Emits {
  (e: 'update:visible', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
})

const copyAppKey = () => {
  if (props.appData?.appKey) {
    navigator.clipboard.writeText(props.appData.appKey)
    ElMessage.success('已复制到剪贴板')
  }
}
</script>
