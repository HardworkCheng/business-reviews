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
				<button class="btn btn-secondary" @click="callPhone">📞 电话</button>
			</view>
		</view>

		<view class="reviews-section">
			<text class="section-title">用户评价 ({{ shopData.reviewCount || 0 }})</text>
			
			<!-- 评价输入框 -->
			<view class="review-input-section clay-shadow">
				<view class="rating-input">
					<text class="rating-label">评分：</text>
					<view class="stars-input">
						<text 
							v-for="star in 5" 
							:key="star" 
							class="star-btn"
							:class="{ active: star <= reviewForm.rating }"
							@click="setRating(star)"
						>⭐</text>
					</view>
				</view>
				<view class="score-inputs">
					<view class="score-item">
						<text class="score-label">口味</text>
						<view class="score-btns">
							<text 
								v-for="s in 5" 
								:key="s" 
								class="mini-star"
								:class="{ active: s <= reviewForm.tasteScore }"
								@click="reviewForm.tasteScore = s"
							>⭐</text>
						</view>
					</view>
					<view class="score-item">
						<text class="score-label">环境</text>
						<view class="score-btns">
							<text 
								v-for="s in 5" 
								:key="s" 
								class="mini-star"
								:class="{ active: s <= reviewForm.environmentScore }"
								@click="reviewForm.environmentScore = s"
							>⭐</text>
						</view>
					</view>
					<view class="score-item">
						<text class="score-label">服务</text>
						<view class="score-btns">
							<text 
								v-for="s in 5" 
								:key="s" 
								class="mini-star"
								:class="{ active: s <= reviewForm.serviceScore }"
								@click="reviewForm.serviceScore = s"
							>⭐</text>
						</view>
					</view>
				</view>
				<textarea 
					class="review-textarea" 
					v-model="reviewForm.content" 
					placeholder="分享您的用餐体验..."
					maxlength="500"
				></textarea>
				<button class="submit-review-btn bg-primary" @click="submitReview">发表评价</button>
			</view>
			
			<!-- 评价列表 -->
			<view class="review-item" v-for="(review, index) in reviews" :key="index">
				<image :src="review.avatar" class="review-avatar"></image>
				<view class="review-content">
					<view class="review-header">
						<text class="review-author">{{ review.author }}</text>
						<text class="review-rating">{{ '⭐'.repeat(review.rating || 5) }}</text>
					</view>
					<text class="review-date">{{ review.date }}</text>
					<text class="review-text">{{ review.content }}</text>
				</view>
			</view>
			
			<view v-if="reviews.length === 0" class="empty-reviews">
				<text>暂无评价，快来发表第一条评价吧！</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getShopDetail, getShopReviews, favoriteShop, unfavoriteShop, postShopReview } from '../../api/shop'

// 商家信息（从后端获取）
const shopData = ref({})
const isFavorited = ref(false)
const tags = ref([])
const shopId = ref(null)
// 评价列表（从后端获取）
const reviews = ref([])
const loading = ref(false)

// 评价表单
const reviewForm = ref({
	rating: 5,
	tasteScore: 5,
	environmentScore: 5,
	serviceScore: 5,
	content: ''
})

onLoad((options) => {
	console.log('Shop detail loaded, id:', options.id)
	if (options.id) {
		shopId.value = options.id
		fetchShopDetail(options.id)
		fetchReviews(options.id)
	}
})

