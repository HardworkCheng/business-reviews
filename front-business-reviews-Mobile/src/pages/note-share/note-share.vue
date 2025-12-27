<template>
	<view class="share-container">
		<!-- 笔记背景图 -->
		<view class="note-background" :class="{ 'show': isSheetVisible }" v-if="noteData.coverImage">
			<image 
				:src="noteData.coverImage" 
				class="background-image" 
				mode="aspectFill"
			></image>
			<view class="background-blur"></view>
		</view>

		<!-- 半透明遮罩 -->
		<view 
			class="overlay-mask" 
			:class="{ 'show': isSheetVisible }"
			@click="closeSheet"
		></view>

		<!-- Bottom Sheet 弹窗 -->
		<view 
			class="bottom-sheet" 
			:class="{ 'show': isSheetVisible }"
		>
			<!-- 顶部栏 -->
			<view class="sheet-header">
				<view class="header-left"></view>
				<text class="header-title">分享给</text>
				<view class="header-right" @click="closeSheet">
					<text class="close-icon">✕</text>
				</view>
			</view>

			<!-- 笔记预览卡片 -->
			<view class="note-preview">
				<image 
					v-if="noteData.coverImage"
					:src="noteData.coverImage" 
					class="note-cover" 
					mode="aspectFill"
				></image>
				<view class="note-info">
					<text class="note-title">{{ noteData.title || '无标题' }}</text>
					<text class="note-content">{{ noteData.content || '暂无内容' }}</text>
				</view>
			</view>

			<!-- 搜索框 -->
			<view class="search-bar">
				<view class="search-input-wrapper">
					<text class="search-icon">🔍</text>
					<input 
						class="search-input" 
						v-model="searchKeyword"
						placeholder="搜索用户..."
						@input="handleSearch"
					/>
				</view>
			</view>

			<!-- 横向用户列表 -->
			<scroll-view 
				class="user-scroll" 
				scroll-x 
				:show-scrollbar="false"
				:enable-flex="true"
				:enhanced="true"
				:bounces="false"
			>
				<view class="user-scroll-content">
					<!-- 用户头像卡片 -->
					<view 
						class="user-avatar-card" 
						v-for="user in filteredUserList" 
						:key="user.userId"
						@click="toggleSelection(user.userId)"
					>
						<view class="avatar-wrapper">
							<image 
								v-if="user.avatar"
								:src="user.avatar" 
								class="user-avatar" 
								mode="aspectFill"
							></image>
							<view v-else class="user-avatar default-avatar">
								<text class="avatar-text">{{ user.username ? user.username.charAt(0) : '?' }}</text>
							</view>
							<!-- 选中对勾 -->
							<view class="selection-check" v-if="isSelected(user.userId)">
								<text class="check-icon">✓</text>
							</view>
							<!-- 在线状态点 -->
							<view class="online-dot" v-if="user.isOnline"></view>
						</view>
						<text class="user-name">{{ user.username }}</text>
					</view>

					<!-- 空状态 -->
					<view v-if="filteredUserList.length === 0" class="empty-state">
						<text class="empty-icon">👥</text>
						<text class="empty-text">{{ searchKeyword ? '未找到匹配用户' : '暂无可分享的用户' }}</text>
					</view>
				</view>
			</scroll-view>

			<!-- 底部操作栏 -->
			<view class="footer-action">
				<view class="selected-info">
					<text class="selected-count">已选择 {{ selectedUserIds.length }} 人</text>
				</view>
				<view 
					class="share-button" 
					:class="{ 'disabled': !hasSelection || isSharing }"
					@click="handleShare"
				>
					<text class="share-text">{{ isSharing ? '分享中...' : '分享' }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getNoteDetail } from '../../api/note'
import { getFollowingList, getFollowerList } from '../../api/user'
import { shareNoteToUsers } from '../../api/message'

const noteId = ref('')
const searchKeyword = ref('')
const selectedUserIds = ref([])
const isSharing = ref(false)
const isSheetVisible = ref(false)

// 笔记数据
const noteData = ref({
	coverImage: '',
	title: '',
	content: ''
})

// 原始用户列表
const allFollowingList = ref([])
const allFollowersList = ref([])

// 合并去重后的用户列表
const shareUserList = ref([])

// 过滤后的用户列表
const filteredUserList = computed(() => {
	if (!searchKeyword.value.trim()) {
		return shareUserList.value
	}
	
	const keyword = searchKeyword.value.trim().toLowerCase()
	return shareUserList.value.filter(user => 
		user.username.toLowerCase().includes(keyword) ||
		(user.bio && user.bio.toLowerCase().includes(keyword))
	)
})

