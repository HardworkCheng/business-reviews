<template>
	<view class="container">
		<view class="header">
			<view class="search-bar">
				<button class="back-btn" @click="goBack">←</button>
				<view class="location">
					<text>📍</text>
					<text>杭州</text>
				</view>
				<view class="search-input clay-border">
					<text>🔍</text>
					<input type="text" placeholder="搜索商户名或地点" v-model="keyword" />
				</view>
				<view class="user-icon clay-icon bg-primary">
					<text>👤</text>
				</view>
			</view>

			<view class="filters">
				<!-- 分类下拉 -->
				<view 
					class="filter-item"
					:class="{ active: selectedCategory !== '美食' }"
					@click="toggleCategoryDropdown"
				>
					<text>{{ selectedCategory }}</text>
					<text class="arrow" :class="{ 'arrow-up': showCategoryDropdown }">▼</text>
				</view>
				
				<!-- 距离排序 -->
				<view 
					class="filter-item"
					:class="{ active: sortField === 'distance' }"
					@click="toggleSortDropdown('distance')"
				>
					<text>距离</text>
					<text class="arrow" :class="{ 'arrow-up': showSortDropdown === 'distance' }">▼</text>
				</view>
				
				<!-- 人气排序 -->
				<view 
					class="filter-item"
					:class="{ active: sortField === 'popularity' }"
					@click="toggleSortDropdown('popularity')"
				>
					<text>人气</text>
					<text class="arrow" :class="{ 'arrow-up': showSortDropdown === 'popularity' }">▼</text>
				</view>
				
				<!-- 评分排序 -->
				<view 
					class="filter-item"
					:class="{ active: sortField === 'rating' }"
					@click="toggleSortDropdown('rating')"
				>
					<text>评分</text>
					<text class="arrow" :class="{ 'arrow-up': showSortDropdown === 'rating' }">▼</text>
				</view>
				
				<!-- 价格排序 -->
				<view 
					class="filter-item"
					:class="{ active: sortField === 'price' }"
					@click="toggleSortDropdown('price')"
				>
					<text>价格</text>
					<text class="arrow" :class="{ 'arrow-up': showSortDropdown === 'price' }">▼</text>
				</view>
			</view>
			
			<!-- 分类下拉菜单 -->
			<view class="dropdown-menu category-dropdown" v-show="showCategoryDropdown">
				<view 
					class="dropdown-item"
					v-for="(item, index) in categories" 
					:key="index"
					@click="selectCategory(item.name)"
					:class="{ 'active': selectedCategory === item.name }"
				>
					<text class="category-icon-small">{{ item.icon }}</text>
					<text class="dropdown-text">{{ item.name }}</text>
					<text v-if="selectedCategory === item.name" class="check-icon">✓</text>
				</view>
			</view>
			
			<!-- 排序下拉菜单 -->
			<view class="dropdown-menu sort-dropdown" v-show="showSortDropdown">
				<view 
					class="dropdown-item"
					@click="selectSort('asc')"
					:class="{ 'active': sortOrder === 'asc' }"
				>
					<text class="dropdown-text">从小到大</text>
					<text v-if="sortOrder === 'asc'" class="check-icon">✓</text>
				</view>
				<view 
					class="dropdown-item"
					@click="selectSort('desc')"
					:class="{ 'active': sortOrder === 'desc' }"
				>
					<text class="dropdown-text">从大到小</text>
					<text v-if="sortOrder === 'desc'" class="check-icon">✓</text>
				</view>
			</view>
		</view>

		<!-- 遮罩层 -->
		<view 
			class="mask" 
			v-show="showCategoryDropdown || showSortDropdown"
			@click="closeAllDropdowns"
		></view>
		
		<view class="shop-list">
			<view class="shop-item" v-for="(shop, index) in shopList" :key="index" @click="goToShopDetail(shop.id)">
				<image :src="shop.image" class="shop-image" mode="aspectFill"></image>
				<view class="shop-info">
					<text class="shop-name">{{ shop.name }}</text>
					<view class="rating">
						<text class="star">⭐</text>
						<text class="score">{{ shop.rating }}</text>
						<text class="reviews">({{ shop.reviews }}条评价)</text>
					</view>
					<view class="tags">
						<text class="tag" v-for="(tag, idx) in shop.tags" :key="idx">{{ tag }}</text>
					</view>
					<view class="location-info">
						<text class="location-icon">📍</text>
						<text class="distance">{{ shop.location }}，{{ shop.distance }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const keyword = ref('')

// 分类数据
const categories = ref([
	{ name: '美食', icon: '🍜' },
	{ name: 'KTV', icon: '🎤' },
	{ name: '丽人·美发', icon: '💇' },
	{ name: '美睫·美甲', icon: '💅' },
	{ name: '按摩·足疗', icon: '💆' },
	{ name: '美容SPA', icon: '🛁' },
	{ name: '亲子游乐', icon: '👶' },
	{ name: '酒吧', icon: '🍷' }
])

// 筛选和排序状态
const selectedCategory = ref('美食')
const showCategoryDropdown = ref(false)
const showSortDropdown = ref(null) // 'distance', 'popularity', 'rating', 'price'
const sortField = ref(null)
const sortOrder = ref('desc') // 'asc' or 'desc'

// 商家列表（从后端获取）
const shopList = ref([])

onLoad((options) => {
	if (options.category) {
		keyword.value = options.category
		selectedCategory.value = options.category
	}
	// TODO: 根据分类和筛选条件从后端API获取商家列表
	// fetchShopList(selectedCategory.value, sortField.value, sortOrder.value)
})

const goBack = () => {
	uni.navigateBack()
}

// 切换分类下拉框
const toggleCategoryDropdown = () => {
	showCategoryDropdown.value = !showCategoryDropdown.value
	showSortDropdown.value = null
}

// 切换排序下拉框
const toggleSortDropdown = (field) => {
	if (showSortDropdown.value === field) {
		showSortDropdown.value = null
	} else {
		showSortDropdown.value = field
		sortField.value = field
	}
	showCategoryDropdown.value = false
}

// 选择分类
const selectCategory = (category) => {
	selectedCategory.value = category
	showCategoryDropdown.value = false
	uni.showToast({
		title: `已切换到${category}`,
		icon: 'none'
	})
	// TODO: 根据分类筛选商家数据
}

// 选择排序方式
const selectSort = (order) => {
	sortOrder.value = order
	const orderText = order === 'asc' ? '从小到大' : '从大到小'
	const fieldText = {
		distance: '距离',
		popularity: '人气',
		rating: '评分',
		price: '价格'
	}[sortField.value]
	showSortDropdown.value = null
	uni.showToast({
		title: `按${fieldText}${orderText}`,
		icon: 'none'
	})
	// TODO: 根据排序方式排序商家数据
}

// 关闭所有下拉框
const closeAllDropdowns = () => {
	showCategoryDropdown.value = false
	showSortDropdown.value = null
}

const goToShopDetail = (id) => {
	uni.navigateTo({
		url: `/pages/shop-detail/shop-detail?id=${id}`
	})
}
</script>

<style lang="scss" scoped>
.container {
	background: #F7F9FC;
	min-height: 100vh;
}

.header {
	position: sticky;
	top: 0;
	z-index: 100;
	background: white;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.search-bar {
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	gap: 15rpx;
}

.back-btn {
	padding: 0;
	margin: 0;
	border: none;
	background: none;
	font-size: 36rpx;
	line-height: 1;
}

.location {
	display: flex;
	align-items: center;
	gap: 5rpx;
	font-size: 28rpx;
}

.search-input {
	flex: 1;
	display: flex;
	align-items: center;
	padding: 15rpx 25rpx;
	gap: 15rpx;
	background: white;
}

.search-input input {
	flex: 1;
	font-size: 28rpx;
}

.user-icon {
	width: 60rpx;
	height: 60rpx;
	font-size: 32rpx;
	color: white;
}

.filters {
	display: flex;
	padding: 15rpx 20rpx;
	gap: 15rpx;
	overflow-x: auto;
	border-bottom: 1rpx solid #F0F0F0;
}

.filter-item {
	position: relative;
	display: flex;
	align-items: center;
	gap: 6rpx;
	padding: 10rpx 30rpx;
	background: #f5f5f5;
	border-radius: 30rpx;
	font-size: 26rpx;
	white-space: nowrap;
	transition: all 0.3s;
}

.filter-item.active {
	background: #FF9E64;
	color: white;
	font-weight: 600;
}

.arrow {
	font-size: 20rpx;
	color: #999;
	transition: transform 0.3s;
}

.filter-item.active .arrow {
	color: white;
}

.arrow-up {
	transform: rotate(180deg);
}

/* 下拉菜单 */
.dropdown-menu {
	position: absolute;
	top: 136rpx;
	left: 0;
	right: 0;
	background: white;
	max-height: 500rpx;
	overflow-y: auto;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
	animation: slideDown 0.3s ease;
	z-index: 98;
}

@keyframes slideDown {
	from {
		opacity: 0;
		transform: translateY(-20rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

.category-dropdown {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
}

.dropdown-item {
	display: flex;
	align-items: center;
	padding: 30rpx 40rpx;
	border-bottom: 1rpx solid #F5F5F5;
	position: relative;
	transition: background 0.3s;
}

.dropdown-item:active {
	background: #F7F9FC;
}

.dropdown-item.active {
	background: #FFF5ED;
}

.category-icon-small {
	font-size: 36rpx;
	margin-right: 15rpx;
}

.dropdown-text {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.dropdown-item.active .dropdown-text {
	color: #FF9E64;
	font-weight: 600;
}

.check-icon {
	font-size: 32rpx;
	color: #FF9E64;
	font-weight: bold;
}

.sort-dropdown .dropdown-item {
	justify-content: space-between;
	padding: 35rpx 40rpx;
}

/* 遮罩层 */
.mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.3);
	z-index: 97;
	animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
	from {
		opacity: 0;
	}
	to {
		opacity: 1;
	}
}

.shop-list {
	padding: 0 30rpx;
}

.shop-item {
	display: flex;
	background: white;
	padding: 25rpx;
	margin-bottom: 20rpx;
	border-radius: 20rpx;
	box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.05);
}

.shop-image {
	width: 180rpx;
	height: 180rpx;
	border-radius: 20rpx;
	margin-right: 25rpx;
}

.shop-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.shop-name {
	font-size: 30rpx;
	font-weight: 500;
	margin-bottom: 10rpx;
}

.rating {
	display: flex;
	align-items: center;
	margin-bottom: 10rpx;
}

.star {
	margin-right: 5rpx;
}

.score {
	font-size: 28rpx;
	font-weight: 500;
	margin-right: 8rpx;
}

.reviews {
	font-size: 24rpx;
	color: #999;
}

.tags {
	display: flex;
	gap: 10rpx;
	margin-bottom: 10rpx;
	flex-wrap: wrap;
}

.tag {
	padding: 5rpx 15rpx;
	background: #f5f5f5;
	border-radius: 20rpx;
	font-size: 22rpx;
	color: #666;
}

.location-info {
	display: flex;
	align-items: center;
	font-size: 24rpx;
	color: #999;
}

.location-icon {
	color: #FF9E64;
	margin-right: 5rpx;
}
</style>
