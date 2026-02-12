<template>
  <div class="user-import-container">
    <el-dialog
      v-model="dialogVisible"
      title="批量导入用户"
      width="600px"
      @close="handleClose"
    >
      <div class="import-steps">
        <el-steps :active="currentStep" finish-status="success">
          <el-step title="上传文件" />
          <el-step title="数据校验" />
          <el-step title="导入完成" />
        </el-steps>
      </div>
      
      <div v-if="currentStep === 0" class="step-content">
        <div class="upload-area">
          <el-upload
            ref="uploadRef"
            class="upload-demo"
            drag
            :auto-upload="false"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            accept=".xlsx,.xls,.csv"
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                请上传Excel文件，文件大小不超过10MB
              </div>
            </template>
          </el-upload>
        </div>
        
        <div class="template-download">
          <el-button type="primary" link @click="downloadTemplate">
            <el-icon><download /></el-icon>
            下载模板文件
          </el-button>
        </div>
      </div>
      
      <div v-if="currentStep === 1" class="step-content">
        <div class="validation-info">
          <el-alert
            v-if="validationResult.errors.length > 0"
            title="数据校验发现问题"
            type="warning"
            :closable="false"
            show-icon
          />
          <el-alert
            v-else
            title="数据校验通过"
            type="success"
            :closable="false"
            show-icon
          />
        </div>
        
        <div class="validation-details">
          <p>总共 {{ validationResult.total }} 条数据</p>
          <p v-if="validationResult.errors.length > 0">
            发现 {{ validationResult.errors.length }} 个问题
          </p>
          <div v-if="validationResult.errors.length > 0" class="error-list">
            <el-table :data="validationResult.errors" size="small">
              <el-table-column prop="row" label="行号" width="80" />
              <el-table-column prop="field" label="字段" width="120" />
              <el-table-column prop="message" label="错误信息" />
            </el-table>
          </div>
        </div>
      </div>
      
      <div v-if="currentStep === 2" class="step-content">
        <div class="import-result">
          <el-result
            :icon="importResult.success ? 'success' : 'error'"
            :title="importResult.success ? '导入成功' : '导入失败'"
            :sub-title="importResult.message"
          >
            <template #extra>
              <div class="result-stats">
                <p>成功导入: {{ importResult.successCount }} 条</p>
                <p>失败记录: {{ importResult.failCount }} 条</p>
              </div>
            </template>
          </el-result>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleClose">取消</el-button>
          <el-button
            v-if="currentStep === 0 && selectedFile"
            type="primary"
            @click="validateData"
          >
            下一步
          </el-button>
          <el-button
            v-if="currentStep === 1 && validationResult.errors.length === 0"
            type="primary"
            @click="startImport"
          >
            开始导入
          </el-button>
          <el-button
            v-if="currentStep === 1 && validationResult.errors.length > 0"
            @click="currentStep = 0"
          >
            返回修改
          </el-button>
          <el-button
            v-if="currentStep === 2"
            type="primary"
            @click="handleClose"
          >
            完成
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage, UploadFile } from 'element-plus'
import { UploadFilled, Download } from '@element-plus/icons-vue'
import * as XLSX from 'xlsx'

const dialogVisible = defineModel<boolean>('visible', { default: false })
const emits = defineEmits(['success'])

const currentStep = ref(0)
const selectedFile = ref<UploadFile | null>(null)
const uploadRef = ref()

const validationResult = reactive({
  total: 0,
  errors: [] as Array<{ row: number; field: string; message: string }>
})

const importResult = reactive({
  success: false,
  message: '',
  successCount: 0,
  failCount: 0
})

const handleFileChange = (file: UploadFile) => {
  selectedFile.value = file
}

const handleFileRemove = () => {
  selectedFile.value = null
}

