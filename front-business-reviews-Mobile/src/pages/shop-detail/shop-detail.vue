<template>
	<view class="container">
		<view class="shop-header">
			<image :src="shopData.headerImage || 'https://via.placeholder.com/800x600/FF9E64/FFFFFF?text=Shop'" class="header-image" mode="aspectFill" @error="handleImageError"></image>
			<view class="header-overlay"></view>
			<view class="header-actions">
				<view class="action-btn" @click="goBack">
					<image src="/static/icons/back.png" class="icon-img" mode="aspectFit"></image>
				</view>
				<view class="action-btns-right">
					<view class="action-btn" @click="shareShop">
						<image src="/static/icons/share.png" class="icon-img" mode="aspectFit"></image>
					</view>
					<view class="action-btn" :class="{ favorited: isFavorited }" @click="toggleFavorite">
						<image :src="isFavorited ? '/static/icons/like-active.png' : '/static/icons/like.png'" class="icon-img" mode="aspectFit"></image>
					</view>
				</view>
			</view>
		</view>

		<view class="shop-info">
			<text class="shop-name">{{ shopData.name || '商家名称' }}</text>
			<view class="rating-section">
				<text class="rating-score">{{ shopData.rating || 0 }}</text>
				<text class="rating-stars">{{ getStarDisplay(shopData.rating) }}</text>
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
				<view class="icon-box">
					<image src="/static/icons/location.png" class="info-icon" mode="aspectFit"></image>
				</view>
				<view class="info-content">
					<text class="label">地址</text>
					<text class="value">{{ shopData.address || '暂无地址' }}</text>
				</view>
			</view>

			<view class="info-item">
				<view class="icon-box">
					<text style="font-size: 32rpx; line-height: 1;">🕐</text>
				</view>
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

		<!-- 商家相册 -->
		<view class="gallery-section" v-if="galleryImages.length > 0">
			<text class="section-title">商家相册 ({{ galleryImages.length }})</text>
			<scroll-view class="gallery-scroll" scroll-x>
				<view class="gallery-grid">
					<view v-for="(img, index) in galleryImages" :key="index" class="gallery-item" @click="previewGalleryImage(index)">
						<image :src="img" class="gallery-image" mode="aspectFill"></image>
					</view>
				</view>
			</scroll-view>
		</view>

		<view class="reviews-section">
			<text class="section-title">用户评价 ({{ shopData.reviewCount || 0 }})</text>
			
			<!-- 评价输入框 -->
			<view class="review-input-section">
				<view class="main-rating-box">
					<text class="rating-title">整体评价</text>
					<view class="stars-row big-stars">
						<text 
							v-for="star in 5" 
							:key="star" 
							class="star-btn"
							:class="{ active: star <= reviewForm.rating }"
							@click="setRating(star)"
						>★</text>
					</view>
				</view>
				
				<view class="sub-scores-list">
					<view class="sub-score-row">
						<text class="score-label">口味</text>
						<view class="stars-selection">
							<text 
								v-for="s in 5" 
								:key="s" 
								class="mini-star-btn"
								:class="{ active: s <= reviewForm.tasteScore }"
								@click="setSubScore('tasteScore', s)"
							>★</text>
						</view>
					</view>
					<view class="sub-score-row">
						<text class="score-label">环境</text>
						<view class="stars-selection">
							<text 
								v-for="s in 5" 
								:key="s" 
								class="mini-star-btn"
								:class="{ active: s <= reviewForm.environmentScore }"
								@click="setSubScore('environmentScore', s)"
							>★</text>
						</view>
					</view>
					<view class="sub-score-row">
						<text class="score-label">服务</text>
						<view class="stars-selection">
							<text 
								v-for="s in 5" 
								:key="s" 
								class="mini-star-btn"
								:class="{ active: s <= reviewForm.serviceScore }"
								@click="setSubScore('serviceScore', s)"
							>★</text>
						</view>
					</view>
				</view>
				
				<view class="textarea-wrapper">
					<textarea 
						class="review-textarea" 
						v-model="reviewForm.content" 
						placeholder="口味好不好，环境怎么样？写点什么吧..."
						maxlength="500"
						:disable-default-padding="true"
					></textarea>
				</view>
				
				<button class="submit-review-btn" @click="submitReview">发表评价</button>
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
			
			<!-- 加载更多评论 -->
			<view v-if="hasMoreReviews && reviews.length > 0" class="load-more-reviews" @click="loadMoreReviews">
				<text v-if="!reviewsLoading">点击加载更多评论</text>
				<text v-else>加载中...</text>
			</view>
			
			<!-- 没有更多评论 -->
			<view v-if="!hasMoreReviews && reviews.length > 0" class="no-more-reviews">
				<text>— 已加载全部评论 —</text>
			</view>
			
			<view v-if="reviews.length === 0" class="empty-reviews">
				<text>暂无评价，快来发表第一条评价吧！</text>
			</view>
		</view>
		
		<!-- 分享弹窗 -->
		<share-sheet 
			v-model:visible="showShareSheet"
			share-type="shop"
			:shop-id="shopId"
			:note-info="shareShopInfo"
			@close="showShareSheet = false"
			@share-success="handleShareSuccess"
		/>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getShopDetail, getShopReviews, favoriteShop, unfavoriteShop, postShopReview } from '../../api/shop'
