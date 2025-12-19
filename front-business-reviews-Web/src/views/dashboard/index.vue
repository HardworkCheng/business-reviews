<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎回来，{{ userStore.getUserInfo?.merchantName || '商家' }}！</h1>
        <p class="welcome-subtitle">这里是您店铺的核心运营数据概览，UniApp用户数据实时同步</p>
      </div>
      <div class="quick-actions">
        <el-button type="primary" size="large" @click="$router.push('/notes/create')" class="action-btn primary">
          <el-icon><EditPen /></el-icon>发布笔记
        </el-button>
        <el-button size="large" @click="$router.push('/coupons/create')" class="action-btn secondary">
          <el-icon><Ticket /></el-icon>创建优惠券
        </el-button>
      </div>
    </div>

    <!-- UniApp同步状态 -->
    <div class="sync-banner">
      <div class="sync-info">
        <el-icon class="sync-icon"><Connection /></el-icon>
        <div class="sync-text">
          <span class="sync-title">UniApp数据同步</span>
          <span class="sync-desc">最后同步时间：{{ lastSyncTime }}</span>
        </div>
      </div>
      <div class="sync-stats">
        <div class="sync-stat"><span class="stat-value">{{ syncStats.notes }}</span><span class="stat-label">已同步笔记</span></div>
        <div class="sync-stat"><span class="stat-value">{{ syncStats.coupons }}</span><span class="stat-label">已同步优惠券</span></div>
        <div class="sync-stat"><span class="stat-value">{{ syncStats.users }}</span><span class="stat-label">活跃用户</span></div>
      </div>
      <el-button type="primary" plain @click="syncNow">立即同步</el-button>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon orange"><el-icon><Document /></el-icon></div>
        <div class="stat-content">
          <span class="stat-value">{{ dashboardData.todayViews }}</span>
          <span class="stat-label">笔记总数</span>
        </div>
        <div class="stat-trend up"><el-icon><Top /></el-icon>{{ dashboardData.viewGrowth }}%</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green"><el-icon><Ticket /></el-icon></div>
        <div class="stat-content">
          <span class="stat-value">{{ dashboardData.todayInteractions }}</span>
          <span class="stat-label">优惠券总数</span>
        </div>
        <div class="stat-trend up"><el-icon><Top /></el-icon>{{ dashboardData.interactionGrowth }}%</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon blue"><el-icon><View /></el-icon></div>
        <div class="stat-content">
          <span class="stat-value">{{ dashboardData.todayCouponsClaimed }}</span>
          <span class="stat-label">今日访问量</span>
        </div>
        <div class="stat-trend" :class="dashboardData.couponGrowth >= 0 ? 'up' : 'down'">
          <el-icon><component :is="dashboardData.couponGrowth >= 0 ? 'Top' : 'Bottom'" /></el-icon>{{ Math.abs(dashboardData.couponGrowth) }}%
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon purple"><el-icon><TrendCharts /></el-icon></div>
        <div class="stat-content">
          <span class="stat-value">{{ dashboardData.todayCouponsRedeemed }}%</span>
          <span class="stat-label">转化率</span>
        </div>
        <div class="stat-trend up"><el-icon><Top /></el-icon>{{ dashboardData.redeemGrowth }}%</div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card main-chart">
        <div class="chart-header">
          <h3>访问趋势</h3>
          <div class="chart-tabs">
            <button class="tab" :class="{ active: chartPeriod === 'week' }" @click="chartPeriod = 'week'">周</button>
            <button class="tab" :class="{ active: chartPeriod === 'month' }" @click="chartPeriod = 'month'">月</button>
            <button class="tab" :class="{ active: chartPeriod === 'year' }" @click="chartPeriod = 'year'">年</button>
          </div>
        </div>
        <div ref="viewChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header"><h3>流量来源</h3></div>
        <div ref="sourceChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 底部区域 -->
    <div class="bottom-section">
      <div class="activity-card">
        <div class="card-header"><h3>最近活动</h3><el-button link type="primary">查看全部</el-button></div>
        <div class="activity-list">
          <div class="activity-item" v-for="(item, index) in activities" :key="index">
            <div class="activity-icon" :class="item.type"><el-icon><component :is="item.icon" /></el-icon></div>
            <div class="activity-content">
              <p class="activity-text">{{ item.text }}</p>
              <p class="activity-time">{{ item.time }}</p>
            </div>
          </div>
        </div>
      </div>
      <div class="todo-card">
        <div class="card-header"><h3>待办事项</h3><el-button link type="primary">+ 添加</el-button></div>
        <div class="todo-list">
          <div class="todo-item" v-for="(item, index) in todos" :key="index" :class="{ completed: item.completed }">
            <el-checkbox v-model="item.completed" />
            <span class="todo-text">{{ item.text }}</span>
            <span class="todo-tag" :class="item.priority">{{ item.priorityText }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import * as echarts from 'echarts'
import { EditPen, Ticket, Document, View, TrendCharts, Top, Bottom, ChatDotRound, UserFilled, Connection } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getDashboardData } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const viewChartRef = ref()
const sourceChartRef = ref()
const chartPeriod = ref('week')
const lastSyncTime = ref('2024-12-16 10:30:00')
const syncStats = reactive({ notes: 128, coupons: 36, users: 892 })

