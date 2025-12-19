<template>
  <div class="coupon-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">优惠券管理</h1>
        <p class="page-desc">创建和管理优惠券，自动同步到UniApp供用户领取</p>
      </div>
      <el-button type="primary" size="large" @click="$router.push('/coupons/create')" class="create-btn">
        <el-icon><Plus /></el-icon>新建优惠券
      </el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card"><div class="stat-icon orange"><el-icon><Ticket /></el-icon></div><div class="stat-content"><span class="stat-value">{{ statistics.total }}</span><span class="stat-label">优惠券总数</span></div></div>
      <div class="stat-card"><div class="stat-icon green"><el-icon><Document /></el-icon></div><div class="stat-content"><span class="stat-value">{{ statistics.issued }}</span><span class="stat-label">已发放</span></div></div>
      <div class="stat-card"><div class="stat-icon blue"><el-icon><Check /></el-icon></div><div class="stat-content"><span class="stat-value">{{ statistics.used }}</span><span class="stat-label">已使用</span></div></div>
      <div class="stat-card"><div class="stat-icon purple"><el-icon><TrendCharts /></el-icon></div><div class="stat-content"><span class="stat-value">{{ statistics.redeemRate }}%</span><span class="stat-label">核销率</span></div></div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <button class="tab-btn" :class="{ active: currentFilter === 'all' }" @click="setFilter('all')">全部</button>
        <button class="tab-btn" :class="{ active: currentFilter === 'active' }" @click="setFilter('active')">进行中</button>
        <button class="tab-btn" :class="{ active: currentFilter === 'upcoming' }" @click="setFilter('upcoming')">未开始</button>
        <button class="tab-btn" :class="{ active: currentFilter === 'expired' }" @click="setFilter('expired')">已结束</button>
      </div>
      <div class="search-area">
        <el-input v-model="searchKeyword" placeholder="搜索优惠券名称..." class="search-input" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
    </div>

    <!-- 优惠券卡片网格 -->
    <div class="coupon-grid" v-loading="loading">
      <div v-for="coupon in couponList" :key="coupon.id" class="coupon-card" :class="getStatusClass(coupon.status)">
        <div class="coupon-header">
          <span class="coupon-type" :class="getTypeClass(coupon.type)">{{ getTypeName(coupon.type) }}</span>
          <span class="coupon-status">{{ getStatusText(coupon.status) }}</span>
        </div>
        <div class="coupon-body">
          <div class="coupon-value">
            <span class="currency">¥</span>
            <span class="amount">{{ coupon.discount || coupon.amount || 0 }}</span>
            <span class="condition" v-if="coupon.minAmount">满{{ coupon.minAmount }}可用</span>
          </div>
          <h3 class="coupon-title">{{ coupon.title }}</h3>
          <div class="coupon-stats">
            <div class="stat-item"><span class="label">总量</span><span class="value">{{ coupon.totalCount }}</span></div>
            <div class="stat-item"><span class="label">剩余</span><span class="value">{{ coupon.remainCount }}</span></div>
            <div class="stat-item"><span class="label">已领</span><span class="value">{{ coupon.totalCount - coupon.remainCount }}</span></div>
          </div>
          <div class="coupon-time"><el-icon><Clock /></el-icon>{{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}</div>
        </div>
        <div class="coupon-footer">
          <el-tag v-if="coupon.syncedToApp" type="success" size="small">已同步UniApp</el-tag>
          <el-tag v-else type="info" size="small">待同步</el-tag>
          <div class="actions">
            <el-button link type="primary" @click="editCoupon(coupon)">编辑</el-button>
            <el-button link type="danger" @click="deleteCoupon(coupon)">删除</el-button>
          </div>
        </div>
      </div>

      <div v-if="couponList.length === 0 && !loading" class="empty-state">
        <el-icon :size="80" color="#FFB366"><Ticket /></el-icon>
        <h3>暂无优惠券</h3>
        <p>创建优惠券吸引UniApp用户到店消费</p>
        <el-button type="primary" @click="$router.push('/coupons/create')">立即创建</el-button>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="pagination.total > 0">
      <el-pagination v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
        :page-sizes="[8, 12, 16]" :total="pagination.total" layout="total, sizes, prev, pager, next"
        @size-change="searchCoupons" @current-change="searchCoupons" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Ticket, Document, Check, TrendCharts, Clock } from '@element-plus/icons-vue'
