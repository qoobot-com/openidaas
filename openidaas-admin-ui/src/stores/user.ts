import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userApi } from '@/api/user'
import { User, UserDetail, PageResponseUser } from '@/types'

export const useUserStore = defineStore('user', () => {
  // 状态
  const userList = ref<User[]>([])
  const currentUser = ref<UserDetail | null>(null)
  const loading = ref(false)
  const pagination = ref({
    page: 0,
    size: 20,
    total: 0,
    totalPages: 0
  })

  // Actions
  const fetchUsers = async (params: any = {}) => {
    try {
      loading.value = true
      const response: PageResponseUser = await userApi.getUsers({
        page: pagination.value.page,
        size: pagination.value.size,
        ...params
      })
      
      userList.value = response.content
      pagination.value.total = response.totalElements
      pagination.value.totalPages = response.totalPages
      
      return response
    } finally {
      loading.value = false
    }
  }

  const getUserById = async (id: number) => {
    try {
      loading.value = true
      const user: UserDetail = await userApi.getUserById(id)
      currentUser.value = user
      return user
    } finally {
      loading.value = false
    }
  }

  const createUser = async (userData: any) => {
    try {
      loading.value = true
      const user: User = await userApi.createUser(userData)
      // 如果当前在第一页，重新加载列表
      if (pagination.value.page === 0) {
        await fetchUsers()
      }
      return user
    } finally {
      loading.value = false
    }
  }

  const updateUser = async (id: number, userData: any) => {
    try {
      loading.value = true
      const user: User = await userApi.updateUser(id, userData)
      // 更新列表中的用户信息
      const index = userList.value.findIndex(u => u.id === id)
      if (index !== -1) {
        userList.value[index] = user
      }
      // 如果是当前用户，也更新当前用户信息
      if (currentUser.value?.id === id) {
        currentUser.value = { ...currentUser.value, ...user }
      }
      return user
    } finally {
      loading.value = false
    }
  }

  const deleteUser = async (id: number) => {
    try {
      loading.value = true
      await userApi.deleteUser(id)
      // 从列表中移除
      userList.value = userList.value.filter(u => u.id !== id)
      // 如果删除的是当前用户，清空当前用户
      if (currentUser.value?.id === id) {
        currentUser.value = null
      }
      return true
    } finally {
      loading.value = false
    }
  }

  const assignDepartment = async (userId: number, assignmentData: any) => {
    try {
      loading.value = true
      await userApi.assignDepartment(userId, assignmentData)
      // 重新获取用户详情
      if (currentUser.value?.id === userId) {
        await getUserById(userId)
      }
      return true
    } finally {
      loading.value = false
    }
  }

  const assignRole = async (userId: number, assignmentData: any) => {
    try {
      loading.value = true
      await userApi.assignRole(userId, assignmentData)
      // 重新获取用户详情
      if (currentUser.value?.id === userId) {
        await getUserById(userId)
      }
      return true
    } finally {
      loading.value = false
    }
  }

  const setPage = (page: number) => {
    pagination.value.page = page
  }

  const setSize = (size: number) => {
    pagination.value.size = size
  }

  const resetPagination = () => {
    pagination.value.page = 0
    pagination.value.size = 20
    pagination.value.total = 0
    pagination.value.totalPages = 0
  }

  return {
    // 状态
    userList,
    currentUser,
    loading,
    pagination,
    
    // Actions
    fetchUsers,
    getUserById,
    createUser,
    updateUser,
    deleteUser,
    assignDepartment,
    assignRole,
    setPage,
    setSize,
    resetPagination
  }
})