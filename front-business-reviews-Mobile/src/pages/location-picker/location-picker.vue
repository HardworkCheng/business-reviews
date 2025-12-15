<template>
	<view class="container">
		<!-- 顶部操作栏 -->
		<view class="header">
			<view class="close-btn" @click="handleClose">
				<text class="close-icon">✕</text>
			</view>
			<view class="confirm-btn" @click="handleConfirm">
				<text class="confirm-icon">✓</text>
			</view>
		</view>

		<!-- 地图容器 -->
		<view class="map-container">
			<view id="map-box" class="map-box"></view>
			<!-- 中心定位图标 -->
			<view class="center-marker">
				<text class="marker-emoji">📍</text>
			</view>
			<!-- 重新定位按钮 -->
			<view class="relocate-btn" @click="relocate">
				<text class="relocate-icon">⊕</text>
			</view>
			<view v-if="locating" class="map-loading">
				<view class="loading-spinner"></view>
			</view>
		</view>

		<!-- 搜索栏 -->
		<view class="search-container">
			<input 
				class="search-input" 
				v-model="searchKeyword"
				placeholder="搜索地点"
				@input="onSearchInput"
			/>
		</view>

		<!-- 附近地点列表 -->
		<view class="poi-list">
			<view 
				class="poi-item" 
				v-for="(poi, index) in poiList" 
				:key="index"
				@click="selectPoi(poi)"
			>
				<view class="poi-info">
					<text class="poi-name">{{ poi.name }}</text>
					<text class="poi-address">{{ poi.address }}</text>
				</view>
			</view>
			<view v-if="poiList.length === 0 && !loading" class="empty-tip">
				<text class="loading-text">{{ searchKeyword ? '无搜索结果' : '移动地图查看附近地点' }}</text>
			</view>
			<view v-if="loading" class="loading-tip">
				<view class="loading-spinner-small"></view>
				<text class="loading-text">加载中...</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 高德地图API Key
const AMAP_KEY = '168ca31470201b94eaf74770efdb9f45'

const searchKeyword = ref('')
const poiList = ref([])
const locating = ref(false)
const loading = ref(false)
const selectedLocation = ref(null)

let map = null
let marker = null
let currentPosition = { lng: 120.153576, lat: 30.287459 } // 默认杭州

onMounted(() => {
	loadAmapScript()
})

