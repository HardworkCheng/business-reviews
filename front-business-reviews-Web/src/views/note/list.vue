<template>
  <div class="note-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">笔记管理</h1>
        <p class="page-desc">管理您发布的笔记，UniApp用户可实时查看</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/notes/create')" class="create-btn">
        <el-icon><Plus /></el-icon>发布笔记
      </el-button>
    </div>

    <!-- 筛选和搜索 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <button class="tab-btn" :class="{ active: currentFilter === 'all' }" @click="setFilter('all')">全部 <span class="count">{{ counts.all }}</span></button>
        <button class="tab-btn" :class="{ active: currentFilter === 'published' }" @click="setFilter('published')">已发布 <span class="count success">{{ counts.published }}</span></button>
        <button class="tab-btn" :class="{ active: currentFilter === 'pending' }" @click="setFilter('pending')">待审核 <span class="count warning">{{ counts.pending }}</span></button>
        <button class="tab-btn" :class="{ active: currentFilter === 'draft' }" @click="setFilter('draft')">草稿 <span class="count">{{ counts.draft }}</span></button>
      </div>
      <div class="search-area">
        <el-input v-model="searchKeyword" placeholder="搜索笔记标题..." class="search-input" clearable @keyup.enter="searchNotes">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="selectedShop" placeholder="选择门店" clearable class="shop-select">
          <el-option v-for="shop in shopList" :key="shop.id" :label="shop.name" :value="shop.id" />
        </el-select>
      </div>
    </div>

    <!-- 笔记卡片网格 -->
    <div class="note-grid" v-loading="loading">
      <div v-for="note in noteList" :key="note.id" class="note-card" @click="viewNote(note)">
        <div class="card-cover">
          <img :src="getNoteImage(note)" :alt="note.title" @error="handleImageError" />
          <div class="card-status" :class="getStatusClass(note.status)">{{ getStatusText(note.status) }}</div>
          <div class="card-overlay">
            <el-button circle @click.stop="editNote(note)"><el-icon><Edit /></el-icon></el-button>
            <el-button circle type="danger" @click.stop="deleteNote(note)"><el-icon><Delete /></el-icon></el-button>
          </div>
        </div>
        <div class="card-content">
          <div class="card-shop">
            <el-icon><Shop /></el-icon>
            <span>{{ note.shopName || '未关联门店' }}</span>
          </div>
          <h3 class="card-title">{{ note.title }}</h3>
          <p class="card-desc">{{ note.content?.substring(0, 60) || '暂无描述' }}...</p>
          <div class="card-stats">
            <span class="stat"><el-icon><View /></el-icon>{{ note.views || 0 }}</span>
            <span class="stat"><el-icon><Star /></el-icon>{{ note.likes || 0 }}</span>
            <span class="stat"><el-icon><ChatDotRound /></el-icon>{{ note.comments || 0 }}</span>
          </div>
          <div class="card-footer">
            <span class="card-time">{{ formatDate(note.createdAt) }}</span>
            <el-tag v-if="note.syncedToApp" type="success" size="small">已同步</el-tag>
            <el-tag v-else type="info" size="small">待同步</el-tag>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="noteList.length === 0 && !loading" class="empty-state">
        <el-icon :size="80" color="#8e8e8e"><Document /></el-icon>
        <h3>暂无笔记</h3>
        <p>发布您的第一篇笔记，让UniApp用户看到您的精彩内容</p>
        <el-button type="primary" @click="$router.push('/notes/create')">立即发布</el-button>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="pagination.total > 0">
      <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
        :page-sizes="[8, 12, 16, 24]" :total="pagination.total" layout="total, sizes, prev, pager, next"
        @size-change="searchNotes" @current-change="searchNotes" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, View, Star, ChatDotRound, Edit, Delete, Document, Shop } from '@element-plus/icons-vue'
import { getNoteList, deleteNote as deleteNoteApi } from '@/api/note'
import { getShopList } from '@/api/shop'

const router = useRouter()
const currentFilter = ref('all')
const searchKeyword = ref('')
const selectedShop = ref<number | undefined>()
const pagination = ref({ currentPage: 1, pageSize: 12, total: 0 })
const noteList = ref<any[]>([])
const shopList = ref<any[]>([])
const loading = ref(false)
const counts = reactive({ all: 0, published: 0, pending: 0, draft: 0 })

const getStatusClass = (status: number) => ({ 0: 'draft', 1: 'published', 2: 'pending', 3: 'offline' }[status] || 'draft')
const getStatusText = (status: number) => ({ 0: '草稿', 1: '已发布', 2: '待审核', 3: '已下线' }[status] || '草稿')
const formatDate = (date: string) => { if (!date) return '-'; try { return new Date(date).toLocaleDateString('zh-CN') } catch { return date } }

