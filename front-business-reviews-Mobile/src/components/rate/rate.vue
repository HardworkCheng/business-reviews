<template>
  <view class="rate-container" :class="sizeClass">
    <view class="rate-row">
      <text 
        v-for="star in 5" 
        :key="star" 
        class="star-item"
        :class="{ active: star <= value, readonly: readonly }"
        @click="handleClick(star)"
      >★</text>
    </view>
    <text v-if="showScore" class="rate-score">{{ value }}</text>
  </view>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0
  },
  readonly: {
    type: Boolean,
    default: false
  },
  showScore: {
    type: Boolean,
    default: false
  },
  size: {
    type: String, // 'small', 'medium', 'large'
    default: 'medium'
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const value = computed({
  get: () => props.modelValue,
  set: (val) => {
    if (props.readonly) return
    emit('update:modelValue', val)
    emit('change', val)
  }
})

const handleClick = (star) => {
  if (props.readonly) return
  value.value = star
}

const sizeClass = computed(() => `size-${props.size}`)
</script>

<style lang="scss" scoped>
.rate-container {
  display: flex;
  align-items: center;
}

.rate-row {
  display: flex;
}

.star-item {
  color: #ddd;
  line-height: 1;
  transition: color 0.2s;
  
  &.active {
    color: #FF8F1F;
  }
  
  &:not(.readonly):active {
    transform: scale(1.2);
  }
}

.rate-score {
  margin-left: 10rpx;
  color: #FF8F1F;
  font-weight: bold;
}

/* Sizes */
.size-small {
  .star-item { font-size: 24rpx; margin-right: 2rpx; }
  .rate-score { font-size: 24rpx; }
}

.size-medium {
  .star-item { font-size: 36rpx; margin-right: 6rpx; }
  .rate-score { font-size: 32rpx; }
}

.size-large {
  .star-item { font-size: 56rpx; margin-right: 16rpx; }
  .rate-score { font-size: 48rpx; }
}
</style>
