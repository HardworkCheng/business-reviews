<template>
  <div class="note-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>笔记管理</span>
          <el-button type="primary" @click="$router.push('/notes/create')">
            <el-icon><Plus /></el-icon>
            发布笔记
          </el-button>
        </div>
      </template>
      
      <!-- 搜索条件 -->
      <el-form :model="searchForm" inline>
        <el-form-item label="笔记标题">
          <el-input v-model="searchForm.title" placeholder="请输入笔记标题" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="草稿" :value="0" />
            <el-option label="待审核" :value="1" />
            <el-option label="已发布" :value="2" />
            <el-option label="已下线" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="门店">
          <el-select v-model="searchForm.shopId" placeholder="请选择门店" clearable>
            <el-option 
              v-for="shop in shopList" 
              :key="shop.id" 
              :label="shop.name" 
              :value="shop.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchNotes">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 笔记列表 -->
      <el-table :data="noteList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="笔记标题" />
        <el-table-column prop="shopName" label="关联门店" width="120" />
        <el-table-column prop="views" label="浏览量" width="100" />
        <el-table-column prop="likes" label="点赞数" width="100" />
        <el-table-column prop="comments" label="评论数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getNoteStatusTag(scope.row.status)">
              {{ getNoteStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="editNote(scope.row)">编辑</el-button>
            <el-button 
              v-if="scope.row.status === 2" 
              link 
              type="warning" 
              @click="offlineNote(scope.row)"
            >
              下线
            </el-button>
            <el-button 
              v-else-if="scope.row.status === 0 || scope.row.status === 3" 
              link 
              type="success" 
              @click="publishNote(scope.row)"
            >
              发布
            </el-button>
            <el-button link type="danger" @click="deleteNote(scope.row)">删除</el-button>
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
import { getNoteList, deleteNote as deleteNoteApi, publishNote as publishNoteApi, offlineNote as offlineNoteApi } from '@/api/note'
import { getShopList } from '@/api/shop'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  title: '',
  status: undefined as number | undefined,
  shopId: undefined as number | undefined
})

// 分页
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 笔记列表
const noteList = ref<any[]>([])

// 门店列表
const shopList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 获取笔记状态名称
const getNoteStatusName = (status: number) => {
  const statuses: Record<number, string> = {
    0: '草稿',
    1: '待审核',
    2: '已发布',
    3: '已下线'
  }
  return statuses[status] || '未知'
}

// 获取笔记状态标签
const getNoteStatusTag = (status: number) => {
  const tags: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return tags[status] || 'info'
}

// 搜索笔记
const searchNotes = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm.value,
      pageNum: pagination.value.currentPage,
      pageSize: pagination.value.pageSize
    }
    
    const res = await getNoteList(params)
    noteList.value = res.list || res.records || []
    pagination.value.total = res.total || 0
  } catch (error) {
    ElMessage.error('获取笔记列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    title: '',
    status: undefined,
    shopId: undefined
  }
  pagination.value.currentPage = 1
  searchNotes()
}

// 编辑笔记
const editNote = (row: any) => {
  router.push(`/notes/edit/${row.id}`)
}

// 发布笔记
const publishNote = async (row: any) => {
  try {
    await publishNoteApi(row.id)
    ElMessage.success('发布成功')
    searchNotes()
  } catch (error) {
    ElMessage.error('发布失败')
  }
}

// 下线笔记
const offlineNote = async (row: any) => {
  try {
    await offlineNoteApi(row.id)
    ElMessage.success('下线成功')
    searchNotes()
  } catch (error) {
    ElMessage.error('下线失败')
  }
}

// 删除笔记
const deleteNote = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除笔记 "${row.title}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteNoteApi(row.id)
      ElMessage.success('删除成功')
      searchNotes()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 获取门店列表
const fetchShopList = async () => {
  try {
    const res = await getShopList({ pageNum: 1, pageSize: 100 })
    shopList.value = res.list || res.records || []
  } catch (error) {
    ElMessage.error('获取门店列表失败')
  }
}

// 分页变化
const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  searchNotes()
}

const handleCurrentChange = (val: number) => {
  pagination.value.currentPage = val
  searchNotes()
}

// 页面加载时获取数据
onMounted(() => {
  searchNotes()
  fetchShopList()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>