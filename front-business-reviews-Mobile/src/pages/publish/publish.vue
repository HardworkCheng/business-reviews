<template>
	<view class="container">
		<view class="header">
			<view class="cancel-btn" @click="handleCancel">
				<text class="btn-text">取消</text>
			</view>
			<view class="publish-btn" :class="{ disabled: publishing }" @click="handlePublish">
				<text class="btn-text">{{ publishing ? '发布中...' : '发布' }}</text>
			</view>
		</view>

		<view class="content">
			<!-- 标题输入 -->
			<view class="input-section">
				<input 
					type="text" 
					placeholder="填写标题更容易上首页哦~" 
					v-model="title"
					class="title-input"
					maxlength="100"
				/>
			</view>
			
			<view class="divider"></view>
			
			<!-- 内容输入 -->
			<view class="input-section content-section">
				<textarea 
					placeholder="分享你的美食体验吧~"
					v-model="content"
					class="content-input"
					maxlength="1000"
					auto-height
				></textarea>
				<view class="word-count-inline" :class="{ warning: content.length > 900, full: content.length >= 1000 }">
					<text class="count-current">{{ content.length }}</text>
					<text class="count-separator">/</text>
					<text class="count-total">1000</text>
				</view>
			</view>
			
			<view class="divider"></view>
			
			<!-- 图片上传区 -->
			<view class="images-section">
				<view class="image-list" v-if="imageList.length > 0">
					<view class="image-item" v-for="(img, index) in imageList" :key="index">
						<image :src="img" mode="aspectFill" class="preview-image"></image>
						<view class="delete-btn" @click="removeImage(index)">×</view>
					</view>
					<view class="add-image-btn" v-if="imageList.length < 9" @click="chooseImage">
						<text class="add-icon">+</text>
					</view>
				</view>
				<view class="upload-section dashed-border" @click="chooseImage" v-else>
					<view class="upload-icon clay-icon bg-secondary">
						<text>📷</text>
					</view>
					<text class="upload-text">上传照片</text>
					<text class="upload-tip">支持最多9张图片</text>
				</view>
			</view>
			
			<view class="divider"></view>

			<!-- 添加话题 -->
			<view class="option-item topic-section" :class="{ selected: selectedTopics.length > 0 }">
				<view class="option-label">
					<text class="icon">#</text>
					<view class="topic-content">
						<view v-if="selectedTopics.length > 0" class="selected-topics">
							<view 
								class="topic-tag" 
								v-for="(topic, index) in selectedTopics" 
								:key="index"
								@click.stop="removeTopic(index)"
							>
								<text class="topic-text">#{{ topic.name }}</text>
								<text class="topic-remove">×</text>
							</view>
							<view v-if="selectedTopics.length < 5" class="add-topic-btn" @click="showTopicInput">
								<text class="add-icon">+</text>
							</view>
						</view>
						<view v-else class="topic-placeholder" @click="showTopicInput">
							<text>添加话题 (最多5个)</text>
						</view>
					</view>
				</view>
			</view>
			
			<!-- 话题输入弹窗 -->
			<view v-if="showTopicModal" class="modal-overlay" @click="closeTopicModal">
				<view class="topic-modal" @click.stop>
					<view class="modal-header">
						<text class="modal-title">添加话题</text>
						<text class="modal-close" @click="closeTopicModal">×</text>
					</view>
					<view class="modal-body">
						<view class="topic-input-wrapper">
							<text class="topic-hash">#</text>
							<input 
								class="topic-input" 
								v-model="topicInput"
								placeholder="输入话题名称"
								maxlength="20"
								@confirm="addCustomTopic"
							/>
							<view class="topic-add-btn" @click="addCustomTopic">
								<text>添加</text>
							</view>
						</view>
						<view class="hot-topics-section">
							<text class="section-title">热门话题</text>
							<view class="hot-topics-list">
								<view 
									class="hot-topic-item" 
									v-for="topic in hotTopics" 
									:key="topic.id"
									@click="selectHotTopic(topic)"
								>
									<text class="hot-topic-text">#{{ topic.name }}</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>

			<!-- 关联商户 -->
			<view class="option-item" :class="{ selected: selectedShop }" @click="selectShop">
				<view class="option-label">
					<text class="icon">🏪</text>
					<text>{{ selectedShop ? selectedShop.name : '关联商户' }}</text>
				</view>
				<text class="arrow">›</text>
			</view>

			<!-- 添加位置 -->
			<view class="option-item" :class="{ selected: location }" @click="chooseLocation">
				<view class="option-label">
					<text class="icon">📍</text>
					<text>{{ location ? location : '添加位置' }}</text>
				</view>
				<text class="arrow">›</text>
			</view>
		</view>


	</view>
