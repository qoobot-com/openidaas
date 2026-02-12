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
                  <component :is="getIcon(data.type)" />
                </el-icon>
                <span class="node-label">{{ node.label }}</span>
                <el-tag :type="getTypeTag(data.type)" size="small" style="margin-left: 8px">
                  {{ getTypeLabel(data.type) }}
                </el-tag>
                <el-tag v-if="data.code" size="small" type="info" style="margin-left: 8px">
                  {{ data.code }}
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
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入权限编码" />
        </el-form-item>
        <el-form-item label="权限类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择权限类型" style="width: 100%">
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

const formRef = ref<FormInstance>()
const treeRef = ref()
const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const isEdit = ref(false)

const treeProps = {
  label: 'name',
  children: 'children',
  value: 'id'
}

// 模拟权限树数据 - 实际应从后端获取
const treeData = ref([
  {
    id: 1,
    name: '系统管理',
    code: 'system',
    type: 'menu',
    path: '/system',
    icon: 'Setting',
    sortOrder: 1,
    children: [
      {
        id: 11,
        name: '用户管理',
        code: 'system:user',
        type: 'menu',
        path: '/system/user',
        component: 'user/UserList',
        icon: 'User',
        sortOrder: 1,
        children: [
          { id: 111, name: '查看用户', code: 'user:view', type: 'action', sortOrder: 1 },
          { id: 112, name: '创建用户', code: 'user:create', type: 'action', sortOrder: 2 },
          { id: 113, name: '编辑用户', code: 'user:edit', type: 'action', sortOrder: 3 },
          { id: 114, name: '删除用户', code: 'user:delete', type: 'action', sortOrder: 4 }
        ]
      },
      {
        id: 12,
        name: '角色管理',
        code: 'system:role',
        type: 'menu',
        path: '/system/role',
        component: 'role/RoleList',
        icon: 'UserFilled',
        sortOrder: 2,
        children: [
          { id: 121, name: '查看角色', code: 'role:view', type: 'action', sortOrder: 1 },
          { id: 122, name: '创建角色', code: 'role:create', type: 'action', sortOrder: 2 },
          { id: 123, name: '编辑角色', code: 'role:edit', type: 'action', sortOrder: 3 },
          { id: 124, name: '删除角色', code: 'role:delete', type: 'action', sortOrder: 4 },
          { id: 125, name: '分配权限', code: 'role:assign', type: 'action', sortOrder: 5 }
        ]
      },
      {
        id: 13,
        name: '部门管理',
        code: 'system:dept',
        type: 'menu',
        path: '/system/dept',
        component: 'organization/DepartmentTree',
        icon: 'OfficeBuilding',
        sortOrder: 3,
        children: [
          { id: 131, name: '查看部门', code: 'dept:view', type: 'action', sortOrder: 1 },
          { id: 132, name: '创建部门', code: 'dept:create', type: 'action', sortOrder: 2 },
          { id: 133, name: '编辑部门', code: 'dept:edit', type: 'action', sortOrder: 3 },
          { id: 134, name: '删除部门', code: 'dept:delete', type: 'action', sortOrder: 4 }
        ]
      }
    ]
  },
  {
    id: 2,
    name: '业务管理',
    code: 'business',
    type: 'menu',
    path: '/business',
    icon: 'Briefcase',
    sortOrder: 2,
    children: [
      {
        id: 21,
        name: '应用管理',
        code: 'business:app',
        type: 'menu',
        path: '/business/app',
        component: 'application/ApplicationList',
        icon: 'Application',
        sortOrder: 1,
        children: [
          { id: 211, name: '查看应用', code: 'app:view', type: 'action', sortOrder: 1 },
          { id: 212, name: '创建应用', code: 'app:create', type: 'action', sortOrder: 2 },
          { id: 213, name: '编辑应用', code: 'app:edit', type: 'action', sortOrder: 3 },
          { id: 214, name: '删除应用', code: 'app:delete', type: 'action', sortOrder: 4 }
        ]
      },
      {
        id: 22,
        name: '审计管理',
        code: 'business:audit',
        type: 'menu',
        path: '/business/audit',
        component: 'audit/AuditLogList',
        icon: 'Document',
        sortOrder: 2,
        children: [
          { id: 221, name: '查看日志', code: 'audit:view', type: 'action', sortOrder: 1 },
          { id: 222, name: '导出日志', code: 'audit:export', type: 'action', sortOrder: 2 }
        ]
      }
    ]
  }
])

const formData = reactive({
  id: 0,
  name: '',
  code: '',
  type: 'menu',
  parentId: undefined as number | undefined,
  sortOrder: 0,
  path: '',
  component: '',
  icon: '',
  description: ''
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限编码', trigger: 'blur' }
  ],
  type: [
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

// 新增根权限
const handleAddRoot = () => {
  isEdit.value = false
  dialogTitle.value = '新增根权限'
  resetForm()
  formData.parentId = undefined
  dialogVisible.value = true
}

// 新增子权限
const handleAdd = (data: any) => {
  isEdit.value = false
  dialogTitle.value = '新增子权限'
  resetForm()
  formData.parentId = data.id
  dialogVisible.value = true
}

// 编辑权限
const handleEdit = (data: any) => {
  isEdit.value = true
  dialogTitle.value = '编辑权限'
  Object.assign(formData, data)
  dialogVisible.value = true
}

// 删除权限
const handleDelete = async (data: any) => {
  if (data.children && data.children.length > 0) {
    ElMessage.warning('该权限下有子权限，不能删除')
    return
  }
  try {
    await ElMessageBox.confirm(`确认删除权限 "${data.name}" 吗？`, '提示', {
      type: 'warning'
    })
    // 调用删除API
    ElMessage.success('删除成功')
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
      // 调用创建/更新API
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
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
    name: '',
    code: '',
    type: 'menu',
    parentId: undefined,
    sortOrder: 0,
    path: '',
    component: '',
    icon: '',
    description: ''
  })
}

onMounted(() => {
  // 实际应从后端加载权限树
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
