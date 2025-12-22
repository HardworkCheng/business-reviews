<template>
	<view class="container">
		<!-- 顶部搜索栏 -->
		<view class="header">
			<view class="location-search">
				<!-- 简洁的城市显示 -->
				<view class="location-simple" @click="goToCitySelect">
					<image src="/static/icons/location.png" class="location-icon-img" mode="aspectFit"></image>
					<text class="city-name">{{ currentCity }}</text>
					<image src="/static/icons/arrow-down.png" class="arrow-icon-img" mode="aspectFit"></image>
				</view>
				<view class="search-box clay-border" @click="goToSearch">
					<image src="/static/icons/search.png" class="search-icon-img" mode="aspectFit"></image>
					<text class="search-placeholder">搜索商户名或地点</text>
				</view>
				<view class="user-avatar" @click="goToProfile">
					<image 
						v-if="userAvatar" 
						:src="userAvatar" 
						class="avatar-image"
						mode="aspectFill"
					></image>
					<text v-else class="avatar-placeholder">👤</text>
				</view>
			</view>
		</view>

		<!-- 分类图标区 -->
		<view class="category-section">
			<view class="category-grid">
				<view 
					class="category-item" 
					v-for="(item, index) in categories" 
					:key="index"
					@click="goToCategory(item.name)"
				>
					<view class="category-icon">
						<image :src="item.icon" class="icon-image" mode="aspectFit"></image>
					</view>
					<text class="category-name">{{ item.name }}</text>
				</view>
			</view>
		</view>

		<!-- 推荐笔记 -->
		<view class="notes-section">
			<view class="notes-grid">
				<view 
					class="note-card clay-shadow" 
					v-for="(note, index) in noteList" 
					:key="index"
					@click="goToNoteDetail(note.id)"
				>
					<view class="note-image-wrapper">
						<image :src="note.image" class="note-image" mode="aspectFill"></image>
						<view v-if="note.tag" class="note-tag" :class="note.tagClass">
							<text class="tag-text">{{ note.tag }}</text>
						</view>
					</view>
					<view class="note-info">
						<text class="note-title line-clamp-2">{{ note.title }}</text>
						<view class="note-meta">
							<view class="author-info">
								<text class="author">@{{ note.author }}</text>
							</view>
							<view class="like-info">
								<text class="like-icon">❤️</text>
								<text class="like-count">{{ note.likes }}</text>
							</view>
						</view>
						<view class="note-time">
							<text class="time-text">{{ note.createTime }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getRecommendedNotes } from '../../api/note'
import { getUserInfo } from '../../api/user'

// 当前城市
const currentCity = ref('定位中...')
// 用户头像
const userAvatar = ref('')

// 时间格式化函数
const formatTime = (timeStr) => {
	if (!timeStr) return ''
	
	try {
		const date = new Date(timeStr)
		const now = new Date()
		const diff = now - date
		const minutes = Math.floor(diff / 60000)
		const hours = Math.floor(diff / 3600000)
		const days = Math.floor(diff / 86400000)
		
		if (minutes < 1) return '刚刚'
		if (minutes < 60) return `${minutes}分钟前`
		if (hours < 24) return `${hours}小时前`
		if (days < 7) return `${days}天前`
		if (days < 30) return `${Math.floor(days / 7)}周前`
		if (days < 365) return `${Math.floor(days / 30)}个月前`
		return `${Math.floor(days / 365)}年前`
	} catch (e) {
		return ''
	}
}

// 分类数据
const categories = ref([
	{ name: '美食', icon: '/static/icons/food.png' },
	{ name: 'KTV', icon: '/static/icons/ktv.png' },
	{ name: '美发', icon: '/static/icons/beauty.png' },
	{ name: '美甲', icon: '/static/icons/nail.png' },
	{ name: '足疗', icon: '/static/icons/massage.png' },
	{ name: '美容', icon: '/static/icons/spa.png' },
	{ name: '游乐', icon: '/static/icons/entertainment.png' },
	{ name: '酒吧', icon: '/static/icons/bar.png' }
])

