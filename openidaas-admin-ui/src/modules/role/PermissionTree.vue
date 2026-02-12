<template>
  <div class="permission-tree-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <div>
            <el-button type="primary" @click="handleAddRoot">新增根权限</el-button>
          </div>
        </div>
      </template>

      <div class="tree-wrapper">
        <el-tree
          ref="treeRef"
          :data="treeData"
          node-key="id"
          default-expand-all
          :expand-on-click-node="false"
          :props="treeProps"
        >
          <template #default="{ node, data }">
            <div class="custom-tree-node">
              <div class="node-content">
                <el-icon>
                  <component :is="getIcon(data.permType)" />
                </el-icon>
                <span class="node-label">{{ node.label }}</span>
                <el-tag :type="getTypeTag(data.permType)" size="small" style="margin-left: 8px">
                  {{ getTypeLabel(data.permType) }}
                </el-tag>
                <el-tag v-if="data.permCode" size="small" type="info" style="margin-left: 8px">
                  {{ data.permCode }}
                </el-tag>
              </div>
              <div class="node-actions">
                <el-button link type="primary" size="small" @click="handleAdd(data)">新增子权限</el-button>
                <el-button link type="primary" size="small" @click="handleEdit(data)">编辑</el-button>
                <el-button link type="danger" size="small" @click="handleDelete(data)">删除</el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>
    </el-card>

    <!-- 权限表单对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="resetForm"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="权限名称" prop="permName">
          <el-input v-model="formData.permName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permCode">
          <el-input v-model="formData.permCode" placeholder="请输入权限编码" />
        </el-form-item>
        <el-form-item label="权限类型" prop="permType">
          <el-select v-model="formData.permType" placeholder="请选择权限类型" style="width: 100%">
            <el-option label="菜单" value="menu" />
            <el-option label="按钮/操作" value="action" />
          </el-select>
        </el-form-item>
        <el-form-item label="上级权限">
          <el-tree-select
            v-model="formData.parentId"
            :data="treeData"
            :props="treeProps"
            clearable
            placeholder="请选择上级权限"
            :check-strictly="true"
            node-key="id"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="路由地址">
          <el-input v-model="formData.path" placeholder="请输入路由地址" />
        </el-form-item>
        <el-form-item label="组件路径">
          <el-input v-model="formData.component" placeholder="请输入组件路径" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="formData.icon" placeholder="请输入图标名称" />
        </el-form-item>
        <el-form-item label="描述">
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Menu, Document } from '@element-plus/icons-vue'
import { permissionApi } from '@/api/permission'
import type { PermissionVO } from '@/api/permission'

const formRef = ref<FormInstance>()
const treeRef = ref()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const isEdit = ref(false)
const loading = ref(false)

const treeProps = {
  label: 'permName',
  children: 'children',
  value: 'id'
}

// 权限树数据
const treeData = ref<PermissionVO[]>([])

const formData = reactive({
  id: 0,
  permName: '',
  permCode: '',
  permType: 'menu',
  parentId: undefined as number | undefined,
  sortOrder: 0,
  path: '',
  component: '',
  icon: '',
  description: ''
})

const formRules: FormRules = {
  permName: [
    { required: true, message: '请输入权限名称', trigger: 'blur' }
  ],
  permCode: [
    { required: true, message: '请输入权限编码', trigger: 'blur' }
  ],
  permType: [
    { required: true, message: '请选择权限类型', trigger: 'change' }
  ]
}

const getTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    menu: '菜单',
    action: '操作'
  }
  return map[type] || '未知'
}

const getTypeTag = (type: string) => {
  const map: Record<string, string> = {
    menu: 'primary',
    action: 'success'
  }
  return map[type] || 'info'
}

const getIcon = (type: string) => {
  return type === 'menu' ? Menu : Document
}

// 加载权限树
const loadPermissions = async () => {
  loading.value = true
  try {
    const result = await permissionApi.getPermissionTree()
    treeData.value = result.data || []
  } catch (error) {
    console.error('加载权限失败:', error)
    ElMessage.error('加载权限失败')
  } finally {
    loading.value = false
  }
}

// 新增根权限
const handleAddRoot = () => {
  isEdit.value = false
  dialogTitle.value = '新增根权限'
  resetForm()
  formData.parentId = undefined
  dialogVisible.value = true
}

// 新增子权限
const handleAdd = (data: PermissionVO) => {
  isEdit.value = false
  dialogTitle.value = '新增子权限'
  resetForm()
  formData.parentId = data.id
  dialogVisible.value = true
}

// 编辑权限
const handleEdit = (data: PermissionVO) => {
  isEdit.value = true
  dialogTitle.value = '编辑权限'
  Object.assign(formData, data)
  dialogVisible.value = true
}

// 删除权限
const handleDelete = async (data: PermissionVO) => {
  if (data.children && data.children.length > 0) {
    ElMessage.warning('该权限下有子权限，不能删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除权限 "${data.permName}" 吗？`, '提示', {
      type: 'warning'
    })
    await permissionApi.deletePermission(data.id)
    ElMessage.success('删除成功')
    loadPermissions()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
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
        await permissionApi.updatePermission({
          id: formData.id,
          permName: formData.permName,
          permCode: formData.permCode,
          permType: formData.permType,
          parentId: formData.parentId,
          path: formData.path,
          component: formData.component,
          icon: formData.icon,
          sortOrder: formData.sortOrder,
          description: formData.description
        })
      } else {
        await permissionApi.createPermission({
          permName: formData.permName,
          permCode: formData.permCode,
          permType: formData.permType,
          parentId: formData.parentId,
          path: formData.path,
          component: formData.component,
          icon: formData.icon,
          sortOrder: formData.sortOrder,
          description: formData.description
        })
      }
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadPermissions()
    } catch (error: any) {
      console.error('提交失败:', error)
      ElMessage.error(error.response?.data?.message || (isEdit.value ? '更新失败' : '创建失败'))
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
    permName: '',
    permCode: '',
    permType: 'menu',
    parentId: undefined,
    sortOrder: 0,
    path: '',
    component: '',
    icon: '',
    description: ''
  })
}

onMounted(() => {
  loadPermissions()
})
</script>

<style lang="scss" scoped>
.permission-tree-container {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .tree-wrapper {
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
}
</style>