import ShareSheet from '../../components/share-sheet/share-sheet.vue'

// 商家信息（从后端获取）
const shopData = ref({})
const isFavorited = ref(false)
const tags = ref([])
const shopId = ref(null)
// 评价列表（从后端获取）
const reviews = ref([])
// 商家相册图片
const galleryImages = ref([])
const loading = ref(false)

// 评论分页相关
const reviewsLoading = ref(false)
const hasMoreReviews = ref(true)
const reviewPageNum = ref(1)
const reviewPageSize = ref(10)
const totalReviews = ref(0)

// 分享相关
const showShareSheet = ref(false)
const shareShopInfo = computed(() => ({
	coverImage: shopData.value.headerImage || '',
	title: shopData.value.name || '商家详情',
	content: `${shopData.value.rating || 0}分 · ${shopData.value.reviewCount || 0}条评价`
}))

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
			// 解析商家相册图片
			galleryImages.value = processImages(result.images)
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

// 获取评价列表（首次加载）
const fetchReviews = async (id, reset = true) => {
	if (reviewsLoading.value) return
	
	reviewsLoading.value = true
	try {
		// 如果是重置，从第一页开始
		if (reset) {
			reviewPageNum.value = 1
			reviews.value = []
			hasMoreReviews.value = true
		}
		
		const result = await getShopReviews(id, reviewPageNum.value, reviewPageSize.value, 'latest')
		console.log('评价列表:', result)
		
		if (result && result.list) {
			const newReviews = result.list.map(item => ({
				id: item.id,
				avatar: item.userAvatar || 'https://via.placeholder.com/80x80/FF9E64/FFFFFF?text=U',
				author: item.username || '匿名用户',
				date: formatReviewDate(item.createdAt),
				content: item.content || '',
				rating: item.rating || 5
			}))
			
			// 追加到列表
			reviews.value = [...reviews.value, ...newReviews]
			
			// 更新总数
			if (result.total !== undefined) {
				totalReviews.value = result.total
				// 更新商家详情中的评价数量
				shopData.value.reviewCount = result.total
			}
			
			// 判断是否还有更多（优先使用后端返回的hasMore字段）
			if (result.hasMore !== undefined) {
				hasMoreReviews.value = result.hasMore
			} else {
				hasMoreReviews.value = newReviews.length >= reviewPageSize.value
			}
		} else {
			hasMoreReviews.value = false
		}
	} catch (e) {
		console.error('获取评价列表失败:', e)
	} finally {
		reviewsLoading.value = false
	}
}

// 加载更多评论
const loadMoreReviews = async () => {
	if (reviewsLoading.value || !hasMoreReviews.value) return
	
	reviewPageNum.value++
	await fetchReviews(shopId.value, false)
}

// 格式化评论日期
const formatReviewDate = (dateStr) => {
	if (!dateStr) return ''
	try {
		const date = new Date(dateStr)
		const now = new Date()
		const diff = now - date
		
		// 1分钟内
		if (diff < 60000) return '刚刚'
		// 1小时内
		if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
		// 24小时内
		if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
		// 30天内
		if (diff < 2592000000) return Math.floor(diff / 86400000) + '天前'
		// 更早
		return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
	} catch {
		return dateStr
	}
}

const goBack = () => {
	uni.navigateBack()
}

// 打开分享弹窗
const shareShop = () => {
	// 检查是否登录
	const token = uni.getStorageSync('token')
	if (!token) {
		uni.showToast({ title: '请先登录', icon: 'none' })
		setTimeout(() => {
			uni.navigateTo({ url: '/pages/login/login' })
		}, 1500)
		return
	}
	showShareSheet.value = true
}

