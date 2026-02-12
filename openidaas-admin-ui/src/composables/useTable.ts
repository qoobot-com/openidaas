// 表格操作组合式函数
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

export interface TableOptions {
  page?: number
  size?: number
  sort?: string
  [key: string]: any
}

export const useTable = <T>(
  fetchData: (params: TableOptions) => Promise<{ content: T[]; totalElements: number }>,
  options: TableOptions = {}
) => {
  // 表格数据
  const tableData = ref<T[]>([])
  const loading = ref(false)
  const total = ref(0)
  
  // 分页配置
  const pagination = reactive({
    page: options.page || 0,
    size: options.size || 20,
    sizes: [10, 20, 50, 100],
    layout: 'total, sizes, prev, pager, next, jumper',
    background: true,
    ...options
  })
  
  // 选中项
  const selectedRows = ref<T[]>([])
  
  // 获取表格数据
  const getTableData = async (params: TableOptions = {}) => {
    try {
      loading.value = true
      
      const queryParams = {
        page: pagination.page,
        size: pagination.size,
        ...options,
        ...params
      }
      
      const response = await fetchData(queryParams)
      tableData.value = response.content
      total.value = response.totalElements
      
      return response
    } catch (error) {
      console.error('获取表格数据失败:', error)
      ElMessage.error('获取数据失败')
      throw error
    } finally {
      loading.value = false
    }
  }
  
  // 分页改变
  const handleSizeChange = (val: number) => {
    pagination.size = val
    pagination.page = 0
    getTableData()
  }
  
  const handleCurrentChange = (val: number) => {
    pagination.page = val - 1
    getTableData()
  }
  
  // 刷新表格
  const refreshTable = () => {
    getTableData()
  }
  
  // 重置分页
  const resetPagination = () => {
    pagination.page = 0
    pagination.size = 20
  }
  
  // 选中行改变
  const handleSelectionChange = (selection: T[]) => {
    selectedRows.value = selection
  }
  
  // 批量删除
  const batchDelete = async (
    deleteFn: (ids: number[]) => Promise<any>,
    getRowId: (row: T) => number,
    successMessage = '删除成功'
  ) => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请先选择要删除的数据')
      return
    }
    
    try {
      await ElMessageBox.confirm(
        `确定要删除选中的 ${selectedRows.value.length} 条数据吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      
      const ids = selectedRows.value.map(row => getRowId(row as T))
      await deleteFn(ids)
      
      ElMessage.success(successMessage)
      refreshTable()
      selectedRows.value = []
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除失败:', error)
        ElMessage.error('删除失败')
      }
    }
  }
  
  return {
    // 数据
    tableData,
    loading,
    total,
    selectedRows,
    
    // 配置
    pagination,
    
    // 方法
    getTableData,
    handleSizeChange,
    handleCurrentChange,
    refreshTable,
    resetPagination,
    handleSelectionChange,
    batchDelete
  }
}