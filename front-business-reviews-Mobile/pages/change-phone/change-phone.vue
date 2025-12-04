<template>
	<view class="container">
		<!-- 自定义导航栏 -->
		<view class="navbar">
			<view class="navbar-content">
				<view class="nav-left" @click="goBack">
					<text class="icon">←</text>
				</view>
				<view class="nav-title">修改手机号</view>
				<view class="nav-right"></view>
			</view>
		</view>

		<!-- 步骤指示器 -->
		<view class="steps-container">
			<view class="step" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
				<view class="step-number">1</view>
				<text class="step-text">验证身份</text>
			</view>
			<view class="step-line" :class="{ active: currentStep > 1 }"></view>
			<view class="step" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
				<view class="step-number">2</view>
				<text class="step-text">新手机号</text>
			</view>
			<view class="step-line" :class="{ active: currentStep > 2 }"></view>
			<view class="step" :class="{ active: currentStep >= 3 }">
				<view class="step-number">3</view>
				<text class="step-text">完成</text>
			</view>
		</view>

		<!-- 步骤1：验证原手机号 -->
		<view class="step-content" v-if="currentStep === 1">
			<view class="section-header">
				<text class="section-title">验证当前手机号</text>
				<text class="section-desc">为了保障账号安全，请先验证当前绑定的手机号</text>
			</view>
			
			<view class="phone-display">
				<text class="phone-label">当前手机号</text>
				<text class="phone-number">{{ formatPhone(currentPhone) }}</text>
			</view>
			
			<view class="form-item">
				<view class="input-container clay-shadow">
					<input 
						type="number" 
						v-model="oldPhoneCode" 
						placeholder="请输入验证码" 
						maxlength="6"
						class="input-field"
					/>
					<view 
						class="code-btn" 
						:class="{ disabled: oldCodeCountdown > 0 }"
						@click="sendOldPhoneCode"
					>
						<text>{{ oldCodeCountdown > 0 ? `${oldCodeCountdown}s` : '获取验证码' }}</text>
					</view>
				</view>
			</view>
			
			<view class="submit-btn clay-shadow" @click="verifyOldPhone">
				<text class="btn-text">下一步</text>
			</view>
		</view>

		<!-- 步骤2：输入新手机号并验证 -->
		<view class="step-content" v-if="currentStep === 2">
			<view class="section-header">
				<text class="section-title">绑定新手机号</text>
				<text class="section-desc">请输入要绑定的新手机号码</text>
			</view>
			
			<view class="form-item">
				<view class="input-container clay-shadow">
					<text class="input-prefix">+86</text>
					<input 
						type="number" 
						v-model="newPhone" 
						placeholder="请输入新手机号" 
						maxlength="11"
						class="input-field"
					/>
				</view>
			</view>
			
			<view class="form-item">
				<view class="input-container clay-shadow">
					<input 
						type="number" 
						v-model="newPhoneCode" 
						placeholder="请输入验证码" 
						maxlength="6"
						class="input-field"
					/>
					<view 
						class="code-btn" 
						:class="{ disabled: newCodeCountdown > 0 || !isValidPhone(newPhone) }"
						@click="sendNewPhoneCode"
					>
						<text>{{ newCodeCountdown > 0 ? `${newCodeCountdown}s` : '获取验证码' }}</text>
					</view>
				</view>
			</view>
			
			<view class="submit-btn clay-shadow" @click="submitChangePhone">
				<text class="btn-text">确认修改</text>
			</view>
			
			<view class="back-link" @click="currentStep = 1">
				<text>返回上一步</text>
			</view>
		</view>

		<!-- 步骤3：完成 -->
		<view class="step-content success-content" v-if="currentStep === 3">
			<view class="success-icon">✓</view>
			<text class="success-title">手机号修改成功</text>
			<text class="success-desc">新手机号：{{ formatPhone(newPhone) }}</text>
			<text class="success-tip">为确保账号安全，建议您重新登录</text>
			
			<view class="submit-btn clay-shadow" @click="handleReLogin">
				<text class="btn-text">重新登录</text>
			</view>
			
			<view class="back-link" @click="goToSettings">
				<text>返回设置</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { sendCode } from '../../api/auth'
import { getUserPhone, changePhone } from '../../api/user'

// 当前步骤
const currentStep = ref(1)

// 原手机号相关
const currentPhone = ref('')
const oldPhoneCode = ref('')
const oldCodeCountdown = ref(0)

// 新手机号相关
const newPhone = ref('')
const newPhoneCode = ref('')
const newCodeCountdown = ref(0)

// 定时器
let oldCodeTimer = null
let newCodeTimer = null

