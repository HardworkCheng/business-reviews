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
			<view class="notice-item" v-for="(notice, index) in noticeList" :key="index" @click="openNoticeDetail(notice)">
				<!-- 头像 -->
				<image :src="notice.avatar || 'https://via.placeholder.com/100'" class="notice-avatar" mode="aspectFill"></image>
				<view class="notice-content">
					<!-- 标题和时间在一行 -->
					<view class="notice-header">
						<text class="notice-title">{{ notice.title || notice.fromUsername || '系统通知' }}</text>
						<text class="notice-time">{{ notice.time }}</text>
					</view>
					<!-- 内容只显示一行，超出省略 -->
					<text class="notice-text line-clamp-1">{{ notice.text }}</text>
				</view>
				<image v-if="notice.image" :src="notice.image" class="notice-image"></image>
			</view>
		</view>
		
		<!-- 通知详情弹窗 -->
		<view class="notice-modal" v-if="showNoticeModal" @click="closeNoticeModal">
			<view class="notice-modal-content" @click.stop>
				<view class="notice-modal-header">
					<image :src="selectedNotice?.avatar || '/static/icons/ai-assistant.png'" class="notice-modal-avatar" mode="aspectFill"></image>
					<view class="notice-modal-info">
						<text class="notice-modal-title">{{ selectedNotice?.title || '系统通知' }}</text>
						<text class="notice-modal-time">{{ selectedNotice?.time }}</text>
					</view>
					<text class="notice-modal-close" @click="closeNoticeModal">✕</text>
				</view>
				<scroll-view class="notice-modal-body" scroll-y>
					<text class="notice-modal-text">{{ selectedNotice?.fullText || selectedNotice?.text }}</text>
				</scroll-view>
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

// 通知详情弹窗
const showNoticeModal = ref(false)
const selectedNotice = ref(null)

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
				
				// 特殊处理AI审核助手（用户ID为0）
				const isAIAssistant = conv.userId === 0 || conv.userId === '0'
				
				return {
					id: conv.userId || conv.id,
					name: isAIAssistant ? 'AI审核助手' : (conv.username || '未知用户'),
					avatar: isAIAssistant 
						? '/static/icons/ai-assistant.png' 
						: (conv.avatar || 'https://via.placeholder.com/100'),
					lastMessage: conv.lastMessage || '',
					time: formatTime(conv.lastTime || conv.lastMessageTime),
					unread: conv.unreadCount || 0,
					online: false,
					isAIAssistant: isAIAssistant  // 标记是否为AI审核助手
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
				// 判断是否为AI审核通知（type=5）
				const isAIAuditNotice = notice.type === 5
				
				// AI审核通知：列表只显示简短文本，完整内容在弹窗中查看
				const displayText = isAIAuditNotice 
					? '内容审核通知' 
					: (notice.content || '')
				
				return {
					id: notice.id,
					// AI审核通知使用专属头像，其他通知使用发送者头像
					avatar: isAIAuditNotice 
						? '/static/icons/ai-assistant.png' 
						: (notice.fromAvatar || 'https://via.placeholder.com/100'),
					text: displayText,  // 列表中显示的简短文本
					fullText: notice.content || '',  // 弹窗中显示的完整内容
					time: formatTime(notice.createdAt),
					image: notice.noteImage || notice.image || null,
					isAIAuditNotice: isAIAuditNotice,
					title: isAIAuditNotice ? 'AI审核助手' : (notice.fromUsername || '')
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

// 打开通知详情弹窗
const openNoticeDetail = (notice) => {
	selectedNotice.value = notice
	showNoticeModal.value = true
}

// 关闭通知详情弹窗
const closeNoticeModal = () => {
	showNoticeModal.value = false
	selectedNotice.value = null
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

.notice-avatar {
	width: 80rpx;
	height: 80rpx;
	min-width: 80rpx;
	min-height: 80rpx;
	max-width: 80rpx;
	max-height: 80rpx;
	border-radius: 50%;
	margin-right: 25rpx;
	flex-shrink: 0;
	object-fit: cover;
}

.notice-content {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.notice-text {
	font-size: 26rpx;
	color: #666;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.notice-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 8rpx;
}

.notice-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
	flex: 1;
}

.notice-time {
	font-size: 22rpx;
	color: #999;
	flex-shrink: 0;
	margin-left: 16rpx;
}

.notice-image {
	width: 80rpx;
	height: 80rpx;
	border-radius: 12rpx;
	margin-left: 20rpx;
	flex-shrink: 0;
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

/* 单行截断 */
.line-clamp-1 {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

/* 通知详情弹窗 */
.notice-modal {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	z-index: 1000;
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 40rpx;
}

.notice-modal-content {
	background: white;
	border-radius: 24rpx;
	width: 100%;
	max-height: 70vh;
	overflow: hidden;
	box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.15);
}

.notice-modal-header {
	display: flex;
	align-items: center;
	padding: 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
	background: linear-gradient(135deg, #f8f9fa 0%, #fff 100%);
}

.notice-modal-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 20rpx;
	flex-shrink: 0;
}

.notice-modal-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.notice-modal-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 6rpx;
}

.notice-modal-time {
	font-size: 24rpx;
	color: #999;
}

.notice-modal-close {
	font-size: 40rpx;
	color: #999;
	padding: 10rpx;
	margin-left: 20rpx;
}

.notice-modal-body {
	padding: 30rpx;
	max-height: 50vh;
}

.notice-modal-text {
	font-size: 28rpx;
	color: #333;
	line-height: 1.8;
	white-space: pre-wrap;
	word-break: break-all;
}
</style>
