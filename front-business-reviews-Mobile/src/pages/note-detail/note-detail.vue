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
			<!-- 多图展示 -->
			<swiper v-if="noteData.images && noteData.images.length > 1" class="image-swiper" indicator-dots>
				<swiper-item v-for="(img, index) in noteData.images" :key="index">
					<image :src="img" class="note-image" mode="aspectFill"></image>
				</swiper-item>
			</swiper>
			<!-- 单图展示 -->
			<image v-else :src="noteData.image" class="note-image" mode="aspectFill"></image>
			
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
			<view class="author-info" @click="goToUserProfile">
				<image :src="noteData.authorAvatar" class="author-avatar"></image>
				<view class="author-details">
					<text class="author-name">{{ noteData.author }}</text>
					<text class="publish-time">{{ noteData.publishTime }}</text>
				</view>
				<view v-if="!noteData.isAuthor" class="follow-btn clay-icon" :class="{ following: isFollowing }" @click.stop="followAuthor">
					<text>{{ isFollowing ? '✓' : '➕' }}</text>
				</view>
			</view>

			<!-- 笔记内容 -->
			<view class="note-content">
				<text class="note-title">{{ noteData.title }}</text>
				<text class="note-text">{{ noteData.content }}</text>
			</view>

			<!-- 话题标签 -->
			<view class="topics" v-if="noteData.topics && noteData.topics.length > 0">
				<view class="topic-tag" v-for="(topic, index) in noteData.topics" :key="index" @click="goToTopic(topic)">
					<text class="topic-text">#{{ topic.name }}</text>
				</view>
			</view>

			<!-- 关联商户 -->
			<view class="shop-card" v-if="noteData.shopId && noteData.shopName" @click="goToShop">
				<view class="shop-icon">🏪</view>
				<view class="shop-info">
					<text class="shop-name">{{ noteData.shopName }}</text>
					<text class="shop-hint">点击查看商户详情</text>
				</view>
				<text class="shop-arrow">›</text>
			</view>

			<!-- 位置信息 -->
			<view class="location-card" v-if="noteData.location" @click="goToMap">
				<view class="location-icon">📍</view>
				<text class="location-name">{{ noteData.location }}</text>
				<text class="location-arrow">›</text>
			</view>

			<!-- 标签（保留原有标签显示） -->
			<view class="tags" v-if="noteData.tags && noteData.tags.length > 0">
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
import { getNoteDetail, likeNote, unlikeNote, bookmarkNote, unbookmarkNote } from '../../api/note'
import { followUser, unfollowUser } from '../../api/user'
import { getNoteComments, postComment as postCommentAPI, likeComment as likeCommentAPI, unlikeComment as unlikeCommentAPI } from '../../api/comment'

const noteId = ref('')

// 笔记数据（从后端获取）
const noteData = ref({
	image: '',
	title: '',
	content: '',
	author: '',
	authorAvatar: '',
	authorId: null,
	publishTime: '',
	tags: [],
	comments: 0,
	views: 0
})

const isLiked = ref(false)
const isBookmarked = ref(false)
const isFollowing = ref(false)
const likeCount = ref(0)
const commentText = ref('')
const loading = ref(false)

// 评论列表（从后端获取）
const comments = ref([])

onLoad(async (options) => {
	console.log('Note detail loaded, id:', options.id)
	noteId.value = options.id
	if (options.id) {
		await Promise.all([
			fetchNoteDetail(options.id),
			fetchComments(options.id)
		])
	}
})

