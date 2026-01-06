<template>
	<view class="container">
		<view class="header">
			<view class="search-bar">
				<button class="back-btn" @click="goBack">←</button>
				<view class="location" @click="goToCitySelect">
					<image src="/static/icons/location.png" class="location-icon-img" mode="aspectFit"></image>
					<text class="location-text">{{ currentCity }}</text>
					<image src="/static/icons/arrow-down.png" class="location-arrow-img" mode="aspectFit"></image>
				</view>
				<view class="search-input-wrapper">
					<view class="search-input">
						<image src="/static/icons/search.png" class="search-icon-img" mode="aspectFit"></image>
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
					<image :src="item.icon" class="category-icon-small" mode="aspectFit"></image>
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
					<text class="dropdown-text">{{ sortField === 'distance' ? '从近到远' : '从小到大' }}</text>
					<text v-if="sortOrder === 'asc'" class="check-icon">✓</text>
				</view>
				<view 
					class="dropdown-item"
					@click="selectSort('desc')"
					:class="{ 'active': sortOrder === 'desc' }"
				>
					<text class="dropdown-text">{{ sortField === 'distance' ? '从远到近' : '从大到小' }}</text>
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
					<view class="shop-score-row">
						<view class="stars">
							<text class="star-icon" v-for="n in 5" :key="n">⭐</text>
						</view>
						<text class="score-num">{{ shop.rating || 5.0 }}</text>
						<text class="review-count">({{ shop.reviews }}条评价)</text>
					</view>
					<view class="shop-price-row">
						<text class="category-tag">{{ shop.tags[0] || '美食' }}</text>
						<text class="price-text">人均 ¥{{ shop.avgPrice || 85 }}</text>
					</view>
					<view class="shop-loc-row">
						<text class="address">{{ shop.location }}</text>
						<text class="distance" v-if="shop.distance">{{ shop.distance }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, watch } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getShopList, getNearbyShops } from '../../api/shop'
import { getUserInfo } from '../../api/user'

const keyword = ref('')
const currentCity = ref('定位中...')
const userAvatar = ref('')

// 分类数据
const categories = ref([
	{ name: '美食', icon: '/static/icons/food.png', id: 1 },
	{ name: 'KTV', icon: '/static/icons/ktv.png', id: 2 },
	{ name: '美发', icon: '/static/icons/beauty.png', id: 3 },
	{ name: '美甲', icon: '/static/icons/nail.png', id: 4 },
	{ name: '足疗', icon: '/static/icons/massage.png', id: 5 },
	{ name: '美容', icon: '/static/icons/spa.png', id: 6 },
	{ name: '游乐', icon: '/static/icons/entertainment.png', id: 7 },
	{ name: '酒吧', icon: '/static/icons/bar.png', id: 8 }
])

// 筛选和排序状态
const selectedCategory = ref('美食')
const selectedCategoryId = ref(1)
const showCategoryDropdown = ref(false)
const showSortDropdown = ref(null) // 'distance', 'popularity', 'rating', 'price'
const sortField = ref(null)
const sortOrder = ref('asc') // 'asc' or 'desc'，距离排序默认升序（从近到远）
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)

// 用户位置（用于距离排序）
const userLatitude = ref(null)
const userLongitude = ref(null)

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
	// 每次显示页面时检查城市是否变化（与主页保持一致，使用selectedCity）
	const storedCity = uni.getStorageSync('selectedCity')
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
		// 如果是距离排序，调用附近商家接口
		if (sortField.value === 'distance') {
			await fetchNearbyShops()
			return
		}
		
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
		
		processShopResult(result)
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

// 获取附近商家（按距离排序）
const fetchNearbyShops = async () => {
	// 如果没有用户位置，先获取位置
	if (!userLatitude.value || !userLongitude.value) {
		console.log('获取用户位置用于距离排序...')
		await getUserLocation()
	}
	
	// 如果仍然没有位置，降级到普通列表
	if (!userLatitude.value || !userLongitude.value) {
		console.warn('无法获取用户位置，降级到普通列表')
		uni.showToast({
			title: '无法获取位置，请开启定位权限',
			icon: 'none'
		})
		// 降级到普通列表
		sortField.value = null
		const params = {
			pageNum: pageNum.value,
			pageSize: pageSize.value,
			categoryId: selectedCategoryId.value
		}
		const result = await getShopList(params)
		processShopResult(result)
		return
	}
	
	const params = {
		latitude: userLatitude.value,
		longitude: userLongitude.value,
		distance: 10, // 搜索半径10公里
		pageNum: pageNum.value,
		pageSize: pageSize.value,
		categoryId: selectedCategoryId.value
	}
	
	console.log('获取附近商家参数:', params)
	const result = await getNearbyShops(params)
	console.log('附近商家结果:', result)
	
	// 如果是降序（从远到近），反转结果
	if (sortOrder.value === 'desc' && result && result.list) {
		result.list = result.list.reverse()
	}
	
	processShopResult(result)
}

