<template>
  <div class="analytics-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">数据分析</h1>
        <p class="page-desc">UniApp用户行为数据分析与运营洞察</p>
      </div>
      <div class="header-actions">
        <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" />
        <el-button type="primary" @click="exportReport">
          <el-icon><Download /></el-icon>导出报告
        </el-button>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-grid">
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-title">总访问量</span>
          <el-tag type="success" size="small">+12.5%</el-tag>
        </div>
        <div class="metric-value">{{ metrics.totalViews.toLocaleString() }}</div>
        <div class="metric-chart" ref="viewsChartRef"></div>
      </div>
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-title">用户转化率</span>
          <el-tag type="warning" size="small">+3.2%</el-tag>
        </div>
        <div class="metric-value">{{ metrics.conversionRate }}%</div>
        <div class="metric-chart" ref="conversionChartRef"></div>
      </div>
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-title">笔记互动量</span>
          <el-tag type="success" size="small">+8.7%</el-tag>
        </div>
        <div class="metric-value">{{ metrics.interactions.toLocaleString() }}</div>
        <div class="metric-chart" ref="interactionChartRef"></div>
      </div>
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-title">优惠券核销</span>
          <el-tag type="danger" size="small">-2.1%</el-tag>
        </div>
        <div class="metric-value">{{ metrics.couponRedeemed }}</div>
        <div class="metric-chart" ref="couponChartRef"></div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-section">
      <div class="chart-card large">
        <div class="chart-header">
          <h3>访问趋势分析</h3>
          <div class="chart-tabs">
            <button class="tab" :class="{ active: trendPeriod === 'day' }" @click="trendPeriod = 'day'">日</button>
            <button class="tab" :class="{ active: trendPeriod === 'week' }" @click="trendPeriod = 'week'">周</button>
            <button class="tab" :class="{ active: trendPeriod === 'month' }" @click="trendPeriod = 'month'">月</button>
          </div>
        </div>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card">
        <div class="chart-header">
          <h3>用户来源分布</h3>
        </div>
        <div ref="sourceChartRef" class="chart-container"></div>
      </div>
    </div>

    <!-- 热门内容排行 -->
    <div class="ranking-section">
      <div class="ranking-card">
        <div class="ranking-header">
          <h3>热门笔记TOP5</h3>
          <el-button link type="primary">查看更多</el-button>
        </div>
        <div class="ranking-list">
          <div v-for="(item, index) in hotNotes" :key="item.id" class="ranking-item">
            <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
            <img :src="item.cover" class="item-cover" />
            <div class="item-info">
              <span class="item-title">{{ item.title }}</span>
              <span class="item-stats"><el-icon><View /></el-icon>{{ item.views }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="ranking-card">
        <div class="ranking-header">
          <h3>活跃用户TOP5</h3>
          <el-button link type="primary">查看更多</el-button>
        </div>
        <div class="ranking-list">
          <div v-for="(item, index) in activeUsers" :key="item.id" class="ranking-item">
            <span class="rank" :class="{ top: index < 3 }">{{ index + 1 }}</span>
            <el-avatar :size="40" :src="item.avatar">{{ item.name?.charAt(0) }}</el-avatar>
            <div class="item-info">
              <span class="item-title">{{ item.name }}</span>
              <span class="item-stats"><el-icon><Star /></el-icon>{{ item.interactions }}次互动</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import * as echarts from 'echarts'
import { Download, View, Star } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const dateRange = ref<[Date, Date] | null>(null)
const trendPeriod = ref('week')
const trendChartRef = ref()
const sourceChartRef = ref()

const metrics = reactive({ totalViews: 125680, conversionRate: 4.8, interactions: 8956, couponRedeemed: 1234 })

const hotNotes = ref([
  { id: 1, title: '夏日清凉饮品推荐，必喝榜单！', cover: '', views: 12580 },
  { id: 2, title: '周末探店｜藏在巷子里的宝藏小店', cover: '', views: 9876 },
  { id: 3, title: '新品上市！限时特惠不容错过', cover: '', views: 8654 },
  { id: 4, title: '美食攻略：本地人都爱的招牌菜', cover: '', views: 7432 },
  { id: 5, title: '店铺装修升级，全新体验等你来', cover: '', views: 6210 },
])

const activeUsers = ref([
  { id: 1, name: '美食探店家', avatar: '', interactions: 156 },
  { id: 2, name: '旅行达人小王', avatar: '', interactions: 132 },
  { id: 3, name: '吃货小分队', avatar: '', interactions: 98 },
  { id: 4, name: '生活记录者', avatar: '', interactions: 87 },
  { id: 5, name: '周末探险家', avatar: '', interactions: 76 },
])

const exportReport = () => { ElMessage.success('报告导出中...') }

const initCharts = () => {
  if (trendChartRef.value) {
    const chart = echarts.init(trendChartRef.value)
    chart.setOption({
      grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
      tooltip: { trigger: 'axis' },
      legend: { data: ['访问量', '互动量', '转化量'], top: 0, right: 0 },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'], axisLine: { show: false }, axisTick: { show: false } },
      yAxis: { type: 'value', axisLine: { show: false }, axisTick: { show: false }, splitLine: { lineStyle: { color: '#F5F5F5' } } },
      series: [
        { name: '访问量', type: 'line', smooth: true, data: [1200, 1900, 1500, 2300, 2900, 3200, 2541], lineStyle: { color: '#FF7D00', width: 3 }, itemStyle: { color: '#FF7D00' }, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(255, 125, 0, 0.3)' }, { offset: 1, color: 'rgba(255, 125, 0, 0)' }]) } },
        { name: '互动量', type: 'line', smooth: true, data: [300, 450, 380, 520, 680, 750, 620], lineStyle: { color: '#10B981', width: 3 }, itemStyle: { color: '#10B981' } },
        { name: '转化量', type: 'line', smooth: true, data: [89, 120, 95, 150, 180, 210, 175], lineStyle: { color: '#3B82F6', width: 3 }, itemStyle: { color: '#3B82F6' } }
      ]
    })
    window.addEventListener('resize', () => chart.resize())
  }
  if (sourceChartRef.value) {
    const chart = echarts.init(sourceChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: { orient: 'horizontal', bottom: 10 },
      series: [{ type: 'pie', radius: ['45%', '75%'], avoidLabelOverlap: false, itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 }, label: { show: false }, data: [{ value: 40, name: '首页推荐', itemStyle: { color: '#FF7D00' } }, { value: 25, name: '搜索发现', itemStyle: { color: '#10B981' } }, { value: 20, name: '分享链接', itemStyle: { color: '#3B82F6' } }, { value: 15, name: '其他渠道', itemStyle: { color: '#A3A3A3' } }] }]
    })
    window.addEventListener('resize', () => chart.resize())
  }
}

