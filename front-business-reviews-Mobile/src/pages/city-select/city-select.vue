<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="navbar">
			<view class="nav-back" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<view class="nav-title">当前城市：{{ currentCity }}</view>
		</view>

		<!-- 地图容器 -->
		<view class="map-container">
			<view id="map-box" class="map-box"></view>
			<view v-if="locating" class="map-loading">
				<view class="loading-spinner"></view>
				<text class="loading-text">定位中...</text>
			</view>
			<!-- 重新定位按钮 -->
			<view class="relocate-btn" @click="relocate">
				<text class="relocate-icon">📍</text>
			</view>
		</view>

		<!-- 搜索栏 -->
		<view class="search-container">
			<view class="search-box">
				<text class="search-icon">🔍</text>
				<input 
					class="search-input" 
					v-model="searchKeyword"
					placeholder="搜索城市"
					@input="onSearchInput"
				/>
				<text v-if="searchKeyword" class="clear-icon" @click="clearSearch">✕</text>
			</view>
		</view>

		<!-- 搜索结果 -->
		<view v-if="searchResults.length > 0" class="search-results">
			<view 
				class="search-result-item" 
				v-for="(city, index) in searchResults" 
				:key="index"
				@click="selectCity(city)"
			>
				<text class="result-city-name">{{ city }}</text>
			</view>
		</view>

		<!-- 当前定位城市（紧凑单行显示） -->
		<view v-else class="current-location-compact">
			<view class="location-info" @click="selectCity(currentCity)">
				<text class="location-icon">📍</text>
				<text class="location-label">当前定位：</text>
				<text class="location-city">{{ currentCity }}</text>
				<text class="location-status">{{ locating ? '定位中...' : '已定位' }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 高德地图API Key配置
const AMAP_KEY = '168ca31470201b94eaf74770efdb9f45' // 高德地图JS API Key
const AMAP_WEB_KEY = '1521141ae4ee08e1a0e37b59d2fd2438' // 高德地图Web服务Key

const currentCity = ref('定位中...')
const locating = ref(true)
const searchKeyword = ref('')
const searchResults = ref([])

let map = null // 地图实例
let marker = null // 地图标记
let currentPosition = { lng: 120.153576, lat: 30.287459 } // 默认杭州

// 热门城市
const hotCities = ref([
	'北京', '上海', '广州', '深圳',
	'杭州', '南京', '成都', '武汉',
	'西安', '重庆', '苏州', '天津',
	'普洱', '昆明', '大理', '丽江'
])

// 城市列表（按首字母分组）
const cityGroups = ref({
	'A': ['安庆', '鞍山', '安阳', '安顺'],
	'B': ['北京', '保定', '包头', '蚌埠', '宝鸡'],
	'C': ['成都', '长沙', '重庆', '长春', '常州', '沧州', '承德'],
	'D': ['大连', '东莞', '大同', '大庆', '丹东', '大理'],
	'F': ['福州', '佛山', '抚顺', '阜阳'],
	'G': ['广州', '贵阳', '桂林', '赣州'],
	'H': ['杭州', '哈尔滨', '合肥', '海口', '呼和浩特', '惠州'],
	'J': ['济南', '济宁', '吉林', '锦州', '嘉兴'],
	'K': ['昆明', '开封'],
	'L': ['兰州', '洛阳', '廊坊', '临沂', '柳州', '丽江'],
	'N': ['南京', '南昌', '南宁', '宁波', '南通'],
	'P': ['普洱'],
	'Q': ['青岛', '泉州', '秦皇岛', '齐齐哈尔'],
	'S': ['上海', '深圳', '沈阳', '石家庄', '苏州', '汕头', '绍兴'],
	'T': ['天津', '太原', '唐山', '台州', '泰州'],
	'W': ['武汉', '无锡', '乌鲁木齐', '潍坊', '温州', '芜湖'],
	'X': ['西安', '厦门', '徐州', '湘潭', '咸阳', '西双版纳'],
	'Y': ['烟台', '扬州', '银川', '宜昌', '岳阳', '玉溪'],
	'Z': ['郑州', '珠海', '镇江', '淄博', '中山']
})

onMounted(() => {
	// 动态加载高德地图JS API
	loadAmapScript()
})

// 加载高德地图JS API
const loadAmapScript = () => {
	// 检查是否已加载
	if (window.AMap) {
		initMap()
		return
	}
	
	// 配置安全密钥（必须在加载地图脚本之前设置）
	window._AMapSecurityConfig = {
		securityJsCode: '47211e50f5d2ee5c60ec4f023c84b553'
	}
	
	// 动态创建 script 标签
	const script = document.createElement('script')
	script.type = 'text/javascript'
	script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}&plugin=AMap.Geolocation,AMap.Geocoder`
	script.onload = () => {
		console.log('✅ 高德地图JS API加载成功')
		initMap()
	}
	script.onerror = () => {
		console.error('高德地图加载失败，请检查API Key')
		locating.value = false
		currentCity.value = '杭州'
		uni.showToast({
			title: '地图加载失败，请检查API Key配置',
			icon: 'none',
			duration: 3000
		})
	}
	document.head.appendChild(script)
}

// 初始化地图
const initMap = () => {
	if (!window.AMap) {
		console.error('AMap is not loaded')
		return
	}
	
	// 创建地图实例
	map = new AMap.Map('map-box', {
		zoom: 13,
		center: [currentPosition.lng, currentPosition.lat],
		viewMode: '2D'
	})
	
	// 创建标记
	marker = new AMap.Marker({
		position: [currentPosition.lng, currentPosition.lat],
		icon: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_r.png'
	})
	map.add(marker)
	
	// 开始定位
	getLocation()
}

// 重新定位
const relocate = () => {
	locating.value = true
	getLocation()
}

// 获取当前定位
const getLocation = () => {
	locating.value = true
	
	// #ifdef H5
	// 使用浏览器原生GPS定位
	getCityByIP()
	// #endif
	
	// #ifndef H5
	// APP环境使用uni.getLocation
	uni.getLocation({
		type: 'gcj02',
		success: (res) => {
			console.log('APP定位成功:', res)
			currentPosition = { lng: res.longitude, lat: res.latitude }
			
			// 更新地图位置
			if (map && marker) {
				map.setCenter([res.longitude, res.latitude])
				marker.setPosition([res.longitude, res.latitude])
			}
			
			getCityByLocation(res.latitude, res.longitude)
		},
		fail: (err) => {
			console.error('APP定位失败:', err)
			handleLocationError()
		}
	})
	// #endif
}

// 使用浏览器原生GPS定位（更可靠）
const getCityByIP = () => {
	console.log('🔍 开始浏览器GPS定位...')
	
	// 优先使用浏览器原生Geolocation API
	if (!navigator.geolocation) {
		console.error('❌ 浏览器不支持定位')
		handleLocationError()
		return
	}
	
	navigator.geolocation.getCurrentPosition(
		// 定位成功
		(position) => {
			console.log('✅ 浏览器GPS定位成功:', position)
			const { latitude, longitude } = position.coords
			
			// WGS84转GCJ02坐标（高德使用火星坐标系）
			const gcj02Coords = wgs84ToGcj02(longitude, latitude)
			currentPosition = { lng: gcj02Coords[0], lat: gcj02Coords[1] }
			
			console.log('📍 坐标转换完成:', currentPosition)
			
			// 更新地图位置
			if (map && marker) {
				map.setCenter([gcj02Coords[0], gcj02Coords[1]])
				marker.setPosition([gcj02Coords[0], gcj02Coords[1]])
				map.setZoom(13)
			}
			
			// 使用高德逆地理编码获取城市名称
			getCityByLocation(gcj02Coords[1], gcj02Coords[0])
		},
		// 定位失败
		(error) => {
			console.error('❌ 浏览器GPS定位失败:', error)
			console.error('错误详情:', {
				code: error.code,
				message: error.message,
				PERMISSION_DENIED: error.code === 1 ? '用户拒绝定位' : '',
				POSITION_UNAVAILABLE: error.code === 2 ? '位置不可用' : '',
				TIMEOUT: error.code === 3 ? '定位超时' : ''
			})
			
			// 如果是权限被拒绝，提示用户
			if (error.code === 1) {
				uni.showModal({
					title: '需要定位权限',
					content: 'Chrome浏览器需要您授权定位权限。请点击地址栏左侧的🔒图标，允许位置访问。',
					showCancel: false
				})
			}
			
			handleLocationError()
		},
		// 定位选项
		{
			enableHighAccuracy: true, // 使用高精度定位
			timeout: 10000, // 10秒超时
			maximumAge: 0 // 不使用缓存位置，获取最新位置
		}
	)
}

// WGS84坐标转GCJ02坐标（火星坐标系）
const wgs84ToGcj02 = (lng, lat) => {
	const a = 6378245.0 // 长半轴
	const ee = 0.00669342162296594323 // 偏心率平方
	
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

// 通过经纬度获取城市名称（使用高德地图逆地理编码API）
const getCityByLocation = (latitude, longitude) => {
	const url = `https://restapi.amap.com/v3/geocode/regeo?key=${AMAP_WEB_KEY}&location=${longitude},${latitude}&poitype=&radius=1000&extensions=base&batch=false&roadlevel=0`
	
	console.log('逆地理编码请求:', { latitude, longitude, url })
	
	uni.request({
		url: url,
		method: 'GET',
		success: (res) => {
			console.log('逆地理编码完整响应:', JSON.stringify(res.data))
			
			if (res.data.status === '1' && res.data.regeocode) {
				const addressComponent = res.data.regeocode.addressComponent
				console.log('地址组件详情:', JSON.stringify(addressComponent))
				
				let cityName = ''
				
				if (addressComponent) {
					// 优先使用province（省级）作为保底
					if (addressComponent.province && typeof addressComponent.province === 'string') {
						cityName = addressComponent.province
						console.log('✅ 使用province:', cityName)
					}
					
					// 如果有city且不是空数组/空字符串，优先使用city（更精确）
					if (addressComponent.city) {
						if (Array.isArray(addressComponent.city)) {
							if (addressComponent.city.length > 0 && typeof addressComponent.city[0] === 'string') {
								cityName = addressComponent.city[0]
								console.log('✅ 使用city数组第一个元素:', cityName)
							}
							// 空数组则保持使用province
						} else if (typeof addressComponent.city === 'string' && addressComponent.city !== '') {
							cityName = addressComponent.city
							console.log('✅ 使用city字符串:', cityName)
						}
					}
				}
				
				console.log('🔍 最终cityName（处理前）:', cityName, '类型:', typeof cityName)
				
				// 确保cityName是字符串类型且不为空
				if (cityName && typeof cityName === 'string') {
					// 去掉"市"和"省"后缀
					cityName = cityName.replace('市', '').replace('省', '').replace('自治区', '').replace('特别行政区', '')
					currentCity.value = cityName
					uni.setStorageSync('selectedCity', cityName)
					console.log('✅ 定位成功，城市:', cityName)
				} else {
					console.error('❌ 无法从响应中提取城市名称，cityName类型:', typeof cityName, 'cityName值:', cityName)
					handleLocationError()
				}
			} else {
				console.error('逆地理编码失败:', res.data)
				handleLocationError()
			}
			locating.value = false
		},
		fail: (err) => {
			console.error('逆地理编码请求失败:', err)
			handleLocationError()
		}
	})
}

