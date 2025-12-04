<template>
	<view class="container">
		<!-- 顶部导航 -->
		<view class="header">
			<view class="nav-bar">
				<view class="nav-btn clay-icon" @click="goBack">
					<text>←</text>
				</view>
				<text class="nav-title">笔记详情</text>
				<view class="nav-btn clay-icon" @click="shareNote">
					<text>📤</text>
				</view>
			</view>
		</view>

		<!-- 笔记图片 -->
		<view class="image-section">
			<image :src="noteData.image" class="note-image" mode="aspectFill"></image>
			<view class="action-btns">
				<view class="action-btn clay-icon" :class="{ liked: isLiked }" @click="toggleLike">
					<text>❤️</text>
				</view>
				<view class="action-btn clay-icon" :class="{ bookmarked: isBookmarked }" @click="toggleBookmark">
					<text>🔖</text>
				</view>
			</view>
		</view>

		<!-- 内容区 -->
		<view class="content-section">
			<!-- 作者信息 -->
			<view class="author-info">
				<image :src="noteData.authorAvatar" class="author-avatar"></image>
				<view class="author-details">
					<text class="author-name">{{ noteData.author }}</text>
					<text class="publish-time">{{ noteData.publishTime }}</text>
				</view>
				<view class="follow-btn clay-icon bg-primary" @click="followAuthor">
					<text>➕</text>
				</view>
			</view>

			<!-- 笔记内容 -->
			<view class="note-content">
				<text class="note-title">{{ noteData.title }}</text>
				<text class="note-text">{{ noteData.content }}</text>
			</view>

			<!-- 标签 -->
			<view class="tags">
				<view class="tag" v-for="(tag, index) in noteData.tags" :key="index">
					<text class="tag-text">{{ tag }}</text>
				</view>
			</view>

			<!-- 数据统计 -->
			<view class="stats">
				<view class="stat-item">
					<text class="stat-value text-primary">{{ likeCount }}</text>
					<text class="stat-label">点赞</text>
				</view>
				<view class="stat-item">
					<text class="stat-value text-primary">{{ noteData.comments }}</text>
					<text class="stat-label">评论</text>
				</view>
				<view class="stat-item">
					<text class="stat-value text-primary">{{ noteData.views }}</text>
					<text class="stat-label">浏览</text>
				</view>
			</view>

			<!-- 评论区 -->
			<view class="comments-section">
				<text class="section-title">评价 ({{ noteData.comments }})</text>
				
				<!-- 评论输入 -->
				<view class="comment-input clay-border">
					<input 
						type="text" 
						placeholder="分享你的想法..." 
						v-model="commentText"
						class="input"
					/>
					<button class="send-btn bg-primary" @click="postComment">发送</button>
				</view>

				<!-- 评论列表 -->
				<view class="comment-list">
					<view class="comment-item" v-for="(comment, index) in comments" :key="index">
						<image :src="comment.avatar" class="comment-avatar"></image>
						<view class="comment-content">
							<view class="comment-header">
								<text class="comment-author">{{ comment.author }}</text>
								<text class="comment-time">{{ comment.time }}</text>
							</view>
							<text class="comment-text">{{ comment.content }}</text>
							<view class="comment-actions">
								<view class="action" @click="likeComment(index)">
									<text :class="{ 'text-pink': comment.liked }">❤️</text>
									<text>{{ comment.likes }}</text>
								</view>
								<view class="action">
									<text>💬</text>
									<text>回复</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

// 笔记数据（从后端获取）
const noteData = ref({})

const isLiked = ref(false)
const isBookmarked = ref(false)
const likeCount = ref(0)
const commentText = ref('')

// 评论列表（从后端获取）
const comments = ref([])

onLoad((options) => {
	console.log('Note detail loaded, id:', options.id)
	// TODO: 根据noteId从后端API获取笔记详情
	// fetchNoteDetail(options.id)
	// TODO: 获取评论列表
	// fetchComments(options.id)
})

const goBack = () => {
	uni.navigateBack()
}

const shareNote = () => {
	uni.showToast({ title: '分享功能', icon: 'none' })
}

