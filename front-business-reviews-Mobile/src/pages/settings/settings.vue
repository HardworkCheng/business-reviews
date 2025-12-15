<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="navbar">
			<view class="navbar-content">
				<view class="nav-left" @click="goBack">
					<text class="icon">←</text>
				</view>
				<view class="nav-title">设置</view>
				<view class="nav-right"></view>
			</view>
		</view>
		
		<!-- 头像展示区 -->
		<view class="avatar-section">
			<view class="avatar-container" @click="editAvatar">
				<image class="avatar-large" :src="avatarUrl"></image>
				<view class="avatar-edit-tip">
					<text>📷 点击修改</text>
				</view>
			</view>
			<text class="username-display">{{ userInfo.username || '未设置' }}</text>
		</view>

		<!-- 个人信息设置 -->
		<view class="section">
			<view class="section-title">个人信息</view>
			<view class="setting-item clay-shadow" @click="editUsername">
				<text class="item-label">用户名</text>
				<view class="item-right">
					<text class="item-value">{{ userInfo.username || '未设置' }}</text>
					<text class="arrow">›</text>
				</view>
			</view>
			<view class="setting-item clay-shadow" @click="editGender">
				<text class="item-label">性别</text>
				<view class="item-right">
					<text class="item-value">{{ getGenderText(userInfo.gender) }}</text>
					<text class="arrow">›</text>
				</view>
			</view>
			<view class="setting-item clay-shadow">
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
						<text class="arrow">›</text>
					</view>
				</picker>
			</view>
			<view class="setting-item clay-shadow" @click="editBio">
				<text class="item-label">个人简介</text>
				<view class="item-right">
					<text class="item-value">{{ userInfo.bio || '未设置' }}</text>
					<text class="arrow">›</text>
				</view>
			</view>
		</view>

		<!-- 账号与安全 -->
		<view class="section">
			<view class="section-title">账号与安全</view>
			<view class="setting-item clay-shadow" @click="editPhone">
				<text class="item-label">手机号</text>
				<view class="item-right">
					<text class="item-value">{{ formatPhone(userInfo.phone) }}</text>
					<text class="arrow">›</text>
				</view>
			</view>
			<view class="setting-item clay-shadow" @click="editPassword">
				<text class="item-label">修改密码</text>
				<view class="item-right">
					<text class="item-value">••••••</text>
					<text class="arrow">›</text>
				</view>
			</view>
		</view>

		<!-- 第三方账号绑定 -->
		<view class="section">
			<view class="section-title">第三方账号</view>
			<view class="setting-item clay-shadow" @click="editWechat">
				<view class="item-left">
					<text class="social-icon wechat">💬</text>
					<text class="item-label">微信</text>
				</view>
				<view class="item-right">
					<text class="item-value">{{ userInfo.wechatOpenid ? '已绑定' : '未绑定' }}</text>
					<text class="arrow">›</text>
				</view>
			</view>
			<view class="setting-item clay-shadow" @click="editQQ">
				<view class="item-left">
					<text class="social-icon qq">🐧</text>
					<text class="item-label">QQ</text>
				</view>
				<view class="item-right">
					<text class="item-value">{{ userInfo.qqOpenid ? '已绑定' : '未绑定' }}</text>
					<text class="arrow">›</text>
				</view>
			</view>
			<view class="setting-item clay-shadow" @click="editWeibo">
				<view class="item-left">
					<text class="social-icon weibo">🎐</text>
					<text class="item-label">微博</text>
				</view>
				<view class="item-right">
					<text class="item-value">{{ userInfo.weiboUid ? '已绑定' : '未绑定' }}</text>
					<text class="arrow">›</text>
				</view>
			</view>
		</view>

		<!-- 退出登录按钮 -->
		<view class="logout-section">
			<view class="save-btn clay-shadow" @click="handleSave">
				<text class="save-text">保存修改</text>
			</view>
			<view class="logout-btn clay-shadow" @click="handleLogout">
				<text class="logout-text">退出登录</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, nextTick, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { logout, sendCode } from '../../api/auth'
import { getUserInfo, updateUserInfo, changePassword } from '../../api/user'