// 处理定位失败
const handleLocationError = () => {
	locating.value = false
	
	// 检查是否有保存的城市
	const savedCity = uni.getStorageSync('selectedCity')
	if (savedCity) {
		// 使用保存的城市，但明确提示用户这是缓存的
		currentCity.value = savedCity
		console.log('ℹ️ 使用已保存的城市:', savedCity)
		uni.showToast({
			title: `定位失败，使用上次保存的城市：${savedCity}`,
			icon: 'none',
			duration: 3000
		})
		
		// 将地图移动到保存的城市
		moveToCityOnMap(savedCity)
	} else {
		// 没有保存的城市，提示用户手动选择
		currentCity.value = '请搜索并选择城市'
		uni.showToast({
			title: '定位失败，请手动搜索城市',
			icon: 'none',
			duration: 3000
		})
	}
}

// 搜索城市
const onSearchInput = () => {
	if (!searchKeyword.value.trim()) {
		searchResults.value = []
		return
	}
	
	const keyword = searchKeyword.value.trim()
	const results = []
	
	// 在热门城市中搜索
	hotCities.value.forEach(city => {
		if (city.includes(keyword)) {
			results.push(city)
		}
	})
	
	// 在所有城市中搜索
	Object.values(cityGroups.value).forEach(group => {
		group.forEach(city => {
			if (city.includes(keyword) && !results.includes(city)) {
				results.push(city)
			}
		})
	})
	
	searchResults.value = results.slice(0, 20) // 限制显示20个结果
	
	// 如果有搜索结果，将第一个城市在地图上显示
	if (results.length > 0) {
		moveToCityOnMap(results[0])
	}
}

