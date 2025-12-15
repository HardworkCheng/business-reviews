<template>
  <div class="dashboard">
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-title">今日浏览量</div>
            <div class="stat-value">{{ dashboardData.todayViews }}</div>
            <div class="stat-trend" :class="{ 'up': dashboardData.viewGrowth >= 0 }">
              <el-icon><Top v-if="dashboardData.viewGrowth >= 0" /><Bottom v-else /></el-icon>
              {{ Math.abs(dashboardData.viewGrowth) }}%
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-title">今日互动数</div>
            <div class="stat-value">{{ dashboardData.todayInteractions }}</div>
            <div class="stat-trend" :class="{ 'up': dashboardData.interactionGrowth >= 0 }">
              <el-icon><Top v-if="dashboardData.interactionGrowth >= 0" /><Bottom v-else /></el-icon>
              {{ Math.abs(dashboardData.interactionGrowth) }}%
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-title">今日领券数</div>
            <div class="stat-value">{{ dashboardData.todayCouponsClaimed }}</div>
            <div class="stat-trend" :class="{ 'up': dashboardData.couponGrowth >= 0 }">
              <el-icon><Top v-if="dashboardData.couponGrowth >= 0" /><Bottom v-else /></el-icon>
              {{ Math.abs(dashboardData.couponGrowth) }}%
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-title">今日核销量</div>
            <div class="stat-value">{{ dashboardData.todayCouponsRedeemed }}</div>
            <div class="stat-trend" :class="{ 'up': dashboardData.redeemGrowth >= 0 }">
              <el-icon><Top v-if="dashboardData.redeemGrowth >= 0" /><Bottom v-else /></el-icon>
              {{ Math.abs(dashboardData.redeemGrowth) }}%
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="charts">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>近7日浏览趋势</span>
            </div>
          </template>
          <div ref="viewChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>热门内容TOP5</span>
            </div>
          </template>
          <el-table :data="dashboardData.topNotes" style="width: 100%">
            <el-table-column prop="title" label="笔记标题" />
            <el-table-column prop="views" label="浏览量" width="100" />
            <el-table-column prop="interactions" label="互动数" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="quick-actions">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
            </div>
          </template>
          <div class="actions">
            <el-button type="primary" @click="$router.push('/notes/create')">
              <el-icon><EditPen /></el-icon>
              发布笔记
            </el-button>
            <el-button type="success" @click="$router.push('/coupons/create')">
              <el-icon><Ticket /></el-icon>
              创建优惠券
            </el-button>
            <el-button @click="$router.push('/shops/create')">
              <el-icon><Shop /></el-icon>
              添加门店
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { EditPen, Ticket, Shop, Top, Bottom } from '@element-plus/icons-vue'
import { getDashboardData } from '@/api/dashboard'

// 图表引用
const viewChartRef = ref()

// 数据
const dashboardData = ref({
  todayViews: 0,
  todayInteractions: 0,
  todayCouponsClaimed: 0,
  todayCouponsRedeemed: 0,
  viewGrowth: 0,
  interactionGrowth: 0,
  couponGrowth: 0,
  redeemGrowth: 0,
  viewTrend: [] as { date: string; views: number }[],
  topNotes: [] as { title: string; views: number; interactions: number }[]
})

// 获取数据
const fetchData = async () => {
  try {
    const data = await getDashboardData()
    dashboardData.value = data
    drawCharts()
  } catch (error) {
    console.error('获取数据失败:', error)
  }
}

// 绘制图表
const drawCharts = () => {
  // 浏览趋势图
  if (viewChartRef.value) {
    const chart = echarts.init(viewChartRef.value)
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: dashboardData.value.viewTrend.map(item => item.date)
      },
      yAxis: {
        type: 'value'
      },
      series: [{
        data: dashboardData.value.viewTrend.map(item => item.views),
        type: 'line',
        smooth: true,
        areaStyle: {}
      }]
    }
    chart.setOption(option)
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 120px;
}

.stat-content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
}

.stat-title {
  font-size: 14px;
  color: #666;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.stat-trend {
  font-size: 12px;
  color: #f56c6c;
}

.stat-trend.up {
  color: #67c23a;
}

.chart-container {
  height: 300px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.actions {
  display: flex;
  gap: 20px;
}

.actions .el-button {
  padding: 12px 24px;
}
</style>