const userInfo = ref({})
const originalUserInfo = ref({}) // 保存原始数据用于比较
const hasChanges = ref(false) // 是否有修改
const isLoading = ref(false) // 是否正在加载
const showGenderPicker = ref(false)
const genderOptions = [
	{ text: '保密', value: 0 },
	{ text: '男', value: 1 },
	{ text: '女', value: 2 }
]

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
		console.log('fullPhone:', info?.fullPhone)
		console.log('phone:', info?.phone)
		console.log('birthday:', info?.birthday)
		console.log('gender:', info?.gender)
		console.log('bio:', info?.bio)
		
		if (info) {
			// 直接使用后端返回的数据,不使用缓存
			userInfo.value = info
			// 保存原始数据
			originalUserInfo.value = JSON.parse(JSON.stringify(info))
			// 更新缓存
			uni.setStorageSync('userInfo', info)
			console.log('用户信息已更新')
		} else {
			console.warn('未获取到用户信息')
			userInfo.value = {}
			originalUserInfo.value = {}
		}
	} catch (e) {
		console.error('获取用户信息失败:', e)
		
		// 如果是401或用户不存在,不使用缓存,直接跳转登录
		if (e.code === 401 || e.code === 40401) {
			console.warn('认证失败,清除缓存并跳转登录')
			uni.clearStorageSync()
			uni.reLaunch({ url: '/pages/login/login' })
		} else {
			// 其他错误显示提示
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
		// 检查是否有返回页面
		const pages = getCurrentPages()
		if (pages.length > 1) {
			uni.navigateBack({
				delta: 1,
				fail: (err) => {
					console.error('返回失败:', err)
					// 如果返回失败,跳转到首页
					uni.switchTab({
						url: '/pages/profile/profile'
					})
				}
			})
		} else {
			// 如果没有上一页,直接跳转到个人主页
			uni.switchTab({
				url: '/pages/profile/profile'
			})
		}
	} catch (e) {
		console.error('goBack error:', e)
		// 出错时跳转到个人主页
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
	if (!avatar) {
		return 'https://via.placeholder.com/200/FFD166/FFFFFF?text=User'
	}
	
	// 直接返回阿里云OSS URL
	return avatar
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
				
				// 上传头像
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
							console.log('=== 头像上传成功 ===')
							console.log('头像 URL:', avatarUrl)
							console.log('完整 URL:', getAvatarUrl(avatarUrl))
							console.log('更新前 userInfo.avatar:', userInfo.value.avatar)
							
							// 更新用户信息
							try {
								await updateUserInfo({ avatar: avatarUrl })
								console.log('后端更新成功')
								
								// 直接赋值更新
								userInfo.value.avatar = avatarUrl
								originalUserInfo.value.avatar = avatarUrl
								
								console.log('更新后 userInfo.avatar:', userInfo.value.avatar)
								
								// 同步更新localStorage
								const cachedUserInfo = uni.getStorageSync('userInfo')
								if (cachedUserInfo) {
									cachedUserInfo.avatar = avatarUrl
									uni.setStorageSync('userInfo', cachedUserInfo)
									console.log('缓存已更新')
								}
								
								// 使用 nextTick 确保 DOM 已更新
								await nextTick()
								console.log('DOM已更新')
								
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
				// 允许空字符串
				userInfo.value.bio = res.content || ''
				checkChanges()
				uni.showToast({ title: '简介已更新,请保存', icon: 'none' })
			}
		}
	})
}

// 编辑手机号 - 跳转到修改手机号页面
const editPhone = () => {
	// 跳转到修改手机号页面
	uni.navigateTo({
		url: '/pages/change-phone/change-phone'
	})
}

// 修改密码 - 跳转到修改密码页面
const editPassword = () => {
	// 获取完整的手机号(不是脱敏后的)
	let fullPhone = userInfo.value.fullPhone
	
	// 如果 fullPhone 不存在,尝试使用 phone 字段
	if (!fullPhone && userInfo.value.phone) {
		// 检查 phone 是否是脱敏后的(包含****)
		if (userInfo.value.phone.includes('*')) {
			console.error('phone 字段是脱敏后的,无法使用')
			uni.showToast({ 
				title: '无法获取手机号,请重新登录', 
				icon: 'none',
				duration: 2000
			})
			return
		}
		fullPhone = userInfo.value.phone
	}
	
	if (!fullPhone) {
		console.error('fullPhone 和 phone 都为空')
		uni.showToast({ 
			title: '请先绑定手机号', 
			icon: 'none',
			duration: 2000
		})
		return
	}
	
	// 跳转到修改密码页面
	uni.navigateTo({
		url: `/pages/change-password/change-password?phone=${fullPhone}`
	})
}

// 编辑微信绑定
const editWechat = () => {
	if (userInfo.value.wechatOpenid) {
		uni.showModal({
			title: '解绑微信',
			content: '确定要解绑微信账号吗？',
			success: (res) => {
				if (res.confirm) {
					userInfo.value.wechatOpenid = ''
					checkChanges()
					uni.showToast({ title: '微信已解绑，请保存', icon: 'none' })
				}
			}
		})
	} else {
		uni.showToast({ title: '微信绑定功能开发中', icon: 'none' })
	}
}

// 编辑QQ绑定
const editQQ = () => {
	if (userInfo.value.qqOpenid) {
		uni.showModal({
			title: '解绑QQ',
			content: '确定要解绑QQ账号吗？',
			success: (res) => {
				if (res.confirm) {
					userInfo.value.qqOpenid = ''
					checkChanges()
					uni.showToast({ title: 'QQ已解绑，请保存', icon: 'none' })
				}
			}
		})
	} else {
		uni.showToast({ title: 'QQ绑定功能开发中', icon: 'none' })
	}
}

