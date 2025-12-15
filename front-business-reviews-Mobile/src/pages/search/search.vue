<template>
	<view class="container">
		<view class="header">
			<view class="search-bar">
				<button class="back-btn" @click="goBack">←</button>
				<view class="location" @click="goToCitySelect">
					<text class="location-icon">📍</text>
					<text class="location-text">{{ currentCity }}</text>
				</view>
				<view class="search-input-wrapper">
					<view class="search-input">
						<text class="search-icon">🔍</text>
						<input 
							type="text" 
							placeholder="搜索商户名或地点" 
							v-model="keyword"
							@confirm="handleSearch"
						/>
					</view>
				</view>
				<image 
					v-if="userAvatar" 
					:src="userAvatar" 
					class="user-avatar"
					mode="aspectFill"
					@click="goToProfile"
				></image>
				<view v-else class="user-icon" @click="goToProfile">
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
						<view class="rating-stars">
							<text class="star">⭐</text>
							<text class="score">{{ shop.rating }}</text>
						</view>
						<text class="reviews">({{ shop.reviews }}条评价)</text>
					</view>
					<view class="tags">
						<text class="tag" v-for="(tag, idx) in shop.tags" :key="idx">{{ tag }}</text>
						<text class="tag price-tag">人均 ¥{{ shop.avgPrice || 85 }}</text>
					</view>
					<view class="location-info">
						<text class="location-icon">📍</text>
						<text class="distance">{{ shop.location }}{{ shop.distance ? '，' + shop.distance : '' }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getShopList } from '../../api/shop'
import { getUserInfo } from '../../api/user'

const keyword = ref('')
const currentCity = ref('定位中...')
const userAvatar = ref('')

// 分类数据
const categories = ref([
	{ name: '美食', icon: '🍜', id: 1 },
	{ name: 'KTV', icon: '🎤', id: 2 },
	{ name: '丽人·美发', icon: '💇', id: 3 },
	{ name: '美睫·美甲', icon: '💅', id: 4 },
	{ name: '按摩·足疗', icon: '💆', id: 5 },
	{ name: '美容SPA', icon: '🛁', id: 6 },
	{ name: '亲子游乐', icon: '👶', id: 7 },
	{ name: '酒吧', icon: '🍷', id: 8 }
])

// 筛选和排序状态
const selectedCategory = ref('美食')
const selectedCategoryId = ref(1)
const showCategoryDropdown = ref(false)
const showSortDropdown = ref(null) // 'distance', 'popularity', 'rating', 'price'
const sortField = ref(null)
const sortOrder = ref('desc') // 'asc' or 'desc'
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

// 商家列表（从后端获取）
const shopList = ref([])

onLoad((options) => {
	console.log('=== 搜索页面加载 ===')
	console.log('页面参数:', options)
	
	if (options.category) {
		keyword.value = options.category
		selectedCategory.value = options.category
		// 找到对应的分类ID
		const cat = categories.value.find(c => c.name === options.category)
		if (cat) {
			selectedCategoryId.value = cat.id
		}
	}
	
	// 获取用户信息
	fetchUserInfo()
	
	// 获取当前城市
	getCurrentCity()
	
	// 加载商家列表
	fetchShopList()
	
	// 输出当前城市状态
	console.log('当前城市值:', currentCity.value)
})

onShow(() => {
	console.log('=== 搜索页面显示 ===')
	// 每次显示页面时检查城市是否变化
	const storedCity = uni.getStorageSync('currentCity')
	console.log('缓存中的城市:', storedCity)
	console.log('当前显示的城市:', currentCity.value)
	
	if (storedCity && storedCity !== currentCity.value) {
		console.log('城市发生变化，更新为:', storedCity)
		currentCity.value = storedCity
		// 重新加载商家列表
		pageNum.value = 1
		shopList.value = []
		fetchShopList()
	}
})

