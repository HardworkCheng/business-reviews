<template>
	<view class="container">
		<!-- 顶部导航栏 -->
		<view class="navbar">
			<text class="nav-cancel" @click="handleCancel">取消</text>
			<view class="publish-btn" :class="{ disabled: updating }" @click="handleUpdate">
				<text>{{ updating ? '保存中...' : '保存' }}</text>
			</view>
		</view>

		<!-- 滚动内容区 -->
		<scroll-view class="scroll-area" scroll-y>
			<!-- 标题输入 -->
			<view class="input-title-wrapper">
				<input 
					type="text" 
					class="input-title"
					placeholder="填写标题更容易上首页哦~" 
					v-model="title"
					maxlength="100"
				/>
			</view>
			
			<!-- 内容输入 -->
			<view class="input-content-wrapper">
				<textarea 
					class="input-content"
					placeholder="分享你的美好体验吧"
					v-model="content"
					maxlength="1000"
					auto-height
				></textarea>
				<view class="char-count">
					<text class="count-current" :class="{ warning: content.length > 900, full: content.length >= 1000 }">{{ content.length }}</text>
					<text>/1000</text>
				</view>
			</view>
			
			<!-- 图片九宫格 -->
			<view class="media-grid">
				<view class="media-item" v-for="(img, index) in imageList" :key="index">
					<image :src="img" class="media-img" mode="aspectFill"></image>
					<view class="btn-delete" @click="removeImage(index)">
						<text>×</text>
					</view>
				</view>
				<view class="media-item btn-upload" v-if="imageList.length < 9" @click="chooseImage">
					<text class="upload-icon">📷</text>
					<text class="upload-text">添加照片</text>
				</view>
			</view>
			
			<!-- 选项列表 -->
			<view class="options-group">
				<!-- 添加话题 -->
				<view class="cell-item" @click="showTopicInput">
					<view class="cell-left">
						<view class="icon-container theme-yellow">
							<text>#</text>
						</view>
						<text class="cell-title">添加话题</text>
					</view>
					<view class="cell-right">
						<text class="cell-value" v-if="selectedTopics.length > 0">
							{{ selectedTopics.map(t => '#' + t.name).join(' ') }}
						</text>
						<text class="cell-arrow">›</text>
					</view>
				</view>
				
				<!-- 关联商户 -->
				<view class="cell-item" @click="selectShop">
					<view class="cell-left">
						<view class="icon-container theme-blue">
							<text>🏪</text>
						</view>
						<text class="cell-title">关联商户</text>
					</view>
					<view class="cell-right">
						<text class="cell-value selected" v-if="selectedShop">{{ selectedShop.name }}</text>
						<text class="cell-arrow">›</text>
					</view>
				</view>
				
				<!-- 添加位置 -->
				<view class="cell-item" @click="chooseLocation">
					<view class="cell-left">
						<view class="icon-container theme-red">
							<text>📍</text>
						</view>
						<text class="cell-title">添加位置</text>
					</view>
					<view class="cell-right">
						<text class="cell-value" v-if="location">{{ location }}</text>
						<text class="cell-arrow">›</text>
					</view>
				</view>
			</view>
		</scroll-view>
		
		<!-- 话题输入弹窗 -->
		<view v-if="showTopicModal" class="modal-overlay" @click="closeTopicModal">
			<view class="topic-modal-new" @click.stop>
				<view class="modal-header">
					<text class="modal-title">添加话题</text>
					<text class="modal-close" @click="closeTopicModal">×</text>
				</view>
				
				<!-- 输入框 -->
				<view class="topic-input-section">
					<view class="topic-input-wrapper-new">
						<text class="topic-hash-new">#</text>
						<input 
							class="topic-input-new" 
							v-model="topicInput"
							placeholder="输入自定义话题..."
							maxlength="20"
							@confirm="addCustomTopic"
						/>
						<view class="topic-add-btn-new" @click="addCustomTopic">
							<text>添加</text>
						</view>
					</view>
				</view>
				
				<!-- 话题列表 -->
				<scroll-view class="topic-list-scroll" scroll-y>
					<!-- 最近使用 -->
					<view v-if="selectedTopics.length > 0" class="topic-group">
						<view class="topic-group-header">
							<text class="topic-group-icon">🕐</text>
							<text class="topic-group-title">最近使用</text>
						</view>
						<view class="topic-tags-wrapper">
							<view 
								class="topic-tag-item" 
								v-for="(topic, index) in selectedTopics" 
								:key="'selected-' + index"
							>
								<text class="topic-tag-text">#{{ topic.name }}</text>
								<text class="topic-tag-remove" @click.stop="removeTopic(index)">×</text>
							</view>
						</view>
					</view>
					
					<!-- 热门推荐 -->
					<view class="topic-group">
						<view class="topic-group-header">
							<text class="topic-group-icon">🔥</text>
							<text class="topic-group-title">热门推荐</text>
						</view>
						<view class="topic-tags-wrapper">
							<view 
								class="topic-tag-item hot-topic" 
								:class="{ selected: isTopicSelected(topic) }"
								v-for="topic in hotTopics" 
								:key="topic.id"
								@click="toggleHotTopic(topic)"
							>
								<text class="topic-tag-icon" v-if="topic.isHot">🔥</text>
								<text class="topic-tag-text">#{{ topic.name }}</text>
							</view>
						</view>
					</view>
					
					<view style="height: 40rpx;"></view>
				</scroll-view>
			</view>
		</view>
		
		<!-- 商户选择弹窗 -->
		<view v-if="showShopModal" class="modal-overlay" @click="closeShopModal">
			<view class="shop-modal" @click.stop>
				<view class="modal-header">
					<text class="modal-title">关联商户</text>
					<text class="modal-close" @click="closeShopModal">×</text>
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
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import { getNoteDetail, updateNote } from '../../api/note'
import { uploadImages } from '../../api/upload'
import { getHotTopics } from '../../api/common'
import { getRegisteredShops } from '../../api/shop'