const dashboardData = ref({ todayViews: 128, todayInteractions: 36, todayCouponsClaimed: 2541, todayCouponsRedeemed: 4.8, viewGrowth: 12.5, interactionGrowth: 8.3, couponGrowth: -3.2, redeemGrowth: 0.5 })

const activities = reactive([
  { type: 'orange', icon: EditPen, text: '发布了新笔记《夏季新品推荐》，已同步到UniApp', time: '今天 09:32' },
  { type: 'green', icon: Ticket, text: '创建了优惠券"满100减20"，已同步到UniApp', time: '昨天 15:47' },
  { type: 'blue', icon: ChatDotRound, text: 'UniApp用户"美食达人"评论了您的笔记', time: '昨天 10:23' },
  { type: 'purple', icon: UserFilled, text: '新增5位UniApp用户关注了您的店铺', time: '2024-12-15 09:15' }
])

const todos = reactive([
  { text: '回复UniApp用户评论（12条）', completed: false, priority: 'urgent', priorityText: '紧急' },
  { text: '创建618促销优惠券', completed: false, priority: 'normal', priorityText: '普通' },
  { text: '更新店铺信息同步到UniApp', completed: false, priority: 'normal', priorityText: '普通' },
  { text: '发布新品笔记', completed: true, priority: 'done', priorityText: '已完成' }
])

const syncNow = () => { lastSyncTime.value = new Date().toLocaleString('zh-CN'); ElMessage.success('同步成功') }

const fetchData = async () => { try { const data = await getDashboardData(); dashboardData.value = data; drawCharts() } catch { drawCharts() } }

const drawCharts = () => {
  if (viewChartRef.value) {
    const chart = echarts.init(viewChartRef.value)
    chart.setOption({
      grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
      tooltip: { trigger: 'axis' },
      legend: { data: ['访问量', '转化量'], top: 0, right: 0, textStyle: { color: '#737373' } },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'], axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#737373' } },
      yAxis: [{ type: 'value', axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#737373' }, splitLine: { lineStyle: { color: '#F5F5F5' } } }, { type: 'value', position: 'right', axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#10B981' }, splitLine: { show: false } }],
      series: [{ name: '访问量', type: 'bar', barWidth: '40%', data: [1200, 1900, 1500, 2300, 2900, 3200, 2541], itemStyle: { borderRadius: [6, 6, 0, 0], color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#FF7D00' }, { offset: 1, color: '#FFB366' }]) } }, { name: '转化量', type: 'line', yAxisIndex: 1, data: [89, 120, 95, 150, 180, 210, 175], smooth: true, symbol: 'circle', symbolSize: 8, lineStyle: { color: '#10B981', width: 3 }, itemStyle: { color: '#10B981', borderColor: '#fff', borderWidth: 2 } }]
    })
    window.addEventListener('resize', () => chart.resize())
  }
  if (sourceChartRef.value) {
    const chart = echarts.init(sourceChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'horizontal', bottom: 10, textStyle: { color: '#737373' } },
      series: [{ type: 'pie', radius: ['45%', '75%'], avoidLabelOverlap: false, itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 3 }, label: { show: false }, emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } }, data: [{ value: 35, name: '首页推荐', itemStyle: { color: '#FF7D00' } }, { value: 25, name: '搜索', itemStyle: { color: '#10B981' } }, { value: 20, name: '分类', itemStyle: { color: '#3B82F6' } }, { value: 15, name: 'UniApp分享', itemStyle: { color: '#A855F7' } }, { value: 5, name: '其他', itemStyle: { color: '#9CA3AF' } }] }]
    })
    window.addEventListener('resize', () => chart.resize())
  }
}

onMounted(() => { fetchData() })
</script>


<style scoped>
.dashboard { max-width: 1400px; margin: 0 auto; }

