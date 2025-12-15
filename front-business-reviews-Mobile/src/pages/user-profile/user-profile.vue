<template>
	<view class="container">
		<!-- 头部导航 -->
		<view class="header">
			<view class="nav-bar">
				<view class="nav-btn clay-icon" @click="goBack">
					<text>←</text>
				</view>
				<text class="nav-title">用户主页</text>
				<view class="nav-placeholder"></view>
			</view>
		</view>

		<!-- 加载中 -->
		<view v-if="loading" class="loading-container">
			<text>加载中...</text>
		</view>

		<!-- 用户信息 -->
		<view v-else class="profile-content">
			<view class="profile-header">
				<view class="user-info">
					<image class="avatar" :src="userInfo.avatar || defaultAvatar"></image>
					<view class="info">
						<text class="username">{{ userInfo.username || '未知用户' }}</text>
						<text class="user-id">ID: {{ userId }}</text>
					</view>
				</view>

				<text class="bio">{{ userInfo.bio || '这个人很懒，什么都没留下' }}</text>
				
				<!-- 操作按钮 -->
				<view v-if="!isSelf" class="action-buttons">
					<view class="follow-btn clay-icon" :class="{ following: isFollowing }" @click="toggleFollow">
						<text>{{ isFollowing ? '已关注' : '+ 关注' }}</text>
					</view>
					<view class="message-btn clay-icon" @click="goToChat">
						<text>💬 私信</text>
					</view>
				</view>

				<view class="stats-card clay-shadow">
					<view class="stat">
						<text class="stat-value text-primary">{{ userInfo.followingCount || 0 }}</text>
						<text class="stat-label">关注</text>
					</view>
					<view class="divider"></view>
					<view class="stat">
						<text class="stat-value text-primary">{{ userInfo.followerCount || 0 }}</text>
						<text class="stat-label">粉丝</text>
					</view>
					<view class="divider"></view>
					<view class="stat">
						<text class="stat-value text-primary">{{ userInfo.likeCount || 0 }}</text>
						<text class="stat-label">获赞</text>
					</view>
				</view>
			</view>

			<!-- 用户笔记 -->
			<view class="section-title">Ta的笔记</view>
			<view class="notes-grid">
				<view 
					class="note-card clay-shadow" 
					v-for="(note, index) in noteList" 
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
				
				<view v-if="noteList.length === 0" class="empty">
					<text>暂无笔记</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { followUser, unfollowUser } from '../../api/user'
import { getUserNotes } from '../../api/note'
import { get } from '../../api/request'

const userId = ref('')
const userInfo = ref({})
const noteList = ref([])
const loading = ref(false)
const isFollowing = ref(false)
const defaultAvatar = 'https://via.placeholder.com/200/FFD166/FFFFFF?text=User'

// 判断是否是自己的主页
const isSelf = computed(() => {
	const currentUserId = uni.getStorageSync('userInfo')?.userId
	return currentUserId && currentUserId.toString() === userId.value.toString()
})

onLoad(async (options) => {
	console.log('User profile loaded, userId:', options.userId)
	userId.value = options.userId
	if (options.userId) {
		await loadUserProfile()
	}
})

const loadUserProfile = async () => {
	loading.value = true
	try {
		await Promise.all([
			fetchUserInfo(),
			fetchUserNotes()
		])
	} catch (e) {
		console.error('加载用户信息失败:', e)
	} finally {
		loading.value = false
	}
}

const fetchUserInfo = async () => {
	try {
		// 调用获取用户公开信息的API
		const result = await get(`/user/profile/${userId.value}`)
		console.log('用户信息:', result)
		
		if (result) {
			userInfo.value = result
			isFollowing.value = result.isFollowing || false
		}
	} catch (e) {
		console.error('获取用户信息失败:', e)
	}
}

const fetchUserNotes = async () => {
	try {
		const result = await getUserNotes(userId.value, 1, 20)
		console.log('用户笔记:', result)
		
		if (result && result.list) {
			noteList.value = result.list.map(note => ({
				id: note.id,
				title: note.title || '未命名笔记',
				image: note.image || '',
				likes: note.likes || 0
			}))
		}
	} catch (e) {
		console.error('获取用户笔记失败:', e)
		noteList.value = []
	}
}