const noteId = ref('')
const title = ref('')
const content = ref('')
const imageList = ref([]) // 存储图片路径（可能是临时路径或URL）
const originalImages = ref([]) // 原始图片URL
const selectedShop = ref(null)
const selectedTopics = ref([])
const location = ref('')
const latitude = ref(null)
const longitude = ref(null)
const updating = ref(false)
const loading = ref(false)
const showTopicModal = ref(false)
const showShopModal = ref(false)
const topicInput = ref('')
const hotTopics = ref([])
const shopList = ref([])
const shopSearchKeyword = ref('')
const filteredShopList = ref([])

onLoad(async (options) => {
	console.log('Note edit loaded, id:', options.id)
	noteId.value = options.id
	if (options.id) {
		await fetchNoteDetail(options.id)
	}
})

onShow(() => {
	// 监听位置选择事件
	uni.$on('locationSelected', handleLocationSelected)
})

onUnload(() => {
	// 移除事件监听
	uni.$off('locationSelected', handleLocationSelected)
})

// 获取笔记详情并回显
const fetchNoteDetail = async (id) => {
	if (loading.value) return
	
	loading.value = true
	uni.showLoading({ title: '加载中...' })
	
	try {
		const result = await getNoteDetail(id)
		console.log('笔记详情:', result)
		
		if (result) {
			// 回显数据
			title.value = result.title || ''
			content.value = result.content || ''
			imageList.value = result.images || []
			originalImages.value = result.images || []
			location.value = result.location || ''
			latitude.value = result.latitude
			longitude.value = result.longitude
			
			// 回显商户
			if (result.shopId && result.shopName) {
				selectedShop.value = {
					id: result.shopId,
					name: result.shopName
				}
			}
			
			// 回显话题
			if (result.topics && result.topics.length > 0) {
				selectedTopics.value = result.topics.map(t => ({
					id: t.id,
					name: t.name
				}))
				console.log('话题回显成功:', selectedTopics.value)
			} else {
				selectedTopics.value = []
				console.log('没有关联的话题')
			}
			
			console.log('笔记数据回显完成')
		}
	} catch (e) {
		console.error('获取笔记详情失败:', e)
		uni.showToast({
			title: '加载失败，请重试',
			icon: 'none'
		})
		setTimeout(() => {
			uni.navigateBack()
		}, 1500)
	} finally {
		uni.hideLoading()
		loading.value = false
	}
}

const handleCancel = () => {
	uni.showModal({
		title: '提示',
		content: '确定要取消编辑吗？修改的内容将不会保存',
		success: (res) => {
			if (res.confirm) {
				uni.navigateBack()
			}
		}
	})
}

