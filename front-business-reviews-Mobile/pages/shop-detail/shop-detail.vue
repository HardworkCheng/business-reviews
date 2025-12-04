<template>
	<view class="container">
		<view class="shop-header">
			<image :src="shopData.headerImage || 'https://via.placeholder.com/800x600/FF9E64/FFFFFF?text=Shop'" class="header-image" mode="aspectFill"></image>
			<view class="header-actions">
				<view class="action-btn clay-icon" @click="goBack">
					<text>←</text>
				</view>
				<view class="action-btns-right">
					<view class="action-btn clay-icon" @click="shareShop">
						<text>📤</text>
					</view>
					<view class="action-btn clay-icon" :class="{ favorited: isFavorited }" @click="toggleFavorite">
						<text>❤️</text>
					</view>
				</view>
			</view>
		</view>

		<view class="shop-info">
			<text class="shop-name">{{ shopData.name || '商家名称' }}</text>
			<view class="rating-section">
				<text class="rating-score text-primary">{{ shopData.rating || 0 }}</text>
				<text class="rating-stars">⭐⭐⭐⭐⭐</text>
				<text class="review-count">{{ shopData.reviewCount || 0 }}条评价</text>
			</view>

			<view class="rating-details">
				<view class="detail-item">
					<text class="label">口味</text>
					<text class="value text-primary">{{ shopData.tasteScore || 0 }}</text>
				</view>
				<view class="detail-item">
					<text class="label">环境</text>
					<text class="value text-primary">{{ shopData.environmentScore || 0 }}</text>
				</view>
				<view class="detail-item">
					<text class="label">服务</text>
					<text class="value text-primary">{{ shopData.serviceScore || 0 }}</text>
				</view>
			</view>

			<view class="info-item">
				<text class="icon">📍</text>
				<view class="info-content">
					<text class="label">地址</text>
					<text class="value">{{ shopData.address || '暂无地址' }}</text>
				</view>
			</view>

			<view class="info-item">
				<text class="icon">🕐</text>
				<view class="info-content">
					<text class="label">营业时间</text>
					<text class="value">{{ shopData.businessHours || '暂无营业时间' }}</text>
				</view>
			</view>

			<view class="tags">
				<text class="tag" v-for="(tag, index) in tags" :key="index">{{ tag }}</text>
			</view>

			<view class="action-buttons">
				<button class="btn btn-primary bg-primary">🍽️ 点餐</button>
				<button class="btn btn-secondary">📞 电话</button>
			</view>
		</view>

		<view class="reviews-section">
			<text class="section-title">用户评价 ({{ shopData.reviewCount || 0 }})</text>
			<view class="review-item" v-for="(review, index) in reviews" :key="index">
				<image :src="review.avatar" class="review-avatar"></image>
				<view class="review-content">
					<view class="review-header">
						<text class="review-author">{{ review.author }}</text>
						<text class="review-rating">⭐⭐⭐⭐⭐</text>
					</view>
					<text class="review-date">{{ review.date }}</text>
					<text class="review-text">{{ review.content }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

// 商家信息（从后端获取）
const shopData = ref({})
const isFavorited = ref(false)
const tags = ref([])
// 评价列表（从后端获取）
const reviews = ref([])

onLoad((options) => {
	console.log('Shop detail loaded, id:', options.id)
	// TODO: 根据shopId从后端API获取商家详情
	// fetchShopDetail(options.id)
	// TODO: 获取商家评价列表
	// fetchReviews(options.id)
})

const goBack = () => {
	uni.navigateBack()
}

const shareShop = () => {
	uni.showToast({ title: '分享店铺', icon: 'none' })
}

const toggleFavorite = () => {
	isFavorited.value = !isFavorited.value
}
</script>

<style lang="scss" scoped>
.container {
	background: #F7F9FC;
	min-height: 100vh;
	padding-bottom: 100rpx;
}

.shop-header {
	position: relative;
	width: 100%;
	height: 500rpx;
}

.header-image {
	width: 100%;
	height: 100%;
}

.header-actions {
	position: absolute;
	top: 30rpx;
	left: 0;
	right: 0;
	display: flex;
	justify-content: space-between;
	padding: 0 30rpx;
}

.action-btns-right {
	display: flex;
	gap: 20rpx;
}

.action-btn {
	width: 70rpx;
	height: 70rpx;
	background: rgba(255, 255, 255, 0.8);
	backdrop-filter: blur(10rpx);
	font-size: 32rpx;
}

.action-btn.favorited {
	background: #EF476F;
	color: white;
}

.shop-info {
	background: white;
	padding: 30rpx;
}

.shop-name {
	display: block;
	font-size: 40rpx;
	font-weight: bold;
	margin-bottom: 20rpx;
}

.rating-section {
	display: flex;
	align-items: center;
	margin-bottom: 25rpx;
}

.rating-score {
	font-size: 48rpx;
	font-weight: bold;
	margin-right: 15rpx;
}

.rating-stars {
	margin-right: 15rpx;
	font-size: 24rpx;
}

.review-count {
	font-size: 26rpx;
	color: #999;
}

.rating-details {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 20rpx;
	margin-bottom: 30rpx;
}

.detail-item {
	background: #f5f5f5;
	padding: 20rpx;
	border-radius: 15rpx;
	text-align: center;
}

.detail-item .label {
	display: block;
	font-size: 26rpx;
	margin-bottom: 8rpx;
}

.detail-item .value {
	display: block;
	font-size: 32rpx;
	font-weight: bold;
}

.info-item {
	display: flex;
	margin-bottom: 25rpx;
}

.info-item .icon {
	font-size: 36rpx;
	margin-right: 20rpx;
	margin-top: 5rpx;
}

.info-content {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.info-content .label {
	font-size: 28rpx;
	font-weight: 500;
	margin-bottom: 8rpx;
}

.info-content .value {
	font-size: 26rpx;
	color: #666;
}

.tags {
	display: flex;
	flex-wrap: wrap;
	gap: 15rpx;
	margin-bottom: 30rpx;
}

.tag {
	padding: 10rpx 25rpx;
	background: #f5f5f5;
	border-radius: 30rpx;
	font-size: 24rpx;
}

.action-buttons {
	display: flex;
	gap: 20rpx;
}

.btn {
	flex: 1;
	padding: 25rpx;
	border-radius: 20rpx;
	font-size: 32rpx;
	font-weight: 500;
	border: none;
}

.btn-primary {
	color: white;
	box-shadow: 10rpx 10rpx 0rpx rgba(0, 0, 0, 0.1);
}

.btn-secondary {
	background: white;
	border: 3rpx solid #000;
	box-shadow: 10rpx 10rpx 0rpx rgba(0, 0, 0, 0.1);
}

.reviews-section {
	background: white;
	margin-top: 20rpx;
	padding: 30rpx;
}

.section-title {
	display: block;
	font-size: 36rpx;
	font-weight: bold;
	margin-bottom: 30rpx;
}

.review-item {
	display: flex;
	margin-bottom: 30rpx;
	padding-bottom: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.review-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.review-content {
	flex: 1;
}

.review-header {
	display: flex;
	justify-content: space-between;
	margin-bottom: 10rpx;
}

.review-author {
	font-size: 28rpx;
	font-weight: 500;
}

.review-rating {
	font-size: 24rpx;
	color: #FFD166;
}

.review-date {
	display: block;
	font-size: 24rpx;
	color: #999;
	margin-bottom: 15rpx;
}

.review-text {
	display: block;
	font-size: 28rpx;
	line-height: 1.6;
}
</style>