</template>

<script setup>
import { ref } from 'vue'
import { publishNote } from '../../api/note'
import { uploadImages } from '../../api/upload'
import { getHotTopics, search } from '../../api/common'
import { getRegisteredShops } from '../../api/shop'

const title = ref('')
const content = ref('')
const imageList = ref([]) // 存储临时文件路径
const uploadedImageUrls = ref([]) // 存储已上传的图片URL
const selectedShop = ref(null)
const selectedTopics = ref([])
const location = ref('')
const latitude = ref(null)
const longitude = ref(null)
const publishing = ref(false)
const showTopicModal = ref(false)
const topicInput = ref('')
const hotTopics = ref([])

const handleCancel = () => {
	if (title.value || content.value) {
		uni.showModal({
			title: '提示',
			content: '确定要取消发布吗？已编辑内容将不会保存',
			success: (res) => {
				if (res.confirm) {
					uni.switchTab({ url: '/pages/index/index' })
				}
			}
		})
	} else {
		uni.switchTab({ url: '/pages/index/index' })
	}
}

const handlePublish = async () => {
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
	if (publishing.value) {
		console.log('正在发布中，请勿重复提交')
		return
	}
	
	publishing.value = true
	uni.showLoading({ title: '发布中...', mask: true })
	
	try {
		// 1. 上传图片（只上传一次）
		let imageUrls = []
		if (uploadedImageUrls.value.length === 0) {
			console.log('开始上传图片:', imageList.value.length)
			const uploadResult = await uploadImages(imageList.value)
			imageUrls = uploadResult.urls
			uploadedImageUrls.value = imageUrls
			console.log('图片上传成功:', imageUrls)
		} else {
			imageUrls = uploadedImageUrls.value
			console.log('使用已缓存的图片URL:', imageUrls)
		}
		
		// 2. 构建发布数据（默认公开，status=1）
		const noteData = {
			title: title.value.trim(),
			content: content.value.trim(),
			images: imageUrls,
			shopId: selectedShop.value ? selectedShop.value.id : null,
			location: location.value || null,
			latitude: latitude.value,
			longitude: longitude.value,
			// 只提交有id的话题（已存在的话题），自定义话题id为null会被过滤
			topics: selectedTopics.value.length > 0 ? selectedTopics.value.filter(t => t.id).map(t => t.id) : null,
			status: 1 // 默认公开
		}
		
		console.log('发布笔记数据:', noteData)
		
		// 3. 发布笔记
		const result = await publishNote(noteData)
		console.log('发布结果:', result)
		
		uni.hideLoading()
		uni.showToast({
			title: '发布成功',
			icon: 'success',
			duration: 1500
		})
		
		// 清空表单
		clearForm()
		
		// 跳转到首页
		setTimeout(() => {
			uni.switchTab({ url: '/pages/index/index' })
		}, 1500)
		
	} catch (e) {
		uni.hideLoading()
		console.error('发布失败:', e)
		uni.showToast({
			title: e.message || '发布失败，请重试',
			icon: 'none',
			duration: 2000
		})
	} finally {
		publishing.value = false
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
			const shopNames = result.list.map(s => s.name)
			uni.showActionSheet({
				itemList: shopNames,
				success: (res) => {
					selectedShop.value = result.list[res.tapIndex]
					uni.showToast({ title: '已选择商户', icon: 'success' })
				}
			})
		} else {
			uni.showToast({ title: '暂无可关联的商户', icon: 'none' })
		}
	} catch (e) {
		uni.hideLoading()
		console.error('加载商户失败:', e)
		uni.showToast({ title: '加载商户失败', icon: 'none' })
	}
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
				// 过滤掉已选择的话题
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
	
	// 检查是否已存在
	if (selectedTopics.value.find(t => t.name === topicName)) {
		uni.showToast({ title: '该话题已添加', icon: 'none' })
		return
	}
	
	// 添加自定义话题（id为null表示自定义）
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