/* 欢迎区域 */
.welcome-section { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.welcome-title { font-size: 28px; font-weight: 700; color: #171717; margin: 0 0 8px 0; }
.welcome-subtitle { font-size: 14px; color: #737373; margin: 0; }
.quick-actions { display: flex; gap: 12px; }
.action-btn.primary { background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%); border: none; border-radius: 12px; padding: 14px 28px; font-size: 15px; box-shadow: 0 4px 14px rgba(255, 125, 0, 0.35); }
.action-btn.primary:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(255, 125, 0, 0.45); }
.action-btn.secondary { border-radius: 12px; padding: 14px 28px; font-size: 15px; border-color: #E5E5E5; }
.action-btn.secondary:hover { border-color: #FF7D00; color: #FF7D00; }

/* UniApp同步状态 */
.sync-banner { display: flex; align-items: center; justify-content: space-between; background: linear-gradient(135deg, #FFF7ED 0%, #FFEDD5 100%); border: 1px solid #FFEDD5; border-radius: 16px; padding: 20px 28px; margin-bottom: 24px; }
.sync-info { display: flex; align-items: center; gap: 16px; }
.sync-icon { font-size: 32px; color: #FF7D00; }
.sync-title { font-size: 16px; font-weight: 600; color: #171717; display: block; }
.sync-desc { font-size: 13px; color: #737373; }
.sync-stats { display: flex; gap: 40px; }
.sync-stat { text-align: center; }
.sync-stat .stat-value { display: block; font-size: 24px; font-weight: 700; color: #FF7D00; }
.sync-stat .stat-label { font-size: 12px; color: #737373; }

/* 统计卡片 */
.stats-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
@media (max-width: 1200px) { .stats-grid { grid-template-columns: repeat(2, 1fr); } }

.stat-card { display: flex; align-items: center; gap: 18px; padding: 24px; background: white; border-radius: 16px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); transition: all 0.3s ease; }
.stat-card:hover { transform: translateY(-4px); box-shadow: 0 8px 30px rgba(255, 125, 0, 0.12); }
.stat-icon { width: 56px; height: 56px; display: flex; align-items: center; justify-content: center; border-radius: 14px; font-size: 24px; }
.stat-icon.orange { background: rgba(255, 125, 0, 0.1); color: #FF7D00; }
.stat-icon.green { background: rgba(16, 185, 129, 0.1); color: #10B981; }
.stat-icon.blue { background: rgba(59, 130, 246, 0.1); color: #3B82F6; }
.stat-icon.purple { background: rgba(168, 85, 247, 0.1); color: #A855F7; }
.stat-content { flex: 1; }
.stat-content .stat-value { display: block; font-size: 28px; font-weight: 700; color: #171717; }
.stat-content .stat-label { font-size: 13px; color: #737373; }
.stat-trend { display: flex; align-items: center; gap: 4px; font-size: 13px; font-weight: 600; }
.stat-trend.up { color: #10B981; }
.stat-trend.down { color: #EF4444; }

/* 图表区域 */
.charts-section { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-bottom: 24px; }
@media (max-width: 1024px) { .charts-section { grid-template-columns: 1fr; } }

.chart-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.chart-header h3 { font-size: 16px; font-weight: 600; color: #171717; margin: 0; }
.chart-tabs { display: flex; gap: 4px; background: #F5F5F5; padding: 4px; border-radius: 10px; }
.chart-tabs .tab { padding: 8px 16px; font-size: 13px; color: #737373; background: transparent; border: none; border-radius: 8px; cursor: pointer; transition: all 0.2s ease; }
.chart-tabs .tab:hover { color: #FF7D00; }
.chart-tabs .tab.active { background: white; color: #FF7D00; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); }
.chart-container { height: 300px; }

/* 底部区域 */
.bottom-section { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
@media (max-width: 1024px) { .bottom-section { grid-template-columns: 1fr; } }

.activity-card, .todo-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.card-header h3 { font-size: 16px; font-weight: 600; color: #171717; margin: 0; }

.activity-list { display: flex; flex-direction: column; gap: 18px; }
.activity-item { display: flex; gap: 14px; }
.activity-icon { width: 44px; height: 44px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.activity-icon.orange { background: rgba(255, 125, 0, 0.1); color: #FF7D00; }
.activity-icon.green { background: rgba(16, 185, 129, 0.1); color: #10B981; }
.activity-icon.blue { background: rgba(59, 130, 246, 0.1); color: #3B82F6; }
.activity-icon.purple { background: rgba(168, 85, 247, 0.1); color: #A855F7; }
.activity-content { flex: 1; }
.activity-text { font-size: 14px; color: #262626; margin: 0 0 4px 0; }
.activity-time { font-size: 12px; color: #A3A3A3; margin: 0; }

.todo-list { display: flex; flex-direction: column; gap: 14px; }
.todo-item { display: flex; align-items: center; gap: 14px; padding: 16px; background: #FAFAFA; border-radius: 12px; transition: background 0.2s ease; }
.todo-item:hover { background: #FFF7ED; }
.todo-item.completed .todo-text { text-decoration: line-through; color: #A3A3A3; }
.todo-text { flex: 1; font-size: 14px; color: #262626; }
.todo-tag { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.todo-tag.urgent { background: rgba(255, 125, 0, 0.1); color: #FF7D00; }
.todo-tag.normal { background: rgba(59, 130, 246, 0.1); color: #3B82F6; }
.todo-tag.done { background: #E5E5E5; color: #737373; }
</style>