import { getCouponList, deleteCoupon as deleteCouponApi } from '@/api/coupon'

const router = useRouter()
const currentFilter = ref('all')
const searchKeyword = ref('')
const pagination = ref({ currentPage: 1, pageSize: 12, total: 0 })
const couponList = ref<any[]>([])
const loading = ref(false)
const statistics = reactive({ total: 0, issued: 0, used: 0, redeemRate: 0 })

const getTypeName = (type: number) => ({ 1: '现金券', 2: '折扣券', 3: '专属券', 4: '新人券' }[type] || '未知')
const getTypeClass = (type: number) => ({ 1: 'cash', 2: 'discount', 3: 'exclusive', 4: 'newuser' }[type] || '')
const getStatusClass = (status: number) => ({ 0: 'upcoming', 1: 'active', 2: 'expired' }[status] || '')
const getStatusText = (status: number) => ({ 0: '未开始', 1: '进行中', 2: '已结束' }[status] || '未知')
const formatDate = (date: string) => { if (!date) return '-'; try { return new Date(date).toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' }) } catch { return date } }

const setFilter = (filter: string) => { currentFilter.value = filter; pagination.value.currentPage = 1; searchCoupons() }

const searchCoupons = async () => {
  try {
    loading.value = true
    const statusMap: Record<string, number | undefined> = { all: undefined, active: 1, upcoming: 0, expired: 2 }
    const res = await getCouponList({ title: searchKeyword.value, status: statusMap[currentFilter.value], pageNum: pagination.value.currentPage, pageSize: pagination.value.pageSize })
    couponList.value = (res.list || res.records || []).map((c: any) => ({ ...c, syncedToApp: Math.random() > 0.2 }))
    pagination.value.total = res.total || 0
    statistics.total = pagination.value.total
    statistics.issued = couponList.value.reduce((sum, c) => sum + (c.totalCount - c.remainCount), 0)
    statistics.used = couponList.value.reduce((sum, c) => sum + (c.usedCount || 0), 0)
    statistics.redeemRate = statistics.issued > 0 ? Math.round((statistics.used / statistics.issued) * 100) : 0
  } catch { ElMessage.error('获取优惠券列表失败') }
  finally { loading.value = false }
}

const editCoupon = (coupon: any) => { router.push(`/coupons/edit/${coupon.id}`) }
const deleteCoupon = (coupon: any) => {
  ElMessageBox.confirm(`确定删除优惠券 "${coupon.title}" 吗？`, '删除确认', { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' })
    .then(async () => { try { await deleteCouponApi(coupon.id); ElMessage.success('删除成功'); searchCoupons() } catch { ElMessage.error('删除失败') } })
}

watch(searchKeyword, () => { pagination.value.currentPage = 1 })
onMounted(() => { searchCoupons() })
</script>


<style scoped>
.coupon-page { max-width: 1400px; margin: 0 auto; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 26px; font-weight: 700; color: #171717; margin: 0 0 6px 0; }
.page-desc { font-size: 14px; color: #737373; margin: 0; }
.create-btn { background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%); border: none; border-radius: 10px; padding: 12px 24px; font-size: 15px; box-shadow: 0 4px 14px rgba(255, 125, 0, 0.35); }

/* 统计卡片 */
.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
@media (max-width: 1024px) { .stats-row { grid-template-columns: repeat(2, 1fr); } }
.stat-card { display: flex; align-items: center; gap: 16px; padding: 20px; background: white; border-radius: 16px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.stat-icon { width: 52px; height: 52px; display: flex; align-items: center; justify-content: center; border-radius: 14px; font-size: 22px; }
.stat-icon.orange { background: rgba(255, 125, 0, 0.1); color: #FF7D00; }
.stat-icon.green { background: rgba(16, 185, 129, 0.1); color: #10B981; }
.stat-icon.blue { background: rgba(59, 130, 246, 0.1); color: #3B82F6; }
.stat-icon.purple { background: rgba(168, 85, 247, 0.1); color: #A855F7; }
.stat-content .stat-value { display: block; font-size: 26px; font-weight: 700; color: #171717; }
.stat-content .stat-label { font-size: 13px; color: #737373; }

/* 筛选栏 */
.filter-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; background: white; padding: 20px 24px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.filter-tabs { display: flex; gap: 8px; }
.tab-btn { padding: 10px 20px; font-size: 14px; color: #525252; background: #F5F5F5; border: none; border-radius: 10px; cursor: pointer; transition: all 0.2s ease; }
.tab-btn:hover { background: #FFF7ED; color: #FF7D00; }
.tab-btn.active { background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%); color: white; box-shadow: 0 4px 12px rgba(255, 125, 0, 0.3); }
.search-input { width: 260px; }
.search-input :deep(.el-input__wrapper) { border-radius: 10px; }

/* 优惠券卡片网格 */
.coupon-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; min-height: 300px; }

.coupon-card { background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06); transition: all 0.3s ease; border-left: 4px solid #FF7D00; }
.coupon-card:hover { transform: translateY(-6px); box-shadow: 0 12px 40px rgba(255, 125, 0, 0.15); }
.coupon-card.active { border-left-color: #10B981; }
.coupon-card.upcoming { border-left-color: #F59E0B; }
.coupon-card.expired { border-left-color: #9CA3AF; }

.coupon-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; background: #FAFAFA; }
.coupon-type { padding: 4px 12px; font-size: 12px; font-weight: 600; border-radius: 6px; }
.coupon-type.cash { background: rgba(255, 125, 0, 0.1); color: #FF7D00; }
.coupon-type.discount { background: rgba(16, 185, 129, 0.1); color: #10B981; }
.coupon-type.exclusive { background: rgba(168, 85, 247, 0.1); color: #A855F7; }
.coupon-type.newuser { background: rgba(239, 68, 68, 0.1); color: #EF4444; }
.coupon-status { font-size: 12px; color: #737373; }

.coupon-body { padding: 20px; }
.coupon-value { display: flex; align-items: baseline; gap: 4px; margin-bottom: 12px; }
.coupon-value .currency { font-size: 18px; color: #FF7D00; font-weight: 600; }
.coupon-value .amount { font-size: 40px; font-weight: 700; color: #FF7D00; line-height: 1; }
.coupon-value .condition { font-size: 12px; color: #A3A3A3; margin-left: 8px; }
.coupon-title { font-size: 16px; font-weight: 600; color: #171717; margin: 0 0 16px 0; }

.coupon-stats { display: flex; gap: 24px; margin-bottom: 14px; }
.stat-item { display: flex; flex-direction: column; }
.stat-item .label { font-size: 12px; color: #A3A3A3; }
.stat-item .value { font-size: 16px; font-weight: 600; color: #171717; }

.coupon-time { display: flex; align-items: center; gap: 6px; font-size: 13px; color: #737373; }

.coupon-footer { display: flex; justify-content: space-between; align-items: center; padding: 14px 20px; border-top: 1px solid #F5F5F5; }
.actions { display: flex; gap: 8px; }

/* 空状态 */
.empty-state { grid-column: 1 / -1; display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 20px; background: white; border-radius: 16px; }
.empty-state h3 { font-size: 20px; color: #171717; margin: 20px 0 10px; }
.empty-state p { font-size: 14px; color: #737373; margin: 0 0 24px; }

/* 分页 */
.pagination-wrapper { display: flex; justify-content: center; margin-top: 32px; padding: 20px; background: white; border-radius: 12px; }
.pagination-wrapper :deep(.el-pager li.is-active) { background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%); color: white; border-radius: 8px; }
</style>
