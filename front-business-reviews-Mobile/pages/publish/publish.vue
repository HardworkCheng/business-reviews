<template>
	<view class="container">
		<view class="header">
			<button class="cancel-btn" @click="handleCancel">取消</button>
			<button class="publish-btn bg-primary clay-border" @click="handlePublish">发布</button>
		</view>

		<view class="content">
			<view class="upload-section dashed-border" @click="chooseImage">
				<view class="upload-icon clay-icon bg-secondary">
					<text>📷</text>
				</view>
				<text class="upload-text">上传照片</text>
				<text class="upload-tip">支持 JPG、PNG 格式</text>
			</view>

			<view class="input-section">
				<input 
					type="text" 
					placeholder="填写标题更容易上首页哦~" 
					v-model="title"
					class="title-input"
				/>
			</view>

			<view class="divider"></view>

			<view class="input-section">
				<textarea 
					placeholder="最近打卡了什么地方，有什么新奇体验呢？"
					v-model="content"
					class="content-input"
					maxlength="1000"
				></textarea>
			</view>

			<view class="option-item" @click="selectMerchant">
				<view class="option-label">
					<text class="icon">📍</text>
					<text>关联商户</text>
				</view>
				<text class="arrow">›</text>
			</view>

			<view class="option-item" @click="addTopic">
				<view class="option-label">
					<text class="icon">#</text>
					<text>添加话题</text>
				</view>
				<text class="arrow">›</text>
			</view>

			<view class="option-item" @click="addLocation">
				<view class="option-label">
					<text class="icon">📌</text>
					<text>添加位置</text>
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

const title = ref('')
const content = ref('')

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

const handlePublish = () => {
	if (!title.value || !content.value) {
		uni.showToast({
			title: '请填写标题和内容',
			icon: 'none'
		})
		return
	}

	uni.showToast({
		title: '发布成功',
		icon: 'success'
	})

	setTimeout(() => {
		uni.switchTab({ url: '/pages/index/index' })
	}, 1500)
}

const chooseImage = () => {
	uni.chooseImage({
		count: 9,
		success: (res) => {
			console.log('选择图片成功', res)
		}
	})
}

const selectMerchant = () => {
	uni.showToast({ title: '选择关联商户', icon: 'none' })
}

const addTopic = () => {
	uni.showToast({ title: '添加话题', icon: 'none' })
}

const addLocation = () => {
	uni.showToast({ title: '添加位置', icon: 'none' })
}

const addEmoji = () => {
	uni.showToast({ title: '添加表情', icon: 'none' })
}

const atUser = () => {
	uni.showToast({ title: '@好友', icon: 'none' })
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
	margin-bottom: 30rpx;
}

.title-input {
	width: 100%;
	padding: 30rpx 0;
	font-size: 40rpx;
	font-weight: 500;
	background: transparent;
	border: none;
}

.divider {
	height: 1rpx;
	background: #e0e0e0;
	margin: 30rpx 0;
}

.content-input {
	width: 100%;
	min-height: 300rpx;
	padding: 20rpx 0;
	font-size: 32rpx;
	line-height: 1.6;
	background: transparent;
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
