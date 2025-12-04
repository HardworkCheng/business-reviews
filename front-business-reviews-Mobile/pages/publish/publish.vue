<template>
	<view class="container">
		<view class="header">
			<button class="cancel-btn" @click="handleCancel">取消</button>
			<button class="publish-btn bg-primary clay-border" @click="handlePublish" :disabled="publishing">
				{{ publishing ? '发布中...' : '发布' }}
			</button>
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
			<view class="input-section">
				<textarea 
					placeholder="分享你的美食体验吧~"
					v-model="content"
					class="content-input"
					maxlength="1000"
					auto-height
				></textarea>
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

			<!-- 关联商户 -->
			<view class="option-item" @click="selectShop">
				<view class="option-label">
					<text class="icon">📍</text>
					<text>{{ selectedShop ? selectedShop.name : '关联商户' }}</text>
				</view>
				<text class="arrow">›</text>
			</view>

			<!-- 添加话题 -->
			<view class="option-item" @click="showTopicPicker">
				<view class="option-label">
					<text class="icon">#</text>
					<text>{{ selectedTopics.length > 0 ? `已选${selectedTopics.length}个话题` : '添加话题' }}</text>
				</view>
				<text class="arrow">›</text>
			</view>

			<!-- 添加标签 -->
			<view class="option-item" @click="showTagInput">
				<view class="option-label">
					<text class="icon">🏷️</text>
					<text>{{ tags.length > 0 ? tags.join(' ') : '添加标签' }}</text>
				</view>
				<text class="arrow">›</text>
			</view>

			<!-- 添加位置 -->
			<view class="option-item" @click="chooseLocation">
				<view class="option-label">
					<text class="icon">📌</text>
					<text>{{ location ? location : '添加位置' }}</text>
				</view>
				<text class="arrow">›</text>
			</view>

			<!-- 可见性设置 -->
			<view class="option-item" @click="toggleVisibility">
				<view class="option-label">
					<text class="icon">👁️</text>
					<text>{{ visibility === 'public' ? '公开' : '仅自己可见' }}</text>
				</view>
				<text class="arrow">›</text>
			</view>
		</view>

		<view class="footer">
			<view class="toolbar">
				<button class="tool-btn" @click="addEmoji">😊</button>
				<button class="tool-btn" @click="atUser">@</button>
			</view>
			<text class="word-count">{{ content.length }}/1000</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { publishNote } from '../../api/note'
import { uploadImages } from '../../api/upload'
import { getHotTopics, search } from '../../api/common'

