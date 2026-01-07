<template>
  <view class="my-coupon-container">
    <!-- 状态筛选标签 -->
    <view class="wallet-tabs">
      <view 
        class="w-tab" 
        :class="{ active: currentTab === 'all' }"
        @click="switchTab('all')"
      >全部</view>
      <view 
        class="w-tab" 
        :class="{ active: currentTab === 'unused' }"
        @click="switchTab('unused')"
      >未使用</view>
      <view 
        class="w-tab" 
        :class="{ active: currentTab === 'used' }"
        @click="switchTab('used')"
      >已使用</view>
      <view 
        class="w-tab" 
        :class="{ active: currentTab === 'expired' }"
        @click="switchTab('expired')"
      >已过期</view>
    </view>

    <!-- 我的优惠券列表 -->
    <view class="my-coupon-list">
      <view 
        class="my-card" 
        v-for="coupon in coupons" 
        :key="coupon.id"
        :class="getCardClass(coupon.status)"
        @click="handleMyCouponClick(coupon)"
      >
        <view class="mc-left">
          <view class="amount-wrapper">
             <text class="currency" v-if="!isDiscount(coupon)">¥</text>
             <text class="mc-amount">{{ formatCouponNumber(coupon) }}</text>
             <text class="unit" v-if="isDiscount(coupon)">折</text>
          </view>
          <text class="mc-label">{{ getCouponTypeLabel(coupon.type) }}</text>
          <!-- 装饰半圆 -->
          <view class="punch-hole top"></view>
          <view class="punch-hole bottom"></view>
        </view>
        
        <view class="sep-line"></view>

        <view class="mc-right">
          <view class="mc-header">
             <text class="mc-title">{{ coupon.title }}</text>
          </view>
          
          <view class="mc-shop">
            <image 
                :src="coupon.shopLogo || '/static/images/default-shop.png'" 
                class="shop-mini-logo" 
                mode="aspectFill"
            ></image>
            <text class="shop-name-text">{{ coupon.shopName || '全部商家' }}</text>
          </view>
          
          <view class="mc-footer">
            <text class="mc-time">有效期至 {{ formatDate(coupon.expireTime) }}</text>
            
            <!-- 未使用状态显示去使用按钮 -->
            <button 
                v-if="coupon.status === 'unused'" 
                class="btn-use"
                @click.stop="useCoupon(coupon)"
            >去使用</button>
          </view>
          
          <!-- 已使用/已过期状态显示标记 -->
          <image 
            v-if="coupon.status !== 'unused'" 
            :src="coupon.status === 'used' ? '/static/icons/seal-used.png' : '/static/icons/seal-expired.png'" 
            class="status-seal-img"
            mode="aspectFit"
          />
          <view v-else-if="coupon.status !== 'unused'" class="status-seal-text">
            {{ coupon.status === 'used' ? '已使用' : '已过期' }}
          </view>
        </view>
      </view>

      <!-- 空状态 -->
      <view v-if="coupons.length === 0" class="empty-state">
        <text class="empty-icon">📭</text>
        <text class="empty-text">券包空空如也，快去抢神券！</text>
        <button class="empty-btn" @click="$emit('go-center')">去领券中心</button>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  coupons: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['tab-change', 'use-coupon', 'go-center'])

const currentTab = ref('all')

const switchTab = (tab) => {
  currentTab.value = tab
  emit('tab-change', tab)
}

const isDiscount = (coupon) => {
    return coupon.type === 2 && coupon.discount
}

const formatCouponNumber = (coupon) => {
	if (!coupon) return ''
	if (isDiscount(coupon)) {
		const discountValue = coupon.discount * 10
		return discountValue % 1 === 0 ? `${discountValue}` : `${discountValue.toFixed(1)}`
	} else if (coupon.amount) {
		return `${coupon.amount}`
	}
	return ''
}

const formatDate = (dateStr) => {
    if (!dateStr) return ''
    try {
        const date = new Date(dateStr)
        const y = date.getFullYear()
        const m = String(date.getMonth() + 1).padStart(2, '0')
        const d = String(date.getDate()).padStart(2, '0')
        // const h = String(date.getHours()).padStart(2, '0')
        // const min = String(date.getMinutes()).padStart(2, '0')
        return `${y}.${m}.${d}` // 简洁日期格式
    } catch (e) {
        return dateStr || ''
    }
}

const getCardClass = (status) => {
    return {
        'status-unused': status === 'unused',
        'status-used': status === 'used',
        'status-expired': status === 'expired'
    }
}


const getCouponTypeLabel = (type) => {
	switch (type) {
		case 1: return '满减券'
		case 2: return '折扣券'
		case 3: return '代金券'
		default: return '优惠券'
	}
}

const handleMyCouponClick = (coupon) => {
	if (coupon.status === 'unused') {
		uni.showModal({
			title: '优惠券码',
			content: coupon.code,
			showCancel: false,
			confirmText: '复制券码',
			success: (res) => {
				if (res.confirm) {
					uni.setClipboardData({
						data: coupon.code,
						success: () => {
							uni.showToast({ title: '券码已复制', icon: 'success' })
						}
					})
				}
			}
		})
	}
}