const fetchNoteDetail = async (id) => {
	if (loading.value) return
	
	loading.value = true
	uni.showLoading({ title: '加载中...' })
	
	try {
		const result = await getNoteDetail(id)
		console.log('笔记详情:', result)
		
		if (result) {
			// 转换数据格式
			noteData.value = {
				image: result.images && result.images.length > 0 ? result.images[0] : result.image,
				images: result.images || [],
				title: result.title || '',
				content: result.content || '',
				author: result.author || '匿名用户',
				authorAvatar: result.authorAvatar || 'https://via.placeholder.com/100',
				authorId: result.authorId,
				isAuthor: result.isAuthor || false,
				publishTime: result.publishTime || '',
				tags: result.tags || [],
				topics: result.topics || [], // 话题列表
				comments: result.commentCount || 0,
				views: result.viewCount || 0,
				shopId: result.shopId,
				shopName: result.shopName,
				location: result.location,
				latitude: result.latitude,
				longitude: result.longitude
			}
			
			// 设置点赞、收藏和关注状态
			// 后端返回的字段名为 isLiked, isBookmarked, isFollowing
			const toBool = (val) => val === true || val === 'true' || val === 1 || val === '1'
			
			isLiked.value = toBool(result.isLiked)
			isBookmarked.value = toBool(result.isBookmarked)
			isFollowing.value = toBool(result.isFollowing)
			likeCount.value = result.likeCount || 0
			
			console.log('===== 笔记状态加载完成 =====')
			console.log('笔记ID:', noteId.value)
			console.log('点赞状态:', isLiked.value, '(原始值:', result.isLiked, ')')
			console.log('收藏状态:', isBookmarked.value, '(原始值:', result.isBookmarked, ')')
			console.log('关注状态:', isFollowing.value, '(原始值:', result.isFollowing, ')')
			console.log('是否作者:', noteData.value.isAuthor, '(原始值:', result.isAuthor, ')')
			console.log('点赞数:', likeCount.value)
		}
	} catch (e) {
		console.error('获取笔记详情失败:', e)
		uni.showToast({
			title: '加载失败，请重试',
			icon: 'none'
		})
	} finally {
		uni.hideLoading()
		loading.value = false
	}
}

const goBack = () => {
	uni.navigateBack()
}

const shareNote = () => {
	uni.showToast({ title: '分享功能', icon: 'none' })
}

const toggleLike = async () => {
	try {
		if (isLiked.value) {
			// 取消点赞
			await unlikeNote(noteId.value)
			isLiked.value = false
			likeCount.value = Math.max(0, likeCount.value - 1)
			uni.showToast({
				title: '已取消点赞',
				icon: 'success'
			})
		} else {
			// 点赞
			const result = await likeNote(noteId.value)
			console.log('点赞操作结果:', result)
			isLiked.value = true
			likeCount.value = result.likeCount || result.likes || (likeCount.value + 1)
			uni.showToast({
				title: '点赞成功',
				icon: 'success'
			})
		}
	} catch (e) {
		console.error('点赞操作失败:', e)
		uni.showToast({
			title: e.message || '操作失败',
			icon: 'none'
		})
	}
}

const toggleBookmark = async () => {
	try {
		if (isBookmarked.value) {
			// 取消收藏
			await unbookmarkNote(noteId.value)
			isBookmarked.value = false
			uni.showToast({
				title: '已取消收藏',
				icon: 'success'
			})
		} else {
			// 收藏
			await bookmarkNote(noteId.value)
			isBookmarked.value = true
			uni.showToast({
				title: '收藏成功',
				icon: 'success'
			})
		}
	} catch (e) {
		console.error('收藏操作失败:', e)
		uni.showToast({
			title: e.message || '操作失败',
			icon: 'none'
		})
	}
}

const followAuthor = async () => {
	if (!noteData.value.authorId) {
		uni.showToast({ title: '无法获取作者信息', icon: 'none' })
		return
	}
	
	try {
		if (isFollowing.value) {
			// 取消关注
			await unfollowUser(noteData.value.authorId.toString())
			isFollowing.value = false
			uni.showToast({ title: '已取消关注', icon: 'success' })
		} else {
			// 关注
			await followUser(noteData.value.authorId.toString())
			isFollowing.value = true
			uni.showToast({ title: '关注成功', icon: 'success' })
		}
	} catch (e) {
		console.error('关注操作失败:', e)
		uni.showToast({ 
			title: e.message || '操作失败', 
			icon: 'none' 
		})
	}
}

const postComment = async () => {
	if (!commentText.value) {
		uni.showToast({ title: '请输入评论内容', icon: 'none' })
		return
	}
	
	try {
		await postCommentAPI(noteId.value, {
			content: commentText.value
		})
		
		uni.showToast({ title: '评论成功', icon: 'success' })
		commentText.value = ''
		
		// 重新加载评论列表
		await fetchComments(noteId.value)
	} catch (e) {
		console.error('评论失败:', e)
		uni.showToast({ 
			title: e.message || '评论失败', 
			icon: 'none' 
		})
	}
}

const fetchComments = async (id) => {
	try {
		const result = await getNoteComments(id, 1, 50)
		console.log('评论数据:', result)
		
		if (result && result.list) {
			comments.value = result.list.map(item => ({
				id: item.id,
				author: item.username || '匿名用户',
				avatar: item.avatar || 'https://via.placeholder.com/100',
				time: item.createdAt || '',
				content: item.content || '',
				likes: item.likeCount || 0,
				liked: item.isLiked || false
			}))
		}
	} catch (e) {
		console.error('获取评论失败:', e)
		comments.value = []
	}
}

