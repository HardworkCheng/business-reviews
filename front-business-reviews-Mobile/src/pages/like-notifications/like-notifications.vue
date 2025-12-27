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
/* 定义玫瑰金边框流光动画 */
@keyframes roseGoldBorder {
	0% { border-color: #E0BFB8; }
	50% { border-color: #B76E79; }
	100% { border-color: #E0BFB8; }
}

.container {
	background: linear-gradient(180deg, #FBF4F6 0%, #FAFAFA 100%);
	min-height: 100vh;
}

.navbar {
	background: rgba(255, 255, 255, 0.9);
	backdrop-filter: blur(10px);
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
	box-shadow: 0 4rpx 20rpx rgba(224, 191, 184, 0.2);
	position: sticky;
	top: 0;
	z-index: 100;
}

.nav-btn {
	width: 60rpx;
	font-size: 40rpx;
	color: #B76E79;
}

.nav-title {
	font-size: 36rpx;
	font-weight: 600;
	color: #8E5C5C;
}

.like-list {
	padding: 24rpx;
}

.like-item {
	background: white;
	border-radius: 24rpx;
	padding: 30rpx;
	margin-bottom: 24rpx;
	display: flex;
	flex-direction: row;
	align-items: flex-start;
	gap: 24rpx;
	box-shadow: 0 8rpx 24rpx -6rpx rgba(183, 110, 121, 0.1),
				0 4rpx 10rpx -2rpx rgba(224, 191, 184, 0.15);
	/* 静态边框作为回退 */
	border: 2rpx solid transparent;
	/* 动感边框 */
	animation: roseGoldBorder 4s infinite ease-in-out;
	position: relative;
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.98);
	}
}

.user-avatar {
	width: 90rpx;
	height: 90rpx;
	border-radius: 50%;
	border: 3rpx solid #E0BFB8;
	flex-shrink: 0;
	object-fit: cover;
	padding: 2rpx;
	background-clip: content-box;
}

.item-content {
	flex: 1;
	display: flex;
	flex-direction: column;
	gap: 12rpx;
	min-width: 0;
}

.item-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.username {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
}

.time {
	font-size: 24rpx;
	color: #B76E79; /* 玫瑰金时间 */
	opacity: 0.8;
}

.action-text {
	font-size: 28rpx;
	color: #8E5C5C; /* 深色文字 */
	font-weight: 500;
}

.note-info {
	display: flex;
	flex-direction: row;
	align-items: center;
	gap: 16rpx;
	margin-top: 10rpx;
	padding: 16rpx;
	background: #FCF6F7; /* 极淡玫瑰底色 */
	border-radius: 16rpx;
	border: 1rpx solid rgba(224, 191, 184, 0.3);
}

.note-image {
	width: 100rpx;
	height: 100rpx;
	border-radius: 12rpx;
	flex-shrink: 0;
	object-fit: cover;
}

.note-title {
	flex: 1;
	font-size: 26rpx;
	color: #555;
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
	color: #B76E79;
	font-size: 28rpx;
}

.loading {
	text-align: center;
	padding: 50rpx 0;
	color: #B76E79;
	font-size: 28rpx;
}

.clay-shadow {
	/* 已在主类中重新定义更为精致的阴影，此处保留类名兼容性但置空或重写 */
	box-shadow: 0 8rpx 24rpx -6rpx rgba(183, 110, 121, 0.15); 
}
</style>