// 编辑微博绑定
const editWeibo = () => {
	if (userInfo.value.weiboUid) {
		uni.showModal({
			title: '解绑微博',
			content: '确定要解绑微博账号吗？',
			success: (res) => {
				if (res.confirm) {
					userInfo.value.weiboUid = ''
					checkChanges()
					uni.showToast({ title: '微博已解绑，请保存', icon: 'none' })
				}
			}
		})
	} else {
		uni.showToast({ title: '微博绑定功能开发中', icon: 'none' })
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
		// 准备更新数据
		const updateData = {
			username: userInfo.value.username,
			avatar: userInfo.value.avatar,
			bio: userInfo.value.bio,
			gender: userInfo.value.gender,
			birthday: userInfo.value.birthday,
			wechatOpenid: userInfo.value.wechatOpenid || '',
			qqOpenid: userInfo.value.qqOpenid || '',
			weiboUid: userInfo.value.weiboUid || ''
		}
		
		// 调用后端API
		await updateUserInfo(updateData)
		
		// 更新缓存
		uni.setStorageSync('userInfo', userInfo.value)
		
		// 更新原始数据
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
					
					// 调用后端退出登录接口
					await logout()
					console.log('后端退出成功')
					
					// 清除所有本地存储
					uni.clearStorageSync()
					console.log('已清除所有缓存')
					
					// 显示成功提示
					uni.showToast({
						title: '退出成功',
						icon: 'success',
						duration: 1500
					})
					
					// 延迟跳转到登录页面
					setTimeout(() => {
						uni.reLaunch({
							url: '/pages/login/login'
						})
					}, 1500)
					
				} catch (e) {
					console.error('退出登录失败:', e)
					// 即使接口调用失败,也清除本地数据并跳转
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
	background: #F7F9FC;
	min-height: 100vh;
}

.navbar {
	background: white;
	padding-top: calc(var(--status-bar-height) + 10rpx);
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.05);
}

.navbar-content {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 20rpx 30rpx;
}

.nav-left {
	width: 80rpx;
}

.icon {
	font-size: 48rpx;
	color: #333;
}

.nav-title {
	font-size: 36rpx;
	font-weight: 500;
	color: #333;
}

.nav-right {
	width: 80rpx;
}

.avatar-section {
	background: linear-gradient(to bottom, rgba(255, 158, 100, 0.15), transparent);
	padding: 50rpx 0;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.avatar-container {
	position: relative;
	margin-bottom: 20rpx;
}

.avatar-large {
	width: 180rpx;
	height: 180rpx;
	border-radius: 50%;
	border: 6rpx solid white;
	box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.15);
}

.avatar-edit-tip {
	position: absolute;
	bottom: 0;
	right: 0;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
	font-size: 20rpx;
	padding: 8rpx 16rpx;
	border-radius: 20rpx;
	box-shadow: 0 4rpx 10rpx rgba(102, 126, 234, 0.4);
}

.username-display {
	font-size: 36rpx;
	font-weight: 500;
	color: #333;
}

.section {
	margin-top: 40rpx;
	padding: 0 30rpx;
}

.section-title {
	font-size: 28rpx;
	color: #999;
	margin-bottom: 20rpx;
	padding-left: 10rpx;
}

.setting-item {
	background: white;
	border-radius: 30rpx;
	padding: 35rpx 30rpx;
	margin-bottom: 20rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.item-left {
	display: flex;
	align-items: center;
}

.social-icon {
	font-size: 36rpx;
	margin-right: 15rpx;
}

.social-icon.wechat {
	filter: grayscale(0);
}

.social-icon.qq {
	filter: grayscale(0);
}

.social-icon.weibo {
	filter: grayscale(0);
}

.item-label {
	font-size: 32rpx;
	color: #333;
}

.item-right {
	display: flex;
	align-items: center;
}

.avatar-preview {
	width: 80rpx;
	height: 80rpx;
	border-radius: 50%;
	margin-right: 15rpx;
}

.item-value {
	font-size: 28rpx;
	color: #999;
	margin-right: 15rpx;
	max-width: 300rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.arrow {
	font-size: 40rpx;
	color: #ccc;
}

.logout-section {
	padding: 60rpx 30rpx;
}

.save-btn {
	background: linear-gradient(135deg, #FF9E64 0%, #FF7A45 100%);
	border-radius: 40rpx;
	padding: 35rpx;
	text-align: center;
	margin-bottom: 20rpx;
}

.save-text {
	font-size: 32rpx;
	color: white;
	font-weight: 500;
}

.logout-btn {
	background: white;
	border-radius: 40rpx;
	padding: 35rpx;
	text-align: center;
}

.logout-text {
	font-size: 32rpx;
	color: #EF476F;
	font-weight: 500;
}
</style>
