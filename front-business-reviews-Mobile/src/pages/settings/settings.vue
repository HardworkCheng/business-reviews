<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="navbar">
			<view class="navbar-content">
				<view class="nav-left" @click="goBack">
					<image src="/static/icons/back.png" class="back-icon" mode="aspectFit"></image>
				</view>
				<view class="nav-title">设置</view>
				<view class="nav-right"></view>
			</view>
		</view>
		
		<!-- 头像展示区 -->
		<view class="avatar-section">
			<view class="avatar-container" @click="editAvatar">
				<image class="avatar-large" :src="avatarUrl" mode="aspectFill"></image>
				<view class="avatar-edit-badge">
					<image src="/static/icons/camera.png" class="camera-icon" mode="aspectFit"></image>
				</view>
			</view>
			<text class="username-display">{{ userInfo.username || '未设置' }}</text>
			<text class="edit-tip">点击头像可修改</text>
		</view>

		<!-- 个人信息设置 -->
		<view class="section">
			<view class="section-header">
				<text class="section-title">个人信息</text>
			</view>
			<view class="settings-group">
				<view class="setting-item" @click="editUsername">
					<text class="item-label">用户名</text>
					<view class="item-right">
						<text class="item-value">{{ userInfo.username || '未设置' }}</text>
						<image src="/static/icons/arrow-right.png" class="arrow-icon" mode="aspectFit"></image>
					</view>
				</view>
				<view class="setting-item" @click="editGender">
					<text class="item-label">性别</text>
					<view class="item-right">
						<text class="item-value">{{ getGenderText(userInfo.gender) }}</text>
						<image src="/static/icons/arrow-right.png" class="arrow-icon" mode="aspectFit"></image>
					</view>
				</view>
				<view class="setting-item">
					<text class="item-label">生日</text>
					<picker 
						mode="date" 
						:value="userInfo.birthday || '1990-01-01'" 
						:start="'1900-01-01'" 
						:end="getCurrentDate()"
						@change="onBirthdayChange"
					>
						<view class="item-right">
							<text class="item-value">{{ userInfo.birthday || '未设置' }}</text>
							<image src="/static/icons/arrow-right.png" class="arrow-icon" mode="aspectFit"></image>
						</view>
					</picker>
				</view>
				<view class="setting-item" @click="editBio">
					<text class="item-label">个人简介</text>
					<view class="item-right">
						<text class="item-value">{{ userInfo.bio || '未设置' }}</text>
						<image src="/static/icons/arrow-right.png" class="arrow-icon" mode="aspectFit"></image>
					</view>
				</view>
			</view>
		</view>

		<!-- 账号与安全 -->
		<view class="section">
			<view class="section-header">
				<text class="section-title">账号与安全</text>
			</view>
			<view class="settings-group">
				<view class="setting-item" @click="editPhone">
					<text class="item-label">手机号</text>
					<view class="item-right">
						<text class="item-value">{{ formatPhone(userInfo.phone) }}</text>
						<image src="/static/icons/arrow-right.png" class="arrow-icon" mode="aspectFit"></image>
					</view>
				</view>
				<view class="setting-item" @click="editPassword">
					<text class="item-label">修改密码</text>
					<view class="item-right">
						<text class="item-value">••••••</text>
						<image src="/static/icons/arrow-right.png" class="arrow-icon" mode="aspectFit"></image>
					</view>
				</view>
			</view>
		</view>

		<!-- 按钮区 -->
		<view class="footer-actions">
			<view class="btn-save" @click="handleSave" :class="{ disabled: !hasChanges }">
				<text class="btn-text">完成编辑</text>
			</view>
			<view class="btn-logout" @click="handleLogout">
				<text class="logout-text">切换账号 / 退出登录</text>
			</view>
		</view>
	</view>
</template>


