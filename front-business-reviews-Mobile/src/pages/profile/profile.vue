<template>
	<view class="container">
		<view class="profile-header">
			<view class="user-info">
				<image class="avatar" :src="avatarUrl"></image>
				<view class="info">
					<text class="username">{{ userInfo.username || '未登录' }}</text>
					<text class="user-id">{{ userInfo.userId ? 'ID: ' + userInfo.userId : '' }}</text>
				</view>
				<view class="setting-btn clay-icon" @click="openSettings">
					<text>⚙️</text>
				</view>
			</view>

			<text class="bio">{{ userInfo.bio || '还没有个人简介' }}</text>

			<view class="stats-card clay-shadow">
				<view class="stat" @click="goToList('following')">
					<text class="stat-value text-primary">{{ userInfo.followingCount || 0 }}</text>
					<text class="stat-label">关注</text>
				</view>
				<view class="divider"></view>
				<view class="stat" @click="goToList('follower')">
					<text class="stat-value text-primary">{{ userInfo.followerCount || 0 }}</text>
					<text class="stat-label">粉丝</text>
				</view>
				<view class="divider"></view>
				<view class="stat" @click="goToLikeNotifications">
					<text class="stat-value text-primary">{{ userInfo.likeCount || 0 }}</text>
					<text class="stat-label">获赞</text>
				</view>
				<view class="divider"></view>
				<view class="stat" @click="switchTab(2)">
					<text class="stat-value text-primary">{{ userInfo.favoriteCount || 0 }}</text>
					<text class="stat-label">收藏</text>
				</view>
			</view>
		</view>

		<view class="tabs">
			<view 
				class="tab-item" 
				:class="{ active: currentTab === 0 }"
				@click="switchTab(0)"
			>
				<text>我的笔记</text>
			</view>
			<view 
				class="tab-item" 
				:class="{ active: currentTab === 1 }"
				@click="switchTab(1)"
			>
				<text>点赞</text>
			</view>
			<view 
				class="tab-item" 
				:class="{ active: currentTab === 2 }"
				@click="switchTab(2)"
			>
				<text>收藏</text>
			</view>
			<view 
				class="tab-item" 
				:class="{ active: currentTab === 3 }"
				@click="switchTab(3)"
			>
				<text>足迹</text>
			</view>
		</view>

		<view class="content">
			<view v-if="currentTab === 0" class="notes-grid">
				<view 
					class="note-card clay-shadow" 
					v-for="(note, index) in myNotes" 
					:key="index"
					@click="goToNoteDetail(note.id)"
				>
					<image :src="note.image" class="note-image" mode="aspectFill"></image>
					<view class="note-info">
						<text class="note-title line-clamp-2">{{ note.title }}</text>
						<view class="like-count">
							<text class="like-icon">❤️</text>
							<text>{{ note.likes }}</text>
						</view>
					</view>
				</view>
				
				<view v-if="myNotes.length === 0" class="empty">
					<text>还没有发布笔记</text>
				</view>
			</view>

			<view v-if="currentTab === 1" class="notes-grid">
				<view 
					class="note-card clay-shadow" 
					v-for="(item, index) in likedList" 
					:key="index"
					@click="goToNoteDetail(item.id)"
				>
					<image :src="item.image" class="note-image" mode="aspectFill"></image>
					<view class="note-info">
						<text class="note-title line-clamp-2">{{ item.title }}</text>
						<view class="like-count">
							<text class="like-icon">❤️</text>
							<text>{{ item.likes }}</text>
						</view>
					</view>
				</view>
				
				<view v-if="likedList.length === 0" class="empty">
					<text>还没有点赞过笔记</text>
				</view>
			</view>

			<view v-if="currentTab === 2" class="notes-grid">
				<view 
					class="note-card clay-shadow" 
					v-for="(item, index) in favoriteList" 
					:key="index"
					@click="goToNoteDetail(item.id)"
				>
					<image :src="item.image" class="note-image" mode="aspectFill"></image>
					<view class="note-info">
						<text class="note-title line-clamp-2">{{ item.title }}</text>
						<view class="like-count">
							<text class="like-icon">❤️</text>
							<text>{{ item.likes }}</text>
						</view>
					</view>
				</view>
				
				<view v-if="favoriteList.length === 0" class="empty">
					<text>暂无收藏内容</text>
				</view>
			</view>

			<view v-if="currentTab === 3" class="history-list">
				<view 
					class="history-item clay-shadow" 
					v-for="(item, index) in historyList" 
					:key="index"
					@click="goToNoteDetail(item.id)"
				>
					<image :src="item.image" class="history-image" mode="aspectFill"></image>
					<view class="history-info">
						<text class="history-title line-clamp-2">{{ item.title }}</text>
						<text class="history-time">{{ item.time }}</text>
					</view>
				</view>
				
				<view v-if="historyList.length === 0" class="empty">
					<text>暂无足迹记录</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getUserInfo, getMyFavorites, getBrowseHistory } from '../../api/user'
