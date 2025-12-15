<template>
  <div class="shop-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>门店管理</span>
          <el-button type="primary" @click="$router.push('/shops/create')">
            <el-icon><Plus /></el-icon>
            新增门店
          </el-button>
        </div>
      </template>
      
      <!-- 搜索条件 -->
      <el-form :model="searchForm" inline>
        <el-form-item label="门店名称">
          <el-input v-model="searchForm.name" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchShops">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 门店列表 -->
      <el-table :data="shopList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="门店名称" />
        <el-table-column prop="address" label="地址" />
        <el-table-column prop="phone" label="联系电话" width="140">
          <template #default="scope">
            {{ scope.row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="editShop(scope.row)">编辑</el-button>
            <el-button 
              link 
              :type="scope.row.status === 1 ? 'warning' : 'success'" 
              @click="toggleShopStatus(scope.row)"
            >
              {{ scope.row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button 
              link 
              type="danger" 
              @click="deleteShop(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getShopList, deleteShop as deleteShopApi, updateShopStatus } from '@/api/shop'

const router = useRouter()

// 格式化时间
const formatTime = (time: string) => {
  if (!time) return '-'
  try {
    const date = new Date(time)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch {
    return time
  }
}

// 搜索表单
const searchForm = ref({
  name: '',
  status: undefined as number | undefined
})

// 分页
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 门店列表
const shopList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 搜索门店
const searchShops = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm.value,
      pageNum: pagination.value.currentPage,
      pageSize: pagination.value.pageSize
    }
    
    const res = await getShopList(params)
    shopList.value = res.list || res.records || []
    pagination.value.total = res.total || 0
  } catch (error) {
    ElMessage.error('获取门店列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    name: '',
    status: undefined
  }
  pagination.value.currentPage = 1
  searchShops()
}

// 编辑门店
const editShop = (row: any) => {
  router.push(`/shops/edit/${row.id}`)
}

// 切换门店状态
const toggleShopStatus = async (row: any) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '停用'
  
  try {
    await ElMessageBox.confirm(`确定要${action}该门店吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await updateShopStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    searchShops()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败`)
    }
  }
}

// 删除门店
const deleteShop = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除该门店吗？此操作不可恢复！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteShopApi(row.id)
    ElMessage.success('删除成功')
    searchShops()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 分页变化
const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  searchShops()
}

const handleCurrentChange = (val: number) => {
  pagination.value.currentPage = val
  searchShops()
}

// 页面加载时获取数据
onMounted(() => {
  searchShops()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>