const likeComment = async (index) => {
	const comment = comments.value[index]
	if (!comment || !comment.id) return
	
	try {
		if (comment.liked) {
			// 取消点赞
			await unlikeCommentAPI(comment.id)
			comment.liked = false
			comment.likes = Math.max(0, comment.likes - 1)
		} else {
			// 点赞
			await likeCommentAPI(comment.id)
			comment.liked = true
			comment.likes += 1
		}
	} catch (e) {
		console.error('评论点赞失败:', e)
		uni.showToast({ 
			title: e.message || '操作失败', 
			icon: 'none' 
		})
	}
}

// 跳转到用户主页
const goToUserProfile = () => {
	if (noteData.value.authorId) {
		uni.navigateTo({
			url: `/pages/user-profile/user-profile?userId=${noteData.value.authorId}`
		})
	} else {
		uni.showToast({
			title: '无法获取用户信息',
			icon: 'none'
		})
	}
}

// 跳转到商户详情
const goToShop = () => {
	if (noteData.value.shopId) {
		uni.navigateTo({
			url: `/pages/shop-detail/shop-detail?id=${noteData.value.shopId}`
		})
	}
}

// 跳转到话题页面（预留功能）
const goToTopic = (topic) => {
	uni.showToast({
		title: `话题: #${topic.name}`,
		icon: 'none'
	})
	// 预留：跳转到话题详情页
	// uni.navigateTo({
	// 	url: `/pages/topic-detail/topic-detail?id=${topic.id}`
	// })
}

// 跳转到地图页面
const goToMap = () => {
	if (noteData.value.latitude && noteData.value.longitude) {
		uni.navigateTo({
			url: `/pages/map/map?latitude=${noteData.value.latitude}&longitude=${noteData.value.longitude}&name=${encodeURIComponent(noteData.value.location || '')}`
		})
	} else {
		uni.showToast({
			title: noteData.value.location || '位置信息',
			icon: 'none'
		})
	}
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

.image-swiper {
	width: 100%;
	height: 100%;
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
	display: flex;
	align-items: center;
	justify-content: center;
	transition: all 0.3s ease;
}

.action-btn.liked {
	background: #EF476F !important;
	border-color: #EF476F !important;
	color: white !important;
}

.action-btn.bookmarked {
	background: #FF9E64 !important;
	border-color: #FF9E64 !important;
	color: white !important;
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
	background: linear-gradient(135deg, #FF9E64 0%, #FF7A45 100%);
	color: white;
	font-size: 28rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50%;
	transition: all 0.3s ease;
	box-shadow: 0 4rpx 10rpx rgba(255, 158, 100, 0.3);
}

.follow-btn.following {
	background: #E8E8E8 !important;
	color: #666 !important;
	box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.1) !important;
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

// 话题标签样式
.topics {
	display: flex;
	flex-wrap: wrap;
	gap: 15rpx;
	margin-bottom: 20rpx;
}

.topic-tag {
	padding: 10rpx 20rpx;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 30rpx;
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.95);
	}
}

.topic-text {
	font-size: 24rpx;
	color: white;
	font-weight: 500;
}

// 商户卡片样式
.shop-card {
	display: flex;
	align-items: center;
	padding: 25rpx;
	background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
	border-radius: 20rpx;
	margin-bottom: 20rpx;
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.98);
		background: linear-gradient(135deg, #e4e8eb 0%, #d5d9dc 100%);
	}
}

.shop-icon {
	font-size: 40rpx;
	margin-right: 20rpx;
}

.shop-info {
	flex: 1;
	display: flex;
	flex-direction: column;
}

.shop-name {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 5rpx;
}

.shop-hint {
	font-size: 22rpx;
	color: #999;
}

.shop-arrow {
	font-size: 40rpx;
	color: #ccc;
}

// 位置信息样式
.location-card {
	display: flex;
	align-items: center;
	padding: 20rpx 25rpx;
	background: #fff8f0;
	border-radius: 15rpx;
	margin-bottom: 20rpx;
	border: 1rpx solid #ffe4cc;
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.98);
		background: #fff0e0;
	}
}

.location-icon {
	font-size: 32rpx;
	margin-right: 15rpx;
}

.location-name {
	flex: 1;
	font-size: 26rpx;
	color: #666;
}

.location-arrow {
	font-size: 36rpx;
	color: #ccc;
}
</style>