const selectHotTopic = (topic) => {
	if (selectedTopics.value.length >= 5) {
		uni.showToast({ title: '最多选择5个话题', icon: 'none' })
		return
	}
	
	// 检查是否已存在
	if (selectedTopics.value.find(t => t.name === topic.name)) {
		uni.showToast({ title: '该话题已添加', icon: 'none' })
		return
	}
	
	selectedTopics.value.push(topic)
	
	// 从热门列表中移除
	hotTopics.value = hotTopics.value.filter(t => t.id !== topic.id)
	
	uni.showToast({ 
		title: `已添加 (${selectedTopics.value.length}/5)`, 
		icon: 'success',
		duration: 1000
	})
}

const removeTopic = (index) => {
	const removed = selectedTopics.value.splice(index, 1)[0]
	
	// 如果是热门话题，重新加入热门列表
	if (removed.id) {
		hotTopics.value.unshift(removed)
	}
	
	uni.showToast({ 
		title: '已移除', 
		icon: 'success',
		duration: 800
	})
}

const chooseLocation = () => {
	// 跳转到自定义位置选择页面
	uni.navigateTo({
		url: '/pages/location-picker/location-picker'
	})
}

// 监听页面显示和位置选择事件
import { onShow, onUnload } from '@dcloudio/uni-app'

onShow(() => {
	// 监听位置选择事件
	uni.$on('locationSelected', handleLocationSelected)
})

onUnload(() => {
	// 移除事件监听
	uni.$off('locationSelected', handleLocationSelected)
})

// 处理位置选择
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

const clearForm = () => {
	title.value = ''
	content.value = ''
	imageList.value = []
	uploadedImageUrls.value = [] // 清空已上传的图片URL
	selectedShop.value = null
	selectedTopics.value = []
	location.value = ''
	latitude.value = null
	longitude.value = null
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
	background: white;
	padding: 20rpx 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 2rpx 16rpx rgba(0, 0, 0, 0.06);
	position: sticky;
	top: 0;
	z-index: 100;
}

.cancel-btn, .publish-btn {
	min-width: 120rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 30rpx;
	transition: all 0.3s ease;
	
	.btn-text {
		font-size: 30rpx;
		font-weight: 500;
	}
	
	&:active {
		transform: scale(0.95);
	}
}

.cancel-btn {
	background: #f5f5f5;
	
	.btn-text {
		color: #666;
	}
	
	&:active {
		background: #e8e8e8;
	}
}

.publish-btn {
	background: linear-gradient(135deg, #FF9E64 0%, #FF8A50 100%);
	box-shadow: 0 4rpx 12rpx rgba(255, 158, 100, 0.3);
	
	.btn-text {
		color: white;
	}
	
	&.disabled {
		background: #ccc;
		box-shadow: none;
		opacity: 0.6;
	}
	
	&:active:not(.disabled) {
		box-shadow: 0 2rpx 8rpx rgba(255, 158, 100, 0.4);
	}
}



.content {
	flex: 1;
	padding: 30rpx;
	overflow-y: auto;
}

// 图片区域
.images-section {
	margin-bottom: 40rpx;
}

.image-list {
	display: flex;
	flex-wrap: wrap;
	gap: 20rpx;
}

.image-item {
	position: relative;
	width: 200rpx;
	height: 200rpx;
}

.preview-image {
	width: 100%;
	height: 100%;
	border-radius: 20rpx;
}

.delete-btn {
	position: absolute;
	top: -10rpx;
	right: -10rpx;
	width: 50rpx;
	height: 50rpx;
	border-radius: 50%;
	background: rgba(0, 0, 0, 0.6);
	color: white;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 36rpx;
	line-height: 1;
}

.add-image-btn {
	width: 200rpx;
	height: 200rpx;
	border: 3rpx dashed #ccc;
	border-radius: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.add-icon {
	font-size: 60rpx;
	color: #999;
}

.upload-section {
	background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
	padding: 80rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 40rpx;
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.98);
	}
}

