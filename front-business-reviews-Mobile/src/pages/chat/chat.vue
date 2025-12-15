<template>
	<view class="container">
		<!-- 导航栏 -->
		<view class="navbar">
			<view class="nav-btn" @click="goBack">
				<text>←</text>
			</view>
			<text class="nav-title">{{ otherUser.username }}</text>
			<view class="nav-btn"></view>
		</view>

		<!-- 消息列表 -->
		<scroll-view 
			class="message-list" 
			scroll-y 
			:scroll-into-view="scrollToView"
			@scrolltoupper="loadMore"
		>
			<view v-if="loading && messages.length === 0" class="loading">
				<text>加载中...</text>
			</view>
			
			<view v-if="!loading && messages.length === 0" class="empty-tip">
				<text>暂无消息，发送第一条消息吧~</text>
			</view>
			
			<view 
				v-for="(msg, index) in messages" 
				:key="msg.id || index"
				:id="'msg-' + (msg.id || index)"
				class="message-item"
				:class="{ 'message-mine': msg.isMine }"
			>
				<image 
					:src="msg.isMine ? myAvatar : otherUser.avatar" 
					class="avatar"
					mode="aspectFill"
				></image>
				<view class="message-content">
					<view class="message-bubble">
						<text>{{ msg.content }}</text>
					</view>
					<text class="message-time">{{ formatTime(msg.createdAt) }}</text>
				</view>
			</view>
		</scroll-view>

		<!-- 输入框区域 -->
		<view class="input-wrapper">
			<view class="input-bar">
				<input 
					v-model="inputText" 
					class="input-field" 
					placeholder="输入消息..."
					confirm-type="send"
					:adjust-position="true"
					:cursor-spacing="20"
					@confirm="handleSend"
					@focus="onInputFocus"
				/>
				<view class="send-btn" :class="{ 'send-btn-active': inputText.trim() }" @click="handleSend">
					<text>发送</text>
				</view>
			</view>
			<!-- 安全区域占位 -->
			<view class="safe-area-bottom"></view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad, onShow as uniOnShow } from '@dcloudio/uni-app'
import { getConversationMessages, sendMessage as sendMessageApi, markAsRead } from '../../api/message'
import { getUserInfo } from '../../api/user'
import websocket from '../../utils/websocket'

const otherUserId = ref(null)
const otherUser = ref({
	username: '用户',
	avatar: 'https://via.placeholder.com/100'
})
const myAvatar = ref('')
const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const pageNum = ref(1)
const hasMore = ref(true)
const scrollToView = ref('')
const conversationId = ref(null)

onLoad((options) => {
	otherUserId.value = parseInt(options.userId)
	if (options.username) {
		otherUser.value.username = options.username
	}
	if (options.avatar) {
		otherUser.value.avatar = options.avatar
	}
	
	// 获取我的信息
	fetchMyInfo()
	
	// 加载聊天记录
	loadMessages()
	
	// 连接WebSocket
	connectWebSocket()
	
	// 标记消息为已读
	markMessagesAsRead()
})

uniOnShow(() => {
	// 每次页面显示时标记消息为已读
	console.log('聊天页面显示，标记消息为已读')
	markMessagesAsRead()
})

onUnmounted(() => {
	// 移除消息监听
	websocket.offMessage(handleNewMessage)
})

// 获取我的信息
const fetchMyInfo = async () => {
	try {
		const result = await getUserInfo()
		if (result && result.avatar) {
			myAvatar.value = result.avatar
		}
	} catch (e) {
		console.error('获取用户信息失败:', e)
	}
}

