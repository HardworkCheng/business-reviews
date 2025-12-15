<template>
	<view class="container">
		<!-- 导航栏 -->
		<view class="navbar">
			<view class="nav-btn" @click="goBack">
				<text>←</text>
			</view>
			<text class="nav-title">获赞通知</text>
			<view class="nav-btn"></view>
		</view>

		<!-- 获赞列表 -->
		<view class="like-list">
			<view 
				class="like-item clay-shadow" 
				v-for="(item, index) in likeList" 
				:key="index"
				@click="goToNoteDetail(item.noteId)"
			>
				<image :src="item.userAvatar" class="user-avatar"></image>
				<view class="item-content">
					<view class="item-header">
						<text class="username">{{ item.username }}</text>
						<text class="time">{{ item.time }}</text>
					</view>
					<text class="action-text">赞了你的笔记</text>
					<view class="note-info" v-if="item.noteImage || item.noteTitle">
						<image v-if="item.noteImage" :src="item.noteImage" class="note-image" mode="aspectFill"></image>
						<text class="note-title line-clamp-2">{{ item.noteTitle || '查看笔记' }}</text>
					</view>
				</view>
			</view>

			<view v-if="likeList.length === 0 && !loading" class="empty">
				<text>还没有获赞记录</text>
			</view>

			<view v-if="loading" class="loading">
				<text>加载中...</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNotifications } from '../../api/message'

const likeList = ref([])
const loading = ref(false)

onLoad(async () => {
	console.log('Like notifications page loaded')
	await fetchLikeNotifications()
})

const fetchLikeNotifications = async () => {
	loading.value = true
	try {
		// 调用获取通知的API，type=1 表示点赞通知
		const result = await getNotifications(1, 50, 1)
		console.log('获赞通知结果:', result)
		
		if (result && result.list) {
			// 映射后端返回的字段
			likeList.value = result.list.map(item => ({
				id: item.id,
				username: item.fromUsername || '未知用户',
				userAvatar: item.fromAvatar || 'https://via.placeholder.com/100',
				time: item.timeAgo || item.createdAt || '',
				noteId: item.noteId || item.relatedId,
				noteImage: item.noteImage || '',
				noteTitle: item.noteTitle || '查看笔记'
			}))
			console.log('获赞列表已更新:', likeList.value.length)
		} else {
			likeList.value = []
		}
	} catch (e) {
		console.error('获取点赞通知失败:', e)
		likeList.value = []
	} finally {
		loading.value = false
	}
}

const goBack = () => {
	uni.navigateBack()
}

const goToNoteDetail = (noteId) => {
	if (noteId) {
		uni.navigateTo({
			url: `/pages/note-detail/note-detail?id=${noteId}`
		})
	}
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

.like-list {
	padding: 30rpx;
}

.like-item {
	background: white;
	border-radius: 20rpx;
	padding: 25rpx;
	margin-bottom: 20rpx;
	display: flex;
	flex-direction: row;
	align-items: flex-start;
	gap: 20rpx;
}

.user-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	border: 3rpx solid #FF9E64;
	flex-shrink: 0;
	object-fit: cover;
}

.item-content {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	min-width: 0;
}

.item-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.username {
	font-size: 28rpx;
	font-weight: 500;
	color: #333;
}

.time {
	font-size: 24rpx;
	color: #999;
}

.action-text {
	font-size: 26rpx;
	color: #666;
}

.note-info {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 15rpx;
	margin-top: 10rpx;
	padding: 15rpx;
	background: #F7F9FC;
	border-radius: 15rpx;
}

.note-image {
	width: 100rpx;
	height: 100rpx;
	border-radius: 10rpx;
	flex-shrink: 0;
	object-fit: cover;
}

.note-title {
	flex: 1;
	font-size: 26rpx;
	color: #333;
	line-height: 1.5;
	word-break: break-all;
}

.line-clamp-2 {
	display: -webkit-box;
	-webkit-box-orient: vertical;
	-webkit-line-clamp: 2;
	overflow: hidden;
}

.empty {
	text-align: center;
	padding: 100rpx 0;
	color: #999;
	font-size: 28rpx;
}

.loading {
	text-align: center;
	padding: 50rpx 0;
	color: #999;
	font-size: 28rpx;
}

.clay-shadow {
	box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.08);
}
</style>
