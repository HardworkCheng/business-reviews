<template>
	<view class="container">
		<view class="profile-header">
			<!-- 背景装饰 -->
			<view class="header-bg"></view>
			
			<view class="user-info">
				<view class="avatar-wrapper">
					<image class="avatar" :src="avatarUrl" mode="aspectFill"></image>
					<view class="avatar-border"></view>
				</view>
				<view class="info">
					<text class="username">{{ userInfo.username || '未登录' }}</text>
					<text class="user-id">{{ userInfo.userId ? 'ID: ' + userInfo.userId : '点击登录' }}</text>
				</view>
				<view class="setting-btn" @click="openSettings">
					<image src="/static/icons/settings.png" class="setting-icon" mode="aspectFit"></image>
				</view>
			</view>

			<text class="bio">{{ userInfo.bio || '这个人很懒，什么都没留下~' }}</text>

			<view class="stats-card">
				<view class="stats-inner">
					<view class="stat-item" @click="goToList('following')">
						<text class="stat-num">{{ userInfo.followingCount || 0 }}</text>
						<text class="stat-label">关注</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item" @click="goToList('follower')">
						<text class="stat-num">{{ userInfo.followerCount || 0 }}</text>
						<text class="stat-label">粉丝</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item" @click="goToLikeNotifications">
						<text class="stat-num">{{ userInfo.likeCount || 0 }}</text>
						<text class="stat-label">获赞</text>
					</view>
					<view class="stat-divider"></view>
					<view class="stat-item" @click="switchTab(2)">
						<text class="stat-num">{{ userInfo.favoriteCount || 0 }}</text>
						<text class="stat-label">收藏</text>
					</view>
				</view>
			</view>
		</view>

		<view class="tabs-container">
			<view class="tabs">
				<view 
					class="tab-item" 
					:class="{ active: currentTab === 0 }"
					@click="switchTab(0)"
				>
					<text class="tab-text">我的笔记</text>
					<view class="tab-indicator" v-if="currentTab === 0"></view>
				</view>
				<view 
					class="tab-item" 
					:class="{ active: currentTab === 3 }"
					@click="switchTab(3)"
				>
					<text class="tab-text">足迹</text>
					<view class="tab-indicator" v-if="currentTab === 3"></view>
				</view>
				<view 
					class="tab-item" 
					:class="{ active: currentTab === 2 }"
					@click="switchTab(2)"
				>
					<text class="tab-text">收藏</text>
					<view class="tab-indicator" v-if="currentTab === 2"></view>
				</view>
				<view 
					class="tab-item" 
					:class="{ active: currentTab === 1 }"
					@click="switchTab(1)"
				>
					<text class="tab-text">点赞</text>
					<view class="tab-indicator" v-if="currentTab === 1"></view>
				</view>
			</view>
		</view>

		<view class="content">
			<!-- 瀑布流/网格布局内容 -->
			<view class="notes-grid">
				<template v-if="currentList.length > 0">
					<view 
						class="note-card" 
						v-for="(note, index) in currentList" 
						:key="index"
						@click="goToNoteDetail(note.id)"
					>
						<view class="image-box">
							<image :src="note.image" class="note-image" mode="aspectFill"></image>
							<view class="image-mask"></view>
						</view>
						<view class="card-body">
							<text class="note-title">{{ note.title }}</text>
							<view class="card-footer">
								<view class="author-row">
									<text class="author-name">@{{ note.author }}</text>
								</view>
								<view class="like-row">
									<image src="/static/icons/like-small.png" class="like-icon-small" mode="aspectFit"></image>
									<text class="like-num">{{ note.likes }}</text>
								</view>
							</view>
							<text class="publish-time">{{ note.createTime }}</text>
						</view>
					</view>
				</template>
				
				<!-- 空状态 -->
				<view v-else class="empty-state">
					<image src="/static/icons/empty.png" class="empty-icon" mode="aspectFit"></image>
					<text class="empty-text">{{ getEmptyText() }}</text>
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
import { getImageUrl } from '../../utils/placeholder'


const currentTab = ref(0)
const userInfo = ref({})
const myNotes = ref([])
const likedList = ref([])
const favoriteList = ref([])
const historyList = ref([])
const loading = ref(false)

// 当前显示的列表
const currentList = computed(() => {
	switch(currentTab.value) {
		case 0: return myNotes.value
		case 1: return likedList.value
		case 2: return favoriteList.value
		case 3: return historyList.value
		default: return []
	}
})

// 空状态文案
const getEmptyText = () => {
	switch(currentTab.value) {
		case 0: return '还没有发布笔记，去发布第一篇吧~'
		case 1: return '还没有点赞过笔记~'
		case 2: return '还没有收藏内容~'
		case 3: return '还没有浏览足迹~'
		default: return '暂无内容'
	}
}