const toggleLike = () => {
	isLiked.value = !isLiked.value
	likeCount.value += isLiked.value ? 1 : -1
}

const toggleBookmark = () => {
	isBookmarked.value = !isBookmarked.value
}

const followAuthor = () => {
	uni.showToast({ title: '已关注', icon: 'success' })
}

const postComment = () => {
	if (!commentText.value) {
		uni.showToast({ title: '请输入评论内容', icon: 'none' })
		return
	}
	uni.showToast({ title: '评论成功', icon: 'success' })
	commentText.value = ''
}

const likeComment = (index) => {
	comments.value[index].liked = !comments.value[index].liked
	comments.value[index].likes += comments.value[index].liked ? 1 : -1
}
</script>

<style lang="scss" scoped>
.container {
	background: #F7F9FC;
	min-height: 100vh;
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

.image-section {
	position: relative;
	width: 100%;
	height: 750rpx;
}

.note-image {
	width: 100%;
	height: 100%;
}

.action-btns {
	position: absolute;
	bottom: 30rpx;
	right: 30rpx;
	display: flex;
	gap: 20rpx;
}

.action-btn {
	width: 80rpx;
	height: 80rpx;
	background: white;
	border: 2rpx solid #000;
	font-size: 36rpx;
}

.action-btn.liked {
	background: #EF476F;
}

.action-btn.bookmarked {
	color: #FF9E64;
}

.content-section {
	background: white;
	padding: 30rpx;
}

.author-info {
	display: flex;
	align-items: center;
	margin-bottom: 30rpx;
}

.author-avatar {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	border: 4rpx solid #FF9E64;
	margin-right: 20rpx;
}

.author-details {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.author-name {
	font-size: 28rpx;
	font-weight: 500;
}

.publish-time {
	font-size: 24rpx;
	color: #999;
}

.follow-btn {
	width: 70rpx;
	height: 70rpx;
	color: white;
	font-size: 28rpx;
}

.note-content {
	margin-bottom: 30rpx;
}

.note-title {
	display: block;
	font-size: 36rpx;
	font-weight: bold;
	margin-bottom: 20rpx;
}

.note-text {
	display: block;
	font-size: 28rpx;
	line-height: 1.6;
	color: #666;
}

.tags {
	display: flex;
	flex-wrap: wrap;
	gap: 15rpx;
	margin-bottom: 30rpx;
}

.tag {
	padding: 10rpx 25rpx;
	background: rgba(255, 158, 100, 0.2);
	border-radius: 30rpx;
}

.tag-text {
	font-size: 24rpx;
}

.stats {
	display: flex;
	justify-content: space-around;
	padding: 30rpx 0;
	border-top: 1rpx solid #f0f0f0;
	border-bottom: 1rpx solid #f0f0f0;
	margin-bottom: 30rpx;
}

.stat-item {
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

.section-title {
	display: block;
	font-size: 32rpx;
	font-weight: bold;
	margin-bottom: 20rpx;
}

.comment-input {
	display: flex;
	align-items: center;
	padding: 20rpx 25rpx;
	margin-bottom: 30rpx;
	background: white;
}

.input {
	flex: 1;
	font-size: 28rpx;
}

.send-btn {
	padding: 10rpx 25rpx;
	color: white;
	border-radius: 30rpx;
	font-size: 24rpx;
	border: none;
}

.comment-list {
	display: flex;
	flex-direction: column;
	gap: 30rpx;
}

.comment-item {
	display: flex;
}

.comment-avatar {
	width: 60rpx;
	height: 60rpx;
	border-radius: 50%;
	margin-right: 20rpx;
}

.comment-content {
	flex: 1;
}

.comment-header {
	display: flex;
	justify-content: space-between;
	margin-bottom: 10rpx;
}

.comment-author {
	font-size: 24rpx;
	font-weight: 500;
}

.comment-time {
	font-size: 22rpx;
	color: #999;
}

.comment-text {
	display: block;
	font-size: 26rpx;
	margin-bottom: 15rpx;
}

.comment-actions {
	display: flex;
	gap: 30rpx;
}

.action {
	display: flex;
	align-items: center;
	gap: 5rpx;
	font-size: 24rpx;
	color: #999;
}
</style>