// 加载高德地图JS API
const loadAmapScript = () => {
	if (window.AMap) {
		initMap()
		return
	}
	
	window._AMapSecurityConfig = {
		securityJsCode: '47211e50f5d2ee5c60ec4f023c84b553'
	}
	
	const script = document.createElement('script')
	script.type = 'text/javascript'
	script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}&plugin=AMap.Geolocation,AMap.PlaceSearch`
	script.onload = () => {
		console.log('✅ 高德地图JS API加载成功')
		initMap()
	}
	script.onerror = () => {
		console.error('❌ 地图加载失败')
		uni.showToast({ title: '地图加载失败', icon: 'none' })
	}
	document.head.appendChild(script)
}

// 初始化地图
const initMap = () => {
	if (!window.AMap) {
		console.error('AMap未加载')
		return
	}
	
	map = new AMap.Map('map-box', {
		zoom: 15,
		center: [currentPosition.lng, currentPosition.lat],
		viewMode: '2D'
	})
	
	// 监听地图移动
	map.on('moveend', onMapMoveEnd)
	
	// 开始定位
	getLocation()
}

// 获取当前定位
const getLocation = () => {
	locating.value = true
	
	if (!navigator.geolocation) {
		console.error('浏览器不支持定位')
		locating.value = false
		return
	}
	
	navigator.geolocation.getCurrentPosition(
		(position) => {
			console.log('✅ GPS定位成功:', position)
			const { latitude, longitude } = position.coords
			
			// WGS84转GCJ02
			const gcj02Coords = wgs84ToGcj02(longitude, latitude)
			currentPosition = { lng: gcj02Coords[0], lat: gcj02Coords[1] }
			
			if (map) {
				map.setCenter([gcj02Coords[0], gcj02Coords[1]])
				map.setZoom(15)
			}
			
			locating.value = false
			// 搜索附近地点
			searchNearbyPoi()
		},
		(error) => {
			console.error('❌ GPS定位失败:', error)
			locating.value = false
			searchNearbyPoi() // 即使定位失败也搜索
		},
		{
			enableHighAccuracy: true,
			timeout: 10000,
			maximumAge: 0
		}
	)
}

// 重新定位
const relocate = () => {
	getLocation()
}

// 地图移动结束
const onMapMoveEnd = () => {
	if (!map) return
	const center = map.getCenter()
	currentPosition = { lng: center.lng, lat: center.lat }
	// 搜索附近地点
	if (!searchKeyword.value) {
		searchNearbyPoi()
	}
}

// 搜索附近POI
const searchNearbyPoi = () => {
	if (!window.AMap) return
	
	loading.value = true
	
	AMap.plugin('AMap.PlaceSearch', function() {
		const placeSearch = new AMap.PlaceSearch({
			type: '',
			pageSize: 20,
			pageIndex: 1,
			city: '全国',
			citylimit: false,
			extensions: 'base'
		})
		
		placeSearch.searchNearBy('', [currentPosition.lng, currentPosition.lat], 1000, function(status, result) {
			loading.value = false
			if (status === 'complete' && result.poiList && result.poiList.pois) {
				poiList.value = result.poiList.pois.map(poi => ({
					name: poi.name,
					address: poi.address || poi.pname + poi.cityname + poi.adname,
					location: poi.location,
					latitude: poi.location.lat,
					longitude: poi.location.lng
				}))
				console.log('✅ 搜索到附近POI:', poiList.value.length)
			} else {
				console.error('❌ 搜索附近POI失败:', result)
				poiList.value = []
			}
		})
	})
}

// 搜索输入
const onSearchInput = () => {
	if (!searchKeyword.value.trim()) {
		searchNearbyPoi()
		return
	}
	
	loading.value = true
	
	AMap.plugin('AMap.PlaceSearch', function() {
		const placeSearch = new AMap.PlaceSearch({
			city: '全国',
			pageSize: 20
		})
		
		placeSearch.search(searchKeyword.value, function(status, result) {
			loading.value = false
			if (status === 'complete' && result.poiList && result.poiList.pois) {
				poiList.value = result.poiList.pois.map(poi => ({
					name: poi.name,
					address: poi.address || poi.pname + poi.cityname + poi.adname,
					location: poi.location,
					latitude: poi.location.lat,
					longitude: poi.location.lng
				}))
			} else {
				poiList.value = []
			}
		})
	})
}

// 选择POI
const selectPoi = (poi) => {
	selectedLocation.value = poi
	// 移动地图到选中位置
	if (map && poi.location) {
		map.setCenter([poi.location.lng, poi.location.lat])
		currentPosition = { lng: poi.location.lng, lat: poi.location.lat }
	}
	// 高亮选中项（可选）
	uni.showToast({ title: '已选择', icon: 'success', duration: 800 })
}

// 确认选择
const handleConfirm = () => {
	if (!selectedLocation.value) {
		// 如果没有选择POI，使用地图中心位置
		selectedLocation.value = {
			name: '当前位置',
			address: '',
			latitude: currentPosition.lat,
			longitude: currentPosition.lng
		}
	}
	
	console.log('✅ 确认选择位置:', selectedLocation.value)
	
	// 使用 uni.$emit 发送事件
	uni.$emit('locationSelected', selectedLocation.value)
	
	uni.navigateBack()
}

// 关闭
const handleClose = () => {
	uni.navigateBack()
}

// WGS84转GCJ02
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
</script>

<style lang="scss" scoped>
.container {
	width: 100vw;
	height: 100vh;
	background: #F7F9FC;
	display: flex;
	flex-direction: column;
	position: relative;
}

.header {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	z-index: 100;
	display: flex;
	justify-content: space-between;
	padding: 20rpx;
}

.close-btn, .confirm-btn {
	width: 70rpx;
	height: 70rpx;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.9);
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
	transition: all 0.3s;
	
	&:active {
		transform: scale(0.95);
	}
}

.close-icon {
	font-size: 36rpx;
	color: #333;
}

.confirm-btn {
	background: #06D6A0;
}

.confirm-icon {
	font-size: 40rpx;
	color: white;
	font-weight: bold;
}

.map-container {
	position: relative;
	width: 100%;
	height: 50vh;
	background: #e5e5e5;
}

.map-box {
	width: 100%;
	height: 100%;
}

.center-marker {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -100%);
	z-index: 10;
	pointer-events: none;
}

.marker-emoji {
	font-size: 60rpx;
	filter: drop-shadow(0 2rpx 4rpx rgba(0, 0, 0, 0.3));
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
	font-size: 48rpx;
	color: #333;
}

.map-loading {
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

.loading-spinner {
	width: 60rpx;
	height: 60rpx;
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #06D6A0;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.search-container {
	padding: 20rpx;
	background: white;
	border-bottom: 1rpx solid #f0f0f0;
}

.search-input {
	width: 100%;
	padding: 15rpx 20rpx;
	background: #F7F9FC;
	border-radius: 30rpx;
	font-size: 28rpx;
}

.poi-list {
	flex: 1;
	overflow-y: auto;
	background: white;
}

.poi-item {
	padding: 25rpx 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
	transition: background 0.3s;
	
	&:active {
		background: #f7f9fc;
	}
}

.poi-info {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.poi-name {
	font-size: 30rpx;
	color: #333;
	font-weight: 500;
}

.poi-address {
	font-size: 24rpx;
	color: #999;
}

.empty-tip, .loading-tip {
	padding: 60rpx 30rpx;
	text-align: center;
}

.loading-tip {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 20rpx;
}

.loading-spinner-small {
	width: 40rpx;
	height: 40rpx;
	border: 3rpx solid #f3f3f3;
	border-top: 3rpx solid #06D6A0;
	border-radius: 50%;
	animation: spin 1s linear infinite;
}

.loading-text {
	font-size: 26rpx;
	color: #999;
}
</style>
