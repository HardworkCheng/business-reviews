<template>
	<view class="container">
		<!-- 顶部导航栏 -->
		<view class="header">
			<view class="nav-bar">
				<view class="nav-btn" @click="goBack">
					<text>←</text>
				</view>
				<text class="nav-title">{{ locationName || '位置' }}</text>
				<view class="nav-btn" @click="openInMap">
					<text>🧭</text>
				</view>
			</view>
		</view>

		<!-- 地图容器 -->
		<view class="map-container">
			<map
				:latitude="latitude"
				:longitude="longitude"
				:markers="markers"
				:show-location="true"
				class="map"
			>
			</map>
		</view>

		<!-- 位置信息卡片 -->
		<view class="location-info" v-if="locationName">
			<view class="info-card">
				<view class="location-icon">📍</view>
				<view class="location-details">
					<text class="location-name">{{ locationName }}</text>
					<text class="location-address">{{ address || '正在获取地址...' }}</text>
				</view>
			</view>
			
			<!-- 操作按钮 -->
			<view class="action-buttons">
				<button class="action-btn" @click="copyAddress">
					<text class="btn-icon">📋</text>
					<text>复制地址</text>
				</button>
				<button class="action-btn primary" @click="openInMap">
					<text class="btn-icon">🗺️</text>
					<text>打开地图</text>
				</button>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const latitude = ref(0)
const longitude = ref(0)
const locationName = ref('')
const address = ref('')
const markers = ref([])

onLoad((options) => {
	console.log('Map page loaded with options:', options)
	
	if (options.latitude && options.longitude) {
		latitude.value = parseFloat(options.latitude)
		longitude.value = parseFloat(options.longitude)
		
		if (options.name) {
			locationName.value = decodeURIComponent(options.name)
		}
		
		// 设置地图标记
		markers.value = [{
			id: 1,
			latitude: latitude.value,
			longitude: longitude.value,
			title: locationName.value || '位置',
			iconPath: '/static/icons/location.png',
			width: 30,
			height: 30
		}]
		
		// 获取详细地址
		getAddress()
	} else {
		uni.showToast({
			title: '位置信息不完整',
			icon: 'none'
		})
	}
})

// 获取详细地址
const getAddress = () => {
	const key = '1521141ae4ee08e1a0e37b59d2fd2438' // 高德地图Web服务Key
	const url = `https://restapi.amap.com/v3/geocode/regeo?key=${key}&location=${longitude.value},${latitude.value}&poitype=&radius=1000&extensions=base&batch=false&roadlevel=0`
	
	uni.request({
		url: url,
		method: 'GET',
		success: (res) => {
			console.log('逆地理编码响应:', res.data)
			
			if (res.data.status === '1' && res.data.regeocode) {
				address.value = res.data.regeocode.formatted_address || '未知地址'
			} else {
				address.value = '无法获取地址信息'
			}
		},
		fail: (err) => {
			console.error('获取地址失败:', err)
			address.value = '获取地址失败'
		}
	})
}

// 返回上一页
const goBack = () => {
	uni.navigateBack()
}

// 复制地址
const copyAddress = () => {
	const textToCopy = address.value || locationName.value
	
	// #ifdef H5
	if (navigator.clipboard) {
		navigator.clipboard.writeText(textToCopy).then(() => {
			uni.showToast({
				title: '地址已复制',
				icon: 'success'
			})
		}).catch(() => {
			uni.showToast({
				title: '复制失败',
				icon: 'none'
			})
		})
	} else {
		uni.showToast({
			title: '浏览器不支持复制',
			icon: 'none'
		})
	}
	// #endif
	
	// #ifndef H5
	uni.setClipboardData({
		data: textToCopy,
		success: () => {
			uni.showToast({
				title: '地址已复制',
				icon: 'success'
			})
		},
		fail: () => {
			uni.showToast({
				title: '复制失败',
				icon: 'none'
			})
		}
	})
	// #endif
}

// 在外部地图应用中打开
const openInMap = () => {
	const name = encodeURIComponent(locationName.value || '目的地')
	
	// #ifdef H5
	// 在H5中打开高德地图网页版
	const url = `https://uri.amap.com/marker?position=${longitude.value},${latitude.value}&name=${name}&coordinate=gaode&callnative=1`
	window.open(url, '_blank')
	// #endif
	
	// #ifndef H5
	// 在APP中打开系统地图
	uni.openLocation({
		latitude: latitude.value,
		longitude: longitude.value,
		name: locationName.value || '位置',
		address: address.value || '',
		success: () => {
			console.log('打开地图成功')
		},
		fail: (err) => {
			console.error('打开地图失败:', err)
			uni.showToast({
				title: '打开地图失败',
				icon: 'none'
			})
		}
	})
	// #endif
}
</script>

<style lang="scss" scoped>
.container {
	width: 100%;
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: #F7F9FC;
}

.header {
	background: white;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	z-index: 100;
}

.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
}

.nav-btn {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32rpx;
	border-radius: 50%;
	background: #f5f5f5;
	transition: all 0.3s;
	
	&:active {
		background: #e0e0e0;
		transform: scale(0.95);
	}
}

.nav-title {
	flex: 1;
	text-align: center;
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	padding: 0 20rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.map-container {
	flex: 1;
	position: relative;
}

.map {
	width: 100%;
	height: 100%;
}

.location-info {
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
	background: white;
	border-radius: 30rpx 30rpx 0 0;
	padding: 30rpx;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.info-card {
	display: flex;
	align-items: flex-start;
	margin-bottom: 30rpx;
}

.location-icon {
	font-size: 48rpx;
	margin-right: 20rpx;
}

.location-details {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.location-name {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 10rpx;
}

.location-address {
	font-size: 26rpx;
	color: #999;
	line-height: 1.5;
}

.action-buttons {
	display: flex;
	gap: 20rpx;
}

.action-btn {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;
	height: 80rpx;
	border-radius: 40rpx;
	font-size: 28rpx;
	background: #f5f5f5;
	color: #333;
	border: none;
	transition: all 0.3s;
	
	&:active {
		transform: scale(0.98);
		background: #e0e0e0;
	}
	
	&.primary {
		background: linear-gradient(135deg, #FF9E64 0%, #FFB787 100%);
		color: white;
		
		&:active {
			background: linear-gradient(135deg, #FF8A4C 0%, #FFA56F 100%);
		}
	}
}

.btn-icon {
	font-size: 32rpx;
}
</style>
