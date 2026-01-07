<template>
  <view v-if="coupons && coupons.length > 0" class="seckill-zone flash-sale-zone">
    <view class="hot-tag seckill-tag">秒</view>
    <view class="zone-header">
      <view class="zone-title">
        <text class="fire-icon">⚡</text>
        <text>秒杀券专区</text>
      </view>
      <view class="seckill-tip">即将过期</view>
    </view>
    <scroll-view 
      class="seckill-scroll" 
      scroll-x
      scroll-with-animation
    >
      <view 
        class="seckill-card flash-sale-card" 
        v-for="coupon in coupons" 
        :key="coupon.id"
        :class="{ 'sold-out': coupon.status === 'sold_out', 'is-claimed': coupon.status === 'claimed' }"
        @click="$emit('click-item', coupon)"
      >
        <!-- 倒计时标签（右上角） -->
        <view class="countdown-badge">
           <text class="cd-text">{{ formatCountdown(coupon) }}</text>
        </view>
        
        <view class="sk-price flash-price" :class="{ disabled: coupon.status === 'sold_out' || coupon.status === 'claimed' }">
           <text class="unit" v-if="!coupon.discount">¥</text>
           <text class="amount">{{ formatValueMain(coupon) }}</text>
           <text class="unit" v-if="coupon.discount">折</text>
        </view>
        <text class="sk-name">{{ coupon.title }}</text>
        <text class="flash-shop">{{ coupon.shopName || '商家' }}</text>
        <!-- 使用图标按钮显示领取状态 -->
        <view class="flash-btn-wrapper" @click.stop="$emit('claim', coupon)">
          <image 
            v-if="coupon.status === 'available'" 
            src="/static/icons/coupon-claim.png" 
            class="flash-btn-icon"
            mode="aspectFit"
          />
          <image 
            v-else-if="coupon.status === 'claimed'" 
            src="/static/icons/coupon-claimed.png" 
            class="flash-btn-icon"
            mode="aspectFit"
          />
          <button 
            v-else
            class="btn-grab flash-btn" 
            :class="getFlashBtnClass(coupon.status)"
          >
            {{ getCouponBtnText(coupon.status) }}
          </button>
        </view>
      </view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  coupons: {
    type: Array,
    default: () => []
  }
})

defineEmits(['click-item', 'claim'])

// 倒计时状态，使用 Map 存储每个优惠券的倒计时
const countdownMap = ref({})
let countdownTimer = null

// 启动倒计时更新
const startCountdownTimer = () => {
  updateAllCountdowns()
  countdownTimer = setInterval(updateAllCountdowns, 1000)
}

// 更新所有优惠券的倒计时
const updateAllCountdowns = () => {
  if (!props.coupons || props.coupons.length === 0) return
  
  const newMap = {}
  props.coupons.forEach(coupon => {
    if (coupon.endTime) {
      newMap[coupon.id] = calculateCountdown(coupon.endTime)
    }
  })
  countdownMap.value = newMap
}

// 计算倒计时
const calculateCountdown = (endTimeStr) => {
  const now = new Date().getTime()
  const end = new Date(endTimeStr).getTime()
  const diff = Math.max(0, end - now)
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  return {
    hours: String(hours).padStart(2, '0'),
    minutes: String(minutes).padStart(2, '0'),
    seconds: String(seconds).padStart(2, '0'),
    expired: diff <= 0
  }
}

// 格式化倒计时显示
const formatCountdown = (coupon) => {
  const cd = countdownMap.value[coupon.id]
  if (!cd) {
    if (coupon.endTime) {
      const initialCd = calculateCountdown(coupon.endTime)
      return `${initialCd.hours}:${initialCd.minutes}:${initialCd.seconds}`
    }
    return '00:00:00'
  }
  if (cd.expired) return '已结束'
  return `${cd.hours}:${cd.minutes}:${cd.seconds}`
}

const formatValueMain = (coupon) => {
    if (coupon.type === 2 && coupon.discount) {
        const val = coupon.discount * 10
        return val % 1 === 0 ? val : val.toFixed(1)
    }
    return coupon.amount
}


const getFlashBtnClass = (status) => {
	return {
		'flash-available': status === 'available',
		'flash-claimed': status === 'claimed',
		'flash-sold-out': status === 'sold_out',
		'flash-not-started': status === 'not_started'
	}
}

const getCouponBtnText = (status) => {
	if (status === 'available') return '领取'
	if (status === 'claimed') return '已领取'
	if (status === 'sold_out') return '已抢光'
	if (status === 'not_started') return '未开始'
	return '领取'
}