.dashed-border {
	border: 3rpx dashed #FF9E64;
	border-radius: 40rpx;
}

.upload-icon {
	width: 120rpx;
	height: 120rpx;
	font-size: 60rpx;
	margin-bottom: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #FFE4CC 0%, #FFD4A8 100%);
	border-radius: 50%;
	box-shadow: 0 4rpx 12rpx rgba(255, 158, 100, 0.2);
}

.upload-text {
	font-size: 32rpx;
	font-weight: 500;
	margin-bottom: 10rpx;
}

.upload-tip {
	font-size: 24rpx;
	color: #999;
}

.input-section {
	background: white;
	border-radius: 24rpx;
	padding: 25rpx 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
	transition: all 0.3s ease;
	
	&:focus-within {
		box-shadow: 0 4rpx 20rpx rgba(255, 158, 100, 0.15);
		transform: translateY(-2rpx);
	}
}

.content-section {
	position: relative;
	min-height: 300rpx;
}

.title-input {
	width: 100%;
	font-size: 36rpx;
	font-weight: 600;
	color: #333;
	background: transparent;
	border: none;
	
	&::placeholder {
		color: #bbb;
		font-weight: 500;
	}
}

.divider {
	height: 1rpx;
	background: linear-gradient(90deg, transparent, #e8e8e8, transparent);
	margin: 30rpx 0;
}

.content-input {
	width: 100%;
	min-height: 280rpx;
	font-size: 32rpx;
	line-height: 1.8;
	color: #333;
	background: transparent;
	border: none;
	padding-bottom: 60rpx;
	
	&::placeholder {
		color: #bbb;
	}
}

.word-count-inline {
	position: absolute;
	right: 30rpx;
	bottom: 25rpx;
	display: flex;
	align-items: baseline;
	gap: 4rpx;
	padding: 8rpx 18rpx;
	background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
	border-radius: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(14, 165, 233, 0.1);
	transition: all 0.3s ease;
	
	&.warning {
		background: linear-gradient(135deg, #fff7ed 0%, #ffedd5 100%);
		box-shadow: 0 2rpx 8rpx rgba(249, 115, 22, 0.15);
		
		.count-current {
			color: #f97316;
		}
	}
	
	&.full {
		background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
		box-shadow: 0 2rpx 8rpx rgba(220, 38, 38, 0.15);
		animation: pulse 1.5s ease-in-out infinite;
		
		.count-current {
			color: #dc2626;
			font-weight: 700;
		}
	}
}

@keyframes pulse {
	0%, 100% {
		transform: scale(1);
	}
	50% {
		transform: scale(1.05);
	}
}

.count-current {
	font-size: 28rpx;
	font-weight: 600;
	color: #0ea5e9;
	transition: color 0.3s ease;
}

.count-separator {
	font-size: 22rpx;
	color: #cbd5e1;
	margin: 0 2rpx;
}

.count-total {
	font-size: 24rpx;
	color: #94a3b8;
	font-weight: 500;
}

// 选项区域样式
.option-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx 25rpx;
	margin-bottom: 16rpx;
	background: white;
	border-radius: 20rpx;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
	transition: all 0.3s ease;
	border: 2rpx solid transparent;
	
	&:active {
		transform: translateX(4rpx);
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
	}
	
	&.selected {
		border-color: #FFE4CC;
		background: linear-gradient(135deg, #FFFBF5 0%, #FFF8F0 100%);
	}
}

.option-label {
	display: flex;
	align-items: center;
	font-size: 30rpx;
	flex: 1;
	overflow: hidden;
}

.option-label .icon {
	width: 56rpx;
	height: 56rpx;
	font-size: 34rpx;
	margin-right: 24rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #FFE4CC 0%, #FFD4A8 100%);
	border-radius: 14rpx;
	box-shadow: 0 2rpx 8rpx rgba(255, 158, 100, 0.15);
}

.option-label text:last-child {
	flex: 1;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	color: #333;
}

// 已选中状态
.option-item.selected .option-label text:last-child {
	color: #FF9E64;
	font-weight: 500;
}

// 话题区域
.topic-section {
	.option-label {
		width: 100%;
	}
}

.topic-content {
	flex: 1;
	min-height: 40rpx;
}

.topic-placeholder {
	flex: 1;
	padding: 10rpx 0;
	
	text {
		color: #999;
		font-size: 30rpx;
	}
}

.selected-topics {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	align-items: center;
}

.topic-tag {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 8rpx 16rpx;
	background: linear-gradient(135deg, #FFE4CC 0%, #FFD4A8 100%);
	border-radius: 20rpx;
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.95);
		opacity: 0.8;
	}
}

.topic-text {
	font-size: 26rpx;
	color: #FF9E64;
	font-weight: 500;
}

.topic-remove {
	font-size: 32rpx;
	color: #FF8A50;
	font-weight: bold;
	line-height: 1;
}

.add-topic-btn {
	width: 56rpx;
	height: 56rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: white;
	border-radius: 50%;
	border: 3rpx dashed #0ea5e9;
	transition: all 0.3s ease;
	flex-shrink: 0;
	
	&:active {
		transform: scale(0.9);
		background: #f0f9ff;
	}
}

.add-icon {
	font-size: 36rpx;
	color: #0ea5e9;
	font-weight: bold;
	line-height: 1;
	display: flex;
	align-items: center;
	justify-content: center;
}

// 话题弹窗
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
	from {
		opacity: 0;
	}
	to {
		opacity: 1;
	}
}

