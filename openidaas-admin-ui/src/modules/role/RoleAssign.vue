<template>
  <div class="role-assign-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色权限分配 - {{ roleInfo.roleName }}</span>
          <div>
            <el-button @click="handleBack">返回</el-button>
            <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          </div>
        </div>
      </template>

      <div class="assign-content">
        <div class="role-info">
          <el-descriptions :column="3" border>
            <el-descriptions-item label="角色编码">{{ roleInfo.roleCode }}</el-descriptions-item>
            <el-descriptions-item label="角色名称">{{ roleInfo.roleName }}</el-descriptions-item>
            <el-descriptions-item label="角色类型">
              {{ getRoleTypeLabel(roleInfo.roleType) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="permission-tree">
          <div class="tree-header">
            <el-checkbox
              v-model="checkAll"
              :indeterminate="isIndeterminate"
              @change="handleCheckAll"
            >
              全选
            </el-checkbox>
            <div class="selected-count">
              已选: {{ checkedPermissions.length }} 项
            </div>
          </div>
          <el-tree
            ref="treeRef"
            :data="permissionTree"
            node-key="id"
            show-checkbox
            default-expand-all
            :props="treeProps"
            @check="handleCheckChange"
          >
            <template #default="{ node, data }">
              <div class="custom-tree-node">
                <el-icon>
                  <component :is="getIcon(data.type)" />
                </el-icon>
                <span class="node-label">{{ node.label }}</span>
                <el-tag v-if="data.code" size="small" type="info">{{ data.code }}</el-tag>
              </div>
            </template>
          </el-tree>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { roleApi, type RoleVO } from '@/api/role'

const router = useRouter()
const route = useRoute()
const treeRef = ref()
const saving = ref(false)
const checkAll = ref(false)
const isIndeterminate = ref(false)
const checkedPermissions = ref<number[]>([])

const roleInfo = reactive({
  id: 0,
  roleCode: '',
  roleName: '',
  roleType: 2
})

const treeProps = {
  label: 'name',
  children: 'children'
}

// 模拟权限树数据 - 实际应从后端获取
const permissionTree = ref([
  {
    id: 1,
    name: '系统管理',
    type: 'menu',
    children: [
      {
        id: 11,
        name: '用户管理',
        type: 'menu',
        children: [
          { id: 111, name: '查看用户', type: 'action', code: 'user:view' },
          { id: 112, name: '创建用户', type: 'action', code: 'user:create' },
          { id: 113, name: '编辑用户', type: 'action', code: 'user:edit' },
          { id: 114, name: '删除用户', type: 'action', code: 'user:delete' }
        ]
      },
      {
        id: 12,
        name: '角色管理',
        type: 'menu',
        children: [
          { id: 121, name: '查看角色', type: 'action', code: 'role:view' },
          { id: 122, name: '创建角色', type: 'action', code: 'role:create' },
          { id: 123, name: '编辑角色', type: 'action', code: 'role:edit' },
          { id: 124, name: '删除角色', type: 'action', code: 'role:delete' },
          { id: 125, name: '分配权限', type: 'action', code: 'role:assign' }
        ]
      },
      {
        id: 13,
        name: '部门管理',
        type: 'menu',
        children: [
          { id: 131, name: '查看部门', type: 'action', code: 'dept:view' },
          { id: 132, name: '创建部门', type: 'action', code: 'dept:create' },
          { id: 133, name: '编辑部门', type: 'action', code: 'dept:edit' },
          { id: 134, name: '删除部门', type: 'action', code: 'dept:delete' }
        ]
      }
    ]
  },
  {
    id: 2,
    name: '业务管理',
    type: 'menu',
    children: [
      {
        id: 21,
        name: '应用管理',
        type: 'menu',
        children: [
          { id: 211, name: '查看应用', type: 'action', code: 'app:view' },
          { id: 212, name: '创建应用', type: 'action', code: 'app:create' },
          { id: 213, name: '编辑应用', type: 'action', code: 'app:edit' },
          { id: 214, name: '删除应用', type: 'action', code: 'app:delete' }
        ]
      },
      {
        id: 22,
        name: '审计管理',
        type: 'menu',
        children: [
          { id: 221, name: '查看日志', type: 'action', code: 'audit:view' },
          { id: 222, name: '导出日志', type: 'action', code: 'audit:export' }
        ]
      }
    ]
  }
])

const getRoleTypeLabel = (type: number) => {
  const map: Record<number, string> = {
    1: '系统角色',
    2: '业务角色',
    3: '数据角色'
  }
  return map[type] || '未知'
}

const getIcon = (type: string) => {
  const icons: Record<string, string> = {
    menu: 'Folder',
    action: 'Document'
  }
  return icons[type] || 'Menu'
}

// 加载角色信息
const loadRoleInfo = async (id: number) => {
  try {
    const role = await roleApi.getRoleById(id)
    Object.assign(roleInfo, role)
  } catch (error) {
    ElMessage.error('加载角色信息失败')
  }
}

// 加载角色已有权限
const loadRolePermissions = async (id: number) => {
  try {
    const permIds = await roleApi.getRolePermissions(id)
    checkedPermissions.value = permIds
    treeRef.value?.setCheckedKeys(permIds)
  } catch (error) {
    ElMessage.error('加载角色权限失败')
  }
}

// 全选
const handleCheckAll = (val: boolean) => {
  const allKeys = getAllKeys(permissionTree.value)
  treeRef.value?.setCheckedKeys(val ? allKeys : [])
  checkedPermissions.value = val ? allKeys : []
  isIndeterminate.value = false
}

// 获取所有节点key
const getAllKeys = (nodes: any[]): number[] => {
  const keys: number[] = []
  const traverse = (list: any[]) => {
    list.forEach(node => {
      keys.push(node.id)
      if (node.children) {
        traverse(node.children)
      }
    })
  }
  traverse(nodes)
  return keys
}

// 复选框变化
const handleCheckChange = () => {
  const checkedKeys = treeRef.value?.getCheckedKeys() || []
  const allKeys = getAllKeys(permissionTree.value)
  checkedPermissions.value = checkedKeys

  checkAll.value = checkedKeys.length === allKeys.length
  isIndeterminate.value = checkedKeys.length > 0 && checkedKeys.length < allKeys.length
}

// 保存
const handleSave = async () => {
  saving.value = true
  try {
    const checkedKeys = treeRef.value?.getCheckedKeys() || []
    await roleApi.assignPermissions(roleInfo.id, checkedKeys)
    ElMessage.success('保存成功')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

// 返回
const handleBack = () => {
  router.push('/role/list')
}

onMounted(async () => {
  const id = parseInt(route.params.id as string)
  if (id) {
    await loadRoleInfo(id)
    await loadRolePermissions(id)
  }
})
</script>

<style lang="scss" scoped>
.role-assign-container {
  padding: 20px;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .assign-content {
    .role-info {
      margin-bottom: 20px;
    }

    .permission-tree {
      .tree-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 10px 0;
        border-bottom: 1px solid #e4e7ed;
        margin-bottom: 15px;

        .selected-count {
          color: #909399;
          font-size: 14px;
        }
      }

      .custom-tree-node {
        display: flex;
        align-items: center;
        gap: 8px;

        .node-label {
          flex: 1;
        }
      }

      :deep(.el-tree-node__content) {
        height: 36px;
      }
    }
  }
}
</style>