import { getMyNotes, getMyLikedNotes } from '../../api/note'

const currentTab = ref(0)

// 用户信息（从后端获取）
const userInfo = ref({})

// 我的笔记（从后端获取）
const myNotes = ref([])

// 点赞列表（从后端获取）
const likedList = ref([])

// 收藏列表（从后端获取）
const favoriteList = ref([])

// 足迹列表（从后端获取）
const historyList = ref([])

const loading = ref(false)

// 计算属性 - 确保头像响应式更新
const avatarUrl = computed(() => {
	return getAvatarUrl(userInfo.value.avatar)
})

onLoad(async () => {
	console.log('Profile page loaded')
	// 强制从服务器获取最新数据
	await loadData()
})

onShow(async () => {
	console.log('Profile page show')
	// 每次显示时都强制刷新数据
	await loadData()
})

// 统一的数据加载函数
const loadData = async () => {
	const token = uni.getStorageSync('token')
	console.log('=== loadData ===', 'token:', token ? token.substring(0, 20) + '...' : '无')
	
	if (!token) {
		console.warn('未找到 token，跳转登录页')
		uni.reLaunch({ url: '/pages/login/login' })
		return
	}
	
	// 清空旧数据，避免显示缓存
	userInfo.value = {}
	myNotes.value = []
	
	try {
		// 并发获取用户信息和笔记列表
		await Promise.all([
			fetchUserInfo(),
			fetchMyNotes()
		])
	} catch (e) {
		console.error('加载数据失败:', e)
		// 如果是认证错误，跳转登录页
		if (e && (e.code === 401 || e.code === 40401)) {
			uni.clearStorageSync()
			uni.reLaunch({ url: '/pages/login/login' })
		}
	}
}

const fetchUserInfo = async () => {
	try {
		console.log('开始请求用户信息...')
		const info = await getUserInfo()
		console.log('用户信息响应:', info)
		
		if (info) {
			// 更新显示数据
			userInfo.value = info
			// 更新缓存
			uni.setStorageSync('userInfo', info)
			console.log('用户信息已更新')
		} else {
			console.warn('未获取到用户信息')
			userInfo.value = {}
		}
	} catch (e) {
		console.error('获取用户信息失败:', e)
		
		// 如果是401或用户不存在,不使用缓存,直接跳转登录
		if (e.code === 401 || e.code === 40401) {
			console.warn('认证失败,清除缓存并跳转登录')
			uni.clearStorageSync()
			uni.reLaunch({ url: '/pages/login/login' })
		} else {
			// 其他错误(如网络错误)，不使用缓存以避免展示旧账号信息
			uni.removeStorageSync('userInfo')
			userInfo.value = {}
			uni.showToast({ title: '获取用户信息失败，请稍后重试', icon: 'none' })
		}
	}
}

const fetchMyNotes = async () => {
	if (loading.value) return
	
	loading.value = true
	try {
		const result = await getMyNotes(1, 20)
		console.log('获取我的笔记:', result)
		
		if (result && result.list) {
			// 转换数据格式
			myNotes.value = result.list.map(note => ({
				id: note.id,
				title: note.title,
				image: note.image || '',
				likes: note.likes || 0
			}))
			console.log('我的笔记已更新:', myNotes.value.length, myNotes.value)
		}
	} catch (e) {
		console.error('获取我的笔记失败:', e)
		// 静默失败，不显示错误
	} finally {
		loading.value = false
	}
}

const fetchFavorites = async () => {
	if (loading.value) return
	
	loading.value = true
	try {
		const result = await getMyFavorites(1, 1, 20) // type=1 表示笔记
		console.log('获取收藏列表:', result)
		
		if (result && result.list) {
			favoriteList.value = result.list.map(item => ({
				id: item.targetId || item.id,
				title: item.title || '未命名笔记',
				image: item.image || '',
				likes: item.likes || 0
			}))
			console.log('收藏列表已更新:', favoriteList.value.length)
		}
	} catch (e) {
		console.error('获取收藏列表失败:', e)
		favoriteList.value = []
	} finally {
		loading.value = false
	}
}

const fetchHistory = async () => {
	if (loading.value) return
	
	loading.value = true
	try {
		const result = await getBrowseHistory(1, 20)
		console.log('获取足迹列表:', result)
		
		if (result && result.list) {
			historyList.value = result.list.map(item => ({
				id: item.noteId || item.targetId || item.id,
				title: item.title || '未命名笔记',
				image: item.image || '',
				time: item.viewTime || item.createdAt || ''
			}))
			console.log('足迹列表已更新:', historyList.value.length)
		}
	} catch (e) {
		console.error('获取足迹列表失败:', e)
		historyList.value = []
	} finally {
		loading.value = false
	}
}