.topic-modal {
	width: 100%;
	max-height: 80vh;
	background: white;
	border-radius: 40rpx 40rpx 0 0;
	animation: slideUp 0.3s ease;
}

@keyframes slideUp {
	from {
		transform: translateY(100%);
	}
	to {
		transform: translateY(0);
	}
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

.modal-body {
	padding: 30rpx 40rpx;
	max-height: 70vh;
	overflow-y: auto;
}

.topic-input-wrapper {
	display: flex;
	align-items: center;
	gap: 12rpx;
	padding: 20rpx 25rpx;
	background: #f7f9fc;
	border-radius: 30rpx;
	margin-bottom: 30rpx;
}

.topic-hash {
	font-size: 32rpx;
	color: #FF9E64;
	font-weight: bold;
}

.topic-input {
	flex: 1;
	font-size: 30rpx;
	color: #333;
	background: transparent;
	border: none;
}

.topic-add-btn {
	padding: 10rpx 24rpx;
	background: linear-gradient(135deg, #FF9E64 0%, #FF8A50 100%);
	border-radius: 20rpx;
	
	text {
		font-size: 26rpx;
		color: white;
		font-weight: 500;
	}
	
	&:active {
		opacity: 0.8;
	}
}

.hot-topics-section {
	margin-top: 20rpx;
}

.section-title {
	font-size: 28rpx;
	color: #666;
	font-weight: 500;
	margin-bottom: 20rpx;
	display: block;
}

.hot-topics-list {
	display: flex;
	flex-wrap: wrap;
	gap: 16rpx;
}

.hot-topic-item {
	padding: 12rpx 24rpx;
	background: white;
	border: 2rpx solid #e8e8e8;
	border-radius: 24rpx;
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.95);
		border-color: #FF9E64;
		background: #FFF8F0;
	}
}

.hot-topic-text {
	font-size: 28rpx;
	color: #666;
}

.arrow {
	font-size: 40rpx;
	color: #ccc;
	margin-left: 10rpx;
}


</style>
