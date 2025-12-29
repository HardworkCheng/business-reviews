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
					<!-- 文本消息 -->
					<view v-if="msg.messageType === 1 || !msg.messageType" class="message-bubble">
						<text>{{ msg.content }}</text>
					</view>
					
					<!-- 笔记分享卡片 -->
					<view v-else-if="msg.messageType === 4" class="note-card" @click="goToNoteDetail(msg.noteData)">
						<view class="note-card-header">
							<text class="note-card-label">📝 分享了一篇笔记</text>
						</view>
						<view class="note-card-body">
							<image 
								v-if="msg.noteData && msg.noteData.coverImage" 
								:src="msg.noteData.coverImage" 
								class="note-card-cover" 
								mode="aspectFill"
							></image>
							<view class="note-card-info">
								<text class="note-card-title">{{ msg.noteData?.title || '无标题' }}</text>
								<text class="note-card-content">{{ msg.noteData?.content || '' }}</text>
							</view>
						</view>
						<view class="note-card-footer">
							<text class="note-card-hint">点击查看详情</text>
						</view>
					</view>
					
					<!-- 店铺分享卡片 -->
					<view v-else-if="msg.messageType === 5" class="shop-card" @click="goToShopDetail(msg.shopData || msg.noteData)">
						<view class="shop-card-header">
							<text class="shop-card-label">🏪 分享了一家店铺</text>
						</view>
						<view class="shop-card-body">
							<image 
								v-if="getShopData(msg).headerImage" 
								:src="getShopData(msg).headerImage" 
								class="shop-card-cover" 
								mode="aspectFill"
							></image>
							<view class="shop-card-info">
								<text class="shop-card-title">{{ getShopData(msg).name || '商家名称' }}</text>
								<view class="shop-card-rating">
									<text class="shop-card-score">{{ getShopData(msg).rating || 0 }}分</text>
									<text class="shop-card-review">{{ getShopData(msg).reviewCount || 0 }}条评价</text>
								</view>
								<text class="shop-card-address">{{ getShopData(msg).address || '' }}</text>
							</view>
						</view>
						<view class="shop-card-footer">
							<text class="shop-card-hint">点击查看详情</text>
						</view>
					</view>
					
					<text class="message-time">{{ formatTime(msg.createdAt) }}</text>
				</view>
			</view>
		</scroll-view>

		<!-- 输入框区域 - AI审核助手不可回复 -->
		<view class="input-wrapper" v-if="!isAIAssistant">
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
		
		<!-- AI审核助手的底部提示 -->
		<view class="ai-assistant-tip" v-if="isAIAssistant">
			<text>🤖 这是AI审核助手的自动通知，无法回复</text>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad, onShow as uniOnShow, onHide as uniOnHide } from '@dcloudio/uni-app'
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
const pollingTimer = ref(null)
const lastMessageId = ref(null)
const isAIAssistant = ref(false)  // 是否为AI审核助手

onLoad((options) => {
	otherUserId.value = parseInt(options.userId)
	
	// 检测是否为AI审核助手（用户ID为0）
	isAIAssistant.value = otherUserId.value === 0
	
	if (isAIAssistant.value) {
		// AI审核助手的特殊处理
		otherUser.value.username = 'AI审核助手'
		otherUser.value.avatar = '/static/icons/ai-assistant.png'
	} else {
		if (options.username) {
			otherUser.value.username = options.username
		}
		if (options.avatar) {
			otherUser.value.avatar = options.avatar
		}
	}
	
	// 获取我的信息
	fetchMyInfo()
	
	// 加载聊天记录
	loadMessages()
	
	// 连接WebSocket
	connectWebSocket()
	
	// 启动轮询（作为WebSocket的备用方案）
	startPolling()
	
	// 标记消息为已读
	markMessagesAsRead()
})

uniOnShow(() => {
	// 每次页面显示时标记消息为已读
	console.log('聊天页面显示，标记消息为已读')
	markMessagesAsRead()
	// 重新启动轮询和WebSocket
	startPolling()
	connectWebSocket()
})

uniOnHide(() => {
	// 页面隐藏时停止轮询和WebSocket，节省资源
	console.log('聊天页面隐藏，停止轮询和WebSocket')
	stopPolling()
	websocket.disable()
})