// 处理商家列表结果
const processShopResult = (result) => {
	if (result && result.list) {
		const newList = result.list.map(shop => ({
			id: shop.id,
			name: shop.name,
			image: shop.image || 'https://via.placeholder.com/400x300/FF9E64/FFFFFF?text=Shop',
			rating: shop.rating || 0,
			reviews: shop.noteCount || 0,
			tags: shop.category ? [shop.category] : [],
			avgPrice: shop.avgPrice || 85,
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
}

// 获取用户位置
const getUserLocation = () => {
	return new Promise((resolve) => {
		// #ifdef H5
		if (navigator.geolocation) {
			navigator.geolocation.getCurrentPosition(
				(position) => {
					const { latitude, longitude } = position.coords
					// WGS84转GCJ02坐标
					const gcj02Coords = wgs84ToGcj02(longitude, latitude)
					userLongitude.value = gcj02Coords[0]
					userLatitude.value = gcj02Coords[1]
					console.log('✅ 获取用户位置成功:', userLatitude.value, userLongitude.value)
					resolve(true)
				},
				(error) => {
					console.warn('获取用户位置失败:', error.message)
					resolve(false)
				},
				{
					enableHighAccuracy: false,
					timeout: 5000,
					maximumAge: 300000 // 5分钟内的缓存位置
				}
			)
		} else {
			resolve(false)
		}
		// #endif
		
		// #ifndef H5
		uni.getLocation({
			type: 'gcj02',
			success: (res) => {
				userLatitude.value = res.latitude
				userLongitude.value = res.longitude
				console.log('✅ APP获取用户位置成功:', userLatitude.value, userLongitude.value)
				resolve(true)
			},
			fail: (err) => {
				console.error('APP获取用户位置失败:', err)
				resolve(false)
			}
		})
		// #endif
	})
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
	
	// 根据排序类型显示不同的文案
	let orderText
	if (sortField.value === 'distance') {
		orderText = order === 'asc' ? '从近到远' : '从远到近'
	} else {
		orderText = order === 'asc' ? '从小到大' : '从大到小'
	}
	
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
	// 先从缓存获取（与主页保持一致，使用selectedCity）
	const storedCity = uni.getStorageSync('selectedCity')
	if (storedCity) {
		currentCity.value = storedCity
		console.log('搜索页从缓存获取城市:', storedCity)
		return
	}
	
	// 尝试定位
	currentCity.value = '定位中...'
	console.log('搜索页开始定位...')
	
	// 使用多重定位策略：先尝试IP定位（快速），同时尝试GPS定位（精确）
	// #ifdef H5
	// 1. 先使用IP定位（快速获取大致位置）
	getCityByIP()
	
	// 2. 同时尝试GPS定位（更精确，但可能较慢）
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(
			(position) => {
				console.log('搜索页GPS定位成功:', position)
				const { latitude, longitude } = position.coords
				// WGS84转GCJ02坐标
				const gcj02Coords = wgs84ToGcj02(longitude, latitude)
				// GPS定位成功后，覆盖IP定位的结果（更精确）
				reverseGeocode(gcj02Coords[1], gcj02Coords[0])
			},
			(error) => {
				console.warn('搜索页GPS定位失败（已使用IP定位）:', error.message)
				// IP定位已经执行，不需要再处理
			},
			{
				enableHighAccuracy: false, // 降低精度要求，提高成功率
				timeout: 8000,
				maximumAge: 30000 // 允许使用30秒内的缓存位置
			}
		)
	}
	// #endif
	
	// APP环境使用uni.getLocation
	// #ifndef H5
	// 先尝试IP定位
	getCityByIP()
	
	uni.getLocation({
		type: 'gcj02',
		success: (res) => {
			console.log('搜索页APP定位成功:', res)
			reverseGeocode(res.latitude, res.longitude)
		},
		fail: (err) => {
			console.error('搜索页APP定位失败:', err)
			// IP定位已经执行，不需要再处理
		}
	})
	// #endif
}

// WGS84坐标转GCJ02坐标（火星坐标系）
const wgs84ToGcj02 = (lng, lat) => {
	const a = 6378245.0
	const ee = 0.00669342162296594323
	
	if (outOfChina(lng, lat)) {
		return [lng, lat]
	}
	
	let dLat = transformLat(lng - 105.0, lat - 35.0)
	let dLng = transformLng(lng - 105.0, lat - 35.0)
	const radLat = lat / 180.0 * Math.PI
	let magic = Math.sin(radLat)
	magic = 1 - ee * magic * magic
	const sqrtMagic = Math.sqrt(magic)
	dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI)
	dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI)
	const mgLat = lat + dLat
	const mgLng = lng + dLng
	return [mgLng, mgLat]
}

const transformLat = (lng, lat) => {
	let ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng))
	ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
	ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0
	ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0
	return ret
}