const useCoupon = (coupon) => {
	if (coupon.shopId) {
		uni.navigateTo({
			url: `/pages/shop-detail/shop-detail?id=${coupon.shopId}`
		})
	} else {
		uni.showToast({ title: '该优惠券适用于全部商家', icon: 'none' })
	}
}
</script>

<style lang="scss" scoped>
.wallet-tabs {
	display: flex;
	justify-content: space-around;
	background: #f5f6fa; 
	padding: 24rpx 20rpx;
	position: sticky;
	top: 0;
	z-index: 10;
}

.w-tab {
	font-size: 28rpx;
	color: #666;
	padding: 12rpx 32rpx;
	border-radius: 34rpx;
	transition: all 0.3s;
    background: transparent;
	
	&.active {
		color: #fff;
		background: #ff4757;
		font-weight: 600;
        box-shadow: 0 4rpx 12rpx rgba(255, 71, 87, 0.3);
	}
}

.my-coupon-list {
	padding: 0 24rpx 40rpx;
}

.my-card {
	display: flex;
	background: #fff;
	border-radius: 16rpx;
	margin-bottom: 24rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
	position: relative;
	min-height: 180rpx;
    transition: transform 0.1s;
    
    &:active {
        transform: scale(0.99);
    }
	
	&.status-unused {
        .mc-left {
            background: linear-gradient(135deg, #fffff0 0%, #fff5f5 100%);
            border-right: none;
        }
        .mc-amount, .currency, .unit {
            color: #ff4757;
        }
        .mc-label {
            color: #ff6b81;
            background: rgba(255, 71, 87, 0.08);
        }
        .sep-line {
            border-left: 2rpx dashed #ffccc7;
        }
    }
    
    &.status-used, &.status-expired {
        .mc-left {
            background: #f7f8fa;
        }
        .mc-amount, .currency, .unit, .mc-label {
            color: #bdc3c7;
        }
        .mc-label {
            background: rgba(0,0,0,0.03);
        }
        .mc-title {
            color: #999;
        }
        .sep-line {
            border-left: 2rpx dashed #ddd;
        }
    }
}

.mc-left {
	width: 200rpx;
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
}

.currency {
    font-size: 28rpx;
    font-weight: bold;
    margin-right: 4rpx;
}

.mc-amount {
	font-size: 56rpx;
	font-weight: bold;
    font-family: 'DIN Alternate', sans-serif; // 数码字体感
}

.unit {
    font-size: 24rpx;
    font-weight: bold;
    margin-left: 4rpx;
}

.mc-label {
	font-size: 22rpx;
	margin-top: 12rpx;
    padding: 4rpx 16rpx;
    border-radius: 20rpx;
}

.sep-line {
    width: 2rpx;
    position: relative;
    background: transparent;
    border-left: 2rpx dashed #eee;
    margin: 20rpx 0;
}

// 票据缺口
.punch-hole {
    position: absolute;
    width: 24rpx;
    height: 24rpx;
    background: #f5f6fa; // 必须与页面背景色一致
    border-radius: 50%;
    right: -12rpx;
    z-index: 2;
    
    &.top {
        top: -12rpx;
    }
    &.bottom {
        bottom: -12rpx;
    }
}

.mc-right {
	flex: 1;
	padding: 24rpx 30rpx;
	display: flex;
	flex-direction: column;
	position: relative;
    background: #fff;
    justify-content: space-between;
}

.mc-header {
    margin-bottom: 12rpx;
}

.mc-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
    line-height: 1.4;
}

.mc-shop {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
}

.shop-mini-logo {
    width: 32rpx;
    height: 32rpx;
    border-radius: 50%;
    margin-right: 8rpx;
    background: #eee;
}

.shop-name-text {
    font-size: 24rpx;
    color: #666;
}

.mc-footer {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
}

.mc-time {
	font-size: 22rpx;
	color: #999;
}

.btn-use {
	font-size: 24rpx;
	color: #fff;
	background: linear-gradient(90deg, #ff6b81, #ff4757);
	padding: 0 32rpx;
	height: 52rpx;
	line-height: 52rpx;
	border-radius: 26rpx;
    margin: 0;
    box-shadow: 0 4rpx 10rpx rgba(255, 71, 87, 0.25);
    border: none;
    
    &:active {
        opacity: 0.9;
    }
}

.status-seal-img {
	position: absolute;
	right: 20rpx;
	bottom: 20rpx;
    width: 100rpx;
    height: 100rpx;
	opacity: 0.8;
    transform: rotate(-15deg);
}

.status-seal-text {
    position: absolute;
	right: 30rpx;
	bottom: 30rpx;
    font-size: 28rpx;
    color: #ccc;
    border: 2rpx solid #ccc;
    padding: 4rpx 10rpx;
    border-radius: 8rpx;
    transform: rotate(-15deg);
}


.empty-state {
	padding: 100rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 30rpx;
}

.empty-text {
	color: #999;
	font-size: 28rpx;
	margin-bottom: 40rpx;
}

.empty-btn {
	background: #ff4757;
	color: #fff;
	font-size: 28rpx;
	padding: 0 60rpx;
	border-radius: 40rpx;
}
</style>
