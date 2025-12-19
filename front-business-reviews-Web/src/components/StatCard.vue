<template>
  <div class="stat-card">
    <div class="stat-card-content">
      <div class="stat-info">
        <p class="stat-title">{{ title }}</p>
        <h3 class="stat-value">{{ formattedValue }}</h3>
        <div class="stat-growth">
          <GrowthIndicator :value="growth" :suffix="growthSuffix" />
          <span class="growth-label">{{ growthLabel }}</span>
        </div>
      </div>
      <div class="stat-icon" :class="`icon-${iconColor}`">
        <el-icon :size="24">
          <component :is="iconComponent" />
        </el-icon>
      </div>
    </div>
    <div class="stat-decoration" :class="`decoration-${iconColor}`"></div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import GrowthIndicator from './GrowthIndicator.vue'
import { 
  View, 
  ChatDotRound, 
  Ticket, 
  TrendCharts,
  Document,
  Shop
} from '@element-plus/icons-vue'

interface Props {
  title: string
  value: number | string
  growth: number
  icon: string
  iconColor?: 'primary' | 'secondary' | 'info' | 'warning' | 'success' | 'danger'
  growthLabel?: string
  growthSuffix?: string
}

const props = withDefaults(defineProps<Props>(), {
  iconColor: 'primary',
  growthLabel: '较上月',
  growthSuffix: '%'
})

const formattedValue = computed(() => {
  if (typeof props.value === 'number') {
    return props.value.toLocaleString()
  }
  return props.value
})

const iconMap: Record<string, any> = {
  view: View,
  chat: ChatDotRound,
  ticket: Ticket,
  trend: TrendCharts,
  document: Document,
  shop: Shop
}

const iconComponent = computed(() => iconMap[props.icon] || View)
</script>

<style scoped>
.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  border: 1px solid #F5F5F5;
}

.stat-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  transform: translateY(-4px);
}

.stat-card-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
  z-index: 1;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #737373;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #171717;
  margin: 0 0 12px 0;
}

.stat-growth {
  display: flex;
  align-items: center;
  gap: 8px;
}

.growth-label {
  font-size: 12px;
  color: #A3A3A3;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.icon-primary {
  background-color: rgba(255, 125, 0, 0.1);
  color: #FF7D00;
}

.icon-secondary {
  background-color: rgba(76, 175, 80, 0.1);
  color: #4CAF50;
}

.icon-info {
  background-color: rgba(59, 130, 246, 0.1);
  color: #3B82F6;
}

.icon-warning {
  background-color: rgba(245, 158, 11, 0.1);
  color: #F59E0B;
}

.icon-success {
  background-color: rgba(16, 185, 129, 0.1);
  color: #10B981;
}

.icon-danger {
  background-color: rgba(239, 68, 68, 0.1);
  color: #EF4444;
}

.stat-decoration {
  position: absolute;
  top: -40px;
  right: -40px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  opacity: 0.05;
  transition: all 0.5s ease;
}

.stat-card:hover .stat-decoration {
  transform: scale(1.5);
}

.decoration-primary {
  background-color: #FF7D00;
}

.decoration-secondary {
  background-color: #4CAF50;
}

.decoration-info {
  background-color: #3B82F6;
}

.decoration-warning {
  background-color: #F59E0B;
}

.decoration-success {
  background-color: #10B981;
}

.decoration-danger {
  background-color: #EF4444;
}
</style>
