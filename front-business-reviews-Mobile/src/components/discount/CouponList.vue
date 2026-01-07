<template>
  <view class="shop-coupon-list">
    <view class="list-title">好店神券</view>
    
    <view 
      class="coupon-row" 
      v-for="coupon in coupons" 
      :key="coupon.id"
      @click="$emit('click-item', coupon)"
    >
      <view class="cr-left">
        <view class="amount-wrapper">
             <text class="currency">¥</text>
             <text class="mc-amount">{{ coupon.amount || formatValueSimple(coupon) }}</text>
        </view>
        <view class="punch-hole top"></view>
        <view class="punch-hole bottom"></view>
        <view class="sep-line"></view>
      </view>

      <view class="cr-right">
        <view class="cr-main">
            <text class="cr-title">{{ coupon.title }}</text>
            <view class="shop-info">
                <image :src="coupon.shopLogo || '/static/images/default-shop.png'" class="cr-logo" mode="aspectFill"></image>
                <text class="cr-name">{{ coupon.shopName || '商家' }}</text>
            </view>
            <text class="cr-condition">{{ formatCouponCondition(coupon) }}</text>
        </view>
        
        <view class="cr-action">
          <!-- 使用图标按钮显示领取状态 -->
          <view class="btn-wrapper" @click.stop="$emit('claim', coupon)">
            <image 
              v-if="coupon.status === 'available'" 
              src="/static/icons/coupon-claim.png" 
              class="btn-icon"
              mode="aspectFit"
            />
            <image 
              v-else-if="coupon.status === 'claimed'" 
              src="/static/icons/coupon-claimed.png" 
              class="btn-icon"
              mode="aspectFit"
            />
            <button 
              v-else
              class="btn-get" 
              :class="getCouponBtnClass(coupon.status)"
            >
              {{ getCouponBtnText(coupon.status) }}
            </button>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="coupons.length === 0" class="empty-center">
      <text class="empty-icon">🎟️</text>
      <text class="empty-text">{{ emptyText }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  coupons: {
    type: Array,
    default: () => []
  },
  emptyText: {
    type: String,
    default: '暂无可领取的优惠券'
  }
})

defineEmits(['click-item', 'claim'])

const formatValueSimple = (coupon) => {
    if (coupon.type === 2 && coupon.discount) {
        return coupon.discount * 10
    }
    return coupon.amount
}


const formatCouponCondition = (coupon) => {
	if (!coupon) return ''
	if (coupon.condition) return coupon.condition
	
	const type = coupon.type
	const minAmount = coupon.minAmount || 0
	
	if (type === 3) {
		return '无门槛'
	} else if (minAmount > 0) {
		return `满${minAmount}可用`
	} else {
		return '无门槛'
	}
}

const getCouponBtnClass = (status) => {
	return {
		'btn-available': status === 'available',
		'btn-claimed': status === 'claimed',
		'btn-sold-out': status === 'sold_out',
		'btn-not-started': status === 'not_started'
	}
}

const getCouponBtnText = (status) => {
	if (status === 'available') return '领取'
	if (status === 'claimed') return '已领取'
	if (status === 'sold_out') return '已抢光'
	if (status === 'not_started') return '未开始'
	return '领取'
}
</script>

<style lang="scss" scoped>
.shop-coupon-list {
	padding: 0 20rpx 40rpx;
}

.list-title {
	font-size: 34rpx;
	font-weight: 800;
	color: #333;
	margin: 30rpx 0 24rpx 10rpx;
}

.coupon-row {
	display: flex;
	background: #fff;
	border-radius: 20rpx;
	margin-bottom: 24rpx;
	position: relative;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
	overflow: hidden;
    min-height: 180rpx;
    transition: transform 0.1s;
    
    &:active {
        transform: scale(0.99);
    }
}

.cr-left {
	width: 180rpx;
    background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
    position: relative;
    padding: 20rpx 0;
}

.amount-wrapper {
    display: flex;
    align-items: baseline;
    color: #ff4757;
}

.currency {
    font-size: 28rpx;
    font-weight: bold;
    margin-right: 4rpx;
}

.mc-amount {
    font-size: 52rpx;
	font-weight: bold;
    font-family: 'DIN Alternate', sans-serif;
}

.sep-line {
    position: absolute;
    right: 0;
    top: 20rpx;
    bottom: 20rpx;
    border-right: 2rpx dashed #ffccc7;
    width: 2rpx;
}

// 票据缺口
.punch-hole {
    position: absolute;
    width: 20rpx;
    height: 20rpx;
    background: #f5f6fa; // Match page bg
    border-radius: 50%;
    right: -10rpx;
    z-index: 2;
    
    &.top {
        top: -10rpx;
    }
    &.bottom {
        bottom: -10rpx;
    }
}

.cr-right {
	flex: 1;
	display: flex;
	justify-content: space-between;
	align-items: center;
    padding: 24rpx 30rpx;
    background: #fff;
}

.cr-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    margin-right: 20rpx;
}

.cr-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
    margin-bottom: 12rpx;
}

.shop-info {
    display: flex;
    align-items: center;
    margin-bottom: 12rpx;
}

.cr-logo {
	width: 32rpx;
	height: 32rpx;
	background: #f8f8f8;
	border-radius: 50%;
	margin-right: 8rpx;
}

.cr-name {
	font-size: 24rpx;
	color: #666;
    max-width: 240rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.cr-condition {
	font-size: 22rpx;
	color: #999;
}

.cr-action {
	display: flex;
	align-items: center;
}

.btn-wrapper {
    width: 140rpx;
    height: 56rpx;
}

.btn-icon {
	width: 100%;
	height: 100%;
}

.btn-get {
	width: 140rpx;
	height: 56rpx;
	line-height: 56rpx;
	border-radius: 28rpx;
	font-size: 24rpx;
    font-weight: bold;
	background: linear-gradient(90deg, #ff6b81, #ff4757);
	color: #fff;
	padding: 0;
    box-shadow: 0 4rpx 10rpx rgba(255, 71, 87, 0.3);
    border: none;
	
	&.btn-claimed {
		background: #ffecf0;
		color: #ff4757;
        box-shadow: none;
	}
	
	&.btn-sold-out {
		background: #ccc;
		color: #fff;
        box-shadow: none;
	}
}

.empty-center {
    text-align: center;
    padding: 100rpx 0;
    color: #999;
}
.empty-icon {
    font-size: 64rpx;
    display: block;
    margin-bottom: 20rpx;
}
</style>