const downloadTemplate = () => {
  // 创建模板数据
  const templateData = [
    ['用户名*', '邮箱*', '手机号', '姓名', '部门', '职位'],
    ['example1', 'example1@test.com', '13800138001', '张三', '技术部', '工程师'],
    ['example2', 'example2@test.com', '13800138002', '李四', '产品部', '产品经理']
  ]
  
  const ws = XLSX.utils.aoa_to_sheet(templateData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '用户导入模板')
  
  XLSX.writeFile(wb, '用户导入模板.xlsx')
  ElMessage.success('模板下载成功')
}

const validateData = async () => {
  if (!selectedFile.value?.raw) {
    ElMessage.error('请先选择文件')
    return
  }
  
  try {
    const data = await readFileAsArrayBuffer(selectedFile.value.raw)
    const workbook = XLSX.read(data, { type: 'array' })
    const worksheet = workbook.Sheets[workbook.SheetNames[0]]
    const jsonData = XLSX.utils.sheet_to_json(worksheet, { header: 1 })
    
    validationResult.total = jsonData.length - 1 // 减去标题行
    validationResult.errors = []
    
    // 验证数据
    for (let i = 1; i < jsonData.length; i++) {
      const row = jsonData[i] as string[]
      validateRow(row, i + 1)
    }
    
    currentStep.value = 1
  } catch (error) {
    ElMessage.error('文件读取失败')
  }
}

const validateRow = (row: string[], rowIndex: number) => {
  // 验证必填字段
  if (!row[0]) {
    validationResult.errors.push({
      row: rowIndex,
      field: '用户名',
      message: '用户名不能为空'
    })
  }
  
  if (!row[1]) {
    validationResult.errors.push({
      row: rowIndex,
      field: '邮箱',
      message: '邮箱不能为空'
    })
  } else if (!isValidEmail(row[1])) {
    validationResult.errors.push({
      row: rowIndex,
      field: '邮箱',
      message: '邮箱格式不正确'
    })
  }
  
  if (row[2] && !isValidMobile(row[2])) {
    validationResult.errors.push({
      row: rowIndex,
      field: '手机号',
      message: '手机号格式不正确'
    })
  }
}

const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

const isValidMobile = (mobile: string): boolean => {
  const mobileRegex = /^1[3-9]\d{9}$/
  return mobileRegex.test(mobile)
}

const readFileAsArrayBuffer = (file: File): Promise<ArrayBuffer> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target?.result as ArrayBuffer)
    reader.onerror = reject
    reader.readAsArrayBuffer(file)
  })
}

const startImport = async () => {
  // 模拟导入过程
  try {
    importResult.success = true
    importResult.message = '用户数据导入完成'
    importResult.successCount = validationResult.total - validationResult.errors.length
    importResult.failCount = validationResult.errors.length
    currentStep.value = 2
    
    setTimeout(() => {
      emits('success')
    }, 2000)
  } catch (error) {
    importResult.success = false
    importResult.message = '导入过程中发生错误'
    currentStep.value = 2
  }
}

const handleClose = () => {
  dialogVisible.value = false
  currentStep.value = 0
  selectedFile.value = null
  validationResult.total = 0
  validationResult.errors = []
  Object.assign(importResult, {
    success: false,
    message: '',
    successCount: 0,
    failCount: 0
  })
  uploadRef.value?.clearFiles()
}
</script>

<style lang="scss" scoped>
.user-import-container {
  .import-steps {
    margin-bottom: 30px;
  }
  
  .step-content {
    min-height: 200px;
    
    .upload-area {
      margin-bottom: 20px;
    }
    
    .template-download {
      text-align: center;
    }
    
    .validation-info {
      margin-bottom: 20px;
    }
    
    .validation-details {
      p {
        margin: 5px 0;
      }
      
      .error-list {
        margin-top: 15px;
      }
    }
    
    .import-result {
      .result-stats {
        p {
          margin: 5px 0;
          text-align: center;
        }
      }
    }
  }
  
  .dialog-footer {
    text-align: right;
  }
}
</style>