onLoad(async () => {
	await fetchCurrentPhone()
})

// 获取当前绑定的手机号
const fetchCurrentPhone = async () => {
	try {
		const res = await getUserPhone()
		currentPhone.value = res.phone || res
		console.log('当前手机号:', currentPhone.value)
	} catch (e) {
		console.error('获取手机号失败:', e)
		// 尝试从缓存获取
		const userInfo = uni.getStorageSync('userInfo')
		if (userInfo && userInfo.fullPhone) {
			currentPhone.value = userInfo.fullPhone
		} else if (userInfo && userInfo.phone && !userInfo.phone.includes('*')) {
			currentPhone.value = userInfo.phone
		} else {
			uni.showToast({ title: '获取手机号失败', icon: 'none' })
			setTimeout(() => goBack(), 1500)
		}
	}
}

// 格式化手机号（脱敏显示）
const formatPhone = (phone) => {
	if (!phone) return ''
	return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 验证手机号格式
const isValidPhone = (phone) => {
	return /^1[3-9]\d{9}$/.test(phone)
}

// 发送原手机号验证码
const sendOldPhoneCode = async () => {
	if (oldCodeCountdown.value > 0) return
	if (!currentPhone.value) {
		uni.showToast({ title: '获取手机号失败', icon: 'none' })
		return
	}
	
	try {
		uni.showLoading({ title: '发送中...' })
		// type=4 表示修改手机号验证原手机
		await sendCode(currentPhone.value, 4)
		uni.hideLoading()
		uni.showToast({ title: '验证码已发送', icon: 'success' })
		
		// 开始倒计时
		oldCodeCountdown.value = 60
		oldCodeTimer = setInterval(() => {
			oldCodeCountdown.value--
			if (oldCodeCountdown.value <= 0) {
				clearInterval(oldCodeTimer)
			}
		}, 1000)
	} catch (e) {
		uni.hideLoading()
		uni.showToast({ title: e.message || '发送失败', icon: 'none' })
	}
}

// 验证原手机号
const verifyOldPhone = () => {
	if (!oldPhoneCode.value || oldPhoneCode.value.length !== 6) {
		uni.showToast({ title: '请输入6位验证码', icon: 'none' })
		return
	}
	
	// 原手机号验证通过，进入下一步
	// 实际验证会在最终提交时由后端完成
	currentStep.value = 2
}

// 发送新手机号验证码
const sendNewPhoneCode = async () => {
	if (newCodeCountdown.value > 0) return
	if (!isValidPhone(newPhone.value)) {
		uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
		return
	}
	
	// 检查新手机号是否与当前手机号相同
	if (newPhone.value === currentPhone.value) {
		uni.showToast({ title: '新手机号不能与当前手机号相同', icon: 'none' })
		return
	}
	
	try {
		uni.showLoading({ title: '发送中...' })
		// type=5 表示修改手机号验证新手机
		await sendCode(newPhone.value, 5)
		uni.hideLoading()
		uni.showToast({ title: '验证码已发送', icon: 'success' })
		
		// 开始倒计时
		newCodeCountdown.value = 60
		newCodeTimer = setInterval(() => {
			newCodeCountdown.value--
			if (newCodeCountdown.value <= 0) {
				clearInterval(newCodeTimer)
			}
		}, 1000)
	} catch (e) {
		uni.hideLoading()
		uni.showToast({ title: e.message || '发送失败', icon: 'none' })
	}
}

// 提交修改手机号
const submitChangePhone = async () => {
	// 验证输入
	if (!oldPhoneCode.value || oldPhoneCode.value.length !== 6) {
		uni.showToast({ title: '原手机验证码错误', icon: 'none' })
		currentStep.value = 1
		return
	}
	
	if (!isValidPhone(newPhone.value)) {
		uni.showToast({ title: '请输入正确的新手机号', icon: 'none' })
		return
	}
	
	if (!newPhoneCode.value || newPhoneCode.value.length !== 6) {
		uni.showToast({ title: '请输入6位验证码', icon: 'none' })
		return
	}
	
	try {
		uni.showLoading({ title: '修改中...' })
		
		await changePhone({
			oldPhoneCode: oldPhoneCode.value,
			newPhone: newPhone.value,
			newPhoneCode: newPhoneCode.value
		})
		
		uni.hideLoading()
		
		// 进入完成步骤
		currentStep.value = 3
		
	} catch (e) {
		uni.hideLoading()
		console.error('修改手机号失败:', e)
		uni.showToast({ title: e.message || '修改失败', icon: 'none' })
	}
}

// 重新登录
const handleReLogin = () => {
	uni.clearStorageSync()
	uni.reLaunch({ url: '/pages/login/login' })
}

// 返回设置页
const goToSettings = () => {
	uni.navigateBack()
}

// 返回
const goBack = () => {
	if (currentStep.value === 3) {
		// 修改成功后返回设置页
		uni.navigateBack()
	} else if (currentStep.value === 2) {
		// 返回上一步
		currentStep.value = 1
	} else {
		uni.navigateBack()
	}
}

// 清理定时器
onMounted(() => {
	return () => {
		if (oldCodeTimer) clearInterval(oldCodeTimer)
		if (newCodeTimer) clearInterval(newCodeTimer)
	}
})
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

// 步骤指示器
.steps-container {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 50rpx 40rpx;
	background: white;
	margin-bottom: 30rpx;
}

.step {
	display: flex;
	flex-direction: column;
	align-items: center;
	
	.step-number {
		width: 60rpx;
		height: 60rpx;
		border-radius: 50%;
		background: #E0E0E0;
		color: #999;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
		font-weight: bold;
		margin-bottom: 10rpx;
		transition: all 0.3s;
	}
	
	.step-text {
		font-size: 24rpx;
		color: #999;
		transition: all 0.3s;
	}
	
	&.active {
		.step-number {
			background: linear-gradient(135deg, #FF9E64 0%, #FF7A45 100%);
			color: white;
		}
		.step-text {
			color: #FF9E64;
		}
	}
	
	&.completed {
		.step-number {
			background: #4CAF50;
			color: white;
		}
		.step-text {
			color: #4CAF50;
		}
	}
}

.step-line {
	width: 80rpx;
	height: 4rpx;
	background: #E0E0E0;
	margin: 0 20rpx;
	margin-bottom: 30rpx;
	transition: all 0.3s;
	
	&.active {
		background: #4CAF50;
	}
}

// 步骤内容
.step-content {
	padding: 0 30rpx;
}

.section-header {
	margin-bottom: 40rpx;
	
	.section-title {
		display: block;
		font-size: 36rpx;
		font-weight: 600;
		color: #333;
		margin-bottom: 10rpx;
	}
	
	.section-desc {
		display: block;
		font-size: 26rpx;
		color: #999;
	}
}

.phone-display {
	background: white;
	border-radius: 20rpx;
	padding: 30rpx;
	margin-bottom: 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	
	.phone-label {
		font-size: 28rpx;
		color: #666;
	}
	
	.phone-number {
		font-size: 32rpx;
		color: #333;
		font-weight: 500;
		letter-spacing: 2rpx;
	}
}

.form-item {
	margin-bottom: 30rpx;
}

.input-container {
	background: white;
	border-radius: 50rpx;
	padding: 25rpx 30rpx;
	display: flex;
	align-items: center;
	
	.input-prefix {
		font-size: 32rpx;
		color: #333;
		margin-right: 20rpx;
		padding-right: 20rpx;
		border-right: 1rpx solid #E0E0E0;
	}
	
	.input-field {
		flex: 1;
		font-size: 32rpx;
		color: #333;
	}
	
	.code-btn {
		background: linear-gradient(135deg, #FF9E64 0%, #FF7A45 100%);
		color: white;
		font-size: 26rpx;
		padding: 15rpx 25rpx;
		border-radius: 30rpx;
		white-space: nowrap;
		
		&.disabled {
			background: #E0E0E0;
			color: #999;
		}
	}
}

.submit-btn {
	background: linear-gradient(135deg, #FF9E64 0%, #FF7A45 100%);
	border-radius: 50rpx;
	padding: 35rpx;
	text-align: center;
	margin-top: 50rpx;
	
	.btn-text {
		font-size: 32rpx;
		color: white;
		font-weight: 500;
	}
}

.back-link {
	text-align: center;
	margin-top: 30rpx;
	
	text {
		font-size: 28rpx;
		color: #999;
	}
}

// 完成页面
.success-content {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding-top: 80rpx;
}

.success-icon {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	background: linear-gradient(135deg, #4CAF50 0%, #66BB6A 100%);
	color: white;
	font-size: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	margin-bottom: 40rpx;
}

.success-title {
	font-size: 40rpx;
	font-weight: 600;
	color: #333;
	margin-bottom: 20rpx;
}

.success-desc {
	font-size: 30rpx;
	color: #666;
	margin-bottom: 15rpx;
}

.success-tip {
	font-size: 26rpx;
	color: #FF9E64;
	margin-bottom: 60rpx;
}

// 阴影效果
.clay-shadow {
	box-shadow: 8rpx 8rpx 20rpx rgba(0, 0, 0, 0.08),
				-8rpx -8rpx 20rpx rgba(255, 255, 255, 0.8);
}
</style>