const handleUpdate = async () => {
	// 验证
	if (!content.value || content.value.trim().length < 10) {
		uni.showToast({
			title: '请输入至少10个字的内容',
			icon: 'none'
		})
		return
	}
	
	if (imageList.value.length === 0) {
		uni.showToast({
			title: '请至少上传一张图片',
			icon: 'none'
		})
		return
	}
	
	if (!title.value || title.value.trim().length < 2) {
		uni.showToast({
			title: '请输入至少2个字的标题',
			icon: 'none'
		})
		return
	}
	
	// 防止重复提交
	if (updating.value) {
		console.log('正在保存中，请勿重复提交')
		return
	}
	
	updating.value = true
	uni.showLoading({ title: '保存中...', mask: true })
	
	try {
		// 1. 检查是否有新上传的图片（临时路径）
		const newImages = imageList.value.filter(img => img.startsWith('http://tmp/') || img.startsWith('wxfile://'))
		let imageUrls = []
		
		if (newImages.length > 0) {
			console.log('上传新图片:', newImages.length)
			const uploadResult = await uploadImages(newImages)
			const newUrls = uploadResult.urls
			
			// 合并图片：保留原有的URL，替换新上传的
			imageUrls = imageList.value.map(img => {
				if (img.startsWith('http://tmp/') || img.startsWith('wxfile://')) {
					return newUrls.shift()
				}
				return img
			})
		} else {
			// 没有新图片，使用现有的
			imageUrls = imageList.value
		}
		
		console.log('最终图片列表:', imageUrls)
		
		// 2. 构建更新数据
		const noteData = {
			title: title.value.trim(),
			content: content.value.trim(),
			images: imageUrls,
			shopId: selectedShop.value ? selectedShop.value.id : null,
			location: location.value || null,
			latitude: latitude.value,
			longitude: longitude.value,
			// 发送话题名称列表（支持自定义话题）
			topicNames: selectedTopics.value.length > 0 ? selectedTopics.value.map(t => t.name) : null
		}
		
		console.log('更新笔记数据:', noteData)
		
		// 3. 更新笔记
		const result = await updateNote(noteId.value, noteData)
		console.log('更新结果:', result)
		
		uni.hideLoading()
		uni.showToast({
			title: '保存成功',
			icon: 'success',
			duration: 1500
		})
		
		// 返回详情页
		setTimeout(() => {
			uni.navigateBack()
		}, 1500)
		
	} catch (e) {
		uni.hideLoading()
		console.error('保存失败:', e)
		uni.showToast({
			title: e.message || '保存失败，请重试',
			icon: 'none',
			duration: 2000
		})
	} finally {
		updating.value = false
	}
}

const chooseImage = () => {
	const remainCount = 9 - imageList.value.length
	if (remainCount <= 0) {
		uni.showToast({
			title: '最多上传9张图片',
			icon: 'none'
		})
		return
	}
	
	uni.chooseImage({
		count: remainCount,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: (res) => {
			imageList.value.push(...res.tempFilePaths)
		}
	})
}

const removeImage = (index) => {
	imageList.value.splice(index, 1)
}

const selectShop = async () => {
	try {
		uni.showLoading({ title: '加载商户...' })
		const result = await getRegisteredShops('', 1, 50)
		uni.hideLoading()
		
		if (result.list && result.list.length > 0) {
			shopList.value = result.list
			filteredShopList.value = result.list
			showShopModal.value = true
		} else {
			uni.showToast({ title: '暂无可关联的商户', icon: 'none' })
		}
	} catch (e) {
		uni.hideLoading()
		console.error('加载商户失败:', e)
		uni.showToast({ title: '加载商户失败', icon: 'none' })
	}
}

const closeShopModal = () => {
	showShopModal.value = false
	shopSearchKeyword.value = ''
	filteredShopList.value = shopList.value
}