const title = ref('')
const content = ref('')
const imageList = ref([]) // 存储临时文件路径
const uploadedImageUrls = ref([]) // 存储已上传的图片URL
const selectedShop = ref(null)
const selectedTopics = ref([])
const tags = ref([])
const location = ref('')
const latitude = ref(null)
const longitude = ref(null)
const visibility = ref('public') // 'public' 或 'private'
const publishing = ref(false)

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
		
		// 2. 构建发布数据
		const noteData = {
			title: title.value.trim(),
			content: content.value.trim(),
			images: imageUrls,
			shopId: selectedShop.value ? selectedShop.value.id : null,
			location: location.value || null,
			latitude: latitude.value,
			longitude: longitude.value,
			tags: tags.value.length > 0 ? tags.value : null,
			topics: selectedTopics.value.length > 0 ? selectedTopics.value.map(t => t.id) : null,
			status: visibility.value === 'public' ? 1 : 2
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

const selectShop = () => {
	// 搜索商家
	uni.showModal({
		title: '关联商户',
		editable: true,
		placeholderText: '搜索商户名称',
		success: async (res) => {
			if (res.confirm && res.content) {
				try {
					uni.showLoading({ title: '搜索中...' })
					const result = await search(res.content, 'shop', 1, 10)
					uni.hideLoading()
					
					if (result.shops && result.shops.length > 0) {
						// 简化：直接选择第一个
						selectedShop.value = result.shops[0]
						uni.showToast({ title: '已选择商户', icon: 'success' })
					} else {
						uni.showToast({ title: '未找到商户', icon: 'none' })
					}
				} catch (e) {
					uni.hideLoading()
					uni.showToast({ title: '搜索失败', icon: 'none' })
				}
			}
		}
	})
}

const showTopicPicker = async () => {
	try {
		uni.showLoading({ title: '加载话题...' })
		const topics = await getHotTopics(1, 20)
		uni.hideLoading()
		
		if (topics.list && topics.list.length > 0) {
			const topicNames = topics.list.map(t => t.name)
			uni.showActionSheet({
				itemList: topicNames,
				success: (res) => {
					const topic = topics.list[res.tapIndex]
					if (!selectedTopics.value.find(t => t.id === topic.id)) {
						selectedTopics.value.push(topic)
						uni.showToast({ title: '已添加话题', icon: 'success' })
					}
				}
			})
		} else {
			uni.showToast({ title: '暂无话题', icon: 'none' })
		}
	} catch (e) {
		uni.hideLoading()
		uni.showToast({ title: '加载话题失败', icon: 'none' })
	}
}

const showTagInput = () => {
	uni.showModal({
		title: '添加标签',
		editable: true,
		placeholderText: '输入标签，多个用空格分隔',
		content: tags.value.join(' '),
		success: (res) => {
			if (res.confirm && res.content) {
				const newTags = res.content.split(/\s+/).filter(t => t.trim())
				tags.value = newTags
				uni.showToast({ title: '标签已更新', icon: 'success' })
			}
		}
	})
}

const chooseLocation = () => {
	uni.chooseLocation({
		success: (res) => {
			location.value = res.name || res.address
			latitude.value = res.latitude
			longitude.value = res.longitude
			uni.showToast({ title: '位置已添加', icon: 'success' })
		},
		fail: () => {
			uni.showToast({ title: '获取位置失败', icon: 'none' })
		}
	})
}

const toggleVisibility = () => {
	uni.showActionSheet({
		itemList: ['公开', '仅自己可见'],
		success: (res) => {
			visibility.value = res.tapIndex === 0 ? 'public' : 'private'
		}
	})
}

const addEmoji = () => {
	uni.showToast({ title: '表情功能开发中', icon: 'none' })
}

const atUser = () => {
	uni.showToast({ title: '@功能开发中', icon: 'none' })
}

const clearForm = () => {
	title.value = ''
	content.value = ''
	imageList.value = []
	uploadedImageUrls.value = [] // 清空已上传的图片URL
	selectedShop.value = null
	selectedTopics.value = []
	tags.value = []
	location.value = ''
	latitude.value = null
	longitude.value = null
	visibility.value = 'public'
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
	padding: 25rpx 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.cancel-btn {
	background: none;
	border: none;
	font-size: 32rpx;
	padding: 0;
}

.publish-btn {
	padding: 15rpx 40rpx;
	color: white;
	font-size: 28rpx;
	border: none;
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
	background: white;
	padding: 80rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 40rpx;
}

.dashed-border {
	border: 3rpx dashed #000;
	border-radius: 40rpx;
}

.upload-icon {
	width: 120rpx;
	height: 120rpx;
	font-size: 60rpx;
	margin-bottom: 20rpx;
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
	border-radius: 20rpx;
	padding: 20rpx 30rpx;
	margin-bottom: 20rpx;
}

.title-input {
	width: 100%;
	font-size: 36rpx;
	font-weight: 500;
	color: #333;
	background: white;
	border: none;
}

.divider {
	height: 1rpx;
	background: #e0e0e0;
	margin: 30rpx 0;
}

.content-input {
	width: 100%;
	min-height: 200rpx;
	font-size: 32rpx;
	line-height: 1.6;
	color: #333;
	background: white;
	border: none;
}

.option-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 30rpx 0;
	border-top: 1rpx solid #f0f0f0;
}

.option-label {
	display: flex;
	align-items: center;
	font-size: 32rpx;
}

.option-label .icon {
	font-size: 36rpx;
	margin-right: 15rpx;
	color: #FF9E64;
}

.arrow {
	font-size: 48rpx;
	color: #ccc;
}

.footer {
	background: white;
	border-top: 3rpx solid #000;
	padding: 20rpx 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.toolbar {
	display: flex;
	gap: 30rpx;
}

.tool-btn {
	background: none;
	border: none;
	font-size: 40rpx;
	padding: 0;
}

.word-count {
	font-size: 26rpx;
	color: #999;
}
</style>
