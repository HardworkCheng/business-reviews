<template>
	<view class="container">
		<!-- 头部导航栏 -->
		<view class="header">
			<view class="nav-bar">
				<text class="nav-title">优惠券</text>
			</view>
			
			<!-- 分类标签 -->
			<view class="tabs">
				<view 
					class="tab-item" 
					:class="{ active: currentTab === index }"
					v-for="(tab, index) in tabs"
					:key="index"
					@click="switchTab(index)"
				>
					<text>{{ tab }}</text>
				</view>
			</view>
		</view>

		<!-- 优惠券列表 -->
		<scroll-view class="coupon-list" scroll-y>
			<view 
				class="coupon-card clay-shadow" 
				v-for="(coupon, index) in filteredCoupons" 
				:key="index"
				@click="handleCouponClick(coupon)"
			>
				<view class="coupon-left" :class="getCouponClass(coupon.type)">
					<view class="coupon-amount">
						<text class="amount-symbol">￥</text>
						<text class="amount-value">{{ coupon.amount }}</text>
					</view>
					<text class="coupon-condition">{{ coupon.condition }}</text>
				</view>
				
				<view class="coupon-right">
					<view class="coupon-info">
						<text class="coupon-title">{{ coupon.title }}</text>
						<text class="coupon-desc">{{ coupon.description }}</text>
						<view class="coupon-time">
							<text class="time-icon">🕒</text>
							<text>{{ coupon.validTime }}</text>
						</view>
					</view>
					
					<button 
						class="coupon-btn" 
						:class="getCouponBtnClass(coupon.status)"
						@click.stop="receiveCoupon(coupon)"
						v-if="coupon.status !== 'used'"
					>
						{{ getCouponBtnText(coupon.status) }}
					</button>
					<view v-else class="used-tag">已使用</view>
				</view>
				
				<!-- 装饰圆点 -->
				<view class="circle circle-left"></view>
				<view class="circle circle-right"></view>
			</view>
			
			<!-- 空状态 -->
			<view v-if="filteredCoupons.length === 0" class="empty">
				<text class="empty-icon">🎟️</text>
				<text class="empty-text">暂无优惠券</text>
			</view>
		</scroll-view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const currentTab = ref(0)
const tabs = ref(['全部', '未使用', '已使用', '已过期'])

// 模拟优惠券数据
const coupons = ref([
	{
		id: 1,
		type: 'discount', // 折扣券
		amount: '8.8',
		condition: '满100元可用',
		title: '全场通用8.8折',
		description: '适用于全部商家',
		validTime: '有效期至2025.12.31',
		status: 'available' // available, received, used, expired
	},
	{
		id: 2,
		type: 'cash', // 现金券
		amount: '20',
		condition: '满150元可用',
		title: '美食专享券',
		description: '仅限美食类商家使用',
		validTime: '有效期至2025.12.25',
		status: 'available'
	},
	{
		id: 3,
		type: 'special', // 特殊券
		amount: '50',
		condition: '满200元可用',
		title: 'KTV专属优惠',
		description: '仅限KTV类商家使用',
		validTime: '有效期至2025.12.20',
		status: 'received'
	},
	{
		id: 4,
		type: 'cash',
		amount: '30',
		condition: '满180元可用',
		title: 'SPA体验券',
		description: '适用于美容SPA商家',
		validTime: '有效期至2025.11.30',
		status: 'used'
	},
	{
		id: 5,
		type: 'discount',
		amount: '9',
		condition: '满80元可用',
		title: '新人专享9折',
		description: '新用户首次下单使用',
		validTime: '有效期至2025.11.15',
		status: 'expired'
	}
])

// 筛选后的优惠券
const filteredCoupons = computed(() => {
	if (currentTab.value === 0) {
		return coupons.value // 全部
	} else if (currentTab.value === 1) {
		return coupons.value.filter(c => c.status === 'available' || c.status === 'received')
	} else if (currentTab.value === 2) {
		return coupons.value.filter(c => c.status === 'used')
	} else if (currentTab.value === 3) {
		return coupons.value.filter(c => c.status === 'expired')
	}
	return coupons.value
})

onLoad(() => {
	console.log('Coupon page loaded')
})

const switchTab = (index) => {
	currentTab.value = index
}

const getCouponClass = (type) => {
	return {
		'coupon-discount': type === 'discount',
		'coupon-cash': type === 'cash',
		'coupon-special': type === 'special'
	}
}