onUnmounted(() => {
	// 移除消息监听
	websocket.offMessage(handleNewMessage)
	// 禁用WebSocket（停止连接和重连）
	websocket.disable()
	// 停止轮询
	stopPolling()
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
			
			// 处理消息，添加 isMine 属性，并解析 noteData
			const processedMessages = result.list.map(msg => {
				const processed = {
					...msg,
					isMine: msg.senderId?.toString() === myUserId?.toString(),
					messageType: msg.type || msg.messageType
				}
				
				// 如果是笔记分享消息，解析 noteData JSON 字符串
				if (processed.messageType === 4 && msg.noteData) {
					try {
						if (typeof msg.noteData === 'string') {
							processed.noteData = JSON.parse(msg.noteData)
						} else {
							processed.noteData = msg.noteData
						}
						console.log('解析笔记数据:', processed.noteData)
					} catch (e) {
						console.error('解析笔记数据失败:', e, msg.noteData)
						processed.noteData = null
					}
				}
				
				// 如果是店铺分享消息，解析店铺数据（存储在noteData字段）
				if (processed.messageType === 5 && msg.noteData) {
					try {
						if (typeof msg.noteData === 'string') {
							processed.shopData = JSON.parse(msg.noteData)
						} else {
							processed.shopData = msg.noteData
						}
						console.log('解析店铺数据:', processed.shopData)
					} catch (e) {
						console.error('解析店铺数据失败:', e, msg.noteData)
						processed.shopData = null
					}
				}
				
				return processed
			}).reverse()
			
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
		// 启用WebSocket并连接
		websocket.enable()
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
				isMine: false,
				messageType: msgData.messageType || msgData.type
			}
			
			// 如果是笔记分享消息，处理 noteData
			if (newMessage.messageType === 4 && msgData.noteData) {
				try {
					if (typeof msgData.noteData === 'string') {
						newMessage.noteData = JSON.parse(msgData.noteData)
					} else {
						newMessage.noteData = msgData.noteData
					}
					console.log('WebSocket-解析笔记数据:', newMessage.noteData)
				} catch (e) {
					console.error('WebSocket-解析笔记数据失败:', e)
					newMessage.noteData = null
				}
			}
			
			// 如果是店铺分享消息，处理店铺数据
			if (newMessage.messageType === 5) {
				try {
					const shopSource = msgData.shopData || msgData.noteData
					if (typeof shopSource === 'string') {
						newMessage.shopData = JSON.parse(shopSource)
					} else {
						newMessage.shopData = shopSource
					}
					console.log('WebSocket-解析店铺数据:', newMessage.shopData)
				} catch (e) {
					console.error('WebSocket-解析店铺数据失败:', e)
					newMessage.shopData = null
				}
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

// 启动轮询
const startPolling = () => {
	// 先停止之前的轮询
	stopPolling()
	
	// 每3秒轮询一次新消息
	pollingTimer.value = setInterval(() => {
		pollNewMessages()
	}, 3000)
	
	console.log('消息轮询已启动')
}

// 停止轮询
const stopPolling = () => {
	if (pollingTimer.value) {
		clearInterval(pollingTimer.value)
		pollingTimer.value = null
		console.log('消息轮询已停止')
	}
}

// 轮询新消息
const pollNewMessages = async () => {
	if (!otherUserId.value) return
	
	try {
		const result = await getConversationMessages(otherUserId.value, 1, 20)
		
		if (result && result.list && result.list.length > 0) {
			// 获取当前用户ID
			const userInfo = uni.getStorageSync('userInfo')
			const myUserId = userInfo?.userId || userInfo?.id
			
			// 处理消息，添加 isMine 属性，并解析 noteData
			const newMessages = result.list.map(msg => {
				const processed = {
					...msg,
					isMine: msg.senderId?.toString() === myUserId?.toString(),
					messageType: msg.type || msg.messageType
				}
				
				// 如果是笔记分享消息，解析 noteData JSON 字符串
				if (processed.messageType === 4 && msg.noteData) {
					try {
						if (typeof msg.noteData === 'string') {
							processed.noteData = JSON.parse(msg.noteData)
						} else {
							processed.noteData = msg.noteData
						}
					} catch (e) {
						console.error('轮询-解析笔记数据失败:', e)
						processed.noteData = null
					}
				}
				
				// 如果是店铺分享消息，解析店铺数据
				if (processed.messageType === 5 && msg.noteData) {
					try {
						if (typeof msg.noteData === 'string') {
							processed.shopData = JSON.parse(msg.noteData)
						} else {
							processed.shopData = msg.noteData
						}
					} catch (e) {
						console.error('轮询-解析店铺数据失败:', e)
						processed.shopData = null
					}
				}
				
				return processed
			}).reverse()
			
			// 检查是否有新消息
			const existingIds = new Set(messages.value.map(m => m.id?.toString()))
			const reallyNewMessages = newMessages.filter(msg => !existingIds.has(msg.id?.toString()))
			
			if (reallyNewMessages.length > 0) {
				console.log('轮询发现新消息:', reallyNewMessages.length, '条')
				
				// 添加新消息到列表
				messages.value.push(...reallyNewMessages)
				
				// 滚动到底部
				setTimeout(() => {
					scrollToBottom()
				}, 100)
				
				// 标记为已读
				markMessagesAsRead()
			}
		}
	} catch (e) {
		console.error('轮询消息失败:', e)
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

// 跳转到笔记详情
const goToNoteDetail = (noteData) => {
	console.log('点击笔记卡片，noteData:', noteData)
	
	if (!noteData) {
		console.error('noteData为空')
		uni.showToast({
			title: '笔记数据为空',
			icon: 'none'
		})
		return
	}
	
	if (noteData && noteData.noteId) {
		console.log('跳转到笔记详情，noteId:', noteData.noteId)
		uni.navigateTo({
			url: `/pages/note-detail/note-detail?id=${noteData.noteId}`
		})
	} else {
		console.error('noteData缺少noteId字段:', noteData)
		uni.showToast({
			title: '笔记不存在',
			icon: 'none'
		})
	}
}

// 跳转到店铺详情
const goToShopDetail = (shopData) => {
	console.log('点击店铺卡片，shopData:', shopData)
	
	if (!shopData) {
		console.error('shopData为空')
		uni.showToast({
			title: '店铺数据为空',
			icon: 'none'
		})
		return
	}
	
	const shopId = shopData.shopId || shopData.id
	if (shopId) {
		console.log('跳转到店铺详情，shopId:', shopId)
		uni.navigateTo({
			url: `/pages/shop-detail/shop-detail?id=${shopId}`
		})
	} else {
		console.error('shopData缺少shopId字段:', shopData)
		uni.showToast({
			title: '店铺不存在',
			icon: 'none'
		})
	}
}

// 获取店铺数据（兼容不同数据格式）
const getShopData = (msg) => {
	return msg.shopData || msg.noteData || {}
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

// 统一卡片基础样式
.share-card {
	background: white;
	border-radius: 24rpx;
	overflow: hidden;
	width: 500rpx;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	border: 1rpx solid rgba(255, 158, 100, 0.1);
	
	&:active {
		transform: scale(0.98);
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.04);
	}
}

// 笔记卡片样式
.note-card {
	@extend .share-card;
}

.note-card-header,
.shop-card-header {
	padding: 24rpx 30rpx;
	background: linear-gradient(135deg, #FFF8F0 0%, #FFF0E0 100%);
	border-bottom: 1rpx solid rgba(255, 158, 100, 0.1);
	display: flex;
	align-items: center;
}

.note-card-label,
.shop-card-label {
	font-size: 26rpx;
	color: #FF8F1F;
	font-weight: 600;
	display: flex;
	align-items: center;
	gap: 8rpx;
	
	&::before {
		content: '';
		width: 6rpx;
		height: 24rpx;
		background: #FF8F1F;
		border-radius: 4rpx;
		margin-right: 8rpx;
	}
}

.note-card-body,
.shop-card-body {
	display: flex;
	padding: 24rpx;
	gap: 24rpx;
	background: #fff;
}

.note-card-cover,
.shop-card-cover {
	width: 140rpx;
	height: 140rpx;
	border-radius: 16rpx;
	flex-shrink: 0;
	background: #f8f8f8;
	object-fit: cover;
	box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.05);
}

.note-card-info,
.shop-card-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	height: 140rpx;
	min-width: 0; // Fix overlapping in flex items
}

.note-card-title,
.shop-card-title {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	line-height: 1.4;
	margin-bottom: 8rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
}

.note-card-content {
	font-size: 24rpx;
	color: #888;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	margin-bottom: auto;
}

.shop-card-rating {
	display: flex;
	align-items: center;
	gap: 12rpx;
	margin-top: 4rpx;
}

.shop-card-score {
	font-size: 26rpx;
	color: #FF4D4F;
	font-weight: 700;
}

.shop-card-review {
	font-size: 22rpx;
	color: #999;
}

.shop-card-address {
	font-size: 22rpx;
	color: #999;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	margin-top: 8rpx;
}

.note-card-footer,
.shop-card-footer {
	padding: 20rpx 30rpx;
	background: #fafafa;
	border-top: 1rpx solid #f5f5f5;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.note-card-hint,
.shop-card-hint {
	font-size: 24rpx;
	color: #999;
	display: flex;
	align-items: center;
	
	&::after {
		content: '›';
		font-size: 32rpx;
		margin-left: 6rpx;
		color: #ccc;
		line-height: 1;
		margin-top: -4rpx; // visual adjustment
	}
}

// 店铺分享卡片样式
.shop-card {
	@extend .share-card;
}

// 移除Mine message的特殊颜色，保持统一
.message-mine .note-card,
.message-mine .shop-card {
	.note-card-header,
	.shop-card-header {
		// 保持统一背景，或者稍微加深一点点以示区别，但为了“统一”要求，这里保持一致
		background: linear-gradient(135deg, #FFF8F0 0%, #FFF0E0 100%);
	}
}
</style>