// 分享成功回调
const handleShareSuccess = () => {
	console.log('店铺分享成功')
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

// 统一处理图片数据
const processImages = (images) => {
	if (Array.isArray(images)) {
		return images.filter(Boolean)
	}
	if (typeof images === 'string' && images) {
		try {
			const parsed = JSON.parse(images)
			return Array.isArray(parsed) ? parsed : images.split(',').filter(Boolean)
		} catch {
			return images.split(',').filter(Boolean)
		}
	}
	return []
}

// 处理图片加载错误
const handleImageError = (e) => {
	console.log('图片加载失败:', e)
}

// 预览商家相册图片
const previewGalleryImage = (index) => {
	uni.previewImage({
		urls: galleryImages.value,
		current: index
	})
}

// 设置评分
const setRating = (rating) => {
	reviewForm.value.rating = rating
}

// 设置子评分
const setSubScore = (type, score) => {
	reviewForm.value[type] = score
}

// 获取星星显示
const getStarDisplay = (rating) => {
	const r = Math.round(rating || 0)
	return '★'.repeat(r) + '☆'.repeat(5 - r)
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
	background: #F5F7FA;
	min-height: 100vh;
	padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.shop-header {
	position: relative;
	width: 100%;
	height: 560rpx;
}

.header-image {
	width: 100%;
	height: 100%;
}

.header-overlay {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 160rpx;
	background: linear-gradient(to bottom, rgba(0,0,0,0.3), transparent);
	pointer-events: none;
}

.header-actions {
	position: absolute;
	top: calc(var(--status-bar-height) + 20rpx);
	left: 0;
	right: 0;
	display: flex;
	justify-content: space-between;
	padding: 0 32rpx;
	z-index: 100;
}

.action-btns-right {
	display: flex;
	gap: 24rpx;
}

.action-btn {
	width: 72rpx;
	height: 72rpx;
	background: rgba(255, 255, 255, 0.95);
	backdrop-filter: blur(8px);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.1);
	transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-btn:active {
	transform: scale(0.92);
	background: rgba(255, 255, 255, 1);
}

.icon-img {
	width: 40rpx;
	height: 40rpx;
}

/* Shop Info Card */
.shop-info {
	position: relative;
	margin-top: -60rpx;
	background: white;
	border-radius: 40rpx 40rpx 0 0;
	padding: 40rpx 32rpx;
	z-index: 10;
	box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.02);
}

.shop-name {
	display: block;
	font-size: 44rpx;
	font-weight: 700;
	color: #1A1A1A;
	margin-bottom: 24rpx;
	line-height: 1.3;
}

.rating-section {
	display: flex;
	align-items: baseline;
	margin-bottom: 40rpx;
}

.rating-score {
	font-size: 56rpx;
	font-weight: 800;
	color: #FF8F1F; /* Vivid Orange */
	margin-right: 12rpx;
	line-height: 1;
}

.rating-stars {
	margin-right: 16rpx;
	font-size: 24rpx;
	color: #FF8F1F;
}

.review-count {
	font-size: 26rpx;
	color: #909399;
}

.rating-details {
	display: flex;
	justify-content: space-between;
	gap: 20rpx;
	margin-bottom: 40rpx;
}

.detail-item {
	flex: 1;
	background: #FFFBF5; /* Start with faint orange tint */
	padding: 24rpx 10rpx;
	border-radius: 20rpx;
	text-align: center;
	transition: transform 0.2s;
}

.detail-item:active {
	transform: scale(0.98);
}

.detail-item .label {
	display: block;
	font-size: 24rpx;
	color: #666;
	margin-bottom: 8rpx;
}

.detail-item .value {
	display: block;
	font-size: 34rpx;
	font-weight: 700;
	color: #333;
}

.text-primary {
	color: #333; /* Override original text-primary for card value, keep simple */
}

/* Info Items */
.info-item {
	display: flex;
	align-items: center;
	padding: 24rpx 0;
	border-bottom: 1rpx solid #F5F7FA;
}

.info-item:last-child {
	border-bottom: none;
}

.icon-box {
	width: 60rpx;
	height: 60rpx;
	background: #F0F4FF;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-right: 24rpx;
}

.info-item .icon {
	font-size: 36rpx; /* Fallback for emoji */
	display: flex;
	align-items: center;
	justify-content: center;
	width: 60rpx;
	margin-right: 24rpx;
}

.info-icon {
	width: 32rpx;
	height: 32rpx;
}

.info-content {
	flex: 1;
}

.info-content .label {
	display: block;
	font-size: 24rpx;
	color: #909399;
	margin-bottom: 4rpx;
}

.info-content .value {
	font-size: 28rpx;
	color: #303133;
	line-height: 1.5;
}

/* Tags */
.tags {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
	margin: 30rpx 0 40rpx;
}

.tag {
	padding: 10rpx 24rpx;
	background: #F2F3F5;
	border-radius: 8rpx;
	font-size: 24rpx;
	color: #606266;
}

/* Action Buttons */
.action-buttons {
	display: flex;
	gap: 24rpx;
	margin-top: 20rpx;
}

.btn {
	flex: 1;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 20rpx;
	font-size: 32rpx;
	font-weight: 600;
	border: none;
	position: relative;
	overflow: hidden;
}

.btn-primary {
	background: linear-gradient(135deg, #FF8F1F 0%, #FF7043 100%);
	color: white;
	box-shadow: 0 8rpx 20rpx rgba(255, 143, 31, 0.25);
}

.btn-primary:active {
	opacity: 0.9;
	transform: translateY(2rpx);
}

.btn-secondary {
	background: #fff;
	color: #333;
	border: 1rpx solid #E4E7ED;
	box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
}

.btn-secondary:active {
	background: #f9f9f9;
}

/* Gallery */
.gallery-section {
	background: white;
	margin: 24rpx 32rpx;
	padding: 32rpx;
	border-radius: 24rpx;
}

.section-title {
	font-size: 34rpx;
	font-weight: 700;
	color: #1A1A1A;
	margin-bottom: 24rpx;
	display: flex;
	align-items: center;
}

.section-title::before {
	content: '';
	display: block;
	width: 8rpx;
	height: 32rpx;
	background: #FF8F1F;
	border-radius: 4rpx;
	margin-right: 12rpx;
}

.gallery-scroll {
	width: 100%;
}

.gallery-grid {
	display: flex;
	gap: 16rpx;
}

.gallery-item {
	flex-shrink: 0;
	width: 220rpx;
	height: 220rpx;
	border-radius: 16rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 10rpx rgba(0,0,0,0.05);
}

.gallery-image {
	width: 100%;
	height: 100%;
}

/* Reviews */
.reviews-section {
	background: white;
	margin: 0 32rpx 32rpx;
	padding: 32rpx;
	border-radius: 24rpx;
}

.review-item {
	padding: 32rpx 0;
	border-bottom: 1rpx solid #F5F7FA;
}

.review-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 24rpx;
	border: 2rpx solid #fff;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
}

.review-author {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
}

.review-rating {
	font-size: 24rpx;
	color: #FF8F1F;
}

.review-text {
	font-size: 28rpx;
	color: #444;
	line-height: 1.6;
	margin-top: 12rpx;
}

.review-date {
	color: #C0C4CC;
	font-size: 24rpx;
}

/* Review Input */
.review-input-section {
	background: #fff;
	border-radius: 24rpx;
	padding: 40rpx 32rpx;
	margin-bottom: 40rpx;
	border: 1rpx solid #F0F2F5;
	box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.03);
}