<script setup>
import { ref, nextTick, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { logout } from '../../api/auth'
import { getUserInfo, getUserPhone, updateUserInfo } from '../../api/user'
import { getImageUrl } from '../../utils/placeholder'


const userInfo = ref({})
const originalUserInfo = ref({})
const hasChanges = ref(false)
const isLoading = ref(false)

// 计算属性 - 确保头像响应式更新
const avatarUrl = computed(() => {
	return getAvatarUrl(userInfo.value.avatar)
})

onLoad(async () => {
	await fetchUserInfo()
})

const fetchUserInfo = async () => {
	if (isLoading.value) {
		console.log('正在加载中,跳过重复请求')
		return
	}
	
	isLoading.value = true
	try {
		const info = await getUserInfo()
		console.log('=== 获取到的用户信息 ===', JSON.stringify(info, null, 2))
		
		if (info) {
			userInfo.value = info
			originalUserInfo.value = JSON.parse(JSON.stringify(info))
			uni.setStorageSync('userInfo', info)
			console.log('用户信息已更新')
		} else {
			console.warn('未获取到用户信息')
			userInfo.value = {}
			originalUserInfo.value = {}
		}
	} catch (e) {
		console.error('获取用户信息失败:', e)
		
		if (e.code === 401 || e.code === 40401) {
			console.warn('认证失败,清除缓存并跳转登录')
			uni.clearStorageSync()
			uni.reLaunch({ url: '/pages/login/login' })
		} else {
			uni.showToast({ 
				title: '获取用户信息失败', 
				icon: 'none' 
			})
		}
	} finally {
		isLoading.value = false
	}
}

const goBack = () => {
	try {
		const pages = getCurrentPages()
		if (pages.length > 1) {
			uni.navigateBack({
				delta: 1,
				fail: (err) => {
					console.error('返回失败:', err)
					uni.switchTab({
						url: '/pages/profile/profile'
					})
				}
			})
		} else {
			uni.switchTab({
				url: '/pages/profile/profile'
			})
		}
	} catch (e) {
		console.error('goBack error:', e)
		uni.switchTab({
			url: '/pages/profile/profile'
		})
	}
}

// 获取当前日期
const getCurrentDate = () => {
	const date = new Date()
	const year = date.getFullYear()
	const month = String(date.getMonth() + 1).padStart(2, '0')
	const day = String(date.getDate()).padStart(2, '0')
	return `${year}-${month}-${day}`
}

// 格式化手机号
const formatPhone = (phone) => {
	if (!phone) return '未绑定'
	return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 获取性别文本
const getGenderText = (gender) => {
	const genderMap = { 0: '保密', 1: '男', 2: '女' }
	return genderMap[gender] || '未设置'
}

// 获取头像完整URL
const getAvatarUrl = (avatar) => {
	return getImageUrl(avatar)
}


// 编辑头像
const editAvatar = () => {
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: async (res) => {
			const tempFilePath = res.tempFilePaths[0]
			console.log('选择的图片:', tempFilePath)
			
			try {
				uni.showLoading({ title: '上传中...' })
				
				const token = uni.getStorageSync('token') || ''
				
				uni.uploadFile({
					url: 'http://localhost:8080/api/upload/avatar',
					filePath: tempFilePath,
					name: 'file',
					header: {
						'Authorization': `Bearer ${token}`
					},
					success: async (uploadRes) => {
						uni.hideLoading()
						
						const data = JSON.parse(uploadRes.data)
						console.log('上传响应:', data)
						
						if (data.code === 200) {
							const avatarUrl = data.data.url
							
							try {
								await updateUserInfo({ avatar: avatarUrl })
								
								userInfo.value.avatar = avatarUrl
								originalUserInfo.value.avatar = avatarUrl
								
								const cachedUserInfo = uni.getStorageSync('userInfo')
								if (cachedUserInfo) {
									cachedUserInfo.avatar = avatarUrl
									uni.setStorageSync('userInfo', cachedUserInfo)
								}
								
								await nextTick()
								uni.showToast({ title: '头像修改成功', icon: 'success' })
							} catch (e) {
								console.error('更新用户信息失败:', e)
								uni.showToast({ title: e.message || '修改失败', icon: 'none' })
							}
						} else {
							uni.showToast({ title: data.message || '上传失败', icon: 'none' })
						}
					},
					fail: (err) => {
						uni.hideLoading()
						console.error('上传失败:', err)
						uni.showToast({ title: '上传失败', icon: 'none' })
					}
				})
			} catch (e) {
				uni.hideLoading()
				console.error('错误:', e)
				uni.showToast({ title: '操作失败', icon: 'none' })
			}
		},
		fail: (err) => {
			console.error('选择图片失败:', err)
		}
	})
}