// 获取点赞笔记列表
const fetchLikedNotes = async () => {
	if (loading.value) return
	
	loading.value = true
	try {
		const result = await getMyLikedNotes(1, 20)
		console.log('获取点赞列表:', result)
		
		if (result && result.list) {
			likedList.value = result.list.map(item => ({
				id: item.id,
				title: item.title || '未命名笔记',
				image: item.image || '',
				likes: item.likes || 0
			}))
			console.log('点赞列表已更新:', likedList.value.length)
		}
	} catch (e) {
		console.error('获取点赞列表失败:', e)
		likedList.value = []
	} finally {
		loading.value = false
	}
}

const switchTab = (index) => {
	currentTab.value = index
	
	// 根据标签加载对应数据
	if (index === 1) {
		// 点赞标签
		fetchLikedNotes()
	} else if (index === 2) {
		// 收藏标签
		fetchFavorites()
	} else if (index === 3) {
		// 足迹标签
		fetchHistory()
	}
}

const openSettings = () => {
	uni.navigateTo({
		url: '/pages/settings/settings'
	})
}

const goToNoteDetail = (id) => {
	uni.navigateTo({
		url: `/pages/note-detail/note-detail?id=${id}`
	})
}

const goToList = (type) => {
	uni.navigateTo({
		url: `/pages/user-list/user-list?type=${type}`
	})
}

const goToLikeNotifications = () => {
	uni.navigateTo({
		url: '/pages/like-notifications/like-notifications'
	})
}

// 获取头像完整URL
const getAvatarUrl = (avatar) => {
	if (!avatar) {
		return 'https://via.placeholder.com/200/FFD166/FFFFFF?text=User'
	}
	
	// 直接返回阿里云OSS URL
	return avatar
}
</script>

<style lang="scss" scoped>
.container {
	background: #F7F9FC;
	min-height: 100vh;
	padding-bottom: 100rpx;
}

.profile-header {
	background: linear-gradient(to bottom, rgba(255, 158, 100, 0.2), white);
	padding: 30rpx;
}

.user-info {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
}

.avatar {
	width: 140rpx;
	height: 140rpx;
	border-radius: 50%;
	border: 6rpx solid white;
	box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.1);
	margin-right: 25rpx;
}

.info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.username {
	font-size: 40rpx;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.user-id {
	font-size: 26rpx;
	color: #999;
}

.setting-btn {
	width: 70rpx;
	height: 70rpx;
	background: white;
	font-size: 32rpx;
}

.bio {
	display: block;
	font-size: 28rpx;
	color: #666;
	margin-bottom: 30rpx;
}

.stats-card {
	background: white;
	border-radius: 40rpx;
	padding: 30rpx;
	display: flex;
	justify-content: space-around;
}

.stat {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.stat-value {
	font-size: 40rpx;
	font-weight: bold;
	margin-bottom: 5rpx;
}

.stat-label {
	font-size: 24rpx;
	color: #999;
}

.divider {
	width: 1rpx;
	background: #f0f0f0;
}

.tabs {
	display: flex;
	background: white;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
	position: sticky;
	top: 0;
	z-index: 10;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 30rpx;
	font-size: 28rpx;
	color: #999;
	position: relative;
}

.tab-item.active {
	color: #FF9E64;
	font-weight: 500;
}

.tab-item.active::after {
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

.content {
	padding: 30rpx;
}

.notes-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;
}

.note-card {
	background: white;
	border-radius: 30rpx;
	overflow: hidden;
}

.note-image {
	width: 100%;
	height: 350rpx;
}

.note-info {
	padding: 20rpx;
}

.note-title {
	display: block;
	font-size: 28rpx;
	font-weight: 500;
	margin-bottom: 15rpx;
}

.like-count {
	display: flex;
	align-items: center;
	font-size: 24rpx;
	color: #EF476F;
}

.like-icon {
	margin-right: 5rpx;
}

.empty {
	text-align: center;
	padding: 100rpx 0;
	color: #999;
	font-size: 28rpx;
}

.history-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.history-item {
	background: white;
	border-radius: 20rpx;
	padding: 20rpx;
	display: flex;
	gap: 20rpx;
}

.history-image {
	width: 160rpx;
	height: 160rpx;
	border-radius: 15rpx;
	flex-shrink: 0;
}

.history-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.history-title {
	font-size: 28rpx;
	font-weight: 500;
	color: #333;
	line-height: 1.4;
}

.history-time {
	font-size: 24rpx;
	color: #999;
}
</style>
