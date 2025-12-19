<template>
  <span class="growth-indicator" :class="indicatorClass">
    <el-icon v-if="value !== 0">
      <Top v-if="value > 0" />
      <Bottom v-else />
    </el-icon>
    <span>{{ Math.abs(value) }}{{ suffix }}</span>
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Top, Bottom } from '@element-plus/icons-vue'

interface Props {
  value: number
  suffix?: string
}

const props = withDefaults(defineProps<Props>(), {
  suffix: '%'
})

const indicatorClass = computed(() => {
  if (props.value > 0) return 'growth-up'
  if (props.value < 0) return 'growth-down'
  return ''
})
</script>

<style scoped>
.growth-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  font-weight: 500;
}

.growth-up {
  color: #10B981;
}

.growth-down {
  color: #EF4444;
}
</style>
