<template>
	<view class="login-container">
		<!-- Logo区域 -->
		<view class="logo-section">
			<view class="clay-icon logo-icon bg-primary">
				<text class="icon-text">🍜</text>
			</view>
			<text class="app-title">美食点评</text>
			<text class="app-subtitle">探索美食，分享生活</text>
		</view>

		<!-- 登录表单 -->
		<view class="form-container clay-shadow">
			<text class="form-title">欢迎登录</text>

			<!-- 登录模式切换 -->
			<view class="login-tabs">
				<view 
					class="tab-item" 
					:class="{ active: loginMode === 'sms' }"
					@click="loginMode = 'sms'"
				>
					<text>短信验证码登录</text>
				</view>
				<view 
					class="tab-item" 
					:class="{ active: loginMode === 'password' }"
					@click="loginMode = 'password'"
				>
					<text>密码登录</text>
				</view>
			</view>

			<!-- 手机号输入 -->
			<view class="input-section">
				<text class="label">手机号</text>
				<view class="input-wrapper clay-border">
					<text class="input-icon">📱</text>
					<input 
						type="number" 
						v-model="phone" 
						placeholder="请输入手机号" 
						maxlength="11"
						class="input-field"
					/>
				</view>
			</view>

			<!-- 验证码输入 (短信登录模式) -->
			<view class="input-section" v-if="loginMode === 'sms'">
				<text class="label">验证码</text>
				<view class="code-wrapper">
					<view class="input-wrapper clay-border flex-1">
						<text class="input-icon">🛡️</text>
						<input 
							type="number" 
							v-model="code" 
							placeholder="请输入验证码" 
							maxlength="6"
							class="input-field"
						/>
					</view>
					<button 
						class="code-btn" 
						:disabled="countdown > 0"
						@click="getCode"
					>
						{{ countdown > 0 ? `${countdown}秒后重试` : '获取验证码' }}
					</button>
				</view>
			</view>

			<!-- 密码输入 (密码登录模式) -->
			<view class="input-section" v-if="loginMode === 'password'">
				<text class="label">密码</text>
				<view class="input-wrapper clay-border">
					<text class="input-icon">🔒</text>
					<input 
						:type="showPassword ? 'text' : 'password'" 
						v-model="password" 
						placeholder="请输入密码" 
						class="input-field"
					/>
					<view class="password-toggle" @click="showPassword = !showPassword">
						<text>{{ showPassword ? '👁️' : '👁️‍🗨️' }}</text>
					</view>
				</view>
				<text class="password-hint">新用户首次登录请使用短信验证码，密码默认为手机号</text>
			</view>

			<!-- 登录按钮 -->
			<button class="login-btn bg-primary clay-border clay-shadow" @click="handleLogin">
				登录
			</button>

			<!-- 协议 -->
			<view class="agreement">
				<text class="agreement-text">登录即表示同意</text>
				<text class="agreement-link" @click="openAgreement('user')">《用户协议》</text>
				<text class="agreement-text">和</text>
				<text class="agreement-link" @click="openAgreement('privacy')">《隐私政策》</text>
			</view>
		</view>

		<!-- 游客访问 -->
		<view class="guest-mode" @click="guestMode">
			<text class="guest-text">游客模式浏览</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { sendCode, loginByCode, loginByPassword } from '../../api/auth'

const phone = ref('')
const code = ref('')
const password = ref('')
const countdown = ref(0)
const loginMode = ref('sms') // 'sms' | 'password'
const showPassword = ref(false)
let timer = null

onLoad(() => {
  console.log('Login page loaded')
})

// 获取验证码
const getCode = async () => {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  if (countdown.value > 0) return

  try {
    // type = 1 表示登录验证码
    await sendCode(phone.value, 1)

    countdown.value = 60
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)

    uni.showToast({
      title: '验证码已发送',
      icon: 'success'
    })
  } catch (e) {
    // 失败提示已在统一 request 里处理
  }
}

// 登录
const handleLogin = async () => {
  if (!phone.value || phone.value.length !== 11) {
    uni.showToast({
      title: '请输入正确的手机号',
      icon: 'none'
    })
    return
  }

  if (loginMode.value === 'sms') {
    await handleSmsLogin()
  } else {
    await handlePasswordLogin()
  }
}

// 短信验证码登录
const handleSmsLogin = async () => {
  if (!code.value || code.value.length !== 6) {
    uni.showToast({
      title: '请输入6位验证码',
      icon: 'none'
    })
    return
  }

  try {
    const res = await loginByCode(phone.value, code.value)
    handleLoginSuccess(res)
  } catch (e) {
    console.error('短信登录失败:', e)
  }
}

