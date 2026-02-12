<template>
  <div class="department-tree">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>部门管理</span>
          <div>
            <el-input
              v-model="searchText"
              placeholder="搜索部门"
              clearable
              style="width: 200px; margin-right: 10px"
              @input="filterTree"
            />
            <el-button type="primary" @click="handleAddRoot">新增根部门</el-button>
          </div>
        </div>
      </template>

      <el-tree
        ref="treeRef"
        :data="filteredTreeData"
        :props="treeProps"
        node-key="id"
        default-expand-all
        :expand-on-click-node="false"
        :filter-node-method="filterNode"
      >
        <template #default="{ node, data }">
          <div class="custom-tree-node">
            <div class="node-content">
              <el-icon><OfficeBuilding /></el-icon>
              <span class="node-label">{{ node.label }}</span>
              <el-tag v-if="data.status === 0" type="danger" size="small" style="margin-left: 8px">禁用</el-tag>
            </div>
            <div class="node-actions">
              <el-button link type="primary" size="small" @click="handleAdd(data)">新增子部门</el-button>
              <el-button link type="primary" size="small" @click="handleEdit(data)">编辑</el-button>
              <el-button link type="danger" size="small" @click="handleDelete(data)">删除</el-button>
            </div>
          </div>
        </template>
      </el-tree>
    </el-card>

    <!-- 部门表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="formData.deptCode" placeholder="请输入部门编码" />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="formData.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="formData.parentId"
            :data="treeData"
            :props="treeProps"
            clearable
            placeholder="请选择上级部门"
            :check-strictly="true"
            node-key="id"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="部门描述">
          <el-input v-model="formData.description" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { OfficeBuilding } from '@element-plus/icons-vue'
import { organizationApi, type DepartmentVO } from '@/api/organization'

const treeRef = ref()
const formRef = ref<FormInstance>()
const treeData = ref<DepartmentVO[]>([])
const filteredTreeData = ref<DepartmentVO[]>([])
const searchText = ref('')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const isEdit = ref(false)

const treeProps = {
  label: 'deptName',
  children: 'children',
  value: 'id'
}

const formData = reactive({
  id: 0,
  deptCode: '',
  deptName: '',
  parentId: undefined as number | undefined,
  sortOrder: 0,
  description: '',
  status: 1
})

const formRules: FormRules = {
  deptCode: [
    { required: true, message: '请输入部门编码', trigger: 'blur' }
  ],
  deptName: [
    { required: true, message: '请输入部门名称', trigger: 'blur' }
  ]
}

// 加载部门树
const loadTreeData = async () => {
  try {
    const data = await organizationApi.getDepartmentTree()
    treeData.value = data
    filteredTreeData.value = data
  } catch (error) {
    ElMessage.error('加载部门树失败')
  }
}

// 过滤树节点
const filterNode = (value: string, data: DepartmentVO) => {
  if (!value) return true
  return data.deptName.includes(value) || data.deptCode.includes(value)
}

// 监听搜索框变化
watch(searchText, (val) => {
  treeRef.value?.filter(val)
})

const filterTree = () => {
  treeRef.value?.filter(searchText.value)
}

// 新增根部门
const handleAddRoot = () => {
  isEdit.value = false
  dialogTitle.value = '新增部门'
  resetForm()
  formData.parentId = undefined
  dialogVisible.value = true
}

// 新增子部门
const handleAdd = (data: DepartmentVO) => {
  isEdit.value = false
  dialogTitle.value = '新增子部门'
  resetForm()
  formData.parentId = data.id
  dialogVisible.value = true
}

// 编辑部门
const handleEdit = async (data: DepartmentVO) => {
  isEdit.value = true
  dialogTitle.value = '编辑部门'
  try {
    const detail = await organizationApi.getDepartmentById(data.id)
    Object.assign(formData, detail)
    dialogVisible.value = true
  } catch (error) {
    ElMessage.error('加载部门详情失败')
  }
}

// 删除部门
const handleDelete = async (data: DepartmentVO) => {
  try {
    await ElMessageBox.confirm(`确认删除部门 "${data.deptName}" 吗？`, '提示', {
      type: 'warning'
    })
    await organizationApi.deleteDepartment(data.id)
    ElMessage.success('删除成功')
    await loadTreeData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value) {
        await organizationApi.updateDepartment(formData)
        ElMessage.success('更新成功')
      } else {
        await organizationApi.createDepartment(formData)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      await loadTreeData()
    } catch (error) {
      ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: 0,
    deptCode: '',
    deptName: '',
    parentId: undefined,
    sortOrder: 0,
    description: '',
    status: 1
  })
}

onMounted(() => {
  loadTreeData()
})
</script>

<style lang="scss" scoped>
.department-tree {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-right: 8px;
    width: 100%;

    .node-content {
      display: flex;
      align-items: center;
      flex: 1;

      .node-label {
        margin-left: 8px;
      }
    }

    .node-actions {
      display: none;
    }

    &:hover .node-actions {
      display: flex;
      gap: 4px;
    }
  }

  :deep(.el-tree-node__content) {
    height: 40px;
  }
}
</style>
