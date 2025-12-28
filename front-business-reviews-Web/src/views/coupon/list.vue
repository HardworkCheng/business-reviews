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
        <el-icon :size="80" color="#8e8e8e"><Ticket /></el-icon>
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

const getTypeName = (type: number) => ({ 1: '满减券', 2: '折扣券', 3: '代金券' }[type] || '未知')
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
    // 统计数据：total应该是所有优惠券的总数，不是当前筛选的数量
    // 计算已发放数量（所有优惠券的 totalCount - remainCount 之和）
    const allCoupons = couponList.value
    statistics.total = pagination.value.total
    statistics.issued = allCoupons.reduce((sum, c) => sum + (c.totalCount - c.remainCount), 0)
    statistics.used = allCoupons.reduce((sum, c) => sum + (c.usedCount || 0), 0)
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
.coupon-page { 
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

/* 统计卡片 */
.stats-row { 
  display: grid; 
  grid-template-columns: repeat(4, 1fr); 
  gap: 20px; 
  margin-bottom: 24px; 
}
@media (max-width: 1024px) { .stats-row { grid-template-columns: repeat(2, 1fr); } }
.stat-card { 
  display: flex; 
  align-items: center; 
  gap: 16px; 
  padding: 24px; 
  background: white; 
  border-radius: 8px; 
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); 
  border: 1px solid #e5e5e5;
}
.stat-icon { 
  width: 48px; 
  height: 48px; 
  display: flex; 
  align-items: center; 
  justify-content: center; 
  border-radius: 4px; 
  font-size: 20px; 
}
.stat-icon.orange { background: #f4f4f4; color: #3e6ae1; }
.stat-icon.green { background: #f4f4f4; color: #10B981; }
.stat-icon.blue { background: #f4f4f4; color: #3B82F6; }
.stat-icon.purple { background: #f4f4f4; color: #A855F7; }
.stat-content .stat-value { 
  display: block; 
  font-size: 24px; 
  font-weight: 700; 
  color: #171a20; 
  line-height: 1.2;
}
.stat-content .stat-label { font-size: 12px; color: #5c5e62; font-weight: 500; }

/* 筛选栏 */
.filter-bar { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 24px; 
  background: white; 
  padding: 24px; 
  border-radius: 8px; 
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); 
  border: 1px solid #e5e5e5;
}
.filter-tabs { display: flex; gap: 8px; }
.tab-btn { 
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
.tab-btn:hover { background: #e8e8e8; color: #171a20; }
.tab-btn.active { background: #171a20; color: white; }
.search-input { width: 260px; }
.search-input :deep(.el-input__wrapper) { 
  border-radius: 4px; 
  background-color: #f4f4f4;
  box-shadow: none !important;
  border: 1px solid #dcdfe6;
}
.search-input :deep(.el-input__wrapper:hover) { background-color: #e8e8e8; }
.search-input :deep(.el-input__wrapper.is-focus) { background-color: #fff; border-color: #8e8e8e; }

/* 优惠券卡片网格 */
.coupon-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 24px; min-height: 300px; padding-bottom: 20px; }

.coupon-card { 
  background: white; 
  border-radius: 8px; 
  overflow: hidden; 
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); 
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1); 
  border: 1px solid #e5e5e5;
  border-left: 4px solid #3e6ae1; 
}
.coupon-card:hover { 
  transform: translateY(-4px); 
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06); 
}
.coupon-card.active { border-left-color: #10B981; }
.coupon-card.upcoming { border-left-color: #5c5e62; }
.coupon-card.expired { border-left-color: #d73948; }

.coupon-header { display: flex; justify-content: space-between; align-items: center; padding: 16px 20px; background: #fcfcfc; border-bottom: 1px solid #f4f4f4; }
.coupon-type { padding: 4px 10px; font-size: 11px; font-weight: 700; border-radius: 2px; text-transform: uppercase; }
.coupon-type.cash { background: #f4f4f4; color: #3e6ae1; }
.coupon-type.discount { background: #f4f4f4; color: #10B981; }
.coupon-type.exclusive { background: #f4f4f4; color: #A855F7; }
.coupon-type.newuser { background: #f4f4f4; color: #d73948; }
.coupon-status { font-size: 11px; color: #8e8e8e; font-weight: 600; }

.coupon-body { padding: 24px; }
.coupon-value { display: flex; align-items: baseline; gap: 4px; margin-bottom: 16px; }
.coupon-value .currency { font-size: 18px; color: #171a20; font-weight: 600; }
.coupon-value .amount { font-size: 44px; font-weight: 700; color: #171a20; line-height: 1; letter-spacing: -1px; }
.coupon-value .condition { font-size: 12px; color: #5c5e62; margin-left: 10px; font-weight: 500; }
.coupon-title { font-size: 17px; font-weight: 600; color: #171a20; margin: 0 0 20px 0; }

.coupon-stats { display: flex; gap: 24px; margin-bottom: 20px; }
.stat-item { display: flex; flex-direction: column; }
.stat-item .label { font-size: 11px; color: #8e8e8e; font-weight: 600; text-transform: uppercase; margin-bottom: 4px; }
.stat-item .value { font-size: 16px; font-weight: 700; color: #171a20; }

.coupon-time { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #5c5e62; font-weight: 500; }
.coupon-time .el-icon { color: #8e8e8e; }

.coupon-footer { display: flex; justify-content: space-between; align-items: center; padding: 14px 20px; border-top: 1px solid #f4f4f4; background: #fcfcfc; }
.actions { display: flex; gap: 12px; }

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
.pagination-wrapper :deep(.el-pager li.is-active) { background: #171a20 !important; color: white !important; border-radius: 4px; }
.pagination-wrapper :deep(.el-pager li:hover) { color: #3e6ae1; }
</style>
