<template>
	<view class="container">
		<!-- 顶部搜索栏 -->
		<view class="header">
			<view class="location-search">
				<view class="location">
					<text class="location-icon">📍</text>
					<text class="location-text">杭州</text>
				</view>
				<view class="search-box clay-border" @click="goToSearch">
					<text class="search-icon">🔍</text>
					<text class="search-placeholder">搜索商户名或地点</text>
				</view>
				<view class="user-icon clay-icon bg-primary" @click="goToProfile">
					<text class="icon-text">👤</text>
				</view>
			</view>
		</view>

		<!-- 分类图标区 -->
		<view class="category-section">
			<view class="category-grid">
				<view 
					class="category-item" 
					v-for="(item, index) in categories" 
					:key="index"
					@click="goToCategory(item.name)"
				>
					<view class="category-icon clay-icon" :style="{ backgroundColor: item.color }">
						<text class="icon-emoji">{{ item.icon }}</text>
					</view>
					<text class="category-name">{{ item.name }}</text>
				</view>
			</view>
		</view>

		<!-- 推荐笔记 -->
		<view class="notes-section">
			<view class="notes-grid">
				<view 
					class="note-card clay-shadow" 
					v-for="(note, index) in noteList" 
					:key="index"
					@click="goToNoteDetail(note.id)"
				>
					<view class="note-image-wrapper">
						<image :src="note.image" class="note-image" mode="aspectFill"></image>
						<view v-if="note.tag" class="note-tag" :class="note.tagClass">
							<text class="tag-text">{{ note.tag }}</text>
						</view>
					</view>
					<view class="note-info">
						<text class="note-title line-clamp-2">{{ note.title }}</text>
						<view class="note-meta">
							<text class="author">@{{ note.author }}</text>
							<view class="like-info">
								<text class="like-icon">❤️</text>
								<text class="like-count">{{ note.likes }}</text>
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
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getRecommendedNotes } from '../../api/note'

// 分类数据
const categories = ref([
	{ name: '美食', icon: '🍜', color: '#FFD166' },
	{ name: 'KTV', icon: '🎤', color: '#EF476F' },
	{ name: '丽人·美发', icon: '💇', color: '#FF9E64' },
	{ name: '美睫·美甲', icon: '💅', color: '#06D6A0' },
	{ name: '按摩·足疗', icon: '💆', color: '#FFD166' },
	{ name: '美容SPA', icon: '🛁', color: '#EF476F' },
	{ name: '亲子游乐', icon: '👶', color: '#06D6A0' },
	{ name: '酒吧', icon: '🍷', color: '#FF9E64' }
])

// 笔记列表（从后端获取）
const noteList = ref([])
const loading = ref(false)

onLoad(() => {
	console.log('Index page loaded')
	// 清空旧数据
	noteList.value = []
	fetchNoteList()
})

onShow(() => {
	console.log('Index page show')
	// 清空旧数据，重新获取
	noteList.value = []
	fetchNoteList()
})

// 获取推荐笔记列表
const fetchNoteList = async () => {
	if (loading.value) return
	
	loading.value = true
	try {
		const result = await getRecommendedNotes(1, 20)
		console.log('获取笔记列表:', result)
		
		if (result && result.list) {
			// 转换数据格式
			noteList.value = result.list.map(note => ({
				id: note.id,
				title: note.title,
				image: note.image || '',
				author: note.author || '匿名用户',
				likes: note.likes || 0,
				tag: note.tag || null,
				tagClass: note.tagClass || ''
			}))
			console.log('笔记列表已更新:', noteList.value.length, noteList.value)
		}
	} catch (e) {
		console.error('获取笔记列表失败:', e)
		uni.showToast({
			title: '加载失败，请重试',
			icon: 'none'
		})
	} finally {
		loading.value = false
	}
}

// 跳转搜索
const goToSearch = () => {
	uni.navigateTo({
		url: '/pages/search/search'
	})
}

// 跳转个人主页
const goToProfile = () => {
	uni.switchTab({
		url: '/pages/profile/profile'
	})
}

// 分类筛选
const goToCategory = (category) => {
	uni.navigateTo({
		url: `/pages/search/search?category=${category}`
	})
}

// 笔记详情
const goToNoteDetail = (id) => {
	uni.navigateTo({
		url: `/pages/note-detail/note-detail?id=${id}`
	})
}
</script>

<style lang="scss" scoped>
.container {
	background: #F7F9FC;
	min-height: 100vh;
	padding-bottom: 20rpx;
}

.header {
	position: sticky;
	top: 0;
	z-index: 100;
	background: white;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.location-search {
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	gap: 20rpx;
}

.location {
	display: flex;
	align-items: center;
}

.location-icon {
	font-size: 32rpx;
	color: #FF9E64;
	margin-right: 8rpx;
}

.location-text {
	font-size: 28rpx;
	color: #333;
}

.search-box {
	flex: 1;
	display: flex;
	align-items: center;
	padding: 15rpx 30rpx;
	background: white;
}

.search-icon {
	font-size: 32rpx;
	margin-right: 15rpx;
}

.search-placeholder {
	font-size: 28rpx;
	color: #999;
}

.user-icon {
	width: 60rpx;
	height: 60rpx;
}

.icon-text {
	font-size: 32rpx;
	color: white;
}

.category-section {
	background: white;
	padding: 30rpx;
	margin-bottom: 20rpx;
}

.category-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 30rpx;
}

.category-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.category-icon {
	width: 100rpx;
	height: 100rpx;
	margin-bottom: 10rpx;
}

.icon-emoji {
	font-size: 48rpx;
}

.category-name {
	font-size: 24rpx;
	text-align: center;
	color: #333;
}

.notes-section {
	padding: 0 30rpx;
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
	transition: all 0.3s;
}

.note-image-wrapper {
	position: relative;
	width: 100%;
	height: 350rpx;
}

.note-image {
	width: 100%;
	height: 100%;
}

.note-tag {
	position: absolute;
	top: 20rpx;
	right: 20rpx;
	padding: 8rpx 20rpx;
	border-radius: 30rpx;
}

.tag-hot {
	background: #EF476F;
}

.tag-discount {
	background: #06D6A0;
}

.tag-new {
	background: #FFD166;
}

.tag-text {
	font-size: 22rpx;
	color: white;
}

.note-info {
	padding: 20rpx;
}

.note-title {
	font-size: 28rpx;
	font-weight: 500;
	margin-bottom: 15rpx;
	line-height: 1.4;
}

.note-meta {
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.author {
	font-size: 24rpx;
	color: #999;
}

.like-info {
	display: flex;
	align-items: center;
}

.like-icon {
	font-size: 24rpx;
	margin-right: 5rpx;
}

.like-count {
	font-size: 24rpx;
	color: #EF476F;
}
</style>