const transformLng = (lng, lat) => {
	let ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng))
	ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
	ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0
	ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0
	return ret
}

const outOfChina = (lng, lat) => {
	return (lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271)
}

// IP定位（使用高德Web服务API）
const getCityByIP = () => {
	console.log('🔍 搜索页开始IP定位...')
	const key = '1521141ae4ee08e1a0e37b59d2fd2438'
	const url = `https://restapi.amap.com/v3/ip?key=${key}`
	
	uni.request({
		url: url,
		method: 'GET',
		success: (res) => {
			console.log('搜索页IP定位响应:', JSON.stringify(res.data))
			
			if (res.data.status === '1') {
				let cityName = ''
				
				// 获取城市名称
				if (res.data.city && typeof res.data.city === 'string' && res.data.city !== '') {
					cityName = res.data.city
				} else if (res.data.province && typeof res.data.province === 'string') {
					cityName = res.data.province
				}
				
				if (cityName) {
					cityName = cityName.replace('市', '').replace('省', '').replace('自治区', '').replace('特别行政区', '')
					currentCity.value = cityName
					uni.setStorageSync('selectedCity', cityName)
					console.log('✅ 搜索页IP定位成功:', cityName)
				} else {
					console.warn('⚠️ 搜索页IP定位无城市信息，使用默认城市')
					setDefaultCity()
				}
			} else {
				console.warn('⚠️ 搜索页IP定位失败，使用默认城市')
				setDefaultCity()
			}
		},
		fail: (err) => {
			console.error('❌ 搜索页IP定位请求失败:', err)
			setDefaultCity()
		}
	})
}

// 逆地理编码获取城市名
const reverseGeocode = (lat, lng) => {
	console.log('搜索页开始逆地理编码:', lat, lng)
	
	const key = '1521141ae4ee08e1a0e37b59d2fd2438'
	const url = `https://restapi.amap.com/v3/geocode/regeo?key=${key}&location=${lng},${lat}&poitype=&radius=1000&extensions=base&batch=false&roadlevel=0`
	
	uni.request({
		url: url,
		method: 'GET',
		success: (res) => {
			console.log('搜索页逆地理编码响应:', JSON.stringify(res.data))
			
			if (res.data.status === '1' && res.data.regeocode) {
				const addressComponent = res.data.regeocode.addressComponent
				let cityName = ''
				
				if (addressComponent) {
					// 优先使用province（省级）
					if (addressComponent.province && typeof addressComponent.province === 'string') {
						cityName = addressComponent.province
					}
					
					// 如果有city且不是空数组/空字符串，优先使用city（更精确）
					if (addressComponent.city) {
						if (Array.isArray(addressComponent.city)) {
							if (addressComponent.city.length > 0 && typeof addressComponent.city[0] === 'string') {
								cityName = addressComponent.city[0]
							}
						} else if (typeof addressComponent.city === 'string' && addressComponent.city !== '') {
							cityName = addressComponent.city
						}
					}
				}
				
				if (cityName && typeof cityName === 'string') {
					cityName = cityName.replace('市', '').replace('省', '').replace('自治区', '').replace('特别行政区', '')
					currentCity.value = cityName
					uni.setStorageSync('selectedCity', cityName)
					console.log('✅ 搜索页GPS定位成功:', cityName)
				} else {
					console.log('⚠️ 搜索页逆地理编码无城市信息，保持IP定位结果')
				}
			} else {
				console.log('⚠️ 搜索页逆地理编码失败，保持IP定位结果')
			}
		},
		fail: (err) => {
			console.error('❌ 搜索页逆地理编码请求失败:', err)
		}
	})
}

// 设置默认城市
const setDefaultCity = () => {
	currentCity.value = '杭州'
	uni.setStorageSync('selectedCity', '杭州')
}