// 清空搜索
const clearSearch = () => {
	searchKeyword.value = ''
	searchResults.value = []
	// 恢复地图到当前定位
	if (currentCity.value !== '定位中...' && currentCity.value !== '请选择城市') {
		moveToCityOnMap(currentCity.value)
	}
}

// 将地图移动到指定城市
const moveToCityOnMap = (cityName) => {
	if (!window.AMap || !map) {
		console.warn('地图未初始化')
		return
	}
	
	console.log('🗺️ 搜索城市地理位置:', cityName)
	
	// 使用高德地理编码API获取城市经纬度
	AMap.plugin('AMap.Geocoder', function() {
		const geocoder = new AMap.Geocoder()
		
		geocoder.getLocation(cityName, function(status, result) {
			if (status === 'complete' && result.info === 'OK') {
				const location = result.geocodes[0].location
				console.log('✅ 城市地理编码成功:', cityName, location)
				
				// 移动地图中心到城市位置
				map.setCenter([location.lng, location.lat])
				map.setZoom(11)
				
				// 更新标记位置
				if (marker) {
					marker.setPosition([location.lng, location.lat])
				}
			} else {
				console.error('❌ 城市地理编码失败:', cityName, result)
			}
		})
	})
}

// 选择城市
const selectCity = (city) => {
	if (city === '定位中...' || city === '请选择城市') return
	
	// 在地图上显示选中的城市
	moveToCityOnMap(city)
	
	// 保存到本地存储
	uni.setStorageSync('selectedCity', city)
	
	uni.showToast({
		title: `已切换至${city}`,
		icon: 'success'
	})
	
	// 返回上一页并传递数据
	setTimeout(() => {
		uni.navigateBack({
			delta: 1
		})
	}, 500)
}

