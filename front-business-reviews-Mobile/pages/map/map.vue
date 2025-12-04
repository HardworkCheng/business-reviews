<template>
	<view class="container">
		<view class="header">
			<view class="search-box">
				<input type="text" placeholder="搜索附近的店铺..." class="search-input" />
				<button class="location-btn clay-icon bg-primary" @click="getLocation">
					<text>📍</text>
				</button>
			</view>

			<view class="categories">
				<view 
					class="category" 
					:class="{ active: activeCategory === index }"
					v-for="(cat, index) in categories"
					:key="index"
					@click="selectCategory(index)"
				>
					<text>{{ cat }}</text>
				</view>
			</view>
		</view>

		<view class="map-container">
			<map 
				id="map" 
				:latitude="latitude" 
				:longitude="longitude"
				:markers="markers"
				:scale="scale"
				@markertap="onMarkerTap"
				class="map"
			></map>

			<view class="map-controls">
				<button class="control-btn clay-icon" @click="zoomIn">
					<text>+</text>
				</button>
				<button class="control-btn clay-icon" @click="zoomOut">
					<text>-</text>
				</button>
			</view>
		</view>

		<view v-if="selectedShop" class="shop-card clay-shadow" @click="goToShopDetail">
			<view class="drag-handle"></view>
			<view class="shop-content">
				<image :src="selectedShop.image" class="shop-image"></image>
				<view class="shop-info">
					<text class="shop-name">{{ selectedShop.name }}</text>
					<view class="rating">
						<text class="star">⭐</text>
						<text class="score">{{ selectedShop.rating }}</text>
						<text class="reviews">({{ selectedShop.reviews }}条)</text>
					</view>
					<view class="distance">
						<text class="icon">📍</text>
						<text>距您 {{ selectedShop.distance }}</text>
					</view>
					<view class="actions">
						<button class="action-btn bg-primary" @click.stop="navigate">
							<text>🧭</text>
							<text>导航</text>
						</button>
						<button class="action-btn detail-btn">查看详情</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const activeCategory = ref(0)
const categories = ref(['全部', '美食', 'KTV', '美容', 'SPA'])

const latitude = ref(30.2741)
const longitude = ref(120.1551)
const scale = ref(15)

const shops = ref([
	{
		id: 1,
		name: '蔡馬洪烤肉',
		rating: 4.6,
		reviews: 1460,
		distance: '170m',
		image: 'https://via.placeholder.com/200/FF9E64/FFFFFF?text=1',
		latitude: 30.2751,
		longitude: 120.1561
	},
	{
		id: 2,
		name: '羊老三火锅',
		rating: 4.4,
		reviews: 1363,
		distance: '243m',
		image: 'https://via.placeholder.com/200/FFD166/FFFFFF?text=2',
		latitude: 30.2731,
		longitude: 120.1571
	}
])

const markers = ref(shops.value.map((shop, index) => ({
	id: index,
	latitude: shop.latitude,
	longitude: shop.longitude,
	iconPath: '/static/marker.png',
	width: 30,
	height: 30,
	callout: {
		content: shop.name,
		color: '#000',
		fontSize: 12,
		borderRadius: 5,
		padding: 5,
		display: 'ALWAYS'
	}
})))

const selectedShop = ref(null)

onLoad(() => {
	console.log('Map page loaded')
})

const selectCategory = (index) => {
	activeCategory.value = index
}

const getLocation = () => {
	uni.getLocation({
		success: (res) => {
			latitude.value = res.latitude
			longitude.value = res.longitude
			uni.showToast({ title: '定位成功', icon: 'success' })
		},
		fail: () => {
			uni.showToast({ title: '定位失败', icon: 'none' })
		}
	})
}

const zoomIn = () => {
	if (scale.value < 18) scale.value++
}

const zoomOut = () => {
	if (scale.value > 3) scale.value--
}

const onMarkerTap = (e) => {
	const shopId = e.detail.markerId
	selectedShop.value = shops.value[shopId]
}

const navigate = () => {
	uni.showToast({ title: '开始导航', icon: 'none' })
}

const goToShopDetail = () => {
	if (selectedShop.value) {
		uni.navigateTo({
			url: `/pages/shop-detail/shop-detail?id=${selectedShop.value.id}`
		})
	}
}
</script>

<style lang="scss" scoped>
.container {
	height: 100vh;
	display: flex;
	flex-direction: column;
}

.header {
	background: white;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	z-index: 10;
}

.search-box {
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	gap: 15rpx;
}

.search-input {
	flex: 1;
	padding: 20rpx 30rpx;
	background: #f5f5f5;
	border-radius: 50rpx;
	font-size: 28rpx;
}

.location-btn {
	width: 70rpx;
	height: 70rpx;
	font-size: 32rpx;
	color: white;
}

.categories {
	display: flex;
	padding: 15rpx 30rpx;
	gap: 15rpx;
	overflow-x: auto;
}

.category {
	padding: 10rpx 30rpx;
	background: #f5f5f5;
	border-radius: 30rpx;
	font-size: 26rpx;
	white-space: nowrap;
}

.category.active {
	background: #FF9E64;
	color: white;
}

.map-container {
	flex: 1;
	position: relative;
}

.map {
	width: 100%;
	height: 100%;
}

.map-controls {
	position: absolute;
	right: 30rpx;
	top: 50%;
	transform: translateY(-50%);
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.control-btn {
	width: 80rpx;
	height: 80rpx;
	background: white;
	box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1);
	font-size: 40rpx;
	border: none;
	padding: 0;
}

.shop-card {
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
	background: white;
	border-radius: 40rpx 40rpx 0 0;
	padding: 30rpx;
}

.drag-handle {
	width: 80rpx;
	height: 8rpx;
	background: #ddd;
	border-radius: 4rpx;
	margin: 0 auto 30rpx;
}

.shop-content {
	display: flex;
	gap: 20rpx;
}

.shop-image {
	width: 150rpx;
	height: 150rpx;
	border-radius: 20rpx;
}

.shop-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.shop-name {
	font-size: 32rpx;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.rating {
	display: flex;
	align-items: center;
	margin-bottom: 10rpx;
	font-size: 24rpx;
}

.star {
	margin-right: 5rpx;
}

.score {
	font-weight: 500;
	margin-right: 5rpx;
}

.reviews {
	color: #999;
}

.distance {
	display: flex;
	align-items: center;
	font-size: 24rpx;
	color: #999;
	margin-bottom: 20rpx;
}

.distance .icon {
	color: #FF9E64;
	margin-right: 5rpx;
}

.actions {
	display: flex;
	gap: 15rpx;
}

.action-btn {
	flex: 1;
	padding: 15rpx;
	border-radius: 15rpx;
	font-size: 26rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 5rpx;
	border: none;
}

.action-btn.bg-primary {
	color: white;
}

.detail-btn {
	background: #f5f5f5;
	color: #333;
}
</style>