// 笔记列表（从后端获取）
const noteList = ref([])
const loading = ref(false)

onLoad(() => {
	console.log('Index page loaded')
	// 初始化定位
	initLocation()
	// 获取用户信息
	fetchUserInfo()
	// 强制从服务器获取最新数据
	loadData()
})

onShow(() => {
	console.log('Index page show')
	// 检查城市是否更改
	const savedCity = uni.getStorageSync('selectedCity')
	if (savedCity) {
		currentCity.value = savedCity
	}
	// 更新用户头像
	fetchUserInfo()
	// 每次显示时都强制刷新数据
	loadData()
})

// 统一的数据加载函数
const loadData = () => {
	const token = uni.getStorageSync('token')
	console.log('=== loadData ===', 'token:', token ? token.substring(0, 20) + '...' : '无')
	
	// 清空旧数据
	noteList.value = []
	
	// 获取最新笔记列表
	fetchNoteList()
}

// 初始化定位
const initLocation = () => {
	// 先检查是否有保存的城市
	const savedCity = uni.getStorageSync('selectedCity')
	if (savedCity) {
		currentCity.value = savedCity
		return
	}
	
	// 如果没有保存的城市，自动定位
	currentCity.value = '定位中...'
	
	// 使用多重定位策略：先尝试IP定位（快速），同时尝试GPS定位（精确）
	// #ifdef H5
	// 1. 先使用IP定位（快速获取大致位置）
	getCityByIP()
	
	// 2. 同时尝试GPS定位（更精确，但可能较慢）
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(
			(position) => {
				console.log('GPS定位成功，覆盖IP定位结果:', position)
				const { latitude, longitude } = position.coords
				// WGS84转GCJ02坐标
				const gcj02Coords = wgs84ToGcj02(longitude, latitude)
				// GPS定位成功后，覆盖IP定位的结果（更精确）
				getCityByLocation(gcj02Coords[1], gcj02Coords[0])
			},
			(error) => {
				console.warn('GPS定位失败（已使用IP定位）:', error.message)
				// IP定位已经执行，不需要再处理
			},
			{
				enableHighAccuracy: false, // 改为false，降低精度要求，提高成功率
				timeout: 8000, // 减少到8秒
				maximumAge: 30000 // 允许使用30秒内的缓存位置
			}
		)
	}
	// #endif
	
	// #ifndef H5
	uni.getLocation({
		type: 'gcj02',
		success: (res) => {
			console.log('APP定位成功:', res)
			getCityByLocation(res.latitude, res.longitude)
		},
		fail: (err) => {
			console.error('APP定位失败:', err)
			setDefaultCity()
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
	console.log('🔍 首页开始IP定位...')
	const key = '1521141ae4ee08e1a0e37b59d2fd2438'
	const url = `https://restapi.amap.com/v3/ip?key=${key}`
	
	uni.request({
		url: url,
		method: 'GET',
		success: (res) => {
			console.log('首页IP定位响应:', JSON.stringify(res.data))
			
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
					console.log('✅ 首页IP定位成功:', cityName)
				} else {
					console.warn('⚠️ 首页IP定位无城市信息，使用默认城市')
					setDefaultCity()
				}
			} else {
				console.warn('⚠️ 首页IP定位失败，使用默认城市')
				setDefaultCity()
			}
		},
		fail: (err) => {
			console.error('❌ 首页IP定位请求失败:', err)
			setDefaultCity()
		}
	})
}