// 获取笔记封面图片
const getNoteImage = (note: any) => {
  // 优先使用coverImage
  if (note.coverImage && note.coverImage.trim()) {
    return note.coverImage
  }
  // 其次使用image
  if (note.image && note.image.trim()) {
    return note.image
  }
  // 尝试从images中获取第一张
  if (note.images) {
    try {
      let images = note.images
      if (typeof images === 'string') {
        // 尝试JSON解析
        if (images.startsWith('[')) {
          images = JSON.parse(images)
        } else {
          images = images.split(',').filter((s: string) => s.trim())
        }
      }
      if (Array.isArray(images) && images.length > 0) {
        return images[0]
      }
    } catch {
      // 解析失败，返回默认图片
    }
  }
  return '/default-cover.png'
}

// 图片加载失败处理
const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.src = '/default-cover.png'
}

const setFilter = (filter: string) => { currentFilter.value = filter; pagination.value.currentPage = 1; searchNotes() }

const searchNotes = async () => {
  try {
    loading.value = true
    // 状态映射：0草稿，1已发布，2待审核，3已下线
    const statusMap: Record<string, number | undefined> = { all: undefined, published: 1, pending: 2, draft: 0 }
    const res = await getNoteList({ 
      title: searchKeyword.value, 
      status: statusMap[currentFilter.value], 
      shopId: selectedShop.value, 
      pageNum: pagination.value.currentPage, 
      pageSize: pagination.value.pageSize 
    })
    console.log('📝 笔记列表响应:', res)
    
    // 处理笔记数据
    noteList.value = (res.list || res.records || []).map((n: any) => ({
      ...n,
      // 确保syncedToApp有值
      syncedToApp: n.syncedToApp ?? (n.syncStatus === 1)
    }))
    
    pagination.value.total = res.total || 0
    
    // 更新统计数据
    counts.all = pagination.value.total
    // 这里可以根据实际API返回的统计数据更新
    counts.published = noteList.value.filter((n: any) => n.status === 1).length
    counts.pending = noteList.value.filter((n: any) => n.status === 2).length
    counts.draft = noteList.value.filter((n: any) => n.status === 0).length
  } catch (e: any) { 
    console.error('获取笔记列表失败:', e)
    ElMessage.error('获取笔记列表失败: ' + (e.message || '未知错误')) 
  }
  finally { loading.value = false }
}

const fetchShops = async () => { try { const res = await getShopList({ pageNum: 1, pageSize: 100 }); shopList.value = res.list || res.records || [] } catch {} }
const viewNote = (note: any) => { router.push(`/notes/detail/${note.id}`) }
const editNote = (note: any) => { router.push(`/notes/edit/${note.id}`) }
const deleteNote = (note: any) => {
  ElMessageBox.confirm(`确定删除笔记 "${note.title}" 吗？删除后UniApp用户将无法查看`, '删除确认', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    .then(async () => { try { await deleteNoteApi(note.id); ElMessage.success('删除成功'); searchNotes() } catch { ElMessage.error('删除失败') } })
}

watch([searchKeyword, selectedShop], () => { pagination.value.currentPage = 1 })
onMounted(() => { searchNotes(); fetchShops() })
</script>


<style scoped>
.note-page { 
  max-width: 1400px; 
  margin: 0 auto; 
  padding: 0 40px;
}

/* 页面头部 */
.page-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  padding: 40px 0 28px;
}
.page-title { 
  font-size: 28px; 
  font-weight: 600; 
  color: #171a20; 
  margin: 0 0 4px 0; 
  letter-spacing: -0.5px;
}
.page-desc { 
  font-size: 14px; 
  color: #5c5e62; 
  margin: 0; 
}
.create-btn { 
  background-color: #3e6ae1 !important; 
  border-color: #3e6ae1 !important; 
  border-radius: 4px; 
  padding: 10px 24px; 
  font-size: 14px; 
  font-weight: 500;
  transition: all 0.3s;
}
.create-btn:hover { 
  background-color: #3458b9 !important; 
  transform: translateY(-1px); 
  box-shadow: 0 4px 12px rgba(62, 106, 225, 0.2);
}

/* 筛选区域 */
.filter-bar { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 24px; 
  flex-wrap: wrap; 
  gap: 16px; 
  background: white; 
  padding: 24px; 
  border-radius: 8px; 
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); 
  border: 1px solid #e5e5e5;
}
.filter-tabs { 
  display: flex; 
  gap: 8px; 
}
.tab-btn { 
  display: flex; 
  align-items: center; 
  gap: 8px; 
  padding: 8px 20px; 
  font-size: 14px; 
  color: #393c41; 
  background: #f4f4f4; 
  border: none; 
  border-radius: 4px; 
  cursor: pointer; 
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1); 
  font-weight: 500;
}
.tab-btn:hover { 
  background: #e8e8e8; 
  color: #171a20; 
}
.tab-btn.active { 
  background: #171a20; 
  color: white; 
}
.tab-btn .count { 
  padding: 2px 8px; 
  border-radius: 4px; 
  font-size: 11px; 
  background: rgba(255, 255, 255, 0.15); 
  font-weight: 600;
}
.tab-btn:not(.active) .count { 
  background: rgba(0, 0, 0, 0.05); 
  color: #5c5e62;
}

