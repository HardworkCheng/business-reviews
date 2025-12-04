<template>
	<view class="container">
		<view class="header">
			<text class="title">消息</text>
			<text class="more-btn">⋯</text>
		</view>

		<view class="tabs">
			<view class="tab" :class="{ active: currentTab === 0 }" @click="switchTab(0)">
				<text>聊天</text>
			</view>
			<view class="tab" :class="{ active: currentTab === 1 }" @click="switchTab(1)">
				<text>通知</text>
			</view>
		</view>

		<view v-if="currentTab === 0" class="chat-list">
			<view class="chat-item" v-for="(chat, index) in chatList" :key="index" @click="openChat(chat.id)">
				<view class="avatar-wrapper">
					<image :src="chat.avatar" class="avatar"></image>
					<view v-if="chat.online" class="online-status"></view>
				</view>
				<view class="chat-info">
					<view class="chat-header">
						<text class="chat-name">{{ chat.name }}</text>
						<text class="chat-time">{{ chat.time }}</text>
					</view>
					<text class="chat-message line-clamp-1">{{ chat.lastMessage }}</text>
				</view>
				<view v-if="chat.unread" class="unread-badge">
					<text>{{ chat.unread }}</text>
				</view>
			</view>
		</view>

		<view v-if="currentTab === 1" class="notice-list">
			<view class="notice-item" v-for="(notice, index) in noticeList" :key="index">
				<view class="notice-icon clay-icon" :class="notice.type">
					<text>{{ notice.icon }}</text>
				</view>
				<view class="notice-content">
					<text class="notice-text">
						<text class="highlight">{{ notice.user }}</text>
						{{ notice.action }}
					</text>
					<text class="notice-time">{{ notice.time }}</text>
				</view>
				<image v-if="notice.image" :src="notice.image" class="notice-image"></image>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const currentTab = ref(0)

// 聊天列表（从后端获取）
const chatList = ref([])

// 通知列表（从后端获取）
const noticeList = ref([])

onLoad(() => {
	console.log('Message page loaded')
	// TODO: 从后端API获取聊天列表
	// fetchChatList()
	// TODO: 获取通知列表
	// fetchNoticeList()
})

const switchTab = (index) => {
	currentTab.value = index
}

const openChat = (id) => {
	uni.showToast({ title: '打开聊天 ' + id, icon: 'none' })
}
</script>

<style lang="scss" scoped>
.container {
	background: #F7F9FC;
	min-height: 100vh;
	padding-bottom: 100rpx;
}

.header {
	background: white;
	padding: 25rpx 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.title {
	font-size: 40rpx;
	font-weight: bold;
}

.more-btn {
	font-size: 40rpx;
	color: #999;
}

.tabs {
	background: white;
	display: flex;
	border-bottom: 1rpx solid #f0f0f0;
}

.tab {
	flex: 1;
	text-align: center;
	padding: 30rpx;
	font-size: 28rpx;
	color: #999;
	position: relative;
}

.tab.active {
	color: #FF9E64;
	font-weight: 500;
}

.tab.active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 60rpx;
	height: 6rpx;
	background: #FF9E64;
	border-radius: 3rpx;
}

.chat-list, .notice-list {
	background: white;
}

.chat-item {
	display: flex;
	align-items: center;
	padding: 25rpx 30rpx;
	border-bottom: 1rpx solid #f5f5f5;
}

.avatar-wrapper {
	position: relative;
	margin-right: 25rpx;
}

.avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
}

.online-status {
	position: absolute;
	top: 0;
	right: 0;
	width: 20rpx;
	height: 20rpx;
	background: #07c160;
	border-radius: 50%;
	border: 3rpx solid white;
}

.chat-info {
	flex: 1;
	min-width: 0;
}

.chat-header {
	display: flex;
	justify-content: space-between;
	margin-bottom: 10rpx;
}

.chat-name {
	font-size: 30rpx;
	font-weight: 500;
}

.chat-time {
	font-size: 24rpx;
	color: #999;
}

.chat-message {
	font-size: 26rpx;
	color: #666;
}

.unread-badge {
	width: 40rpx;
	height: 40rpx;
	background: #EF476F;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	color: white;
	font-size: 22rpx;
	margin-left: 20rpx;
}

.notice-item {
	display: flex;
	align-items: center;
	padding: 25rpx 30rpx;
	border-bottom: 1rpx solid #f5f5f5;
}

.notice-icon {
	width: 80rpx;
	height: 80rpx;
	font-size: 36rpx;
	margin-right: 25rpx;
}

.notice-icon.like {
	background: rgba(239, 71, 111, 0.2);
}

.notice-icon.comment {
	background: rgba(6, 214, 160, 0.2);
}

.notice-icon.follow {
	background: rgba(255, 158, 100, 0.2);
}

.notice-content {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.notice-text {
	font-size: 28rpx;
	margin-bottom: 8rpx;
}

.highlight {
	font-weight: 500;
}

.notice-time {
	font-size: 24rpx;
	color: #999;
}

.notice-image {
	width: 100rpx;
	height: 100rpx;
	border-radius: 15rpx;
	margin-left: 20rpx;
}
</style>