// 跳转到城市选择页面
const goToCitySelect = () => {
	// 显示操作菜单
	uni.showActionSheet({
		itemList: ['重新定位', '选择城市'],
		success: (res) => {
			if (res.tapIndex === 0) {
				// 重新定位
				reLocation()
			} else if (res.tapIndex === 1) {
				// 跳转到城市选择页面
				uni.navigateTo({
					url: '/pages/city-select/city-select'
				})
			}
		}
	})
}

// 重新定位（强制刷新）
const reLocation = () => {
	// 清除缓存
	uni.removeStorageSync('selectedCity')
	// 显示定位中状态
	currentCity.value = '定位中...'
	
	uni.showLoading({
		title: '正在定位...',
		mask: true
	})
	
	// 使用多重定位策略
	// #ifdef H5
	// 1. 先尝试GPS定位（更精确）
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(
			(position) => {
				console.log('🎯 搜索页重新定位GPS成功:', position)
				const { latitude, longitude } = position.coords
				// WGS84转GCJ02坐标
				const gcj02Coords = wgs84ToGcj02(longitude, latitude)
				// GPS定位成功，使用逆地理编码获取城市
				reverseGeocodeForReLocation(gcj02Coords[1], gcj02Coords[0])
			},
			(error) => {
				console.warn('⚠️ 搜索页重新定位GPS失败:', error.message)
				// GPS失败，尝试IP定位
				getCityByIPForReLocation()
			},
			{
				enableHighAccuracy: true, // 重新定位时使用高精度
				timeout: 10000,
				maximumAge: 0 // 不使用缓存
			}
		)
	} else {
		// 不支持GPS，使用IP定位
		getCityByIPForReLocation()
	}
	// #endif
	
	// APP环境
	// #ifndef H5
	uni.getLocation({
		type: 'gcj02',
		success: (res) => {
			console.log('🎯 搜索页重新定位APP成功:', res)
			reverseGeocodeForReLocation(res.latitude, res.longitude)
		},
		fail: (err) => {
			console.error('❌ 搜索页重新定位APP失败:', err)
			getCityByIPForReLocation()
		}
	})
	// #endif
}

// 重新定位专用的IP定位
const getCityByIPForReLocation = () => {
	console.log('🔍 搜索页重新定位使用IP定位...')
	const key = '1521141ae4ee08e1a0e37b59d2fd2438'
	const url = `https://restapi.amap.com/v3/ip?key=${key}`
	
	uni.request({
		url: url,
		method: 'GET',
		success: (res) => {
			uni.hideLoading()
			console.log('搜索页重新定位IP响应:', JSON.stringify(res.data))
			
			if (res.data.status === '1') {
				let cityName = ''
				
				if (res.data.city && typeof res.data.city === 'string' && res.data.city !== '') {
					cityName = res.data.city
				} else if (res.data.province && typeof res.data.province === 'string') {
					cityName = res.data.province
				}
				
				if (cityName) {
					cityName = cityName.replace('市', '').replace('省', '').replace('自治区', '').replace('特别行政区', '')
					currentCity.value = cityName
					uni.setStorageSync('selectedCity', cityName)
					console.log('✅ 搜索页重新定位成功:', cityName)
					uni.showToast({
						title: `定位成功: ${cityName}`,
						icon: 'success'
					})
				} else {
					handleLocationFailed()
				}
			} else {
				handleLocationFailed()
			}
		},
		fail: (err) => {
			uni.hideLoading()
			console.error('❌ 搜索页重新定位IP请求失败:', err)
			handleLocationFailed()
		}
	})
}

// 重新定位专用的逆地理编码
const reverseGeocodeForReLocation = (lat, lng) => {
	console.log('搜索页重新定位逆地理编码:', lat, lng)
	
	const key = '1521141ae4ee08e1a0e37b59d2fd2438'
	const url = `https://restapi.amap.com/v3/geocode/regeo?key=${key}&location=${lng},${lat}&poitype=&radius=1000&extensions=base&batch=false&roadlevel=0`
	
	uni.request({
		url: url,
		method: 'GET',
		success: (res) => {
			uni.hideLoading()
			console.log('搜索页重新定位逆地理编码响应:', JSON.stringify(res.data))
			
			if (res.data.status === '1' && res.data.regeocode) {
				const addressComponent = res.data.regeocode.addressComponent
				let cityName = ''
				
				if (addressComponent) {
					if (addressComponent.province && typeof addressComponent.province === 'string') {
						cityName = addressComponent.province
					}
					
					if (addressComponent.city) {
						if (Array.isArray(addressComponent.city)) {
							if (addressComponent.city.length > 0 && typeof addressComponent.city[0] === 'string') {
								cityName = addressComponent.city[0]
							}
						} else if (typeof addressComponent.city === 'string' && addressComponent.city !== '') {
							cityName = addressComponent.city
						}
					}
				}
				
				if (cityName && typeof cityName === 'string') {
					cityName = cityName.replace('市', '').replace('省', '').replace('自治区', '').replace('特别行政区', '')
					currentCity.value = cityName
					uni.setStorageSync('selectedCity', cityName)
					console.log('✅ 搜索页重新定位GPS成功:', cityName)
					uni.showToast({
						title: `定位成功: ${cityName}`,
						icon: 'success'
					})
				} else {
					// GPS逆地理编码失败，尝试IP定位
					getCityByIPForReLocation()
				}
			} else {
				// 逆地理编码失败，尝试IP定位
				getCityByIPForReLocation()
			}
		},
		fail: (err) => {
			console.error('❌ 搜索页重新定位逆地理编码请求失败:', err)
			// 失败后尝试IP定位
			getCityByIPForReLocation()
		}
	})
}

