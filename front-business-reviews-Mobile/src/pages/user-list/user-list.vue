<template>
	<view class="container">
		<!-- 导航栏 -->
		<view class="navbar">
			<view class="nav-btn" @click="goBack">
				<text>←</text>
			</view>
			<text class="nav-title">{{ title }}</text>
			<view class="nav-btn"></view>
		</view>

		<!-- 加载中 -->
		<view v-if="loading" class="loading-container">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 错误提示 -->
		<view v-else-if="error" class="error-container" @click="retry">
			<text class="error-text">{{ error }}</text>
			<text class="retry-text">点击重试</text>
		</view>

		<!-- 列表 -->
		<view v-else class="list">
			<view 
				class="list-item clay-shadow" 
				v-for="(item, index) in list" 
				:key="index"
				@click="goToUserProfile(item.id)"
			>
				<image :src="item.avatar" class="avatar" mode="aspectFill"></image>
				<view class="info">
					<text class="username">{{ item.username }}</text>
					<text class="bio">{{ item.bio || '这个人很懒，什么都没留下' }}</text>
				</view>
				<view class="follow-btn" :class="{ 'followed': item.isFollowing }" @click.stop="toggleFollow(item)">
					<text>{{ item.isFollowing ? '已关注' : '+ 关注' }}</text>
				</view>
			</view>
			
			<view v-if="list.length === 0 && !loading" class="empty">
				<text>{{ emptyText }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getFollowingList, getFollowerList, followUser, unfollowUser } from '../../api/user'

const type = ref('') // following, follower
const title = ref('')
const list = ref([])
const loading = ref(false)
const error = ref('')

const emptyText = computed(() => {
	if (type.value === 'following') return '还没有关注任何人'
	if (type.value === 'follower') return '还没有粉丝'
	return '暂无数据'
})

onLoad((options) => {
	console.log('=== user-list页面加载 ===')
	console.log('options:', options)
	type.value = options.type || ''
	
	if (type.value === 'following') {
		title.value = '关注'
		fetchFollowing()
	} else if (type.value === 'follower') {
		title.value = '粉丝'
		fetchFollowers()
	} else {
		console.warn('未知的type参数:', type.value)
	}
})

// 重试
const retry = () => {
	error.value = ''
	if (type.value === 'following') {
		fetchFollowing()
	} else if (type.value === 'follower') {
		fetchFollowers()
	}
}

const fetchFollowing = async () => {
	if (loading.value) return
	loading.value = true
	error.value = ''
	
	console.log('=== 开始获取关注列表 ===')
	try {
		const result = await getFollowingList(1, 50)
		console.log('关注列表结果:', result)
		
		if (result && result.list) {
			list.value = result.list.map(user => ({
				id: user.userId || user.id,
				username: user.username || '未知用户',
				avatar: user.avatar || 'https://via.placeholder.com/100',
				bio: user.bio || '',
				isFollowing: true
			}))
			console.log('处理后的列表数据:', list.value)
		} else {
			console.warn('无效的结果格式:', result)
			list.value = []
		}
	} catch (e) {
		console.error('获取关注列表失败:', e)
		console.error('错误详情:', JSON.stringify(e))
		error.value = e.message || '网络请求失败，请检查网络连接'
	} finally {
		loading.value = false
		console.log('=== 获取关注列表结束 ===')
	}
}

const fetchFollowers = async () => {
	if (loading.value) return
	loading.value = true
	error.value = ''
	
	try {
		const result = await getFollowerList(1, 50)
		if (result && result.list) {
			list.value = result.list.map(user => ({
				id: user.userId || user.id,
				username: user.username || '未知用户',
				avatar: user.avatar || 'https://via.placeholder.com/100',
				bio: user.bio || '',
				isFollowing: user.isFollowing || false
			}))
		}
	} catch (e) {
		console.error('获取粉丝列表失败:', e)
		error.value = e.message || '网络请求失败，请检查网络连接'
	} finally {
		loading.value = false
	}
}

const toggleFollow = async (item) => {
	try {
		if (item.isFollowing) {
			await unfollowUser(item.id.toString())
			item.isFollowing = false
			uni.showToast({ title: '已取消关注', icon: 'success' })
		} else {
			await followUser(item.id.toString())
			item.isFollowing = true
			uni.showToast({ title: '关注成功', icon: 'success' })
		}
	} catch (e) {
		console.error('关注操作失败:', e)
		uni.showToast({ title: e.message || '操作失败', icon: 'none' })
	}
}

const goToUserProfile = (userId) => {
	uni.navigateTo({
		url: `/pages/user-profile/user-profile?userId=${userId}`
	})
}

const goBack = () => {
	uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.container {
	background: #F7F9FC;
	min-height: 100vh;
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

.list {
	padding: 20rpx;
}

.list-item {
	background: white;
	border-radius: 30rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.avatar {
	width: 100rpx;
	height: 100rpx;
	border-radius: 50%;
	flex-shrink: 0;
}

.info {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 10rpx;
}

.username {
	font-size: 32rpx;
	font-weight: 500;
	color: #333;
}

.bio {
	font-size: 26rpx;
	color: #999;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.follow-btn {
	padding: 16rpx 32rpx;
	border-radius: 30rpx;
	background: #FF9E64;
	font-size: 26rpx;
	color: white;
	flex-shrink: 0;
}

.follow-btn.followed {
	background: #F5F5F5;
	color: #999;
}

.empty {
	text-align: center;
	padding: 100rpx 0;
	color: #999;
	font-size: 28rpx;
}

.loading-container {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 100rpx 0;
}

.loading-text {
	font-size: 28rpx;
	color: #999;
}

.error-container {
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	padding: 100rpx 30rpx;
}

.error-text {
	font-size: 28rpx;
	color: #999;
	margin-bottom: 20rpx;
	text-align: center;
}

.retry-text {
	font-size: 28rpx;
	color: #FF9E64;
}
</style>