.main-rating-box {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 40rpx;
}

.rating-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 20rpx;
}

.stars-row {
	display: flex;
	gap: 12rpx;
}

.star-btn {
	font-size: 64rpx;
	color: #E4E7ED;
	transition: all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275);
	line-height: 1;
}

.mini-star-btn {
	font-size: 40rpx;
	color: #E4E7ED;
	line-height: 1;
	padding: 0 4rpx;
}

.star-btn.active, .mini-star-btn.active {
	color: #FFB400;
	text-shadow: 0 2rpx 8rpx rgba(255, 180, 0, 0.3);
}

.star-btn:active {
	transform: scale(0.9);
}

.sub-scores-list {
	background: #F8F9FB;
	border-radius: 16rpx;
	padding: 24rpx 32rpx;
	margin-bottom: 32rpx;
}

.sub-score-row {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 16rpx 0;
	border-bottom: 1rpx dashed #E4E7ED;
}

.sub-score-row:last-child {
	border-bottom: none;
}

.score-label {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
	min-width: 100rpx;
}

.stars-selection {
	display: flex;
	gap: 20rpx;
}

.textarea-wrapper {
	background: #F8F9FB;
	border-radius: 16rpx;
	padding: 24rpx;
	margin-bottom: 32rpx;
	border: 1rpx solid #E4E7ED;
}

.review-textarea {
	width: 100%;
	height: 180rpx;
	font-size: 28rpx;
	color: #333;
	background: transparent;
	border: none;
	padding: 0;
	line-height: 1.5;
}

.submit-review-btn {
	background: linear-gradient(135deg, #FF8F1F 0%, #FF7043 100%);
	color: white;
	border-radius: 44rpx;
	font-size: 32rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 20rpx rgba(255, 143, 31, 0.25);
}

.submit-review-btn:active {
	opacity: 0.9;
	transform: translateY(2rpx);
}

.empty-reviews {
	text-align: center;
	padding: 60rpx 0;
	color: #C0C4CC;
}

.load-more-reviews {
	text-align: center;
	padding: 30rpx 0;
	color: #FF8F1F;
	font-size: 28rpx;
	cursor: pointer;
	
	&:active {
		opacity: 0.7;
	}
}

.no-more-reviews {
	text-align: center;
	padding: 30rpx 0;
	color: #C0C4CC;
	font-size: 24rpx;
}
</style>