const selectShopItem = (shop) => {
	selectedShop.value = shop
	uni.showToast({ 
		title: '已选择商户', 
		icon: 'success',
		duration: 800
	})
	setTimeout(() => {
		closeShopModal()
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

const showTopicInput = async () => {
	if (selectedTopics.value.length >= 5) {
		uni.showToast({ title: '最多选择5个话题', icon: 'none' })
		return
	}
	
	showTopicModal.value = true
	
	// 加载热门话题
	if (hotTopics.value.length === 0) {
		try {
			const topics = await getHotTopics(1, 20)
			if (topics.list && topics.list.length > 0) {
				hotTopics.value = topics.list.filter(
					t => !selectedTopics.value.find(st => st.name === t.name)
				)
			}
		} catch (e) {
			console.error('加载热门话题失败:', e)
		}
	}
}

const closeTopicModal = () => {
	showTopicModal.value = false
	topicInput.value = ''
}

const addCustomTopic = () => {
	const topicName = topicInput.value.trim()
	
	if (!topicName) {
		uni.showToast({ title: '请输入话题名称', icon: 'none' })
		return
	}
	
	if (topicName.length > 20) {
		uni.showToast({ title: '话题名称不能超过20个字', icon: 'none' })
		return
	}
	
	if (selectedTopics.value.length >= 5) {
		uni.showToast({ title: '最多选择5个话题', icon: 'none' })
		return
	}
	
	if (selectedTopics.value.find(t => t.name === topicName)) {
		uni.showToast({ title: '该话题已添加', icon: 'none' })
		return
	}
	
	selectedTopics.value.push({
		id: null,
		name: topicName
	})
	
	topicInput.value = ''
	uni.showToast({ 
		title: `已添加 (${selectedTopics.value.length}/5)`, 
		icon: 'success',
		duration: 1000
	})
}

const removeTopic = (index) => {
	const removed = selectedTopics.value.splice(index, 1)[0]
	
	if (removed.id) {
		hotTopics.value.unshift(removed)
	}
	
	uni.showToast({ 
		title: '已移除', 
		icon: 'success',
		duration: 800
	})
}

const isTopicSelected = (topic) => {
	return selectedTopics.value.some(t => t.name === topic.name)
}

const toggleHotTopic = (topic) => {
	const index = selectedTopics.value.findIndex(t => t.name === topic.name)
	
	if (index >= 0) {
		selectedTopics.value.splice(index, 1)
		uni.showToast({ 
			title: '已移除', 
			icon: 'success',
			duration: 800
		})
	} else {
		if (selectedTopics.value.length >= 5) {
			uni.showToast({ title: '最多选择5个话题', icon: 'none' })
			return
		}
		
		selectedTopics.value.push(topic)
		uni.showToast({ 
			title: `已添加 (${selectedTopics.value.length}/5)`, 
			icon: 'success',
			duration: 1000
		})
	}
}

const chooseLocation = () => {
	uni.navigateTo({
		url: '/pages/location-picker/location-picker'
	})
}

const handleLocationSelected = (data) => {
	console.log('✅ 收到位置数据:', data)
	location.value = data.name
	latitude.value = data.latitude
	longitude.value = data.longitude
	
	uni.showToast({
		title: '位置已选择',
		icon: 'success',
		duration: 1500
	})
}
</script>

<style lang="scss" scoped>
.container {
	background: #fff;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
}

.navbar {
	height: 108rpx;
	padding: 0 32rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	background: rgba(255, 255, 255, 0.95);
	backdrop-filter: blur(20rpx);
	position: sticky;
	top: 0;
	z-index: 100;
	border-bottom: 1rpx solid rgba(0, 0, 0, 0.03);
}

.nav-cancel {
	font-size: 32rpx;
	color: #2d3436;
	font-weight: 500;
}

.publish-btn {
	background: linear-gradient(135deg, #ffaf40, #ff9f43);
	color: #fff;
	padding: 12rpx 36rpx;
	border-radius: 40rpx;
	font-size: 28rpx;
	font-weight: 600;
	box-shadow: 0 8rpx 24rpx rgba(255, 159, 67, 0.3);
	transition: all 0.2s;
	
	&:active {
		transform: scale(0.96);
		box-shadow: 0 4rpx 12rpx rgba(255, 159, 67, 0.2);
	}
	
	&.disabled {
		background: #ccc;
		box-shadow: none;
	}
}

.scroll-area {
	flex: 1;
	padding: 0 40rpx 80rpx 40rpx;
}

.input-title-wrapper {
	padding: 40rpx 0 20rpx 0;
	border-bottom: 1rpx solid #f1f2f6;
}

.input-title {
	width: 100%;
	font-size: 36rpx;
	font-weight: 700;
	color: #2d3436;
	background: transparent;
	
	&::placeholder {
		color: #cfd6e0;
		font-weight: 600;
	}
}

.input-content-wrapper {
	padding: 30rpx 0;
	position: relative;
}

.input-content {
	width: 100%;
	font-size: 30rpx;
	color: #2d3436;
	min-height: 240rpx;
	line-height: 1.6;
	background: transparent;
	
	&::placeholder {
		color: #cfd6e0;
	}
}

.char-count {
	text-align: right;
	font-size: 22rpx;
	color: #dfe6e9;
	margin-top: 20rpx;
	
	.count-current {
		color: #b2bec3;
		
		&.warning {
			color: #f97316;
		}
		
		&.full {
			color: #dc2626;
			font-weight: 600;
		}
	}
}

.media-grid {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 16rpx;
	margin-bottom: 60rpx;
}

.media-item {
	aspect-ratio: 1/1;
	border-radius: 24rpx;
	overflow: hidden;
	position: relative;
	background: #f1f2f6;
}

.media-img {
	width: 100%;
	height: 100%;
}

.btn-delete {
	position: absolute;
	top: 8rpx;
	right: 8rpx;
	width: 40rpx;
	height: 40rpx;
	background: rgba(0, 0, 0, 0.6);
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	backdrop-filter: blur(4rpx);
	
	text {
		color: #fff;
		font-size: 24rpx;
		line-height: 1;
	}
}

.btn-upload {
	border: 3rpx dashed #d1d8e0;
	background: #fafbfc;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	transition: all 0.2s;
	
	&:active {
		background: #f1f2f6;
		border-color: #a4b0be;
	}
}

.upload-icon {
	font-size: 48rpx;
	margin-bottom: 8rpx;
}

.upload-text {
	font-size: 22rpx;
	color: #b2bec3;
	font-weight: 500;
}

.options-group {
	border-top: 1rpx solid #f1f2f6;
	padding-top: 20rpx;
}

.cell-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 32rpx 8rpx;
	transition: opacity 0.2s;
	
	&:active {
		opacity: 0.6;
	}
	
	&:not(:last-child) {
		border-bottom: 1rpx solid #fcfcfc;
	}
}

.cell-left {
	display: flex;
	align-items: center;
	gap: 24rpx;
}

.icon-container {
	width: 64rpx;
	height: 64rpx;
	border-radius: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28rpx;
}

.theme-yellow {
	background: #fff5e6;
	color: #ff9f43;
	font-weight: bold;
}

.theme-blue {
	background: #e7f5ff;
	color: #54a0ff;
}

.theme-red {
	background: #ffebec;
	color: #ff6b6b;
}

.cell-title {
	font-size: 30rpx;
	color: #2d3436;
	font-weight: 500;
}

.cell-right {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.cell-value {
	font-size: 26rpx;
	color: #636e72;
	max-width: 300rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	
	&.selected {
		color: #2d3436;
		font-weight: 500;
	}
}

.cell-arrow {
	font-size: 28rpx;
	color: #d1d8e0;
}

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

.topic-modal-new {
	width: 100%;
	max-height: 75vh;
	background: white;
	border-radius: 40rpx 40rpx 0 0;
	animation: slideUp 0.3s ease;
	display: flex;
	flex-direction: column;
}

.topic-input-section {
	padding: 20rpx 32rpx;
	background: #fff;
	border-bottom: 1rpx solid #f0f0f0;
}

.topic-input-wrapper-new {
	background: #f7f9fc;
	border-radius: 20rpx;
	padding: 16rpx 24rpx;
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.topic-hash-new {
	font-size: 32rpx;
	color: #ff9f43;
	font-weight: bold;
}

.topic-input-new {
	flex: 1;
	font-size: 28rpx;
	color: #333;
	background: transparent;
}

.topic-add-btn-new {
	padding: 8rpx 24rpx;
	background: linear-gradient(135deg, #ffaf40, #ff9f43);
	border-radius: 20rpx;
	
	text {
		font-size: 24rpx;
		color: white;
		font-weight: 500;
	}
	
	&:active {
		opacity: 0.8;
	}
}

.topic-list-scroll {
	flex: 1;
	padding: 0 32rpx 40rpx 32rpx;
	max-height: 60vh;
}

.topic-group {
	margin-top: 30rpx;
}

.topic-group-header {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-bottom: 20rpx;
}

.topic-group-icon {
	font-size: 28rpx;
}

.topic-group-title {
	font-size: 26rpx;
	color: #b2bec3;
	font-weight: 500;
}

.topic-tags-wrapper {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.topic-tag-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 14rpx 24rpx;
	background: #f7f9fc;
	border: 2rpx solid #e8e8e8;
	border-radius: 28rpx;
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.95);
	}
	
	&.hot-topic {
		background: white;
		
		&.selected {
			background: linear-gradient(135deg, #fff5e6 0%, #ffedd5 100%);
			border-color: #ff9f43;
		}
	}
}

.topic-tag-icon {
	font-size: 24rpx;
}

.topic-tag-text {
	font-size: 26rpx;
	color: #666;
	
	.topic-tag-item.selected & {
		color: #ff9f43;
		font-weight: 500;
	}
}

.topic-tag-remove {
	font-size: 28rpx;
	color: #ff8a50;
	font-weight: bold;
	line-height: 1;
	margin-left: 4rpx;
}

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