// 密码登录
const handlePasswordLogin = async () => {
  if (!password.value) {
    uni.showToast({
      title: '请输入密码',
      icon: 'none'
    })
    return
  }

  try {
    const res = await loginByPassword(phone.value, password.value)
    handleLoginSuccess(res)
  } catch (e) {
    console.error('密码登录失败:', e)
  }
}

// 处理登录成功
const handleLoginSuccess = (res) => {
  console.log('登录响应:', res)
  const token = res.token
  const userInfo = res.userInfo

  // 清除所有缓存数据，确保新用户登录时不会显示旧数据
  uni.clearStorageSync()
  console.log('已清除所有缓存数据')

  if (token) {
    uni.setStorageSync('token', token)
    console.log('Token 已存储:', token.substring(0, 20) + '...')
  } else {
    console.error('登录响应中没有 token')
    throw new Error('登录响应中没有 token')
  }
  if (userInfo) {
    uni.setStorageSync('userInfo', userInfo)
    console.log('UserInfo 已存储:', userInfo)
  }

  uni.showToast({
    title: '登录成功',
    icon: 'success'
  })

  setTimeout(() => {
    uni.switchTab({
      url: '/pages/index/index'
    })
  }, 800)
}

// 打开协议
const openAgreement = (type) => {
  uni.showToast({
    title: type === 'user' ? '用户协议' : '隐私政策',
    icon: 'none'
  })
}

// 游客模式
const guestMode = () => {
  uni.switchTab({
    url: '/pages/index/index'
  })
}
</script>

<style lang="scss" scoped>
.login-container {
	min-height: 100vh;
	background: linear-gradient(135deg, rgba(255, 158, 100, 0.2) 0%, rgba(255, 209, 102, 0.2) 50%, rgba(6, 214, 160, 0.2) 100%);
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 60rpx 40rpx;
}

.logo-section {
	display: flex;
	flex-direction: column;
	align-items: center;
	margin-bottom: 80rpx;
}

.logo-icon {
	width: 160rpx;
	height: 160rpx;
	margin-bottom: 30rpx;
}

.icon-text {
	font-size: 80rpx;
	color: white;
}

.app-title {
	font-size: 56rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 10rpx;
}

.app-subtitle {
	font-size: 28rpx;
	color: #666;
}

.form-container {
	width: 100%;
	max-width: 600rpx;
	background: white;
	border-radius: 60rpx;
	padding: 60rpx 50rpx;
}

.form-title {
	font-size: 48rpx;
	font-weight: bold;
	text-align: center;
	display: block;
	margin-bottom: 30rpx;
}

/* 登录模式切换Tab */
.login-tabs {
	display: flex;
	background: #f5f5f5;
	border-radius: 30rpx;
	padding: 8rpx;
	margin-bottom: 40rpx;
}

.tab-item {
	flex: 1;
	text-align: center;
	padding: 20rpx 0;
	border-radius: 24rpx;
	font-size: 26rpx;
	color: #666;
	transition: all 0.3s;
}

.tab-item.active {
	background: white;
	color: #FF9E64;
	font-weight: 500;
	box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
}

.input-section {
	margin-bottom: 40rpx;
}

.label {
	display: block;
	font-size: 28rpx;
	font-weight: 500;
	margin-bottom: 15rpx;
}

.input-wrapper {
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
	background: white;
}

.input-icon {
	font-size: 36rpx;
	margin-right: 15rpx;
}

.input-field {
	flex: 1;
	font-size: 28rpx;
}

.password-toggle {
	padding: 10rpx;
	font-size: 32rpx;
}

.password-hint {
	display: block;
	font-size: 22rpx;
	color: #999;
	margin-top: 10rpx;
}

.code-wrapper {
	display: flex;
	gap: 15rpx;
}

.code-btn {
	padding: 20rpx 25rpx;
	background: #f3f3f3;
	border-radius: 30rpx;
	font-size: 24rpx;
	white-space: nowrap;
	border: none;
}

.code-btn[disabled] {
	opacity: 0.6;
}

.login-btn {
	width: 100%;
	padding: 25rpx;
	color: white;
	font-weight: 500;
	font-size: 32rpx;
	margin-top: 20rpx;
	margin-bottom: 40rpx;
	border: none;
}

.agreement {
	text-align: center;
}

.agreement-text {
	font-size: 24rpx;
	color: #999;
}

.agreement-link {
	font-size: 24rpx;
	color: #FF9E64;
}

.guest-mode {
	margin-top: 50rpx;
}

.guest-text {
	font-size: 28rpx;
	color: #666;
	text-decoration: underline;
}
</style>
