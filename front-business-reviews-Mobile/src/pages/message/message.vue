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
			<!-- 加载中 -->
			<view v-if="chatLoading" class="loading-tip">
				<text>加载中...</text>
			</view>
			
			<!-- 空状态 -->
			<view v-else-if="chatList.length === 0" class="empty-tip">
				<text>暂无聊天记录</text>
				<text class="empty-sub">去关注的用户主页发起私信吧~</text>
			</view>
			
			<!-- 聊天列表 -->
			<view v-else class="chat-item" v-for="(chat, index) in chatList" :key="chat.id || index" @click="openChat(chat.id)">
				<view class="avatar-wrapper">
					<image :src="chat.avatar" class="avatar" mode="aspectFill"></image>
					<view v-if="chat.online" class="online-status"></view>
				</view>
				<view class="chat-info">
					<view class="chat-header">
						<text class="chat-name">{{ chat.name }}</text>
						<text class="chat-time">{{ chat.time }}</text>
					</view>
					<text class="chat-message line-clamp-1">{{ chat.lastMessage || '暂无消息' }}</text>
				</view>
				<view v-if="chat.unread && chat.unread > 0" class="unread-badge">
					<text>{{ chat.unread > 99 ? '99+' : chat.unread }}</text>
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
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getConversationList, getUnreadCount, getNotifications } from '../../api/message'

const currentTab = ref(0)

// 聊天列表（从后端获取）
const chatList = ref([])

// 通知列表（从后端获取）
const noticeList = ref([])

// 未读消息数
const unreadCount = ref(0)

// 加载状态
const chatLoading = ref(false)
const noticeLoading = ref(false)

onLoad(() => {
	console.log('Message page loaded')
	loadData()
	// 不再在消息列表页面连接WebSocket，只在聊天页面连接
})

onShow(() => {
	// 每次显示页面时刷新数据
	console.log('消息页面显示，刷新数据')
	loadData()
})

const loadData = async () => {
	if (currentTab.value === 0) {
		await fetchChatList()
	} else {
		await fetchNoticeList()
	}
	await fetchUnreadCount()
}

const fetchChatList = async () => {
	chatLoading.value = true
	try {
		const result = await getConversationList(1, 20)
		console.log('会话列表原始数据:', result)
		
		if (result && result.list && Array.isArray(result.list)) {
			chatList.value = result.list.map(conv => {
				console.log('处理会话:', conv)
				return {
					id: conv.userId || conv.id,
					name: conv.username || '未知用户',
					avatar: conv.avatar || 'https://via.placeholder.com/100',
					lastMessage: conv.lastMessage || '',
					time: formatTime(conv.lastTime || conv.lastMessageTime),
					unread: conv.unreadCount || 0,
					online: false
				}
			})
			console.log('处理后的聊天列表:', chatList.value)
		} else {
			console.log('会话列表为空或格式不正确')
			chatList.value = []
		}
	} catch (e) {
		console.error('获取会话列表失败:', e)
		chatList.value = []
	} finally {
		chatLoading.value = false
	}
}

const fetchNoticeList = async () => {
	try {
		const result = await getNotifications(1, 20)
		console.log('通知列表:', result)
		
		if (result && result.list) {
			noticeList.value = result.list.map(notice => {
				let icon = '📢'
				let type = 'system'
				
				// 根据通知类型设置图标和样式
				if (notice.type === 1) { // 点赞
					icon = '❤️'
					type = 'like'
				} else if (notice.type === 2) { // 评论
					icon = '💬'
					type = 'comment'
				} else if (notice.type === 3) { // 关注
					icon = '👤'
					type = 'follow'
				}
				
				return {
					id: notice.id,
					icon: icon,
					type: type,
					user: notice.title || '',
					action: notice.content || '',
					time: formatTime(notice.createdAt),
					image: notice.image || null
				}
			})
		}
	} catch (e) {
		console.error('获取通知列表失败:', e)
	}
}

const fetchUnreadCount = async () => {
	try {
		const result = await getUnreadCount()
		console.log('未读消息数:', result)
		
		if (result && typeof result.total === 'number') {
			unreadCount.value = result.total
		}
	} catch (e) {
		console.error('获取未读消息数失败:', e)
	}
}

const switchTab = (index) => {
	currentTab.value = index
	loadData()
}

const openChat = (userId) => {
	const chat = chatList.value.find(c => c.id === userId)
	if (chat) {
		uni.navigateTo({
			url: `/pages/chat/chat?userId=${userId}&username=${chat.name}&avatar=${chat.avatar}`
		})
	}
}

// 格式化时间
const formatTime = (dateStr) => {
	if (!dateStr) return ''
	
	try {
		const date = new Date(dateStr)
		const now = new Date()
		const diff = now - date
		
		// 1分钟内
		if (diff < 60000) return '刚刚'
		
		// 1小时内
		if (diff < 3600000) {
			const minutes = Math.floor(diff / 60000)
			return `${minutes}分钟前`
		}
		
		// 今天
		if (date.toDateString() === now.toDateString()) {
			const hours = date.getHours()
			const minutes = String(date.getMinutes()).padStart(2, '0')
			return `${hours}:${minutes}`
		}
		
		// 昨天
		const yesterday = new Date(now)
		yesterday.setDate(yesterday.getDate() - 1)
		if (date.toDateString() === yesterday.toDateString()) {
			return '昨天'
		}
		
		// 今年
		if (date.getFullYear() === now.getFullYear()) {
			return `${date.getMonth() + 1}-${date.getDate()}`
		}
		
		// 其他
		return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
	} catch (e) {
		console.error('时间格式化失败:', e)
		return ''
	}
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

.loading-tip {
	text-align: center;
	padding: 60rpx 0;
	color: #999;
	font-size: 28rpx;
}

.empty-tip {
	text-align: center;
	padding: 100rpx 30rpx;
	color: #999;
	font-size: 28rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.empty-sub {
	font-size: 24rpx;
	color: #ccc;
	margin-top: 16rpx;
}
</style>