// 是否有选中用户
const hasSelection = computed(() => {
	return selectedUserIds.value.length > 0
})

onLoad(async (options) => {
	console.log('Note share loaded, noteId:', options.noteId)
	noteId.value = options.noteId
	
	if (options.noteId) {
		await Promise.all([
			fetchNoteData(options.noteId),
			fetchUserLists()
		])
		
		// 延迟显示弹窗，确保动画效果
		setTimeout(() => {
			isSheetVisible.value = true
		}, 100)
	}
})

// 获取笔记数据
const fetchNoteData = async (id) => {
	try {
		const result = await getNoteDetail(id)
		if (result) {
			noteData.value = {
				coverImage: result.images && result.images.length > 0 ? result.images[0] : result.image,
				title: result.title || '无标题',
				content: result.content || ''
			}
		}
	} catch (e) {
		console.error('获取笔记数据失败:', e)
		uni.showToast({
			title: '加载笔记失败',
			icon: 'none'
		})
	}
}

// 获取并合并用户列表
const fetchUserLists = async () => {
	try {
		const [followingResult, followersResult] = await Promise.all([
			getFollowingList(1, 100),
			getFollowerList(1, 100)
		])
		
		allFollowingList.value = followingResult?.list || []
		allFollowersList.value = followersResult?.list || []
		
		// 合并并去重
		shareUserList.value = mergeAndDeduplicateUsers(
			allFollowingList.value, 
			allFollowersList.value
		)
	} catch (e) {
		console.error('获取用户列表失败:', e)
		uni.showToast({
			title: '加载用户列表失败',
			icon: 'none'
		})
	}
}

// 合并并去重用户列表
const mergeAndDeduplicateUsers = (followingList, fansList) => {
	const userMap = new Map()
	
	// 添加关注列表
	followingList.forEach(user => {
		userMap.set(user.userId, user)
	})
	
	// 添加粉丝列表（如果已存在则跳过）
	fansList.forEach(user => {
		if (!userMap.has(user.userId)) {
			userMap.set(user.userId, user)
		}
	})
	
	return Array.from(userMap.values())
}

// 搜索用户（防抖处理）
let searchTimer = null
const handleSearch = () => {
	if (searchTimer) {
		clearTimeout(searchTimer)
	}
	searchTimer = setTimeout(() => {
		// filteredUserList 会自动更新
	}, 300)
}

// 判断用户是否被选中
const isSelected = (userId) => {
	return selectedUserIds.value.includes(userId)
}

// 切换用户选择
const toggleSelection = (userId) => {
	const index = selectedUserIds.value.indexOf(userId)
	if (index > -1) {
		selectedUserIds.value.splice(index, 1)
	} else {
		selectedUserIds.value.push(userId)
	}
}

// 分享笔记
const handleShare = async () => {
	if (!hasSelection.value || isSharing.value) {
		return
	}
	
	isSharing.value = true
	uni.showLoading({ title: '分享中...', mask: true })
	
	try {
		// 确保userIds是数字数组
		const userIds = selectedUserIds.value.map(id => {
			return typeof id === 'string' ? parseInt(id) : id
		})
		
		console.log('分享笔记:', noteId.value, '给用户:', userIds)
		await shareNoteToUsers(noteId.value, userIds)
		
		uni.hideLoading()
		uni.showToast({
			title: '分享成功',
			icon: 'success',
			duration: 1500
		})
		
		// 分享成功后只关闭弹窗，不返回上一页
		setTimeout(() => {
			closeSheet()
		}, 1500)
	} catch (e) {
		uni.hideLoading()
		console.error('分享失败:', e)
		uni.showToast({
			title: e.message || '分享失败，请重试',
			icon: 'none',
			duration: 2000
		})
	} finally {
		isSharing.value = false
	}
}

// 关闭弹窗
const closeSheet = () => {
	isSheetVisible.value = false
	setTimeout(() => {
		uni.navigateBack()
	}, 300)
}

// 图片加载失败处理（已移除，不再需要）
</script>

<style lang="scss" scoped>
.share-container {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 9999;
}

/* 笔记背景图 */
.note-background {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	opacity: 0;
	transition: opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	
	&.show {
		opacity: 1;
	}
}

.background-image {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	width: 100%;
	height: 100%;
}

.background-blur {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	/* 移除模糊效果，只保留轻微的半透明遮罩 */
	background: rgba(0, 0, 0, 0.1);
}