const toggleFollow = async () => {
	try {
		if (isFollowing.value) {
			await unfollowUser(userId.value.toString())
			isFollowing.value = false
			userInfo.value.followerCount = Math.max(0, (userInfo.value.followerCount || 1) - 1)
			uni.showToast({ title: '已取消关注', icon: 'success' })
		} else {
			await followUser(userId.value.toString())
			isFollowing.value = true
			userInfo.value.followerCount = (userInfo.value.followerCount || 0) + 1
			uni.showToast({ title: '关注成功', icon: 'success' })
		}
	} catch (e) {
		console.error('关注操作失败:', e)
		uni.showToast({ title: e.message || '操作失败', icon: 'none' })
	}
}

const goToNoteDetail = (id) => {
	uni.navigateTo({
		url: `/pages/note-detail/note-detail?id=${id}`
	})
}

const goBack = () => {
	uni.navigateBack()
}

// 跳转到聊天页面
const goToChat = () => {
	uni.navigateTo({
		url: `/pages/chat/chat?userId=${userId.value}&username=${userInfo.value.username}&avatar=${userInfo.value.avatar}`
	})
}
</script>

<style lang="scss" scoped>
.container {
	background: #F7F9FC;
	min-height: 100vh;
	padding-bottom: 50rpx;
}

.header {
	position: sticky;
	top: 0;
	z-index: 100;
	background: white;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
}

.nav-btn {
	width: 60rpx;
	height: 60rpx;
	background: white;
	border: 2rpx solid #000;
	font-size: 32rpx;
}

.nav-title {
	font-size: 36rpx;
	font-weight: bold;
}

.nav-placeholder {
	width: 60rpx;
}

.loading-container {
	display: flex;
	justify-content: center;
	align-items: center;
	height: 400rpx;
	color: #999;
	font-size: 28rpx;
}

.profile-content {
	padding: 30rpx;
}

.profile-header {
	background: linear-gradient(to bottom, rgba(255, 158, 100, 0.2), white);
	border-radius: 30rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
}

.user-info {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	border: 4rpx solid white;
	box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1);
	margin-right: 20rpx;
}

.info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.username {
	font-size: 36rpx;
	font-weight: bold;
	margin-bottom: 8rpx;
}

.user-id {
	font-size: 24rpx;
	color: #999;
}

.action-buttons {
	display: flex;
	gap: 20rpx;
	margin-top: 20rpx;
}

.follow-btn {
	flex: 1;
	padding: 20rpx 30rpx;
	background: linear-gradient(135deg, #FF9E64 0%, #FF7A45 100%);
	color: white;
	border-radius: 40rpx;
	font-size: 28rpx;
	text-align: center;
	box-shadow: 0 4rpx 12rpx rgba(255, 158, 100, 0.3);
}

.follow-btn.following {
	background: #E8E8E8;
	color: #666;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.message-btn {
	flex: 1;
	padding: 20rpx 30rpx;
	background: white;
	border: 2rpx solid #FF9E64;
	border-radius: 40rpx;
	font-size: 28rpx;
	color: #FF9E64;
	text-align: center;
	box-shadow: 0 4rpx 12rpx rgba(255, 158, 100, 0.2);
}

.bio {
	display: block;
	font-size: 28rpx;
	color: #666;
	margin-bottom: 20rpx;
}

.stats-card {
	background: white;
	border-radius: 30rpx;
	padding: 25rpx;
	display: flex;
	justify-content: space-around;
}

.stat {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.stat-value {
	font-size: 36rpx;
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

.section-title {
	font-size: 32rpx;
	font-weight: bold;
	margin-bottom: 20rpx;
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
}

.note-image {
	width: 100%;
	height: 300rpx;
}

.note-info {
	padding: 15rpx;
}

.note-title {
	display: block;
	font-size: 26rpx;
	font-weight: 500;
	margin-bottom: 10rpx;
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
	grid-column: span 2;
	text-align: center;
	padding: 100rpx 0;
	color: #999;
	font-size: 28rpx;
}

.line-clamp-2 {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.text-primary {
	color: #FF9E64;
}

.clay-shadow {
	box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.08);
}

.clay-icon {
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	transition: all 0.3s ease;
}
</style>