// 编辑用户名
const editUsername = () => {
	uni.showModal({
		title: '修改用户名',
		editable: true,
		placeholderText: '请输入新的用户名',
		content: userInfo.value.username || '',
		success: (res) => {
			if (res.confirm && res.content) {
				userInfo.value.username = res.content
				checkChanges()
				uni.showToast({ title: '用户名已更新,请保存', icon: 'none' })
			}
		}
	})
}

// 编辑性别
const editGender = () => {
	const currentIndex = userInfo.value.gender || 0
	uni.showActionSheet({
		itemList: ['保密', '男', '女'],
		success: (res) => {
			if (res.tapIndex !== currentIndex) {
				userInfo.value.gender = res.tapIndex
				checkChanges()
				uni.showToast({ title: '性别已更新,请保存', icon: 'none' })
			}
		}
	})
}

// 生日选择器改变
const onBirthdayChange = (e) => {
	userInfo.value.birthday = e.detail.value
	checkChanges()
	uni.showToast({ title: '生日已更新,请保存', icon: 'none' })
}

// 编辑个人简介
const editBio = () => {
	uni.showModal({
		title: '修改个人简介',
		editable: true,
		placeholderText: '介绍一下自己吧',
		content: userInfo.value.bio || '',
		success: (res) => {
			if (res.confirm) {
				userInfo.value.bio = res.content || ''
				checkChanges()
				uni.showToast({ title: '简介已更新,请保存', icon: 'none' })
			}
		}
	})
}

// 编辑手机号
const editPhone = () => {
	uni.navigateTo({
		url: '/pages/change-phone/change-phone'
	})
}

// 修改密码
const editPassword = async () => {
        try {
                const res = await getUserPhone()
                const fullPhone = (res && res.phone) ? res.phone : res

                if (!fullPhone) {
                        uni.showToast({
                                title: '请先绑定手机号',
                                icon: 'none',
                                duration: 2000
                        })
                        return
                }

                uni.navigateTo({
                        url: `/pages/change-password/change-password?phone=${fullPhone}`
                })
        } catch (e) {
                console.error('获取手机号失败', e)
                uni.showToast({
                        title: e.message || '获取手机号失败',
                        icon: 'none'
                })
        }
}

// 检查是否有修改
const checkChanges = () => {
	hasChanges.value = JSON.stringify(userInfo.value) !== JSON.stringify(originalUserInfo.value)
}

// 保存修改
const handleSave = async () => {
	if (!hasChanges.value) {
		uni.showToast({ title: '没有修改需要保存', icon: 'none' })
		return
	}
	
	uni.showLoading({ title: '保存中...' })
	
	try {
		const updateData = {
			username: userInfo.value.username,
			avatar: userInfo.value.avatar,
			bio: userInfo.value.bio,
			gender: userInfo.value.gender,
			birthday: userInfo.value.birthday
		}
		
		await updateUserInfo(updateData)
		
		uni.setStorageSync('userInfo', userInfo.value)
		originalUserInfo.value = JSON.parse(JSON.stringify(userInfo.value))
		hasChanges.value = false
		
		uni.hideLoading()
		uni.showToast({
			title: '保存成功',
			icon: 'success'
		})
	} catch (e) {
		uni.hideLoading()
		console.error('保存失败:', e)
		uni.showToast({
			title: e.message || '保存失败，请重试',
			icon: 'none'
		})
	}
}

const handleLogout = () => {
	uni.showModal({
		title: '提示',
		content: '确定要退出登录吗?',
		success: async (res) => {
			if (res.confirm) {
				try {
					console.log('开始退出登录...')
					await logout()
					console.log('后端退出成功')
					
					uni.clearStorageSync()
					console.log('已清除所有缓存')
					
					uni.showToast({
						title: '退出成功',
						icon: 'success',
						duration: 1500
					})
					
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/login/login'
						})
					}, 1500)
					
				} catch (e) {
					console.error('退出登录失败:', e)
					uni.clearStorageSync()
					uni.showToast({
						title: '已退出',
						icon: 'success'
					})
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/login/login'
						})
					}, 1500)
				}
			}
		}
	})
}
</script>