.search-area { display: flex; gap: 12px; }
.search-input { width: 260px; }
.search-input :deep(.el-input__wrapper) { 
  border-radius: 4px; 
  background-color: #f4f4f4;
  box-shadow: none !important;
  border: 1px solid #dcdfe6;
  transition: all 0.2s;
}
.search-input :deep(.el-input__wrapper:hover) {
  background-color: #e8e8e8;
}
.search-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #8e8e8e;
}
.shop-select { width: 160px; }
.shop-select :deep(.el-input__wrapper) { 
  border-radius: 4px; 
  background-color: #f4f4f4;
  box-shadow: none !important;
}

/* 笔记列表网格 */
.note-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); 
  gap: 24px; 
  min-height: 400px; 
  padding-bottom: 20px;
}

.note-card { 
  background: white; 
  border-radius: 8px; 
  overflow: hidden; 
  cursor: pointer; 
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); 
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  border: 1px solid #e5e5e5;
}
.note-card:hover { 
  transform: translateY(-4px); 
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06); 
}
.note-card:hover .card-overlay { opacity: 1; }

.card-cover { 
  position: relative; 
  aspect-ratio: 16/10; 
  overflow: hidden; 
  background: #f4f4f4;
}
.card-cover img { 
  width: 100%; 
  height: 100%; 
  object-fit: cover; 
  transition: transform 0.6s cubic-bezier(0.4, 0, 0.2, 1); 
}
.note-card:hover .card-cover img { transform: scale(1.05); }

.card-status { 
  position: absolute; 
  top: 12px; 
  left: 12px; 
  padding: 4px 12px; 
  font-size: 11px; 
  font-weight: 600; 
  border-radius: 4px; 
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.card-status.published { background: #3e6ae1; color: white; }
.card-status.pending { background: #5c5e62; color: white; }
.card-status.draft { background: rgba(255, 255, 255, 0.9); color: #171a20; backdrop-filter: blur(4px); }
.card-status.offline { background: #d73948; color: white; }

.card-overlay { 
  position: absolute; 
  inset: 0; 
  background: rgba(23, 26, 32, 0.4); 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  gap: 12px; 
  opacity: 0; 
  transition: opacity 0.3s ease; 
}
.card-overlay .el-button { 
  width: 40px; 
  height: 40px; 
  border-radius: 4px;
  background: white;
  border: none;
  color: #171a20;
}
.card-overlay .el-button:hover {
  background: #f4f4f4;
  transform: scale(1.1);
}

.card-content { padding: 20px; }
.card-shop { 
  display: flex; 
  align-items: center; 
  gap: 6px; 
  font-size: 12px; 
  color: #3e6ae1; 
  margin-bottom: 10px; 
  font-weight: 600;
}
.card-shop .el-icon { font-size: 14px; }
.card-title { 
  font-size: 18px; 
  font-weight: 600; 
  color: #171a20; 
  margin: 0 0 8px 0; 
  line-height: 1.4; 
  display: -webkit-box; 
  -webkit-line-clamp: 2; 
  -webkit-box-orient: vertical; 
  overflow: hidden; 
}
.card-desc { 
  font-size: 13px; 
  color: #5c5e62; 
  margin: 0 0 16px 0; 
  line-height: 1.6; 
  display: -webkit-box; 
  -webkit-line-clamp: 2; 
  -webkit-box-orient: vertical; 
  overflow: hidden; 
}

.card-stats { 
  display: flex; 
  gap: 16px; 
  margin-bottom: 16px; 
}
.stat { 
  display: flex; 
  align-items: center; 
  gap: 5px; 
  font-size: 12px; 
  color: #5c5e62; 
}
.stat .el-icon { font-size: 15px; color: #8e8e8e; }

.card-footer { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  padding-top: 14px; 
  border-top: 1px solid #f4f4f4; 
}
.card-time { font-size: 12px; color: #8e8e8e; }

/* 空状态 */
.empty-state { 
  grid-column: 1 / -1; 
  display: flex; 
  flex-direction: column; 
  align-items: center; 
  justify-content: center; 
  padding: 100px 20px; 
  background: white; 
  border-radius: 8px; 
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  border: 1px solid #e5e5e5;
}
.empty-state h3 { font-size: 20px; color: #171a20; margin: 24px 0 8px; }
.empty-state p { font-size: 14px; color: #5c5e62; margin: 0 0 24px; text-align: center; }

/* 分页 */
.pagination-wrapper { 
  display: flex; 
  justify-content: center; 
  margin-top: 32px; 
  padding: 24px; 
  background: white; 
  border-radius: 8px; 
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  border: 1px solid #e5e5e5;
}
.pagination-wrapper :deep(.el-pager li.is-active) { 
  background: #171a20 !important; 
  color: white !important; 
  border-radius: 4px; 
}
.pagination-wrapper :deep(.el-pager li:hover) { 
  color: #3e6ae1; 
}
</style>