// 监听关键词变化，防抖搜索
let searchTimer = null
watch(keyword, (newVal) => {
	if (searchTimer) clearTimeout(searchTimer)
	searchTimer = setTimeout(() => {
		pageNum.value = 1
		shopList.value = []
		fetchShopList()
	}, 500)
})

// 获取商家列表
const fetchShopList = async () => {
	if (loading.value) return
	
	loading.value = true
	try {
		// 构建排序参数
		let sortBy = null
		if (sortField.value) {
			if (sortField.value === 'rating') {
				sortBy = 'rating'
			} else if (sortField.value === 'popularity') {
				sortBy = 'popular'
			} else if (sortField.value === 'price') {
				sortBy = sortOrder.value === 'asc' ? 'price_asc' : 'price_desc'
			}
		}
		
		const params = {
			pageNum: pageNum.value,
			pageSize: pageSize.value
		}
		
		// 如果有关键词，使用搜索接口
		if (keyword.value && keyword.value.trim()) {
			params.keyword = keyword.value.trim()
		} else {
			// 否则按分类筛选
			params.categoryId = selectedCategoryId.value
		}
		
		if (sortBy) {
			params.sortBy = sortBy
		}
		
		console.log('获取商家列表参数:', params)
		const result = await getShopList(params)
		console.log('商家列表结果:', result)
		
		if (result && result.list) {
			const newList = result.list.map(shop => ({
				id: shop.id,
				name: shop.name,
				image: shop.image || '/static/default-shop.png',
				rating: shop.rating || 0,
				reviews: shop.noteCount || 0,
				tags: shop.category ? [shop.category] : [],
				location: shop.address || '',
				distance: shop.distance || ''
			}))
			
			if (pageNum.value === 1) {
				shopList.value = newList
			} else {
				shopList.value = [...shopList.value, ...newList]
			}
			
			hasMore.value = result.list.length >= pageSize.value
		} else {
			if (pageNum.value === 1) {
				shopList.value = []
			}
			hasMore.value = false
		}
	} catch (e) {
		console.error('获取商家列表失败:', e)
		uni.showToast({
			title: '加载失败',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

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
	const cat = categories.value.find(c => c.name === category)
	selectedCategory.value = category
	selectedCategoryId.value = cat ? cat.id : 1
	showCategoryDropdown.value = false
	
	// 重新加载数据
	pageNum.value = 1
	shopList.value = []
	keyword.value = '' // 清空关键词
	fetchShopList()
	
	uni.showToast({
		title: `已切换到${category}`,
		icon: 'none'
	})
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
	
	// 重新加载数据
	pageNum.value = 1
	shopList.value = []
	fetchShopList()
	
	uni.showToast({
		title: `按${fieldText}${orderText}`,
		icon: 'none'
	})
}

// 关闭所有下拉框
const closeAllDropdowns = () => {
	showCategoryDropdown.value = false
	showSortDropdown.value = null
}

// 加载更多
const loadMore = () => {
	if (!loading.value && hasMore.value) {
		pageNum.value++
		fetchShopList()
	}
}

// 获取用户信息
const fetchUserInfo = async () => {
	try {
		const result = await getUserInfo()
		if (result && result.avatar) {
			userAvatar.value = result.avatar
		}
	} catch (e) {
		console.error('获取用户信息失败:', e)
	}
}

// 获取当前城市
const getCurrentCity = () => {
	// 先从缓存获取
	const storedCity = uni.getStorageSync('currentCity')
	if (storedCity) {
		currentCity.value = storedCity
		console.log('从缓存获取城市:', storedCity)
		return
	}
	
	// 尝试定位
	currentCity.value = '定位中...'
	console.log('开始定位...')
	
	// H5环境使用浏览器定位
	// #ifdef H5
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(
			(position) => {
				console.log('H5定位成功:', position)
				// 使用高德地图逆地理编码获取城市名
				reverseGeocode(position.coords.latitude, position.coords.longitude)
			},
			(error) => {
				console.error('H5定位失败:', error)
				currentCity.value = '杭州' // 默认城市
				uni.setStorageSync('currentCity', '杭州')
			},
			{
				enableHighAccuracy: true,
				timeout: 5000,
				maximumAge: 0
			}
		)
	} else {
		console.log('浏览器不支持定位，使用默认城市')
		currentCity.value = '杭州'
		uni.setStorageSync('currentCity', '杭州')
	}
	// #endif
	
	// APP环境使用uni.getLocation
	// #ifndef H5
	uni.getLocation({
		type: 'gcj02',
		success: (res) => {
			console.log('APP定位成功:', res)
			reverseGeocode(res.latitude, res.longitude)
		},
		fail: (err) => {
			console.error('APP定位失败:', err)
			currentCity.value = '杭州'
			uni.setStorageSync('currentCity', '杭州')
		}
	})
	// #endif
}

// 逆地理编码获取城市名
const reverseGeocode = (lat, lng) => {
	console.log('开始逆地理编码:', lat, lng)
	
	// 使用高德地图API（需要替换为实际的key）
	const key = 'YOUR_AMAP_KEY'
	
	// 如果没有配置高德地图key，直接使用默认城市
	if (key === 'YOUR_AMAP_KEY') {
		console.log('未配置高德地图key，使用默认城市')
		currentCity.value = '杭州'
		uni.setStorageSync('currentCity', '杭州')
		return
	}
	
	const url = `https://restapi.amap.com/v3/geocode/regeo?key=${key}&location=${lng},${lat}&extensions=base`
	
	uni.request({
		url: url,
		method: 'GET',
		success: (res) => {
			console.log('逆地理编码响应:', res)
			if (res.data && res.data.status === '1' && res.data.regeocode) {
				const addressComponent = res.data.regeocode.addressComponent
				const city = addressComponent.city || addressComponent.province || '杭州'
				const cityName = city.replace('市', '').replace('省', '')
				console.log('解析出的城市名:', cityName)
				currentCity.value = cityName
				uni.setStorageSync('currentCity', cityName)
			} else {
				console.log('逆地理编码失败，使用默认城市')
				currentCity.value = '杭州'
				uni.setStorageSync('currentCity', '杭州')
			}
		},
		fail: (err) => {
			console.error('逆地理编码请求失败:', err)
			currentCity.value = '杭州'
			uni.setStorageSync('currentCity', '杭州')
		}
	})
}

// 跳转到城市选择页面
const goToCitySelect = () => {
	// 显示操作菜单
	uni.showActionSheet({
		itemList: ['重新定位', '选择城市'],
		success: (res) => {
			if (res.tapIndex === 0) {
				// 重新定位
				uni.removeStorageSync('currentCity')
				getCurrentCity()
				uni.showToast({
					title: '正在重新定位...',
					icon: 'loading',
					duration: 2000
				})
			} else if (res.tapIndex === 1) {
				// 跳转到城市选择页面
				uni.navigateTo({
					url: '/pages/city-select/city-select'
				})
			}
		}
	})
}

// 跳转到个人主页
const goToProfile = () => {
	uni.switchTab({
		url: '/pages/user-profile/user-profile'
	})
}

// 处理搜索
const handleSearch = () => {
	if (keyword.value.trim()) {
		pageNum.value = 1
		shopList.value = []
		fetchShopList()
	}
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
	display: flex;
	flex-direction: column;
}

.header {
	position: sticky;
	top: 0;
	z-index: 100;
	background: white;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.08);
}

.search-bar {
	display: flex;
	align-items: center;
	padding: 24rpx 30rpx;
	gap: 16rpx;
}

.back-btn {
	padding: 0;
	margin: 0;
	border: none;
	background: none;
	font-size: 44rpx;
	line-height: 1;
	color: #333;
	flex-shrink: 0;
}

.location {
	display: flex;
	align-items: center;
	gap: 6rpx;
	flex-shrink: 0;
}

.location:active {
	opacity: 0.7;
}

.location-icon {
	font-size: 32rpx;
	color: #FF9E64;
}

.location-text {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
}

.search-input-wrapper {
	flex: 1;
	min-width: 0;
}

.search-input {
	display: flex;
	align-items: center;
	padding: 16rpx 32rpx;
	gap: 12rpx;
	background: white;
	border-radius: 50rpx;
	border: 6rpx solid #000;
}

.search-icon {
	font-size: 32rpx;
	color: #666;
	flex-shrink: 0;
}

.search-input input {
	flex: 1;
	font-size: 28rpx;
	background: transparent;
	min-width: 0;
	color: #333;
}

.search-input input::placeholder {
	color: #999;
}

.user-avatar {
	width: 64rpx;
	height: 64rpx;
	border-radius: 50%;
	border: none;
	flex-shrink: 0;
}

.user-icon {
	width: 64rpx;
	height: 64rpx;
	border-radius: 50%;
	background: #FF9E64;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	color: white;
	flex-shrink: 0;
	box-shadow: 6rpx 6rpx 0px rgba(0, 0, 0, 0.1);
}

.filters {
	display: flex;
	padding: 12rpx 20rpx;
	gap: 12rpx;
	overflow-x: auto;
	border-top: 1rpx solid #F0F0F0;
	background: white;
}

.filters::-webkit-scrollbar {
	display: none;
}

.filter-item {
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 6rpx;
	padding: 10rpx 28rpx;
	background: #F5F5F5;
	border-radius: 50rpx;
	font-size: 28rpx;
	white-space: nowrap;
	transition: all 0.3s;
	color: #333;
	font-weight: 400;
	border: none;
	flex-shrink: 0;
}

.filter-item:first-child {
	background: #FF9E64;
	color: white;
	font-weight: 500;
}

.filter-item.active {
	background: #FF9E64;
	color: white;
	font-weight: 500;
}

.arrow {
	font-size: 18rpx;
	color: #666;
	transition: transform 0.3s;
	margin-left: 4rpx;
}

.filter-item:first-child .arrow,
.filter-item.active .arrow {
	color: white;
}

.arrow-up {
	transform: rotate(180deg);
}

/* 下拉菜单 */
.dropdown-menu {
	position: absolute;
	top: 148rpx;
	left: 0;
	right: 0;
	background: white;
	max-height: 500rpx;
	overflow-y: auto;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.12);
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
	padding: 0;
	background: white;
}

