<template>
	<!-- 商户选择弹窗 -->
	<view v-if="visible" class="modal-overlay" @click="close">
		<view class="shop-modal" @click.stop>
			<view class="modal-header">
				<text class="modal-title">关联商户</text>
				<text class="modal-close" @click="close">×</text>
			</view>
			
			<!-- 搜索栏 -->
			<view class="shop-search-bar">
				<view class="search-input-wrapper">
					<text class="search-icon">🔍</text>
					<input 
						class="search-input" 
						v-model="shopSearchKeyword"
						placeholder="搜索商户名、地点..."
						@input="handleShopSearch"
					/>
				</view>
			</view>
			
			<!-- 商户列表 -->
			<scroll-view class="shop-list" scroll-y>
				<view v-if="filteredShopList.length > 0">
					<view class="list-group-title">附近推荐</view>
					
					<view 
						class="shop-item" 
						:class="{ selected: selectedShop && selectedShop.id === shop.id }"
						v-for="shop in filteredShopList" 
						:key="shop.id"
						@click="selectShopItem(shop)"
					>
						<image 
							v-if="shop.headerImage"
							:src="shop.headerImage" 
							class="shop-img" 
							mode="aspectFill"
						></image>
						<view v-else class="shop-img shop-img-placeholder">
							<text class="placeholder-icon">🏪</text>
						</view>
						<view class="shop-info">
							<text class="shop-name">{{ shop.name }}</text>
							<view class="shop-meta">
								<text class="shop-category">{{ shop.category || '商户' }}</text>
								<text v-if="shop.avgPrice">· 人均¥{{ shop.avgPrice }}</text>
							</view>
						</view>
						<text class="check-icon">✓</text>
					</view>
				</view>
				
				<view v-else class="empty-shop">
					<text>暂无商户</text>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script setup>
import { ref, watch, defineProps, defineEmits } from 'vue'
import { getRegisteredShops } from '../api/shop'

const props = defineProps({
	visible: {
		type: Boolean,
		default: false
	},
	modelValue: {
		type: Object,
		default: null
	}
})

const emit = defineEmits(['update:visible', 'update:modelValue'])

const selectedShop = ref(props.modelValue)
const shopList = ref([])
const shopSearchKeyword = ref('')
const filteredShopList = ref([])

watch(() => props.visible, (val) => {
	if (val) {
		if (shopList.value.length === 0) {
			loadShops()
		}
	}
})

watch(() => props.modelValue, (val) => {
	selectedShop.value = val
})

watch(selectedShop, (val) => {
	emit('update:modelValue', val)
}, { deep: true })

const close = () => {
	emit('update:visible', false)
	shopSearchKeyword.value = ''
	filteredShopList.value = shopList.value
}

const loadShops = async () => {
	try {
		uni.showLoading({ title: '加载商户...' })
		const result = await getRegisteredShops('', 1, 50)
		uni.hideLoading()
		
		if (result.list && result.list.length > 0) {
			shopList.value = result.list
			filteredShopList.value = result.list
		} else {
			uni.showToast({ title: '暂无可关联的商户', icon: 'none' })
		}
	} catch (e) {
		uni.hideLoading()
		console.error('加载商户失败:', e)
		uni.showToast({ title: '加载商户失败', icon: 'none' })
	}
}


const selectShopItem = (shop) => {
	selectedShop.value = shop
	uni.showToast({ 
		title: '已选择商户', 
		icon: 'success',
		duration: 800
	})
	setTimeout(() => {
		close()
	}, 300)
}

const handleShopSearch = () => {
	const keyword = shopSearchKeyword.value.trim().toLowerCase()
	if (!keyword) {
		filteredShopList.value = shopList.value
		return
	}
	
	filteredShopList.value = shopList.value.filter(shop => {
		return shop.name.toLowerCase().includes(keyword) ||
		       (shop.category && shop.category.toLowerCase().includes(keyword)) ||
		       (shop.address && shop.address.toLowerCase().includes(keyword))
	})
}
</script>

<style lang="scss" scoped>
.modal-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: flex-end;
	z-index: 1000;
	animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
	from { opacity: 0; }
	to { opacity: 1; }
}

@keyframes slideUp {
	from { transform: translateY(100%); }
	to { transform: translateY(0); }
}

.modal-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx 40rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.modal-title {
	font-size: 34rpx;
	font-weight: 600;
	color: #333;
}

.modal-close {
	font-size: 48rpx;
	color: #999;
	line-height: 1;
	padding: 0 10rpx;
}

// 商户选择弹窗
.shop-modal {
	width: 100%;
	max-height: 75vh;
	background: white;
	border-radius: 40rpx 40rpx 0 0;
	animation: slideUp 0.3s ease;
	display: flex;
	flex-direction: column;
}

.shop-search-bar {
	padding: 20rpx 32rpx;
	background: #fff;
	border-bottom: 1rpx solid #f0f0f0;
}

.search-input-wrapper {
	background: #f7f9fc;
	border-radius: 20rpx;
	padding: 16rpx 24rpx;
	display: flex;
	align-items: center;
	gap: 16rpx;
}

.search-icon {
	font-size: 28rpx;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: #333;
	background: transparent;
}

.shop-list {
	flex: 1;
	padding: 0 32rpx 40rpx 32rpx;
	max-height: 60vh;
}

.list-group-title {
	font-size: 24rpx;
	color: #b2bec3;
	margin: 30rpx 0 16rpx 0;
	font-weight: 500;
}

.shop-item {
	display: flex;
	align-items: center;
	padding: 24rpx 0;
	border-bottom: 1rpx solid #f1f2f6;
	transition: opacity 0.2s;
	
	&:active {
		opacity: 0.7;
	}
}

.shop-img {
	width: 88rpx;
	height: 88rpx;
	border-radius: 12rpx;
	background-color: #f1f2f6;
	flex-shrink: 0;
}

.shop-img-placeholder {
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #e7f5ff 0%, #f0f0f0 100%);
}

.placeholder-icon {
	font-size: 40rpx;
}

.shop-info {
	flex: 1;
	margin-left: 24rpx;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.shop-name {
	font-size: 30rpx;
	font-weight: 600;
	color: #2d3436;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.shop-meta {
	display: flex;
	align-items: center;
	gap: 8rpx;
	font-size: 24rpx;
	color: #b2bec3;
}

.shop-category {
	color: #ff9f43;
	background: rgba(255, 159, 67, 0.1);
	padding: 2rpx 8rpx;
	border-radius: 8rpx;
	font-size: 22rpx;
}

.check-icon {
	color: #ff9f43;
	font-size: 36rpx;
	opacity: 0;
	transition: 0.2s;
}

.shop-item.selected .check-icon {
	opacity: 1;
}

.empty-shop {
	text-align: center;
	padding: 100rpx 0;
	color: #999;
	font-size: 28rpx;
}
</style>