// 通过经纬度获取城市名称
const getCityByLocation = (latitude, longitude) => {
	const key = '1521141ae4ee08e1a0e37b59d2fd2438' // 高德地图Web服务Key
	const url = `https://restapi.amap.com/v3/geocode/regeo?key=${key}&location=${longitude},${latitude}&poitype=&radius=1000&extensions=base&batch=false&roadlevel=0`
	
	console.log('首页逆地理编码请求:', { latitude, longitude, url })
	
	uni.request({
		url: url,
		method: 'GET',
		success: (res) => {
			console.log('首页逆地理编码完整响应:', JSON.stringify(res.data))
			
			if (res.data.status === '1' && res.data.regeocode) {
				const addressComponent = res.data.regeocode.addressComponent
				console.log('地址组件详情:', JSON.stringify(addressComponent))
				
				let cityName = ''
				
				if (addressComponent) {
					// 优先使用province（省级）
					if (addressComponent.province && typeof addressComponent.province === 'string') {
						cityName = addressComponent.province
						console.log('使用province:', cityName)
					}
					
					// 如果有city且不是空数组/空字符串，优先使用city（更精确）
					if (addressComponent.city) {
						if (Array.isArray(addressComponent.city)) {
							if (addressComponent.city.length > 0 && typeof addressComponent.city[0] === 'string') {
								cityName = addressComponent.city[0]
								console.log('使用city数组第一个元素:', cityName)
							}
							// 空数组则保持使用province
						} else if (typeof addressComponent.city === 'string' && addressComponent.city !== '') {
							cityName = addressComponent.city
							console.log('使用city字符串:', cityName)
						}
						// 空字符串则保持使用province
					}
				}
				
				console.log('最终cityName（处理前）:', cityName, '类型:', typeof cityName)
				
				// 确保cityName是字符串类型且不为空
				if (cityName && typeof cityName === 'string') {
					// 去掉"市"和"省"后缀
					cityName = cityName.replace('市', '').replace('省', '').replace('自治区', '').replace('特别行政区', '')
					currentCity.value = cityName
					uni.setStorageSync('selectedCity', cityName)
					console.log('✅ 首页GPS定位成功:', cityName)
				} else {
					console.error('❌ 无法提取城市名称，cityName类型:', typeof cityName, 'cityName值:', cityName)
					console.log('⚠️ 保持IP定位结果')
					// 不调用setDefaultCity，保持IP定位的结果
				}
			} else {
				console.error('❌ 首页逆地理编码失败:', res.data)
				console.log('⚠️ 保持IP定位结果')
				// 不调用setDefaultCity，保持IP定位的结果
			}
		},
		fail: (err) => {
			console.error('❌ 首页逆地理编码请求失败:', err)
			console.log('⚠️ 保持IP定位结果')
			// 不调用setDefaultCity，保持IP定位的结果
		}
	})
}

// 设置默认城市
const setDefaultCity = () => {
	currentCity.value = '杭州'
	uni.setStorageSync('selectedCity', '杭州')
}

// 获取用户信息
const fetchUserInfo = async () => {
	const token = uni.getStorageSync('token')
	if (!token) {
		userAvatar.value = ''
		return
	}
	
	try {
		const result = await getUserInfo()
		console.log('获取用户信息:', result)
		
		if (result && result.avatar) {
			userAvatar.value = result.avatar
		}
	} catch (e) {
		console.error('获取用户信息失败:', e)
		userAvatar.value = ''
	}
}

