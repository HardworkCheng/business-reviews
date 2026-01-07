<template>
	<view class="container">
		<!-- 顶部导航栏 -->
		<view class="navbar">
			<text class="nav-cancel" @click="handleCancel">取消</text>
			<view class="publish-btn" :class="{ disabled: publishing }" @click="handlePublish">
				<text>{{ publishing ? '发布中...' : '发布' }}</text>
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
				<view class="content-footer">
					<!-- AI魔法生成按钮 (Magic Ball) -->
					<view 
						class="magic-ball-wrapper" 
						v-if="canUseMagic || generating"
						@click="onMagicGenerate"
					>
						<view class="magic-ball" :class="{ 'rotating': generating }">
							<text class="magic-icon">✨</text>
							<view class="ball-highlight"></view>
						</view>
					</view>
					<view class="char-count">
						<text class="count-current" :class="{ warning: content.length > 900, full: content.length >= 1000 }">{{ content.length }}</text>
						<text>/1000</text>
					</view>
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
		
		<TopicSelector v-model="selectedTopics" v-model:visible="showTopicModal" />
		<ShopSelector v-model="selectedShop" v-model:visible="showShopModal" />
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { publishNote } from '../../api/note'
import TopicSelector from '../../components/TopicSelector.vue'
import ShopSelector from '../../components/ShopSelector.vue'
import { useImageUpload } from '../../composables/useImageUpload'
import { useAiNote } from '../../composables/useAiNote'
import { useLocation } from '../../composables/useLocation'

const title = ref('')
const content = ref('')
const selectedShop = ref(null)
const selectedTopics = ref([])
const publishing = ref(false)
const showTopicModal = ref(false)
const showShopModal = ref(false)

// Use Composables
const { imageList, chooseImage, removeImage, uploadImages, clearImages } = useImageUpload()
const { location, latitude, longitude, chooseLocation, clearLocation } = useLocation()
const { generating, handleMagicGenerate } = useAiNote()

// 计算属性：是否可以使用魔法按钮（有图片或有标签时）
const canUseMagic = computed(() => {
	return imageList.value.length > 0 || selectedTopics.value.length > 0
})

const onMagicGenerate = () => {
    handleMagicGenerate({
        imageList: imageList.value,
        uploadImagesFunc: uploadImages,
        selectedShop: selectedShop.value,
        selectedTopics: selectedTopics.value,
        titleRef: title,
        contentRef: content
    })
}

const showTopicInput = () => {
	if (selectedTopics.value.length >= 5) {
		uni.showToast({ title: '最多选择5个话题', icon: 'none' })
		return
	}
	showTopicModal.value = true
}

const selectShop = () => {
	showShopModal.value = true
}

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
		// 1. 上传图片（只上传一次，内部有缓存检查）
		const uploadResult = await uploadImages()
		const imageUrls = uploadResult.urls || uploadResult
		
		// 2. 构建发布数据（默认公开，status=1）
		const noteData = {
			title: title.value.trim(),
			content: content.value.trim(),
			images: imageUrls,
			shopId: selectedShop.value ? selectedShop.value.id : null,
			location: location.value || null,
			latitude: latitude.value,
			longitude: longitude.value,
			// 发送话题名称列表（支持自定义话题）
			topicNames: selectedTopics.value.length > 0 ? selectedTopics.value.map(t => t.name) : null,
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

const clearForm = () => {
	title.value = ''
	content.value = ''
    clearImages()
	selectedShop.value = null
	selectedTopics.value = []
    clearLocation()
}
</script>

<style lang="scss" scoped>
.container {
	background: #fff;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
}

// 顶部导航栏
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

// 滚动区域
.scroll-area {
	flex: 1;
	padding: 0 40rpx 80rpx 40rpx;
}

// 标题输入
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

// 内容输入
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

// 内容区底部（魔法按钮+字数统计）
.content-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 20rpx;
	height: 80rpx;
	
	.char-count {
		margin-top: 0;
	}
}

// AI魔法球按钮容器
.magic-ball-wrapper {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 10rpx;
	border-radius: 50%;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	
	&:active {
		transform: scale(0.92);
	}
}

// 魔法球主体
.magic-ball {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
	// 橙金渐变 - 与发布页面风格统一
	background: radial-gradient(circle at 35% 35%, #fff0d1 0%, #ffaf40 45%, #ff9f43 100%);
	box-shadow: 0 4rpx 16rpx rgba(255, 159, 67, 0.4),
				inset -4rpx -4rpx 8rpx rgba(230, 126, 34, 0.2),
				inset 4rpx 4rpx 8rpx rgba(255, 255, 255, 0.8);
	z-index: 10;
	
	// 旋转动画状态
	&.rotating {
		animation: ballSpin 1.2s linear infinite;
		// 旋转时流光溢彩的橙色系
		background: conic-gradient(from 0deg, #ff9f43, #ffeaa7, #fab1a0, #ff9f43);
		box-shadow: 0 0 24rpx rgba(255, 159, 67, 0.6);
		
		.magic-icon {
			animation: iconReverseSpin 1.2s linear infinite;
			opacity: 0.9;
		}
	}
}

// 高光效果
.ball-highlight {
	position: absolute;
	top: 12rpx;
	left: 14rpx;
	width: 28rpx;
	height: 14rpx;
	background: rgba(255, 255, 255, 0.7);
	border-radius: 50%;
	transform: rotate(-45deg);
	filter: blur(3rpx);
	pointer-events: none;
}

.magic-icon {
	font-size: 36rpx;
	color: #fff;
	text-shadow: 0 2rpx 4rpx rgba(211, 84, 0, 0.3); // 深橙色阴影
	z-index: 2;
}

@keyframes ballSpin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

@keyframes iconReverseSpin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(-360deg); }
}


// 图片九宫格
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

// 选项列表
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
</style>