/* 半透明遮罩 */
.overlay-mask {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0);
	transition: background 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	pointer-events: none;
	z-index: 1;
	
	&.show {
		background: rgba(0, 0, 0, 0);
		pointer-events: auto;
	}
}

/* Bottom Sheet 弹窗 */
.bottom-sheet {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	max-height: 80vh;
	min-height: 400rpx;
	background: white;
	border-radius: 40rpx 40rpx 0 0;
	display: flex;
	flex-direction: column;
	transform: translateY(100%);
	transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.1);
	z-index: 2;
	
	&.show {
		transform: translateY(0);
	}
}

/* 顶部栏 */
.sheet-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 30rpx 30rpx 20rpx 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}

.header-left,
.header-right {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.header-title {
	font-size: 32rpx;
	font-weight: 600;
	color: #333;
}

.close-icon {
	font-size: 36rpx;
	color: #999;
	transition: opacity 0.2s;
	
	&:active {
		opacity: 0.6;
	}
}

/* 笔记预览卡片 */
.note-preview {
	display: flex;
	padding: 20rpx 30rpx;
	background: #f8f9fa;
	margin: 20rpx 30rpx;
	border-radius: 16rpx;
}

.note-cover {
	width: 100rpx;
	height: 100rpx;
	border-radius: 12rpx;
	flex-shrink: 0;
	background: #e0e0e0;
}

.note-info {
	flex: 1;
	margin-left: 20rpx;
	display: flex;
	flex-direction: column;
	justify-content: center;
}

.note-title {
	font-size: 28rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 8rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.note-content {
	font-size: 24rpx;
	color: #999;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
}

/* 搜索框 */
.search-bar {
	padding: 0 30rpx 20rpx 30rpx;
}

.search-input-wrapper {
	background: #f8f9fa;
	border-radius: 40rpx;
	padding: 16rpx 24rpx;
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.search-icon {
	font-size: 28rpx;
	color: #999;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: #333;
	background: transparent;
}

/* 横向滚动用户列表 */
.user-scroll {
	flex: 1;
	overflow: hidden;
	padding: 20rpx 0;
}

.user-scroll-content {
	display: flex;
	padding: 0 30rpx;
	gap: 24rpx;
	min-height: 200rpx;
}

/* 用户头像卡片 */
.user-avatar-card {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 12rpx;
	flex-shrink: 0;
}

.avatar-wrapper {
	position: relative;
	width: 100rpx;
	height: 100rpx;
}

.user-avatar {
	width: 100%;
	height: 100%;
	border-radius: 50%;
	background: #e0e0e0;
	border: 3rpx solid transparent;
	transition: all 0.3s ease;
}

/* 默认头像 */
.default-avatar {
	display: flex;
	align-items: center;
	justify-content: center;
	background: linear-gradient(135deg, #ffaf40, #ff9f43);
}

.avatar-text {
	font-size: 40rpx;
	color: white;
	font-weight: 600;
}

/* 选中对勾 */
.selection-check {
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	border-radius: 50%;
	background: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	border: 3rpx solid #ff9f43;
	animation: checkIn 0.3s ease;
}

@keyframes checkIn {
	0% {
		opacity: 0;
		transform: scale(0.8);
	}
	100% {
		opacity: 1;
		transform: scale(1);
	}
}

.check-icon {
	color: white;
	font-size: 48rpx;
	font-weight: bold;
}

/* 在线状态点 */
.online-dot {
	position: absolute;
	right: 2rpx;
	bottom: 2rpx;
	width: 20rpx;
	height: 20rpx;
	background: #52c41a;
	border: 3rpx solid white;
	border-radius: 50%;
}

.user-name {
	font-size: 24rpx;
	color: #333;
	max-width: 100rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	text-align: center;
}

/* 空状态 */
.empty-state {
	flex: 1;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 60rpx 40rpx;
}

.empty-icon {
	font-size: 80rpx;
	margin-bottom: 20rpx;
	opacity: 0.5;
}

.empty-text {
	font-size: 28rpx;
	color: #999;
}

/* 底部操作栏 */
.footer-action {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	border-top: 1rpx solid #f0f0f0;
	background: white;
}

.selected-info {
	flex: 1;
}

.selected-count {
	font-size: 28rpx;
	color: #666;
}

.share-button {
	padding: 16rpx 60rpx;
	background: linear-gradient(135deg, #ffaf40, #ff9f43);
	border-radius: 40rpx;
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.96);
	}
	
	&.disabled {
		background: #e0e0e0;
		opacity: 0.6;
		
		.share-text {
			color: #999;
		}
	}
}

.share-text {
	font-size: 30rpx;
	color: white;
	font-weight: 600;
}
</style>