onMounted(() => { initCharts() })
</script>


<style scoped>
.analytics-page { max-width: 1400px; margin: 0 auto; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 26px; font-weight: 700; color: #171717; margin: 0 0 6px 0; }
.page-desc { font-size: 14px; color: #737373; margin: 0; }
.header-actions { display: flex; gap: 12px; }

/* 核心指标卡片 */
.metrics-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 24px; }
@media (max-width: 1200px) { .metrics-grid { grid-template-columns: repeat(2, 1fr); } }

.metric-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.metric-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.metric-title { font-size: 14px; color: #737373; }
.metric-value { font-size: 32px; font-weight: 700; color: #171717; margin-bottom: 16px; }
.metric-chart { height: 60px; }

/* 图表区域 */
.charts-section { display: grid; grid-template-columns: 2fr 1fr; gap: 20px; margin-bottom: 24px; }
@media (max-width: 1024px) { .charts-section { grid-template-columns: 1fr; } }

.chart-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.chart-header h3 { font-size: 16px; font-weight: 600; color: #171717; margin: 0; }
.chart-tabs { display: flex; gap: 4px; background: #F5F5F5; padding: 4px; border-radius: 8px; }
.chart-tabs .tab { padding: 6px 14px; font-size: 13px; color: #737373; background: transparent; border: none; border-radius: 6px; cursor: pointer; }
.chart-tabs .tab:hover { color: #FF7D00; }
.chart-tabs .tab.active { background: white; color: #FF7D00; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); }
.chart-container { height: 300px; }

/* 排行榜 */
.ranking-section { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
@media (max-width: 1024px) { .ranking-section { grid-template-columns: 1fr; } }

.ranking-card { background: white; border-radius: 16px; padding: 24px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.ranking-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.ranking-header h3 { font-size: 16px; font-weight: 600; color: #171717; margin: 0; }

.ranking-list { display: flex; flex-direction: column; gap: 16px; }
.ranking-item { display: flex; align-items: center; gap: 14px; }
.rank { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 600; color: #737373; background: #F5F5F5; border-radius: 8px; }
.rank.top { background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%); color: white; }
.item-cover { width: 48px; height: 48px; border-radius: 10px; object-fit: cover; background: #F5F5F5; }
.item-info { flex: 1; display: flex; flex-direction: column; gap: 4px; }
.item-title { font-size: 14px; font-weight: 500; color: #171717; }
.item-stats { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #737373; }
.item-stats .el-icon { font-size: 14px; color: #FF7D00; }
</style>