.shop-item {
	display: flex;
	background: white;
	padding: 30rpx;
	border-bottom: 1rpx solid #F0F0F0;
	transition: background 0.3s;
}

.shop-item:active {
	background: #F7F9FC;
}

.shop-image {
	width: 180rpx;
	height: 180rpx;
	border-radius: 16rpx;
	margin-right: 30rpx;
	flex-shrink: 0;
}

.shop-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	min-width: 0;
}

.shop-name {
	font-size: 32rpx;
	font-weight: 500;
	margin-bottom: 8rpx;
	line-height: 1.4;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 1;
	-webkit-box-orient: vertical;
}

.rating {
	display: flex;
	align-items: center;
	margin-bottom: 8rpx;
	gap: 8rpx;
}

.rating-stars {
	display: flex;
	align-items: center;
	gap: 4rpx;
}

.star {
	font-size: 26rpx;
}

.score {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
}

.reviews {
	font-size: 26rpx;
	color: #999;
}

.price-tag {
	background: #FFF5ED !important;
	color: #FF9E64 !important;
}

.tags {
	display: flex;
	gap: 8rpx;
	margin-bottom: 8rpx;
	flex-wrap: wrap;
}

.tag {
	padding: 4rpx 16rpx;
	background: #F5F5F5;
	border-radius: 30rpx;
	font-size: 24rpx;
	color: #666;
}

.location-info {
	display: flex;
	align-items: center;
	font-size: 24rpx;
	color: #999;
}

.location-icon {
	font-size: 24rpx;
	color: #FF9E64;
	margin-right: 6rpx;
}
</style>