// 加载消息
const loadMessages = async () => {
	if (loading.value || !hasMore.value) return
	
	loading.value = true
	try {
		const result = await getConversationMessages(otherUserId.value, pageNum.value, 50)
		console.log('聊天记录:', result)
		
		if (result && result.list) {
			// 获取当前用户ID
			const userInfo = uni.getStorageSync('userInfo')
			const myUserId = userInfo?.userId || userInfo?.id
			
			// 处理消息，添加 isMine 属性
			const processedMessages = result.list.map(msg => ({
				...msg,
				isMine: msg.senderId?.toString() === myUserId?.toString()
			})).reverse()
			
			// 新消息插入到前面
			messages.value = [...processedMessages, ...messages.value]
			
			hasMore.value = result.list.length >= 50
			
			// 首次加载滚动到底部
			if (pageNum.value === 1 && result.list.length > 0) {
				setTimeout(() => {
					scrollToBottom()
				}, 100)
			}
		}
	} catch (e) {
		console.error('加载消息失败:', e)
		uni.showToast({
			title: '加载失败',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

// 加载更多
const loadMore = () => {
	if (!loading.value && hasMore.value) {
		pageNum.value++
		loadMessages()
	}
}

// 输入框获取焦点
const onInputFocus = () => {
	// 滚动到底部
	setTimeout(() => {
		scrollToBottom()
	}, 300)
}

// 处理发送（统一入口）
const handleSend = () => {
	sendMessage()
}

// 发送消息
const sendMessage = async () => {
	const content = inputText.value.trim()
	if (!content) {
		uni.showToast({
			title: '请输入消息内容',
			icon: 'none'
		})
		return
	}
	
	// 先清空输入框
	const messageContent = content
	inputText.value = ''
	
	try {
		console.log('发送消息:', { receiverId: otherUserId.value, content: messageContent })
		const result = await sendMessageApi({
			receiverId: otherUserId.value,
			content: messageContent,
			messageType: 1
		})
		
		console.log('发送结果:', result)
		
		if (result) {
			// 添加到消息列表
			const newMessage = {
				id: result.id || ('msg_' + Date.now()),
				content: result.content || messageContent,
				senderId: result.senderId,
				receiverId: result.receiverId,
				createdAt: result.createdAt || new Date().toISOString(),
				isMine: true
			}
			messages.value.push(newMessage)
			
			// 滚动到底部
			setTimeout(() => {
				scrollToBottom()
			}, 100)
			
			uni.showToast({
				title: '发送成功',
				icon: 'success',
				duration: 1000
			})
		}
	} catch (e) {
		console.error('发送消息失败:', e)
		uni.showToast({
			title: '发送失败',
			icon: 'none'
		})
	}
}

// 连接WebSocket
const connectWebSocket = () => {
	const token = uni.getStorageSync('token')
	const userInfo = uni.getStorageSync('userInfo')
	
	// userInfo 可能有 id 或 userId 字段
	const userId = userInfo?.id || userInfo?.userId
	
	console.log('连接WebSocket, userId:', userId, 'token:', token ? '存在' : '不存在')
	
	if (token && userId) {
		websocket.connect(userId, token)
		websocket.onMessage(handleNewMessage)
		console.log('WebSocket消息处理器已注册')
	} else {
		console.warn('无法连接WebSocket: 缺少token或userId')
	}
}

// 处理新消息
const handleNewMessage = (message) => {
	console.log('handleNewMessage收到消息:', message)
	
	// 后端发送的消息格式是 {type: "private_message", data: {...}}
	if (message.type === 'private_message') {
		const msgData = message.data
		
		// 检查消息是否来自当前聊天对象
		// senderId 可能是字符串或数字，需要统一比较
		const senderId = msgData.senderId?.toString()
		const targetId = otherUserId.value?.toString()
		
		console.log('消息发送者:', senderId, '当前聊天对象:', targetId)
		
		if (senderId === targetId) {
			// 添加到消息列表
			const newMessage = {
				id: msgData.id || ('ws_' + Date.now()),
				content: msgData.content,
				senderId: msgData.senderId,
				receiverId: msgData.receiverId,
				createdAt: msgData.createdAt || new Date().toISOString(),
				isMine: false
			}
			
			messages.value.push(newMessage)
			
			// 滚动到底部
			setTimeout(() => {
				scrollToBottom()
			}, 100)
			
			// 标记为已读
			markAsRead(otherUserId.value).catch(e => {
				console.error('标记已读失败:', e)
			})
		}
	}
}

// 滚动到底部
const scrollToBottom = () => {
	if (messages.value.length > 0) {
		const lastMsg = messages.value[messages.value.length - 1]
		scrollToView.value = 'msg-' + lastMsg.id
	}
}

// 标记消息为已读
const markMessagesAsRead = async () => {
	if (!otherUserId.value) {
		console.warn('无法标记已读: 缺少对方用户ID')
		return
	}
	
	try {
		console.log('标记消息为已读, 对方用户ID:', otherUserId.value)
		await markAsRead(otherUserId.value)
		console.log('标记已读成功')
	} catch (e) {
		console.error('标记已读失败:', e)
	}
}

// 格式化时间
const formatTime = (dateStr) => {
	if (!dateStr) return ''
	const date = new Date(dateStr)
	const now = new Date()
	const diff = now - date
	
	// 1分钟内
	if (diff < 60000) {
		return '刚刚'
	}
	// 1小时内
	if (diff < 3600000) {
		return Math.floor(diff / 60000) + '分钟前'
	}
	// 今天
	if (date.toDateString() === now.toDateString()) {
		return date.getHours() + ':' + String(date.getMinutes()).padStart(2, '0')
	}
	// 昨天
	const yesterday = new Date(now)
	yesterday.setDate(yesterday.getDate() - 1)
	if (date.toDateString() === yesterday.toDateString()) {
		return '昨天 ' + date.getHours() + ':' + String(date.getMinutes()).padStart(2, '0')
	}
	// 更早
	return (date.getMonth() + 1) + '-' + date.getDate()
}

const goBack = () => {
	uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.container {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background: #F7F9FC;
}

.navbar {
	background: white;
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.nav-btn {
	width: 60rpx;
	font-size: 40rpx;
	color: #333;
}

.nav-title {
	font-size: 36rpx;
	font-weight: 500;
	color: #333;
}

.message-list {
	flex: 1;
	padding: 20rpx;
	padding-bottom: 140rpx;
	overflow-y: auto;
}

.loading {
	text-align: center;
	padding: 40rpx 0;
	color: #999;
	font-size: 28rpx;
}

.message-item {
	display: flex;
	margin-bottom: 30rpx;
	
	&.message-mine {
		flex-direction: row-reverse;
		
		.message-bubble {
			background: #FF9E64;
			color: white;
		}
		
		.message-time {
			text-align: right;
		}
	}
}

.avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	flex-shrink: 0;
}

.message-content {
	max-width: 500rpx;
	margin: 0 20rpx;
}

.message-bubble {
	background: white;
	padding: 20rpx 30rpx;
	border-radius: 20rpx;
	word-wrap: break-word;
	font-size: 28rpx;
	line-height: 1.5;
}

.message-time {
	font-size: 22rpx;
	color: #999;
	margin-top: 10rpx;
	display: block;
}

.empty-tip {
	text-align: center;
	padding: 100rpx 0;
	color: #999;
	font-size: 28rpx;
}

.input-wrapper {
	background: white;
	border-top: 1rpx solid #eee;
	position: fixed;
	bottom: 0;
	left: 0;
	right: 0;
	z-index: 100;
}

.input-bar {
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
}

.input-field {
	flex: 1;
	background: #F5F5F5;
	padding: 20rpx 30rpx;
	border-radius: 40rpx;
	font-size: 28rpx;
	height: 80rpx;
	line-height: 40rpx;
	box-sizing: border-box;
}

.send-btn {
	margin-left: 20rpx;
	background: #CCCCCC;
	color: white;
	padding: 20rpx 40rpx;
	border-radius: 40rpx;
	font-size: 28rpx;
	transition: background 0.2s;
}

.send-btn-active {
	background: #FF9E64;
}

.safe-area-bottom {
	height: constant(safe-area-inset-bottom);
	height: env(safe-area-inset-bottom);
}
</style>
