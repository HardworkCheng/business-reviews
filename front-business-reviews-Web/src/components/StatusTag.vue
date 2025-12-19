<template>
  <span class="status-tag" :class="statusClass" :style="statusStyle">
    <span v-if="showDot" class="status-dot" :style="dotStyle"></span>
    {{ label }}
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { getStatusStyle } from '@/styles/theme'

interface Props {
  status: string
  type?: 'note' | 'coupon'
  showDot?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'note',
  showDot: false
})

const statusConfig = computed(() => getStatusStyle(props.status))

const statusClass = computed(() => `status-${props.status}`)

const statusStyle = computed(() => ({
  backgroundColor: statusConfig.value.bg,
  color: statusConfig.value.text
}))

const dotStyle = computed(() => ({
  backgroundColor: statusConfig.value.dot || statusConfig.value.text
}))

const label = computed(() => statusConfig.value.label || props.status)
</script>

<style scoped>
.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 9999px;
  font-size: 12px;
  font-weight: 500;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
</style>