// 返回
const goBack = () => {
	uni.navigateBack({
		delta: 1
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

.navbar {
	position: sticky;
	top: 0;
	z-index: 100;
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	background: white;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.nav-back {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.back-icon {
	font-size: 36rpx;
	color: #333;
}

.nav-title {
	flex: 1;
	text-align: center;
	font-size: 28rpx;
	font-weight: 500;
	color: #333;
	margin-right: 60rpx;
}

// 地图容器（尽可能大）
.map-container {
	position: relative;
	width: 100%;
	height: calc(100vh - 120rpx); // 减去导航栏高度
	background: #e5e5e5;
}

.map-box {
	width: 100%;
	height: 100%;
}

.map-loading {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	background: rgba(255, 255, 255, 0.9);
}

.loading-spinner {
	width: 60rpx;
	height: 60rpx;
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #FF9E64;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text {
	margin-top: 20rpx;
	font-size: 26rpx;
	color: #666;
}

.relocate-btn {
	position: absolute;
	right: 20rpx;
	bottom: 20rpx;
	width: 80rpx;
	height: 80rpx;
	background: white;
	border-radius: 50%;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s;
	
	&:active {
		transform: scale(0.95);
	}
}

.relocate-icon {
	font-size: 40rpx;
}

// 搜索栏（紧凑单行）
.search-container {
	position: absolute;
	top: 100rpx; // 在地图上方浮动
	left: 20rpx;
	right: 20rpx;
	z-index: 10;
}

.search-box {
	display: flex;
	align-items: center;
	padding: 12rpx 20rpx;
	background: white;
	border-radius: 40rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.search-icon {
	font-size: 32rpx;
	margin-right: 15rpx;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.clear-icon {
	font-size: 28rpx;
	color: #999;
	padding: 5rpx 10rpx;
}

// 搜索结果（在搜索框下方）
.search-results {
	position: absolute;
	top: 170rpx; // 搜索框下方
	left: 20rpx;
	right: 20rpx;
	max-height: 400rpx;
	background: white;
	border-radius: 20rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
	overflow-y: auto;
	z-index: 10;
}

.search-result-item {
	padding: 25rpx 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
	transition: background 0.3s;
	
	&:active {
		background: #f7f9fc;
	}
}

.result-city-name {
	font-size: 30rpx;
	color: #333;
}

// 当前定位城市（紧凑单行）
.current-location-compact {
	position: absolute;
	bottom: 40rpx; // 地图底部
	left: 20rpx;
	right: 20rpx;
	z-index: 10;
}

.location-info {
	display: flex;
	align-items: center;
	padding: 15rpx 25rpx;
	background: white;
	border-radius: 40rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.15);
}

.location-icon {
	font-size: 28rpx;
	margin-right: 8rpx;
}

.location-label {
	font-size: 26rpx;
	color: #666;
	margin-right: 8rpx;
}

.location-city {
	font-size: 30rpx;
	font-weight: 500;
	color: #333;
	flex: 1;
}

.location-status {
	font-size: 24rpx;
	color: #999;
}
</style>