// 获取商家详情
const fetchShopDetail = async (id) => {
	loading.value = true
	try {
		const result = await getShopDetail(id)
		console.log('商家详情:', result)
		if (result) {
			shopData.value = result
			isFavorited.value = result.isFavorited || false
			// 处理标签
			if (result.category) {
				tags.value = [result.category]
			}
			if (result.averagePrice) {
				tags.value.push(`人均¥${result.averagePrice}`)
			}
		}
	} catch (e) {
		console.error('获取商家详情失败:', e)
		uni.showToast({
			title: '加载失败',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

// 获取评价列表
const fetchReviews = async (id) => {
	try {
		const result = await getShopReviews(id, 1, 10, 'latest')
		console.log('评价列表:', result)
		if (result && result.list) {
			reviews.value = result.list.map(item => ({
				avatar: item.userAvatar || '/static/default-avatar.png',
				author: item.username || '匿名用户',
				date: item.createdAt || '',
				content: item.content || '',
				rating: item.rating || 5
			}))
		}
	} catch (e) {
		console.error('获取评价列表失败:', e)
	}
}

const goBack = () => {
	uni.navigateBack()
}

const shareShop = () => {
	uni.showToast({ title: '分享店铺', icon: 'none' })
}

const toggleFavorite = async () => {
	if (!shopId.value) return
	
	try {
		if (isFavorited.value) {
			await unfavoriteShop(shopId.value)
			isFavorited.value = false
			uni.showToast({ title: '已取消收藏', icon: 'none' })
		} else {
			await favoriteShop(shopId.value)
			isFavorited.value = true
			uni.showToast({ title: '收藏成功', icon: 'none' })
		}
	} catch (e) {
		console.error('收藏操作失败:', e)
		uni.showToast({ title: '操作失败', icon: 'none' })
	}
}

// 拨打电话
const callPhone = () => {
	if (shopData.value.phone) {
		uni.makePhoneCall({
			phoneNumber: shopData.value.phone
		})
	} else {
		uni.showToast({ title: '暂无联系电话', icon: 'none' })
	}
}

// 设置评分
const setRating = (rating) => {
	reviewForm.value.rating = rating
}

// 提交评价
const submitReview = async () => {
	if (!shopId.value) {
		uni.showToast({ title: '商家信息错误', icon: 'none' })
		return
	}
	
	if (!reviewForm.value.content.trim()) {
		uni.showToast({ title: '请输入评价内容', icon: 'none' })
		return
	}
	
	const token = uni.getStorageSync('token')
	if (!token) {
		uni.showToast({ title: '请先登录', icon: 'none' })
		setTimeout(() => {
			uni.navigateTo({ url: '/pages/login/login' })
		}, 1500)
		return
	}
	
	try {
		await postShopReview(shopId.value, {
			rating: reviewForm.value.rating,
			tasteScore: reviewForm.value.tasteScore,
			environmentScore: reviewForm.value.environmentScore,
			serviceScore: reviewForm.value.serviceScore,
			content: reviewForm.value.content
		})
		
		uni.showToast({ title: '评价成功', icon: 'success' })
		
		// 清空表单
		reviewForm.value = {
			rating: 5,
			tasteScore: 5,
			environmentScore: 5,
			serviceScore: 5,
			content: ''
		}
		
		// 刷新评价列表
		fetchReviews(shopId.value)
	} catch (e) {
		console.error('提交评价失败:', e)
		uni.showToast({ title: e.message || '评价失败', icon: 'none' })
	}
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

.review-input-section {
	background: white;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
}

.rating-input {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.rating-label {
	font-size: 28rpx;
	margin-right: 20rpx;
}

.stars-input {
	display: flex;
	gap: 10rpx;
}

.star-btn {
	font-size: 40rpx;
	opacity: 0.3;
}

.star-btn.active {
	opacity: 1;
}

.score-inputs {
	display: flex;
	justify-content: space-between;
	margin-bottom: 20rpx;
}

.score-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.score-label {
	font-size: 24rpx;
	color: #666;
	margin-bottom: 10rpx;
}

.score-btns {
	display: flex;
}

.mini-star {
	font-size: 24rpx;
	opacity: 0.3;
}

.mini-star.active {
	opacity: 1;
}

.review-textarea {
	width: 100%;
	height: 200rpx;
	border: 1rpx solid #eee;
	border-radius: 15rpx;
	padding: 20rpx;
	font-size: 28rpx;
	margin-bottom: 20rpx;
	box-sizing: border-box;
}

.submit-review-btn {
	width: 100%;
	height: 80rpx;
	line-height: 80rpx;
	border-radius: 40rpx;
	color: white;
	font-size: 30rpx;
	border: none;
}

.empty-reviews {
	text-align: center;
	padding: 60rpx 0;
	color: #999;
	font-size: 28rpx;
}
</style>