onMounted(() => {
  startCountdownTimer()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style lang="scss" scoped>
.seckill-zone {
	margin: 24rpx 20rpx;
	padding: 32rpx 24rpx 40rpx; // 增加底部padding
	background: #fff;
	border-radius: 24rpx;
	position: relative;
	overflow: hidden;
    box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
}

.flash-sale-zone {
	background: linear-gradient(180deg, #fff0e5 0%, #fff 100%);
}

.hot-tag {
	position: absolute;
	top: 0;
	left: 0;
	background: #ff6b35;
	color: #fff;
	font-size: 20rpx;
	padding: 6rpx 14rpx;
	border-bottom-right-radius: 20rpx;
	font-weight: bold;
    z-index: 2;
}

.seckill-tag {
	background: linear-gradient(135deg, #ff8c42, #ff6b35);
}

.zone-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 30rpx;
	margin-top: 8rpx;
    padding-left: 10rpx;
}

.zone-title {
	display: flex;
	align-items: center;
	gap: 12rpx;
	font-size: 32rpx;
	font-weight: 800;
	color: #333;
}

.fire-icon {
    font-size: 36rpx;
}

.seckill-scroll {
	white-space: nowrap;
	width: 100%;
    // 允许阴影溢出
    padding: 10rpx 0 20rpx; 
    margin: -10rpx 0 -20rpx;
}

.seckill-card {
	display: inline-flex;
	flex-direction: column;
	width: 240rpx; // 稍微加宽
	background: #fff;
	border-radius: 20rpx;
	padding: 24rpx 20rpx;
	margin-right: 24rpx;
	position: relative;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
    transition: transform 0.1s;
	
	&:last-child {
		margin-right: 10rpx;
	}
    
    &:active {
        transform: scale(0.98);
    }
	
	&.sold-out {
		opacity: 0.7;
        filter: grayscale(100%);
	}
}

.flash-sale-card {
    padding-top: 60rpx; // 为倒计时标签留出空间
}

// 倒计时徽章
.countdown-badge {
    position: absolute;
    top: 0;
    right: 0;
    left: 0; // 撑满顶部
    height: 48rpx;
    background: linear-gradient(90deg, #ffecd2 0%, #fcb69f 100%);
    color: #c0392b;
    font-size: 20rpx;
    font-weight: bold;
    display: flex;
    align-items: center;
    justify-content: center;
    border-top-left-radius: 20rpx;
    border-top-right-radius: 20rpx;
    
    .cd-text {
        font-family: 'Arial Rounded MT Bold', sans-serif;
    }
}

.sk-price {
	color: #ff4757;
	font-weight: bold;
    display: flex;
    align-items: baseline;
    justify-content: center;
    margin-bottom: 8rpx;
    
    &.disabled {
		color: #999;
	}
}

.flash-price {
    color: #e64a19;
}

.amount {
    font-size: 48rpx;
    line-height: 1;
    font-family: 'DIN Alternate', sans-serif;
}

.unit {
    font-size: 24rpx;
    margin: 0 4rpx;
}

.sk-name {
	font-size: 26rpx;
	color: #333;
    font-weight: 500;
	margin: 8rpx 0;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
    text-align: center;
}

.flash-shop {
    font-size: 22rpx;
    color: #999;
    margin-bottom: 20rpx;
    overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
    text-align: center;
}

.flash-btn-wrapper {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 56rpx;
}

.flash-btn-icon {
    width: 140rpx;
    height: 56rpx;
}

.btn-grab {
	width: 100%;
	height: 56rpx;
	line-height: 56rpx;
	background: linear-gradient(90deg, #ff6b81, #ff4757);
	color: #fff;
	font-size: 24rpx;
    font-weight: bold;
	border-radius: 28rpx;
	padding: 0;
    border: none;
    box-shadow: 0 4rpx 10rpx rgba(255, 107, 53, 0.3);
}

.flash-btn {
    background: linear-gradient(135deg, #ff6b35, #f7931e);
}

.flash-available {
    background: linear-gradient(135deg, #ff6b35, #f7931e);
}
.flash-claimed {
    background: #e0e0e0; 
    color: #999;
    box-shadow: none;
}
.flash-sold-out {
    background: #e0e0e0;
    color: #999;
    box-shadow: none;
} 

.seckill-tip {
    font-size: 20rpx;
    color: #ff6b35;
    background: #fff;
    padding: 2rpx 12rpx;
    border-radius: 12rpx;
    border: 1rpx solid rgba(255, 107, 53, 0.3);
}
</style>

