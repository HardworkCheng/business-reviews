<template>
	<view class="container">
		<view class="header">
			<view class="back-btn" @click="goBack">
				<text class="back-icon">←</text>
			</view>
			<text class="title">修改密码</text>
		</view>

		<view class="form">
			<!-- 手机号显示 -->
			<view class="form-item">
				<text class="label">手机号</text>
				<text class="phone-value">{{ formatPhone(userPhone) }}</text>
			</view>

			<!-- 验证码输入 -->
			<view class="form-item">
				<text class="label">验证码</text>
				<input 
					class="input" 
					type="number" 
					maxlength="6"
					v-model="formData.code" 
					placeholder="请输入验证码"
				/>
				<button 
					class="code-btn" 
					:disabled="countdown > 0"
					@click="handleSendCode"
				>
					{{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
				</button>
			</view>

			<!-- 旧密码 -->
			<view class="form-item">
				<text class="label">旧密码</text>
				<input 
					class="input" 
					:type="showOldPassword ? 'text' : 'password'"
					v-model="formData.oldPassword" 
					placeholder="请输入旧密码"
				/>
				<text class="eye-icon" @click="showOldPassword = !showOldPassword">
					{{ showOldPassword ? '👁' : '👁‍🗨' }}
				</text>
			</view>

			<!-- 新密码 -->
			<view class="form-item">
				<text class="label">新密码</text>
				<input 
					class="input" 
					:type="showNewPassword ? 'text' : 'password'"
					v-model="formData.newPassword" 
					placeholder="请输入新密码(6-20位)"
				/>
				<text class="eye-icon" @click="showNewPassword = !showNewPassword">
					{{ showNewPassword ? '👁' : '👁‍🗨' }}
				</text>
			</view>

			<!-- 确认新密码 -->
			<view class="form-item">
				<text class="label">确认密码</text>
				<input 
					class="input" 
					:type="showConfirmPassword ? 'text' : 'password'"
					v-model="formData.confirmPassword" 
					placeholder="请再次输入新密码"
				/>
				<text class="eye-icon" @click="showConfirmPassword = !showConfirmPassword">
					{{ showConfirmPassword ? '👁' : '👁‍🗨' }}
				</text>
			</view>

			<!-- 提交按钮 -->
			<button class="submit-btn" @click="handleSubmit">确认修改</button>
		</view>
	</view>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { sendCode } from '../../api/auth'
import { changePassword } from '../../api/user'

const userPhone = ref('')
const countdown = ref(0)
let timer = null

const showOldPassword = ref(false)
const showNewPassword = ref(false)
const showConfirmPassword = ref(false)

const formData = ref({
	code: '',
	oldPassword: '',
	newPassword: '',
	confirmPassword: ''
})

onLoad((options) => {
	// 从上一页传递过来的手机号
	if (options.phone) {
		userPhone.value = options.phone
	}
})

onUnmounted(() => {
	if (timer) {
		clearInterval(timer)
	}
})

const goBack = () => {
	try {
		const pages = getCurrentPages()
		if (pages.length > 1) {
			uni.navigateBack({
				delta: 1,
				fail: (err) => {
					console.error('返回失败:', err)
					// 返回失败时跳转到设置页面
					uni.navigateTo({
						url: '/pages/settings/settings'
					})
				}
			})
		} else {
			// 没有上一页时跳转到设置页
			uni.navigateTo({
				url: '/pages/settings/settings'
			})
		}
	} catch (e) {
		console.error('goBack error:', e)
		uni.navigateTo({
			url: '/pages/settings/settings'
		})
	}
}

// 格式化手机号显示
const formatPhone = (phone) => {
	if (!phone) return ''
	return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

// 发送验证码
const handleSendCode = async () => {
	if (countdown.value > 0) return
	
	if (!userPhone.value) {
		uni.showToast({ title: '手机号不能为空', icon: 'none' })
		return
	}
	
	try {
		uni.showLoading({ title: '发送中...' })
		await sendCode(userPhone.value, 1)
		uni.hideLoading()
		uni.showToast({ title: '验证码已发送', icon: 'success' })
		
		// 开始倒计时 60 秒
		countdown.value = 60
		timer = setInterval(() => {
			countdown.value--
			if (countdown.value <= 0) {
				clearInterval(timer)
				timer = null
			}
		}, 1000)
	} catch (e) {
		uni.hideLoading()
		uni.showToast({ title: e.message || '发送失败', icon: 'none' })
	}
}

// 提交修改密码
const handleSubmit = async () => {
	// 验证表单
	if (!formData.value.code) {
		uni.showToast({ title: '请输入验证码', icon: 'none' })
		return
	}
	
	if (!formData.value.oldPassword) {
		uni.showToast({ title: '请输入旧密码', icon: 'none' })
		return
	}
	
	if (!formData.value.newPassword) {
		uni.showToast({ title: '请输入新密码', icon: 'none' })
		return
	}
	
	if (formData.value.newPassword.length < 6 || formData.value.newPassword.length > 20) {
		uni.showToast({ title: '密码长度必须在6-20位之间', icon: 'none' })
		return
	}
	
	if (!formData.value.confirmPassword) {
		uni.showToast({ title: '请确认新密码', icon: 'none' })
		return
	}
	
	if (formData.value.newPassword !== formData.value.confirmPassword) {
		uni.showToast({ title: '两次密码输入不一致', icon: 'none' })
		return
	}
	
	try {
		uni.showLoading({ title: '修改中...' })
		await changePassword(formData.value)
		uni.hideLoading()
		
		uni.showToast({ 
			title: '密码修改成功', 
			icon: 'success',
			duration: 1500
		})
		
		// 1.5秒后返回上一页
		setTimeout(() => {
			uni.navigateBack()
		}, 1500)
	} catch (e) {
		uni.hideLoading()
		uni.showToast({ title: e.message || '修改失败', icon: 'none' })
	}
}
</script>

<style lang="scss" scoped>
.container {
	min-height: 100vh;
	background-color: #f5f5f5;
}

.header {
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
	height: 44px;
	background-color: #fff;
	border-bottom: 1px solid #eee;
	
	.back-btn {
		position: absolute;
		left: 0;
		top: 0;
		bottom: 0;
		display: flex;
		align-items: center;
		padding: 0 15px;
		
		.back-icon {
			font-size: 20px;
			color: #333;
		}
	}
	
	.title {
		font-size: 17px;
		font-weight: 500;
		color: #333;
	}
}

.form {
	padding: 20px;
}

.form-item {
	position: relative;
	display: flex;
	align-items: center;
	background-color: #fff;
	border-radius: 8px;
	padding: 15px;
	margin-bottom: 15px;
	
	.label {
		width: 80px;
		font-size: 15px;
		color: #333;
	}
	
	.phone-value {
		flex: 1;
		font-size: 15px;
		color: #666;
	}
	
	.input {
		flex: 1;
		font-size: 15px;
		color: #333;
	}
	
	.code-btn {
		width: 100px;
		height: 32px;
		line-height: 32px;
		padding: 0;
		margin: 0;
		font-size: 13px;
		color: #fff;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		border-radius: 16px;
		border: none;
		
		&:disabled {
			background: #ccc;
		}
	}
	
	.eye-icon {
		margin-left: 10px;
		font-size: 18px;
		cursor: pointer;
	}
}

.submit-btn {
	width: 100%;
	height: 50px;
	line-height: 50px;
	margin-top: 30px;
	font-size: 16px;
	font-weight: 500;
	color: #fff;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: 25px;
	border: none;
}
</style>