// 时间格式化
const formatTime = (timeStr) => {
	if (!timeStr) return ''
	try {
		const date = new Date(timeStr)
		const now = new Date()
		const diff = now - date
		const minutes = Math.floor(diff / 60000)
		const hours = Math.floor(diff / 3600000)
		const days = Math.floor(diff / 86400000)
		
		if (minutes < 1) return '刚刚'
		if (minutes < 60) return `${minutes}分钟前`
		if (hours < 24) return `${hours}小时前`
		if (days < 7) return `${days}天前`
		if (days < 30) return `${Math.floor(days / 7)}周前`
		if (days < 365) return `${Math.floor(days / 30)}个月前`
		return `${Math.floor(days / 365)}年前`
	} catch (e) {
		return ''
	}
}

const avatarUrl = computed(() => {
	return getImageUrl(userInfo.value.avatar)
})

onLoad(async () => {
	await loadData()
})

onShow(async () => {
	await loadData()
})

const loadData = async () => {
	const token = uni.getStorageSync('token')
	if (!token) {
		uni.reLaunch({ url: '/pages/login/login' })
		return
	}
	
	try {
		await fetchUserInfo()
		// 初始加载当前Tab数据
		switchTab(currentTab.value)
	} catch (e) {
		if (e && (e.code === 401 || e.code === 40401)) {
			uni.clearStorageSync()
			uni.reLaunch({ url: '/pages/login/login' })
		}
	}
}

const fetchUserInfo = async () => {
	try {
		const info = await getUserInfo()
		if (info) {
			userInfo.value = info
			uni.setStorageSync('userInfo', info)
		}
	} catch (e) {
		console.error('获取用户信息失败', e)
	}
}

// 数据获取函数封装
const fetchList = async (apiFunc, listRef, type = 'normal') => {
	if (loading.value) return
	loading.value = true
	try {
		// 根据不同接口适配参数
		let result
		if (type === 'favorite') {
			result = await apiFunc(1, 1, 20)
		} else {
			result = await apiFunc(1, 20)
		}
		
		if (result && result.list) {
			listRef.value = result.list.map(item => ({
				id: item.noteId || item.targetId || item.id,
				title: item.title || '未命名笔记',
				image: getImageUrl(item.image, 'cover'),
				author: item.author || '匿名用户',
				likes: item.likes || 0,
				createTime: item.viewTime || formatTime(item.createdAt)
			}))
		}
	} catch (e) {
		console.error('获取列表失败', e)
		listRef.value = []
	} finally {
		loading.value = false
	}
}

const switchTab = (index) => {
	currentTab.value = index
	if (index === 0 && myNotes.value.length === 0) fetchList(getMyNotes, myNotes)
	else if (index === 1 && likedList.value.length === 0) fetchList(getMyLikedNotes, likedList)
	else if (index === 2 && favoriteList.value.length === 0) fetchList(getMyFavorites, favoriteList, 'favorite')
	else if (index === 3 && historyList.value.length === 0) fetchList(getBrowseHistory, historyList)
}

const openSettings = () => uni.navigateTo({ url: '/pages/settings/settings' })
const goToNoteDetail = (id) => uni.navigateTo({ url: `/pages/note-detail/note-detail?id=${id}` })
const goToList = (type) => uni.navigateTo({ url: `/pages/user-list/user-list?type=${type}` })
const goToLikeNotifications = () => uni.navigateTo({ url: '/pages/like-notifications/like-notifications' })

</script>

<style lang="scss" scoped>
.container {
	background-color: #FAFBFC;
	min-height: 100vh;
}

.profile-header {
	position: relative;
	padding: 40rpx 40rpx 60rpx;
	overflow: hidden;
	background: white;
	border-bottom-left-radius: 40rpx;
	border-bottom-right-radius: 40rpx;
	box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.02);
	z-index: 1;
}

.header-bg {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: linear-gradient(180deg, rgba(255, 158, 100, 0.15) 0%, rgba(255, 255, 255, 0) 100%);
	z-index: -1;
}

.user-info {
	display: flex;
	align-items: center;
	margin-top: 20rpx;
}

.avatar-wrapper {
	position: relative;
	margin-right: 30rpx;
}

.avatar {
	width: 130rpx;
	height: 130rpx;
	border-radius: 50%;
	border: 4rpx solid #fff;
	box-shadow: 0 8rpx 16rpx rgba(255, 158, 100, 0.2);
}

.info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.username {
	font-size: 44rpx;
	font-weight: 700;
	color: #333;
	margin-bottom: 8rpx;
	letter-spacing: 1rpx;
}

.user-id {
	font-size: 24rpx;
	color: #999;
	background: rgba(0,0,0,0.03);
	padding: 4rpx 12rpx;
	border-radius: 20rpx;
	align-self: flex-start;
}

.setting-btn {
	width: 80rpx;
	height: 80rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background: rgba(255,255,255,0.8);
	border-radius: 50%;
	backdrop-filter: blur(10px);
	box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.05);
	transition: transform 0.2s;
	
	&:active {
		transform: scale(0.95);
	}
}

.setting-icon {
	width: 44rpx;
	height: 44rpx;
	opacity: 0.8;
}

.bio {
	display: block;
	margin-top: 30rpx;
	font-size: 28rpx;
	color: #666;
	line-height: 1.5;
	padding: 0 10rpx;
}

