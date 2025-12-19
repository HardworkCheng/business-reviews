<template>
  <div class="user-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">用户管理</h1>
        <p class="page-desc">查看和管理来自UniApp的用户数据</p>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-value">{{ statistics.totalUsers }}</span>
          <span class="stat-label">总用户数</span>
        </div>
        <div class="stat-item">
          <span class="stat-value success">{{ statistics.activeUsers }}</span>
          <span class="stat-label">活跃用户</span>
        </div>
        <div class="stat-item">
          <span class="stat-value warning">{{ statistics.newUsers }}</span>
          <span class="stat-label">今日新增</span>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <el-input v-model="searchKeyword" placeholder="搜索用户昵称/手机号" class="search-input" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="userType" placeholder="用户类型" clearable class="filter-select">
          <el-option label="普通用户" value="normal" />
          <el-option label="VIP用户" value="vip" />
          <el-option label="新用户" value="new" />
        </el-select>
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" class="date-picker" />
      </div>
      <el-button type="primary" @click="exportUsers">
        <el-icon><Download /></el-icon>导出数据
      </el-button>
    </div>

    <!-- 用户列表 -->
    <div class="user-table-card">
      <el-table :data="userList" v-loading="loading" class="user-table">
        <el-table-column label="用户信息" min-width="240">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :src="row.avatar" :size="48" class="user-avatar">{{ row.nickname?.charAt(0) }}</el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ row.nickname }}</span>
                <span class="user-id">ID: {{ row.id }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140">
          <template #default="{ row }">
            <span class="phone-text">{{ row.phone || '未绑定' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="用户类型" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === 'vip' ? 'warning' : 'info'" size="small">{{ row.type === 'vip' ? 'VIP用户' : '普通用户' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="互动数据" width="200">
          <template #default="{ row }">
            <div class="interaction-stats">
              <span class="int-stat"><el-icon><Document /></el-icon>{{ row.noteCount || 0 }}笔记</span>
              <span class="int-stat"><el-icon><Star /></el-icon>{{ row.likeCount || 0 }}点赞</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastActiveTime" label="最后活跃" width="160">
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.lastActiveTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="140">
          <template #default="{ row }">
            <span class="time-text">{{ formatDate(row.createdAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewUserDetail(row)">详情</el-button>
            <el-button link type="warning" @click="sendMessage(row)">发消息</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]" :total="pagination.total" layout="total, sizes, prev, pager, next" />
      </div>
    </div>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="600px">
      <div class="user-detail-modal" v-if="currentUser">
        <div class="detail-header">
          <el-avatar :src="currentUser.avatar" :size="80" />
          <div class="detail-info">
            <h3>{{ currentUser.nickname }}</h3>
            <p>{{ currentUser.phone || '未绑定手机' }}</p>
          </div>
        </div>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户ID">{{ currentUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户类型">{{ currentUser.type === 'vip' ? 'VIP用户' : '普通用户' }}</el-descriptions-item>
          <el-descriptions-item label="发布笔记">{{ currentUser.noteCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="获得点赞">{{ currentUser.likeCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ formatDate(currentUser.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="最后活跃">{{ formatTime(currentUser.lastActiveTime) }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Download, Document, Star } from '@element-plus/icons-vue'

const searchKeyword = ref('')
const userType = ref('')
const dateRange = ref<[Date, Date] | null>(null)
const loading = ref(false)
const detailVisible = ref(false)
const currentUser = ref<any>(null)
const pagination = ref({ currentPage: 1, pageSize: 10, total: 0 })
const statistics = reactive({ totalUsers: 1256, activeUsers: 892, newUsers: 45 })

// 模拟用户数据
const userList = ref([
  { id: 1001, nickname: '小红薯用户', avatar: '', phone: '138****1234', type: 'vip', noteCount: 12, likeCount: 156, lastActiveTime: '2024-12-16 10:30', createdAt: '2024-06-15' },
  { id: 1002, nickname: '美食探店家', avatar: '', phone: '139****5678', type: 'normal', noteCount: 8, likeCount: 89, lastActiveTime: '2024-12-15 18:20', createdAt: '2024-08-20' },
  { id: 1003, nickname: '旅行达人', avatar: '', phone: '137****9012', type: 'vip', noteCount: 25, likeCount: 320, lastActiveTime: '2024-12-16 09:15', createdAt: '2024-03-10' },
  { id: 1004, nickname: '生活记录者', avatar: '', phone: '', type: 'normal', noteCount: 3, likeCount: 28, lastActiveTime: '2024-12-14 14:45', createdAt: '2024-11-01' },
])

const formatDate = (date: string) => date || '-'
const formatTime = (time: string) => time || '-'
const viewUserDetail = (user: any) => { currentUser.value = user; detailVisible.value = true }
const sendMessage = (user: any) => { ElMessage.info(`向 ${user.nickname} 发送消息`) }
const exportUsers = () => { ElMessage.success('用户数据导出中...') }

onMounted(() => { pagination.value.total = userList.value.length })
</script>


<style scoped>
.user-page { max-width: 1400px; margin: 0 auto; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 26px; font-weight: 700; color: #171717; margin: 0 0 6px 0; }
.page-desc { font-size: 14px; color: #737373; margin: 0; }

.header-stats { display: flex; gap: 32px; }
.stat-item { text-align: center; }
.stat-value { display: block; font-size: 28px; font-weight: 700; color: #171717; }
.stat-value.success { color: #10B981; }
.stat-value.warning { color: #FF7D00; }
.stat-label { font-size: 13px; color: #737373; }

.filter-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; background: white; padding: 20px 24px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.filter-left { display: flex; gap: 12px; }
.search-input { width: 240px; }
.search-input :deep(.el-input__wrapper) { border-radius: 10px; }
.filter-select { width: 140px; }
.filter-select :deep(.el-input__wrapper) { border-radius: 10px; }
.date-picker :deep(.el-input__wrapper) { border-radius: 10px; }

.user-table-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.user-table :deep(.el-table__header th) { background: #FFF7ED; color: #525252; font-weight: 600; }

.user-info { display: flex; align-items: center; gap: 14px; }
.user-avatar { background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%); color: white; font-weight: 600; }
.user-detail { display: flex; flex-direction: column; }
.user-name { font-size: 14px; font-weight: 600; color: #171717; }
.user-id { font-size: 12px; color: #A3A3A3; }

.phone-text { font-size: 13px; color: #525252; }
.time-text { font-size: 13px; color: #737373; }

.interaction-stats { display: flex; gap: 16px; }
.int-stat { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #737373; }
.int-stat .el-icon { font-size: 14px; color: #FF7D00; }

.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
.pagination-wrapper :deep(.el-pager li.is-active) { background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%); color: white; border-radius: 8px; }

.user-detail-modal .detail-header { display: flex; align-items: center; gap: 20px; margin-bottom: 24px; padding-bottom: 20px; border-bottom: 1px solid #F5F5F5; }
.user-detail-modal .detail-info h3 { margin: 0 0 8px 0; font-size: 20px; color: #171717; }
.user-detail-modal .detail-info p { margin: 0; font-size: 14px; color: #737373; }
</style>