<style lang="scss" scoped>
.container {
	background-color: #F8FAFC;
	min-height: 100vh;
	padding-bottom: 60rpx;
}

.navbar {
	background: white;
	position: sticky;
	top: 0;
	z-index: 100;
	padding-top: calc(var(--status-bar-height) + 10rpx);
	box-shadow: 0 1rpx 0 rgba(0,0,0,0.05);
}

.navbar-content {
	height: 88rpx;
	display: flex;
	align-items: center;
	padding: 0 30rpx;
}

.nav-left {
	width: 80rpx;
}

.back-icon {
	width: 40rpx;
	height: 40rpx;
	opacity: 0.7;
}

.nav-title {
	flex: 1;
	text-align: center;
	font-size: 34rpx;
	font-weight: 600;
	color: #333;
}

.nav-right {
	width: 80rpx;
}

.avatar-section {
	padding: 60rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	background: white;
	border-bottom: 20rpx solid #F8FAFC;
}

.avatar-container {
	position: relative;
	margin-bottom: 24rpx;
}

.avatar-large {
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	border: 6rpx solid #fff;
	box-shadow: 0 8rpx 24rpx rgba(0,0,0,0.08);
}

.avatar-edit-badge {
	position: absolute;
	right: 0;
	bottom: 0;
	width: 48rpx;
	height: 48rpx;
	background: #FF9E64;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	border: 4rpx solid #fff;
	box-shadow: 0 2rpx 8rpx rgba(255, 158, 100, 0.4);
}

.camera-icon {
	width: 24rpx;
	height: 24rpx;
}

.username-display {
	font-size: 36rpx;
	font-weight: 700;
	color: #333;
	margin-bottom: 12rpx;
}

.edit-tip {
	font-size: 24rpx;
	color: #999;
}

.section {
	background: white;
	margin-bottom: 20rpx;
}

.section-header {
	padding: 30rpx 30rpx 10rpx;
}

.section-title {
	font-size: 26rpx;
	font-weight: 600;
	color: #999;
	letter-spacing: 2rpx;
	text-transform: uppercase;
}

.settings-group {
	padding: 0 30rpx;
}

.setting-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 36rpx 0;
	border-bottom: 1rpx solid #F1F5F9;
	
	&:last-child {
		border-bottom: none;
	}
	
	&:active {
		opacity: 0.6;
	}
}

.item-label {
	font-size: 30rpx;
	color: #333;
	font-weight: 500;
}

.item-right {
	display: flex;
	align-items: center;
	gap: 12rpx;
}

.item-value {
	font-size: 28rpx;
	color: #666;
	max-width: 340rpx;
	text-align: right;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.arrow-icon {
	width: 24rpx;
	height: 24rpx;
	opacity: 0.3;
}

.footer-actions {
	padding: 60rpx 40rpx;
	display: flex;
	flex-direction: column;
	gap: 30rpx;
}

.btn-save {
	height: 100rpx;
	background: linear-gradient(90deg, #FF9E64, #FF7A45);
	border-radius: 50rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 8rpx 20rpx rgba(255, 158, 100, 0.3);
	transition: all 0.3s;
	
	&.disabled {
		opacity: 0.5;
		filter: grayscale(0.5);
	}
	
	&:active:not(.disabled) {
		transform: scale(0.98);
		box-shadow: 0 4rpx 10rpx rgba(255, 158, 100, 0.2);
	}
}

.btn-text {
	color: white;
	font-size: 32rpx;
	font-weight: 600;
}

.btn-logout {
	height: 100rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	border-radius: 50rpx;
	background: #fff;
	border: 2rpx solid #F1F5F9;
	
	&:active {
		background: #F8FAFC;
	}
}

.logout-text {
	font-size: 28rpx;
	color: #EF476F;
	font-weight: 500;
}

</style>