/* 定义流光动画 */
/* 定义绚丽流光动画 */
/* 定义赛博故障动画 */
/* 定义丝绸流光动画 */
/* 定义水晶折射动画 */
/* 定义皇家流光动画 */
/* 定义森林秘境动画 */
/* 定义玫瑰金流光动画 */
@keyframes roseGoldFlow {
	0% { background-position: 0% 50%; }
	50% { background-position: 100% 50%; }
	100% { background-position: 0% 50%; }
}

.stats-card {
	position: relative;
	margin-top: 40rpx;
	padding: 4rpx;
	/* 玫瑰金：优雅的粉金色调，细腻温润的金属质感 */
	background: linear-gradient(
		125deg, 
		#E0BFB8, /* 柔粉金 */
		#B76E79, /* 经典玫瑰金 */
		#FFFFFF, /* 金属高光 */
		#F5D1C5, /* 浅玫瑰色 */
		#B76E79,
		#E0BFB8
	);
	background-size: 300% 300%;
	animation: roseGoldFlow 6s ease-in-out infinite;
	border-radius: 36rpx;
	/* 柔美光影：温暖的粉棕色阴影 */
	box-shadow: 
		0 12rpx 36rpx -6rpx rgba(183, 110, 121, 0.4),
		0 6rpx 16rpx -2rpx rgba(224, 191, 184, 0.3);
}

.stats-inner {
	position: relative;
	display: flex;
	justify-content: space-around;
	align-items: center;
	/* 提高不透明度，让背景更纯净，衬托边框的鲜艳 */
	background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(255, 255, 255, 0.99) 100%);
	border-radius: 32rpx;
	backdrop-filter: blur(20rpx);
	overflow: hidden;
	height: 160rpx;
}

.stat-item {
	position: relative;
	z-index: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 0 20rpx;
	flex: 1;
	height: 100%;
	transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	cursor: pointer;
	
	&:active {
		transform: scale(0.92);
		background: rgba(0,0,0,0.02);
	}
}

.stat-num {
	font-size: 44rpx;
	font-weight: 800;
	color: #333;
	margin-bottom: 12rpx;
	transition: all 0.3s ease;
	/* 匹配玫瑰金边框：深玫瑰-柔粉 */
	background: linear-gradient(135deg, #B76E79 0%, #E0BFB8 100%);
	-webkit-background-clip: text;
	-webkit-text-fill-color: transparent;
	background-clip: text;
	text-shadow: 0 4rpx 10rpx rgba(183, 110, 121, 0.2);
}

.stat-label {
	font-size: 24rpx;
	color: #888;
	font-weight: 500;
	letter-spacing: 1rpx;
}

.stat-divider {
	position: relative;
	z-index: 1;
	width: 2rpx;
	height: 40%; /* 相对高度 */
	background: linear-gradient(180deg, 
		rgba(255, 158, 100, 0) 0%, 
		rgba(255, 158, 100, 0.4) 50%, 
		rgba(255, 158, 100, 0) 100%
	);
}

/* Tabs */
.tabs-container {
	position: sticky;
	top: 0;
	z-index: 10;
	background: #FAFBFC;
	padding: 20rpx 0;
}

.tabs {
	display: flex;
	justify-content: space-around;
	padding: 0 20rpx;
}

.tab-item {
	position: relative;
	padding: 16rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.tab-text {
	font-size: 28rpx;
	color: #888;
	font-weight: 500;
	transition: all 0.3s;
}

.tab-item.active .tab-text {
	color: #333;
	font-weight: 700;
	font-size: 30rpx;
}

.tab-indicator {
	width: 40rpx;
	height: 6rpx;
	background: linear-gradient(90deg, #FF9E64, #FF7A45);
	border-radius: 3rpx;
	margin-top: 10rpx;
	box-shadow: 0 2rpx 6rpx rgba(255, 158, 100, 0.4);
}

/* Content */
.content {
	padding: 0 20rpx 40rpx;
}

.notes-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 20rpx;
}

.note-card {
	background: white;
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.04);
	display: flex;
	flex-direction: column;
}

.image-box {
	position: relative;
	width: 100%;
	padding-bottom: 133%; /* 3:4 aspect ratio */
	background: #f5f5f5;
}

.note-image {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
}

.image-mask {
	position: absolute;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: linear-gradient(180deg, transparent 70%, rgba(0,0,0,0.05) 100%);
	pointer-events: none;
}

.card-body {
	padding: 20rpx;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
}

.note-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
	line-height: 1.4;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
}

.card-footer {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.author-row {
	display: flex;
	align-items: center;
}

.author-name {
	font-size: 22rpx;
	color: #999;
	max-width: 140rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.like-row {
	display: flex;
	align-items: center;
	gap: 6rpx;
}

.like-icon-small {
	width: 24rpx;
	height: 24rpx;
}

.like-num {
	font-size: 22rpx;
	color: #999;
}

.publish-time {
	font-size: 20rpx;
	color: #ccc;
	align-self: flex-end;
}

/* Empty State */
.empty-state {
	grid-column: 1 / -1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 100rpx 0;
}

.empty-icon {
	width: 200rpx;
	height: 200rpx;
	opacity: 0.5;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

</style>
