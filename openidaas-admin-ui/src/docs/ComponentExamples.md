# 组件使用示例

## 1. 全局状态组件

### GlobalLoading - 全局加载状态

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { GlobalLoading } from '@/components'

const loading = ref(false)

const showLoading = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 2000)
}
</script>

<template>
  <div>
    <el-button @click="showLoading">显示加载</el-button>
    <GlobalLoading v-model="loading" text="数据加载中..." />
  </div>
</template>
```

### GlobalEmpty - 空状态

```vue
<script setup lang="ts">
import { GlobalEmpty } from '@/components'

const handleAction = () => {
  console.log('点击了操作按钮')
}
</script>

<template>
  <div>
    <GlobalEmpty type="data" />
    <GlobalEmpty type="search" @action="handleAction" action-text="重新搜索" />
  </div>
</template>
```

### GlobalError - 错误状态

```vue
<script setup lang="ts">
import { GlobalError } from '@/components'

const handleRetry = () => {
  console.log('重试')
}
</script>

<template>
  <div>
    <GlobalError type="network" @retry="handleRetry" />
    <GlobalError type="permission" show-home />
  </div>
</template>
```

## 2. Composables

### useConfirm - 操作确认对话框

```vue
<script setup lang="ts">
import { useConfirm } from '@/composables'

const { confirm, confirmDelete, confirmAction } = useConfirm()

const handleDelete = async (id: number) => {
  const confirmed = await confirmDelete('用户')
  if (confirmed) {
    // 执行删除操作
    console.log('删除用户', id)
  }
}

const customConfirm = async () => {
  const result = await confirm({
    title: '自定义标题',
    message: '确定执行此操作吗？',
    type: 'warning'
  })
  if (result !== null) {
    // 确认操作
  }
}
</script>

<template>
  <div>
    <el-button @click="handleDelete(1)">删除用户</el-button>
    <el-button @click="customConfirm">自定义确认</el-button>
  </div>
</template>
```

### useMessage - 消息提示

```vue
<script setup lang="ts">
import { useMessage } from '@/composables'

const {
  success,
  error,
  warning,
  info,
  submitSuccess,
  submitError,
  deleteSuccess,
  deleteError
} = useMessage()

const handleSuccess = () => {
  success('操作成功')
}

const handleError = () => {
  error('操作失败')
}

const handleSubmit = () => {
  // 模拟API调用
  try {
    submitSuccess('保存')
  } catch (e) {
    submitError('保存', '网络错误')
  }
}

const handleDelete = () => {
  deleteSuccess()
}
</script>

<template>
  <div>
    <el-space>
      <el-button @click="handleSuccess">成功提示</el-button>
      <el-button @click="handleError">错误提示</el-button>
      <el-button @click="handleSubmit">提交</el-button>
      <el-button @click="handleDelete">删除</el-button>
    </el-space>
  </div>
</template>
```

## 3. 图片懒加载指令

```vue
<script setup lang="ts">
import { ref } from 'vue'

const images = ref([
  'https://example.com/image1.jpg',
  'https://example.com/image2.jpg',
  'https://example.com/image3.jpg'
])
</script>

<template>
  <div>
    <img
      v-for="(img, index) in images"
      :key="index"
      v-lazy="img"
      alt="Lazy Image"
      style="width: 200px; height: 200px;"
    />
  </div>
</template>

<style scoped>
img {
  transition: opacity 0.3s;
}

img.lazy-loaded {
  opacity: 1;
}

img:not(.lazy-loaded) {
  opacity: 0.5;
}

img.lazy-error {
  opacity: 1;
  background-color: #f5f5f5;
}
</style>
```

## 4. VirtualTable - 虚拟滚动表格

```vue
<script setup lang="ts">
import { ref } from 'vue'
import { VirtualTable } from '@/components'

const largeData = ref(
  Array.from({ length: 10000 }, (_, i) => ({
    id: i + 1,
    name: `用户 ${i + 1}`,
    email: `user${i + 1}@example.com`,
    status: i % 3 === 0 ? 'active' : 'inactive'
  }))
)
</script>

<template>
  <VirtualTable :data="largeData" height="600px" stripe border>
    <el-table-column prop="id" label="ID" width="80" />
    <el-table-column prop="name" label="姓名" width="150" />
    <el-table-column prop="email" label="邮箱" width="200" />
    <el-table-column prop="status" label="状态" width="100" />
  </VirtualTable>
</template>
```

## 5. 更新现有组件使用新的 Composables

### 在 UserList.vue 中使用

```vue
<script setup lang="ts">
import { useMessage, useConfirm } from '@/composables'

const { success, error, submitSuccess, deleteSuccess } = useMessage()
const { confirmDelete } = useConfirm()

const handleDelete = async (row: UserVO) => {
  const confirmed = await confirmDelete(row.username)
  if (confirmed) {
    try {
      await userApi.deleteUser(row.id)
      deleteSuccess()
      fetchUsers()
    } catch (err) {
      error('删除失败')
    }
  }
}
</script>
```

## 6. 添加全局样式

在 `src/styles/index.scss` 中添加:

```scss
// 懒加载样式
img.lazy-loaded {
  opacity: 1;
  transition: opacity 0.3s ease;
}

img:not(.lazy-loaded) {
  opacity: 0.5;
}

img.lazy-error {
  opacity: 1;
  background-color: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 14px;
}

// 自定义确认对话框样式
.custom-confirm-dialog {
  .el-message-box__message {
    font-size: 15px;
  }
}

// 虚拟表格样式
.virtual-table-container {
  .el-table {
    width: 100%;
  }
}
```