const getCouponBtnClass = (status) => {
	return {
		'btn-available': status === 'available',
		'btn-received': status === 'received',
		'btn-expired': status === 'expired'
	}
}

const getCouponBtnText = (status) => {
	if (status === 'available') return '领取'
	if (status === 'received') return '去使用'
	if (status === 'expired') return '已过期'
	return '领取'
}

const receiveCoupon = (coupon) => {
	if (coupon.status === 'available') {
		uni.showToast({
			title: '领取成功',
			icon: 'success'
		})
		coupon.status = 'received'
	} else if (coupon.status === 'received') {
		uni.showToast({
			title: '跳转到使用页面',
			icon: 'none'
		})
	} else if (coupon.status === 'expired') {
		uni.showToast({
			title: '优惠券已过期',
			icon: 'none'
		})
	}
}

const handleCouponClick = (coupon) => {
	console.log('Coupon clicked:', coupon)
	// 未来可以跳转到优惠券详情页
}
</script>

<style lang="scss" scoped>
.container {
	background: #F7F9FC;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
}

.header {
	background: white;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	position: sticky;
	top: 0;
	z-index: 100;
}

.nav-bar {
	padding: 30rpx;
	text-align: center;
}

.nav-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.tabs {
	display: flex;
	padding: 20rpx 30rpx;
	border-top: 1rpx solid #f0f0f0;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 15rpx 0;
	font-size: 28rpx;
	color: #666;
	position: relative;
}

.tab-item.active {
	color: #FF9E64;
	font-weight: 500;
}

.tab-item.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 40rpx;
	height: 6rpx;
	background: #FF9E64;
	border-radius: 3rpx;
}

.coupon-list {
	flex: 1;
	padding: 30rpx;
}

.coupon-card {
	background: white;
	border-radius: 20rpx;
	margin-bottom: 30rpx;
	display: flex;
	position: relative;
	overflow: hidden;
}

.coupon-left {
	width: 220rpx;
	padding: 40rpx 20rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	position: relative;
}

.coupon-discount {
	background: linear-gradient(135deg, #FF9E64 0%, #FFB787 100%);
}

.coupon-cash {
	background: linear-gradient(135deg, #EF476F 0%, #F77694 100%);
}

.coupon-special {
	background: linear-gradient(135deg, #06D6A0 0%, #26E7AB 100%);
}

.coupon-amount {
	display: flex;
	align-items: baseline;
	color: white;
	margin-bottom: 10rpx;
}

.amount-symbol {
	font-size: 32rpx;
	font-weight: bold;
}

.amount-value {
	font-size: 72rpx;
	font-weight: bold;
}

.coupon-condition {
	font-size: 22rpx;
	color: rgba(255, 255, 255, 0.9);
}

.coupon-right {
	flex: 1;
	padding: 30rpx;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.coupon-info {
	flex: 1;
}

.coupon-title {
	display: block;
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
	margin-bottom: 10rpx;
}

.coupon-desc {
	display: block;
	font-size: 24rpx;
	color: #999;
	margin-bottom: 15rpx;
}

.coupon-time {
	display: flex;
	align-items: center;
	font-size: 22rpx;
	color: #999;
}

.time-icon {
	margin-right: 5rpx;
}

.coupon-btn {
	width: 140rpx;
	height: 60rpx;
	border-radius: 30rpx;
	font-size: 24rpx;
	border: none;
	margin-top: 20rpx;
	align-self: flex-end;
}

.btn-available {
	background: #FF9E64;
	color: white;
}

.btn-received {
	background: #f0f0f0;
	color: #666;
}

.btn-expired {
	background: #f0f0f0;
	color: #999;
}

.used-tag {
	width: 140rpx;
	height: 60rpx;
	line-height: 60rpx;
	text-align: center;
	background: #f0f0f0;
	color: #999;
	border-radius: 30rpx;
	font-size: 24rpx;
	margin-top: 20rpx;
	align-self: flex-end;
}

.circle {
	position: absolute;
	width: 24rpx;
	height: 24rpx;
	background: #F7F9FC;
	border-radius: 50%;
	top: 50%;
	transform: translateY(-50%);
}

.circle-left {
	left: 210rpx;
	margin-left: -12rpx;
}

.circle-right {
	right: 0;
	margin-right: -12rpx;
}

.empty {
	text-align: center;
	padding: 150rpx 0;
}

.empty-icon {
	display: block;
	font-size: 100rpx;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

.clay-shadow {
	box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.08);
}
</style>