// 获取推荐笔记列表
const fetchNoteList = async () => {
	if (loading.value) return
	
	loading.value = true
	try {
		const result = await getRecommendedNotes(1, 20)
		console.log('获取笔记列表:', result)
		
		if (result && result.list) {
			// 转换数据格式
			noteList.value = result.list.map(note => ({
				id: note.id,
				title: note.title,
				image: note.image || '',
				author: note.author || '匿名用户',
				likes: note.likes || 0,
				tag: note.tag || null,
				tagClass: note.tagClass || '',
				createTime: formatTime(note.createdAt)
			}))
			console.log('笔记列表已更新:', noteList.value.length, '条')
		}
	} catch (e) {
		console.error('获取笔记列表失败:', e)
		uni.showToast({
			title: '加载失败，请重试',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

// 跳转城市选择
const goToCitySelect = () => {
	uni.navigateTo({
		url: '/pages/city-select/city-select'
	})
}

// 跳转搜索
const goToSearch = () => {
	uni.navigateTo({
		url: '/pages/search/search'
	})
}

// 跳转个人主页
const goToProfile = () => {
	uni.switchTab({
		url: '/pages/profile/profile'
	})
}

// 分类筛选
const goToCategory = (category) => {
	uni.navigateTo({
		url: `/pages/search/search?category=${category}`
	})
}

// 笔记详情
const goToNoteDetail = (id) => {
	uni.navigateTo({
		url: `/pages/note-detail/note-detail?id=${id}`
	})
}
</script>

<style lang="scss" scoped>
.container {
	background: #F7F9FC;
	min-height: 100vh;
	padding-bottom: 20rpx;
}

.header {
	position: sticky;
	top: 0;
	z-index: 100;
	background: white;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.location-search {
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	gap: 20rpx;
	justify-content: space-between;
}

// 简洁的城市显示（类似小红书）
.location-simple {
	display: flex;
	align-items: center;
	gap: 5rpx;
	cursor: pointer;
	transition: all 0.3s;
	
	&:active {
		opacity: 0.7;
	}
}

.city-name {
	font-size: 28rpx;
	color: #333;
	font-weight: 600;
}

.arrow-icon {
	font-size: 20rpx;
	color: #999;
	margin-left: 5rpx;
}

.search-box {
	flex: 1;
	display: flex;
	align-items: center;
	padding: 12rpx 24rpx;
	background: white;
	max-width: 400rpx;
	height: 64rpx;
	margin: 0 15rpx;
}

.search-icon {
	font-size: 28rpx;
	margin-right: 12rpx;
}

.search-placeholder {
	font-size: 26rpx;
	color: #999;
}

.search-icon-img {
	width: 32rpx;
	height: 32rpx;
	margin-right: 12rpx;
}

.arrow-icon-img {
	width: 20rpx;
	height: 20rpx;
}

.location-icon-img {
	width: 28rpx;
	height: 28rpx;
	margin-right: 8rpx;
}

.user-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	overflow: hidden;
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
	transition: all 0.3s;
	
	&:active {
		transform: scale(0.95);
	}
}

.avatar-image {
	width: 100%;
	height: 100%;
}

.avatar-placeholder {
	font-size: 32rpx;
	color: white;
}

.category-section {
	background: white;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.category-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 30rpx;
}

.category-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.category-icon {
	width: 100rpx;
	height: 100rpx;
	margin-bottom: 10rpx;
	background: transparent;
	display: flex;
	align-items: center;
	justify-content: center;
}

.icon-image {
	width: 60rpx;
	height: 60rpx;
}

.category-name {
	font-size: 24rpx;
	text-align: center;
	color: #333;
}

.notes-section {
	padding: 0 30rpx;
}

.notes-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;
}

.note-card {
	background: white;
	border-radius: 30rpx;
	overflow: hidden;
	transition: all 0.3s;
}

.note-image-wrapper {
	position: relative;
	width: 100%;
	height: 350rpx;
}

.note-image {
	width: 100%;
	height: 100%;
}

.note-tag {
	position: absolute;
	top: 20rpx;
	right: 20rpx;
	padding: 8rpx 20rpx;
	border-radius: 30rpx;
}

.tag-hot {
	background: #EF476F;
}

.tag-discount {
	background: #06D6A0;
}

.tag-new {
	background: #FFD166;
}

.tag-merchant {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.tag-text {
	font-size: 22rpx;
	color: white;
}

.note-info {
	padding: 20rpx;
}

.note-title {
	font-size: 28rpx;
	font-weight: 500;
	margin-bottom: 15rpx;
	line-height: 1.4;
}

.note-meta {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.author-info {
	display: flex;
	flex-direction: column;
	gap: 4rpx;
}

.author {
	font-size: 24rpx;
	color: #999;
}

.like-info {
	display: flex;
	align-items: center;
}

.like-icon {
	font-size: 24rpx;
	margin-right: 5rpx;
}

.like-count {
	font-size: 24rpx;
	color: #EF476F;
}

.note-time {
	display: flex;
	justify-content: flex-end;
	margin-top: 10rpx;
}

.time-text {
	font-size: 22rpx;
	color: #999;
}
</style>