// 处理定位失败
const handleLocationFailed = () => {
	uni.showModal({
		title: '定位失败',
		content: '无法获取您的位置，是否手动选择城市？',
		confirmText: '选择城市',
		cancelText: '使用默认',
		success: (res) => {
			if (res.confirm) {
				// 跳转到城市选择页面
				uni.navigateTo({
					url: '/pages/city-select/city-select'
				})
			} else {
				// 使用默认城市
				setDefaultCity()
				uni.showToast({
					title: '已使用默认城市',
					icon: 'none'
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

.location-icon-img {
	width: 28rpx;
	height: 28rpx;
	margin-right: 8rpx;
}

.location-arrow-img {
	width: 20rpx;
	height: 20rpx;
	margin-left: 8rpx;
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
	padding: 12rpx 24rpx;
	gap: 12rpx;
	background: white;
	border-radius: 40rpx;
	border: 3rpx solid #000;
}

.search-icon-img {
	width: 32rpx;
	height: 32rpx;
	flex-shrink: 0;
}

.search-input input {
	flex: 1;
	font-size: 26rpx;
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
	padding: 10rpx 24rpx;
	background: transparent;
	border-radius: 30rpx;
	font-size: 26rpx;
	white-space: nowrap;
	transition: all 0.2s;
	color: #222;
	font-weight: 400;
	border: none;
	flex-shrink: 0;
}

.filter-item:first-child {
	background: #fff0e6;
	color: #ff6b00;
	font-weight: 600;
}

.filter-item.active {
	background: #fff0e6;
	color: #ff6b00;
	font-weight: 600;
}

.arrow {
	font-size: 16rpx;
	color: #666;
	transition: transform 0.3s;
	margin-left: 4rpx;
	transform: scale(0.8);
}

.filter-item:first-child .arrow,
.filter-item.active .arrow {
	color: #ff6b00;
}

.arrow-up {
	transform: scale(0.8) rotate(180deg);
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
	width: 36rpx;
	height: 36rpx;
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
	padding: 24rpx 24rpx;
	border-bottom: 1rpx solid #F0F0F0;
	transition: background 0.2s;
}

.shop-item:active {
	background: #FAFAFA;
}

.shop-image {
	width: 160rpx;
	height: 160rpx;
	border-radius: 12rpx;
	margin-right: 24rpx;
	flex-shrink: 0;
	background: #eee;
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
	font-weight: 700;
	color: #222;
	margin-bottom: 8rpx;
	line-height: 1.4;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.shop-score-row {
	display: flex;
	align-items: center;
	font-size: 24rpx;
	margin-bottom: 12rpx;
}

.stars {
	display: flex;
	gap: 2rpx;
	margin-right: 8rpx;
}

.star-icon {
	font-size: 20rpx;
	color: #ff6b00;
}

.score-num {
	color: #ff6b00;
	font-weight: 700;
	margin-right: 12rpx;
	font-size: 24rpx;
}

.review-count {
	color: #999;
	font-size: 24rpx;
}

.shop-price-row {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 12rpx;
}

.category-tag {
	font-size: 22rpx;
	color: #666;
	background: #f5f5f5;
	padding: 4rpx 10rpx;
	border-radius: 8rpx;
}

.price-text {
	font-size: 24rpx;
	color: #222;
}

.shop-loc-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 22rpx;
	color: #999;
}

.address {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	max-width: 360rpx;
}

.distance {
	color: #666;
	font-weight: 500;
	flex-shrink: 0;
	margin-left: 12rpx;
}
</style>
