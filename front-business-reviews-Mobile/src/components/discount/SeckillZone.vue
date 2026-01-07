<template>
  <view v-if="activity && activity.coupons && activity.coupons.length > 0" class="seckill-zone">
    <view class="hot-tag">HOT</view>
    <view class="zone-header">
      <view class="zone-title">
        <text class="fire-icon">🔥</text>
        <text>限时秒杀</text>
      </view>
      <view class="timer">
        <text class="timer-block">{{ countdown.hours }}</text>
        <text class="timer-sep">:</text>
        <text class="timer-block">{{ countdown.minutes }}</text>
        <text class="timer-sep">:</text>
        <text class="timer-block">{{ countdown.seconds }}</text>
      </view>
    </view>
    <scroll-view 
      class="seckill-scroll" 
      scroll-x
      :scroll-left="scrollLeft"
      scroll-with-animation
      @touchstart="handleTouchStart"
      @touchend="handleTouchEnd"
      @scroll="handleScroll"
    >
      <view 
        class="seckill-card" 
        v-for="item in activity.coupons" 
        :key="item.id"
        :class="{ 'sold-out': item.remainStock <= 0 }"
        @click="$emit('click-item', item)"
      >
        <view class="sk-price" :class="{ disabled: item.remainStock <= 0 }">
          <text class="price-symbol">¥</text>{{ item.seckillPrice }}
        </view>
        <text class="sk-name">{{ item.title }}</text>
        <view class="sk-progress">
          <view class="sk-fill" :style="{ width: getProgress(item) + '%' }"></view>
        </view>
        <button 
          class="btn-grab" 
          :class="{ disabled: item.remainStock <= 0 }"
          @click.stop="onClaim(item)"
        >
          {{ getSeckillBtnText(item) }}
        </button>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'

const props = defineProps({
  activity: {
    type: Object,
    default: null
  },
  countdown: {
    type: Object,
    default: () => ({ hours: '00', minutes: '00', seconds: '00' })
  }
})

const emit = defineEmits(['click-item', 'claim'])

// Auto Scroll Logic
const scrollLeft = ref(0)
const isAutoScrolling = ref(false)
const isTouching = ref(false)
let scrollTimer = null
let resumeTimer = null

const AUTO_SCROLL_INTERVAL = 3000
const CARD_WIDTH_RPX = 244

const getCardWidthInPx = () => {
    try {
        const systemInfo = uni.getSystemInfoSync()
        const screenWidth = systemInfo.screenWidth || 375
        return (CARD_WIDTH_RPX * screenWidth) / 750
    } catch (e) {
        return (CARD_WIDTH_RPX * 375) / 750
    }
}

const cardCount = computed(() => props.activity?.coupons?.length || 0)
const maxScrollLeft = computed(() => {
    if (cardCount.value <= 1) return 0
    return getCardWidthInPx() * (cardCount.value - 1)
})

const scrollToNext = () => {
    if (cardCount.value <= 1 || isTouching.value) return
    
    const cardWidthPx = getCardWidthInPx()
    let nextPosition = scrollLeft.value + cardWidthPx
    
    if (nextPosition > maxScrollLeft.value + 10) { // +10 buffer
        nextPosition = 0
    }
    
    isAutoScrolling.value = true
    scrollLeft.value = nextPosition
    
    setTimeout(() => {
        isAutoScrolling.value = false
    }, 500)
}

const startAutoScroll = () => {
    stopAutoScroll()
    scrollTimer = setInterval(scrollToNext, AUTO_SCROLL_INTERVAL)
}

const stopAutoScroll = () => {
    if (scrollTimer) {
        clearInterval(scrollTimer)
        scrollTimer = null
    }
}

const handleTouchStart = () => {
    isTouching.value = true
    stopAutoScroll()
    if (resumeTimer) clearTimeout(resumeTimer)
}

const handleTouchEnd = () => {
    isTouching.value = false
    resumeTimer = setTimeout(startAutoScroll, 3000)
}

const handleScroll = (e) => {
    if (!isAutoScrolling.value) {
        scrollLeft.value = e.detail.scrollLeft
    }
}

const getProgress = (item) => {
    if (!item.seckillStock) return 100
    return Math.round(((item.seckillStock - item.remainStock) / item.seckillStock) * 100)
}

const getSeckillBtnText = (item) => {
    if (item.remainStock <= 0) return '已抢光'
    if (item.remainStock <= 5) return `仅剩${item.remainStock}张`
    return '立即抢'
}

const onClaim = (item) => {
    emit('claim', item)
}

onMounted(() => {
    startAutoScroll()
})

onUnmounted(() => {
    stopAutoScroll()
    if (resumeTimer) clearTimeout(resumeTimer)
})
</script>

<style lang="scss" scoped>
.seckill-zone {
	margin: 24rpx 20rpx;
	padding: 24rpx;
	background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
	border-radius: 20rpx;
	position: relative;
	overflow: hidden;
}

.hot-tag {
	position: absolute;
	top: 0;
	left: 0;
	background: #ff4757;
	color: #fff;
	font-size: 20rpx;
	padding: 4rpx 12rpx;
	border-bottom-right-radius: 20rpx;
	font-weight: bold;
}

.zone-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24rpx;
	margin-top: 8rpx;
}

.zone-title {
	display: flex;
	align-items: center;
	gap: 8rpx;
	font-size: 30rpx;
	font-weight: bold;
	color: #ff4757;
}

.fire-icon {
	font-size: 32rpx;
}

.timer {
	display: flex;
	align-items: center;
	gap: 4rpx;
}

.timer-block {
	background: #ff4757;
	color: #fff;
	font-size: 20rpx;
	padding: 2rpx 8rpx;
	border-radius: 8rpx;
	font-weight: 500;
}

.timer-sep {
	color: #ff4757;
	font-weight: bold;
	font-size: 20rpx;
}

.seckill-scroll {
	white-space: nowrap;
	width: 100%;
}

.seckill-card {
	display: inline-flex;
	flex-direction: column;
	width: 220rpx;
	background: #fff;
	border-radius: 16rpx;
	padding: 16rpx;
	margin-right: 24rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	position: relative;
	
	&:last-child {
		margin-right: 0;
	}
	
	&.sold-out {
		opacity: 0.6;
	}
}

.sk-price {
	color: #ff4757;
	font-size: 36rpx;
	font-weight: bold;
	
	.price-symbol {
		font-size: 24rpx;
		margin-right: 2rpx;
	}
	
	&.disabled {
		color: #999;
	}
}

.sk-name {
	font-size: 24rpx;
	color: #333;
	margin: 8rpx 0;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.sk-progress {
	height: 8rpx;
	background: #fee;
	border-radius: 4rpx;
	margin-bottom: 16rpx;
	overflow: hidden;
}

.sk-fill {
	height: 100%;
	background: #ff4757;
	border-radius: 4rpx;
}

.btn-grab {
	width: 100%;
	height: 52rpx;
	line-height: 52rpx;
	background: linear-gradient(90deg, #ff6b81, #ff4757);
	color: #fff;
	font-size: 24rpx;
	border-radius: 26rpx;
	padding: 0;
	
	&.disabled {
		background: #ccc;
		color: #fff;
	}
}
</style>
