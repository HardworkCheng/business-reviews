<template>
	<view class="container">
		<!-- 顶部自定义导航栏 -->
		<view class="custom-navbar">
			<view class="segment-control">
				<view class="segment-slider" :style="sliderStyle"></view>
				<view 
					class="segment-btn" 
					:class="{ active: currentView === 0 }"
					@click="switchView(0)"
				>领券中心</view>
				<view 
					class="segment-btn" 
					:class="{ active: currentView === 1 }"
					@click="switchView(1)"
				>我的券包</view>
			</view>
		</view>

		<!-- 主内容区域 -->
		<scroll-view class="main-container" scroll-y>
			<!-- 领券中心视图 -->
			<view v-show="currentView === 0" class="view-page" :class="{ show: currentView === 0 }">
				<!-- 搜索框 -->
				<view class="search-box-wrapper">
					<view class="search-box" @click="focusSearch">
						<text class="search-icon">🔍</text>
						<input 
							v-model="searchKeyword"
							class="search-input"
							placeholder="搜索神券、商家..."
							placeholder-class="search-placeholder"
							@input="handleSearch"
							@confirm="handleSearch"
						/>
						<text v-if="searchKeyword" class="clear-icon" @click.stop="clearSearch">✕</text>
					</view>
				</view>

				<!-- 秒杀专区 -->
				<view v-if="seckillActivity && seckillActivity.coupons && seckillActivity.coupons.length > 0" class="seckill-zone">
					<view class="hot-tag">HOT</view>
					<view class="zone-header">
						<view class="zone-title">
							<text class="fire-icon">🔥</text>
							<text>限时秒杀</text>
						</view>
						<view class="timer">
							<text class="timer-block">{{ countdown.hours }}</text>
							<text class="timer-sep">:</text>
							<text class="timer-block">{{ countdown.minutes }}</text>
							<text class="timer-sep">:</text>
							<text class="timer-block">{{ countdown.seconds }}</text>
						</view>
					</view>
					<scroll-view 
						class="seckill-scroll" 
						scroll-x
						:scroll-left="seckillScrollLeft"
						scroll-with-animation
						@touchstart="handleTouchStart"
						@touchend="handleTouchEnd"
						@scroll="handleScroll"
					>
						<view 
							class="seckill-card" 
							v-for="item in seckillActivity.coupons" 
							:key="item.id"
							:class="{ 'sold-out': item.remainStock <= 0 }"
							@click="handleSeckillClick(item)"
						>
							<view class="sk-price" :class="{ disabled: item.remainStock <= 0 }">
								<text class="price-symbol">¥</text>{{ item.seckillPrice }}
							</view>
							<text class="sk-name">{{ item.title }}</text>
							<view class="sk-progress">
								<view class="sk-fill" :style="{ width: getProgress(item) + '%' }"></view>
							</view>
							<button 
								class="btn-grab" 
								:class="{ disabled: item.remainStock <= 0 }"
								@click.stop="claimSeckillCoupon(item)"
							>
								{{ getSeckillBtnText(item) }}
							</button>
						</view>
					</scroll-view>
				</view>

				<!-- 代金券专区（秒杀专区样式） -->
				<view v-if="voucherCoupons && voucherCoupons.length > 0" class="seckill-zone voucher-zone">
					<view class="hot-tag voucher-tag">券</view>
					<view class="zone-header">
						<view class="zone-title">
							<text class="fire-icon">🎫</text>
							<text>代金券专区</text>
						</view>
						<view class="voucher-tip">无门槛使用</view>
					</view>
					<scroll-view 
						class="seckill-scroll" 
						scroll-x
						scroll-with-animation
					>
						<view 
							class="seckill-card voucher-card" 
							v-for="coupon in voucherCoupons" 
							:key="coupon.id"
							:class="{ 'sold-out': coupon.status === 'sold_out', 'is-claimed': coupon.status === 'claimed' }"
							@click="handleCouponClick(coupon)"
						>
							<view class="sk-price voucher-price" :class="{ disabled: coupon.status === 'sold_out' || coupon.status === 'claimed' }">
								<text class="price-symbol">¥</text>{{ coupon.amount }}
							</view>
							<text class="sk-name">{{ coupon.title }}</text>
							<text class="voucher-shop">{{ coupon.shopName || '商家' }}</text>
							<!-- 使用图标按钮显示领取状态 -->
							<view class="voucher-btn-wrapper" @click.stop="claimCoupon(coupon)">
								<image 
									v-if="coupon.status === 'available'" 
									src="/static/icons/coupon-claim.png" 
									class="voucher-btn-icon"
									mode="aspectFit"
								/>
								<image 
									v-else-if="coupon.status === 'claimed'" 
									src="/static/icons/coupon-claimed.png" 
									class="voucher-btn-icon"
									mode="aspectFit"
								/>
								<button 
									v-else
									class="btn-grab voucher-btn" 
									:class="getVoucherBtnClass(coupon.status)"
								>
									{{ getCouponBtnText(coupon.status) }}
								</button>
							</view>
						</view>
					</scroll-view>
				</view>

				<!-- 好店神券列表 -->
				<view class="shop-coupon-list">
					<view class="list-title">好店神券</view>
					
					<view 
						class="coupon-row" 
						v-for="coupon in filteredAvailableCoupons" 
						:key="coupon.id"
						@click="handleCouponClick(coupon)"
					>
						<view class="cr-left">
							<image :src="coupon.shopLogo || '/static/images/default-shop.png'" class="cr-logo" mode="aspectFill"></image>
							<text class="cr-name">{{ coupon.shopName || '商家' }}</text>
						</view>
						<view class="cr-right">
							<view class="cr-info">
								<text class="cr-title">{{ coupon.title }}</text>
								<text class="cr-condition">{{ formatCouponCondition(coupon) }}</text>
							</view>
							<view class="cr-action">
								<view class="price-big">
									{{ formatCouponValue(coupon) }}
								</view>
								<!-- 使用图标按钮显示领取状态 -->
								<view class="btn-icon-wrapper" @click.stop="claimCoupon(coupon)">
									<image 
										v-if="coupon.status === 'available'" 
										src="/static/icons/coupon-claim.png" 
										class="btn-icon"
										mode="aspectFit"
									/>
									<image 
										v-else-if="coupon.status === 'claimed'" 
										src="/static/icons/coupon-claimed.png" 
										class="btn-icon"
										mode="aspectFit"
									/>
									<button 
										v-else
										class="btn-get" 
										:class="getCouponBtnClass(coupon.status)"
									>
										{{ getCouponBtnText(coupon.status) }}
									</button>
								</view>
							</view>
						</view>
						<view class="coupon-circle"></view>
					</view>

					<!-- 空状态 -->
					<view v-if="filteredAvailableCoupons.length === 0" class="empty-center">
						<text class="empty-icon">🎟️</text>
						<text class="empty-text">{{ searchKeyword ? '暂无相关优惠券' : '暂无可领取的优惠券' }}</text>
					</view>
				</view>
			</view>

			<!-- 我的卡包视图 -->
			<view v-show="currentView === 1" class="view-page" :class="{ show: currentView === 1 }">
				<!-- 状态筛选标签 -->
				<view class="wallet-tabs">
					<view 
						class="w-tab" 
						:class="{ active: walletTab === 'all' }"
						@click="filterWallet('all')"
					>全部</view>
					<view 
						class="w-tab" 
						:class="{ active: walletTab === 'unused' }"
						@click="filterWallet('unused')"
					>未使用</view>
					<view 
						class="w-tab" 
						:class="{ active: walletTab === 'used' }"
						@click="filterWallet('used')"
					>已使用</view>
					<view 
						class="w-tab" 
						:class="{ active: walletTab === 'expired' }"
						@click="filterWallet('expired')"
					>已过期</view>
				</view>

				<!-- 我的优惠券列表 -->
				<view class="my-coupon-list">
					<view 
						class="my-card" 
						v-for="coupon in filteredMyCoupons" 
						:key="coupon.id"
						:class="{ 'card-disabled': coupon.status !== 'unused' }"
						@click="handleMyCouponClick(coupon)"
					>
						<view class="mc-left">
							<text class="mc-amount">{{ formatCouponValue(coupon) }}</text>
							<text class="mc-label">{{ getCouponTypeLabel(coupon.type) }}</text>
						</view>
						<view class="mc-right">
							<text class="mc-title">{{ coupon.title }}</text>
							<view class="mc-shop">
								<text class="shop-icon">🏪</text>
								<text>{{ coupon.shopName || '全部商家' }}</text>
							</view>
							<text class="mc-time">有效期至 {{ coupon.expireTime }}</text>
							
							<!-- 未使用状态显示去使用按钮 -->
							<button 
								v-if="coupon.status === 'unused'" 
								class="btn-use"
								@click.stop="useCoupon(coupon)"
							>去使用</button>
							
							<!-- 已使用/已过期状态显示标记 -->
							<view v-else class="status-seal">
								{{ coupon.status === 'used' ? '已核销' : '已过期' }}
							</view>
						</view>
						<view class="mc-circle"></view>
					</view>

					<!-- 空状态 -->
					<view v-if="filteredMyCoupons.length === 0" class="empty-state">
						<text class="empty-icon">📭</text>
						<text class="empty-text">券包空空如也，快去抢神券！</text>
						<button class="empty-btn" @click="switchView(0)">去领券中心</button>
					</view>
				</view>
			</view>
		</scroll-view>

		<!-- Toast 提示 -->
		<view class="toast" :class="{ show: toastVisible }">
			<text class="toast-icon">{{ toastType === 'success' ? '✓' : 'ℹ' }}</text>
			<text class="toast-msg">{{ toastMessage }}</text>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import * as couponApi from '@/api/coupon'

// ==================== 状态管理 ====================
const currentView = ref(0) // 0: 领券中心, 1: 我的卡包
const walletTab = ref('all') // all, unused, used, expired
const searchKeyword = ref('')
const loading = ref(false)

// 数据
const availableCoupons = ref([]) // 可领取的优惠券
const myCoupons = ref([]) // 我的优惠券
const seckillActivity = ref(null) // 秒杀活动

// 倒计时
const countdown = ref({ hours: '00', minutes: '00', seconds: '00' })
let countdownTimer = null

// Toast
const toastVisible = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

// ==================== 自动滚动状态管理 ====================
// 自动滚动相关状态
const scrollLeft = ref(0)              // 当前滚动位置 (px)
const isAutoScrolling = ref(false)     // 是否正在自动滚动
const isTouching = ref(false)          // 用户是否正在触摸
const seckillScrollLeft = ref(0)       // 秒杀区域滚动位置
let scrollTimer = null                 // 自动滚动定时器
let resumeTimer = null                 // 恢复滚动定时器
let scrollDebounceTimer = null         // 滚动防抖定时器

// 配置常量
const AUTO_SCROLL_INTERVAL = 3000      // 自动滚动间隔 (毫秒)
const RESUME_DELAY = 3000              // 恢复滚动延迟 (毫秒)
const SCROLL_DURATION = 500            // 滚动动画时长 (毫秒)
const CARD_WIDTH_RPX = 244             // 单个卡片宽度 (220rpx + 24rpx margin)

// ==================== 计算属性 ====================
// 滑块样式
const sliderStyle = computed(() => {
	return {
		transform: `translateX(${currentView.value === 0 ? '0' : '100%'})`
	}
})

// 筛选后的可领取优惠券（排除代金券，代金券显示在秒杀专区）
const filteredAvailableCoupons = computed(() => {
	// 先过滤掉代金券（type=3），代金券显示在秒杀专区
	let coupons = availableCoupons.value.filter(c => c.type !== 3)
	
	if (!searchKeyword.value) return coupons
	const keyword = searchKeyword.value.toLowerCase()
	return coupons.filter(c => 
		c.title.toLowerCase().includes(keyword) || 
		(c.shopName && c.shopName.toLowerCase().includes(keyword))
	)
})

// 代金券列表（显示在秒杀专区）
const voucherCoupons = computed(() => {
	// 筛选出代金券（type=3）
	let coupons = availableCoupons.value.filter(c => c.type === 3)
	
	if (!searchKeyword.value) return coupons
	const keyword = searchKeyword.value.toLowerCase()
	return coupons.filter(c => 
		c.title.toLowerCase().includes(keyword) || 
		(c.shopName && c.shopName.toLowerCase().includes(keyword))
	)
})

// 筛选后的我的优惠券
const filteredMyCoupons = computed(() => {
	if (walletTab.value === 'all') return myCoupons.value
	return myCoupons.value.filter(c => c.status === walletTab.value)
})

// 计算秒杀卡片总数
const seckillCardCount = computed(() => {
	return seckillActivity.value?.coupons?.length || 0
})

// 计算最大滚动距离
const maxScrollLeft = computed(() => {
	if (seckillCardCount.value <= 1) return 0
	const cardWidthPx = getCardWidthInPx()
	return cardWidthPx * (seckillCardCount.value - 1)
})

// ==================== 生命周期 ====================
onLoad(() => {
	fetchAvailableCoupons()
	fetchSeckillActivities()
})

onShow(() => {
	// 每次页面显示时刷新数据，确保状态正确
	fetchAvailableCoupons()
	if (currentView.value === 1) {
		fetchMyCoupons()
	}
})

onMounted(() => {
	startCountdown()
	// 启动自动滚动
	startAutoScroll()
})

onUnmounted(() => {
	// 清理所有资源
	cleanup()
})

// ==================== 视图切换 ====================
const switchView = (index) => {
	currentView.value = index
	if (index === 1) {
		fetchMyCoupons()
	}
}

// ==================== 搜索功能 ====================
const focusSearch = () => {
	// 搜索框获取焦点
}

const handleSearch = () => {
	// 搜索逻辑已通过 computed 实现
}

const clearSearch = () => {
	searchKeyword.value = ''
}

// ==================== 数据获取 ====================
// 获取可领取优惠券
const fetchAvailableCoupons = async () => {
	if (loading.value) return
	loading.value = true
	
	try {
		const data = await couponApi.getAvailableCoupons(1, 100, searchKeyword.value)
		console.log('获取优惠券响应:', data)
		// 后端返回的是 PageResult，需要从 list 字段获取数据
		if (data && data.list) {
			availableCoupons.value = data.list
		} else if (Array.isArray(data)) {
			availableCoupons.value = data
		} else {
			availableCoupons.value = []
		}
		console.log('可领取优惠券:', availableCoupons.value)
	} catch (e) {
		console.error('获取优惠券失败:', e)
		// 不显示错误提示，避免影响用户体验
		availableCoupons.value = []
	} finally {
		loading.value = false
	}
}

// 获取秒杀活动
const fetchSeckillActivities = async () => {
	try {
		const data = await couponApi.getSeckillActivities(2) // 2表示进行中的活动
		if (data && data.length > 0) {
			// 取第一个进行中的活动
			seckillActivity.value = data[0]
		} else {
			seckillActivity.value = null
		}
		
		updateCountdown()
	} catch (e) {
		console.error('获取秒杀活动失败:', e)
		seckillActivity.value = null
	}
}

// 获取我的优惠券
const fetchMyCoupons = async () => {
	const token = uni.getStorageSync('token')
	if (!token) {
		myCoupons.value = []
		return
	}
	
	try {
		const data = await couponApi.getMyCoupons(walletTab.value, 1, 100)
		console.log('获取我的优惠券响应:', data)
		// 后端返回的是 PageResult，需要从 list 字段获取数据
		if (data && data.list) {
			myCoupons.value = data.list
		} else if (Array.isArray(data)) {
			myCoupons.value = data
		} else {
			myCoupons.value = []
		}
		console.log('我的优惠券:', myCoupons.value)
	} catch (e) {
		console.error('获取我的优惠券失败:', e)
		myCoupons.value = []
	}
}

// ==================== 倒计时 ====================
const startCountdown = () => {
	countdownTimer = setInterval(() => {
		updateCountdown()
	}, 1000)
}

const updateCountdown = () => {
	if (!seckillActivity.value || !seckillActivity.value.endTime) {
		countdown.value = { hours: '00', minutes: '00', seconds: '00' }
		return
	}
	
	const now = new Date().getTime()
	const end = new Date(seckillActivity.value.endTime).getTime()
	const diff = Math.max(0, end - now)
	
	const hours = Math.floor(diff / (1000 * 60 * 60))
	const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
	const seconds = Math.floor((diff % (1000 * 60)) / 1000)
	
	countdown.value = {
		hours: String(hours).padStart(2, '0'),
		minutes: String(minutes).padStart(2, '0'),
		seconds: String(seconds).padStart(2, '0')
	}
}

// ==================== 秒杀相关 ====================
const getProgress = (item) => {
	if (!item.seckillStock) return 100
	return Math.round(((item.seckillStock - item.remainStock) / item.seckillStock) * 100)
}

const getSeckillBtnText = (item) => {
	if (item.remainStock <= 0) return '已抢光'
	if (item.remainStock <= 5) return `仅剩${item.remainStock}张`
	return '立即抢'
}

const handleSeckillClick = (item) => {
	console.log('秒杀券点击:', item)
}

const claimSeckillCoupon = async (item) => {
	if (item.remainStock <= 0) {
		showToast('优惠券已抢光', 'error')
		return
	}
	
	const token = uni.getStorageSync('token')
	if (!token) {
		showToast('请先登录', 'error')
		setTimeout(() => {
			uni.navigateTo({ url: '/pages/login/login' })
		}, 1500)
		return
	}
	
	try {
		const response = await couponApi.claimSeckillCoupon(seckillActivity.value.id, item.id)
		if (response.code === 200) {
			item.remainStock--
			showToast('领取成功！已存入卡包', 'success')
			fetchMyCoupons()
		} else {
			showToast(response.message || '领取失败', 'error')
		}
	} catch (e) {
		showToast(e.message || '领取失败', 'error')
	}
}

// ==================== 优惠券相关 ====================
/**
 * 根据优惠券类型格式化显示值
 * type=1 满减券：显示 "¥{amount}"
 * type=2 折扣券：显示 "{discount*10}折"
 * type=3 代金券：显示 "¥{amount}"
 */
const formatCouponValue = (coupon) => {
	if (!coupon) return ''
	
	const type = coupon.type
	if (type === 2 && coupon.discount) {
		// 折扣券：0.8 显示为 "8折"，0.75 显示为 "7.5折"
		const discountValue = coupon.discount * 10
		return discountValue % 1 === 0 ? `${discountValue}折` : `${discountValue.toFixed(1)}折`
	} else if (coupon.amount) {
		// 满减券或代金券
		return `¥${coupon.amount}`
	}
	return ''
}

/**
 * 根据优惠券类型格式化使用条件
 * type=1 满减券：显示 "满{minAmount}可用"
 * type=2 折扣券：显示 "满{minAmount}可用"
 * type=3 代金券：显示 "无门槛"
 */
const formatCouponCondition = (coupon) => {
	if (!coupon) return ''
	
	// 如果后端已经返回了condition字段，直接使用
	if (coupon.condition) return coupon.condition
	
	const type = coupon.type
	const minAmount = coupon.minAmount || 0
	
	if (type === 3) {
		// 代金券无门槛
		return '无门槛'
	} else if (minAmount > 0) {
		return `满${minAmount}可用`
	} else {
		return '无门槛'
	}
}

/**
 * 获取优惠券类型标签
 */
const getCouponTypeLabel = (type) => {
	switch (type) {
		case 1: return '满减券'
		case 2: return '折扣券'
		case 3: return '代金券'
		default: return '优惠券'
	}
}

const getCouponBtnClass = (status) => {
	return {
		'btn-available': status === 'available',
		'btn-claimed': status === 'claimed',
		'btn-sold-out': status === 'sold_out',
		'btn-not-started': status === 'not_started'
	}
}

const getVoucherBtnClass = (status) => {
	return {
		'voucher-available': status === 'available',
		'voucher-claimed': status === 'claimed',
		'voucher-sold-out': status === 'sold_out',
		'voucher-not-started': status === 'not_started'
	}
}

const getCouponBtnText = (status) => {
	if (status === 'available') return '领取'
	if (status === 'claimed') return '已领取'
	if (status === 'sold_out') return '已抢光'
	if (status === 'not_started') return '未开始'
	return '领取'
}

const handleCouponClick = (coupon) => {
	console.log('优惠券点击:', coupon)
}

const claimCoupon = async (coupon) => {
	if (coupon.status !== 'available') {
		if (coupon.status === 'claimed') {
			showToast('您已领取过该优惠券', 'error')
		} else if (coupon.status === 'sold_out') {
			showToast('优惠券已抢光', 'error')
		} else if (coupon.status === 'not_started') {
			showToast('优惠券活动尚未开始', 'error')
		}
		return
	}
	
	const token = uni.getStorageSync('token')
	if (!token) {
		showToast('请先登录', 'error')
		setTimeout(() => {
			uni.navigateTo({ url: '/pages/login/login' })
		}, 1500)
		return
	}
	
	try {
		const response = await couponApi.receiveCoupon(coupon.id)
		console.log('领取优惠券响应:', response)
		
		// 处理成功响应 - 更新本地状态
		// 使用 Vue 3 的响应式更新方式
		coupon.status = 'claimed'
		
		// 强制更新数组以触发响应式
		// 在 availableCoupons 中找到对应的优惠券并更新
		const index = availableCoupons.value.findIndex(c => c.id === coupon.id)
		if (index !== -1) {
			availableCoupons.value[index].status = 'claimed'
		}
		
		// 在 voucherCoupons 中也更新（如果是代金券）
		if (coupon.type === 3) {
			const voucherIndex = availableCoupons.value.findIndex(c => c.id === coupon.id && c.type === 3)
			if (voucherIndex !== -1) {
				availableCoupons.value[voucherIndex].status = 'claimed'
			}
		}
		
		showToast('领取成功！已存入券包', 'success')
		
		// 刷新我的优惠券列表
		fetchMyCoupons()
		
		// 延迟刷新可领取优惠券列表，确保后端状态已更新
		setTimeout(() => {
			fetchAvailableCoupons()
		}, 500)
	} catch (e) {
		console.error('领取优惠券失败:', e)
		showToast(e.message || '领取失败', 'error')
	}
}

// ==================== 我的卡包相关 ====================
const filterWallet = (tab) => {
	walletTab.value = tab
}

const handleMyCouponClick = (coupon) => {
	if (coupon.status === 'unused') {
		// 显示券码
		uni.showModal({
			title: '优惠券码',
			content: coupon.code,
			showCancel: false,
			confirmText: '复制券码',
			success: (res) => {
				if (res.confirm) {
					uni.setClipboardData({
						data: coupon.code,
						success: () => {
							showToast('券码已复制', 'success')
						}
					})
				}
			}
		})
	}
}

const useCoupon = (coupon) => {
	if (coupon.shopId) {
		uni.navigateTo({
			url: `/pages/shop-detail/shop-detail?id=${coupon.shopId}`
		})
	} else {
		showToast('该优惠券适用于全部商家', 'info')
	}
}

// ==================== Toast ====================
const showToast = (message, type = 'success') => {
	toastMessage.value = message
	toastType.value = type
	toastVisible.value = true
	
	setTimeout(() => {
		toastVisible.value = false
	}, 2000)
}

// ==================== 自动滚动功能 ====================
// 计算卡片宽度（rpx转px）
const getCardWidthInPx = () => {
	try {
		const systemInfo = uni.getSystemInfoSync()
		const screenWidth = systemInfo.screenWidth || 375 // 默认值
		// rpx转px: px = rpx * screenWidth / 750
		return (CARD_WIDTH_RPX * screenWidth) / 750
	} catch (e) {
		console.error('获取屏幕信息失败:', e)
		// 返回默认值（基于375px屏幕宽度）
		return (CARD_WIDTH_RPX * 375) / 750
	}
}

// 滚动到下一个卡片
const scrollToNext = () => {
	// 边界检查：确保有秒杀活动和优惠券
	if (seckillCardCount.value === 0) {
		return
	}
	
	// 如果只有一个卡片，不需要滚动
	if (seckillCardCount.value === 1) {
		return
	}
	
	try {
		const cardWidthPx = getCardWidthInPx()
		const maxScroll = maxScrollLeft.value
		
		// 边界检查：确保当前位置有效
		if (seckillScrollLeft.value < 0) {
			seckillScrollLeft.value = 0
			return
		}
		
		// 计算下一个位置
		let nextPosition = seckillScrollLeft.value + cardWidthPx
		
		// 如果到达末尾，循环回到开始
		if (nextPosition > maxScroll) {
			nextPosition = 0
		}
		
		// 边界检查：确保不超出最大滚动距离
		if (nextPosition > maxScroll) {
			nextPosition = maxScroll
		}
		
		seckillScrollLeft.value = nextPosition
	} catch (e) {
		console.error('滚动计算错误:', e)
		// 出错时重置到开始位置
		seckillScrollLeft.value = 0
	}
}

// 启动自动滚动
const startAutoScroll = () => {
	try {
		// 清除现有定时器
		if (scrollTimer) {
			clearInterval(scrollTimer)
			scrollTimer = null
		}
		
		// 边界检查：只有当有秒杀券且数量大于1时才启动自动滚动
		if (seckillCardCount.value <= 1) {
			return
		}
		
		isAutoScrolling.value = true
		scrollTimer = setInterval(() => {
			try {
				scrollToNext()
			} catch (e) {
				console.error('自动滚动错误:', e)
				// 出错时停止自动滚动
				stopAutoScroll()
			}
		}, AUTO_SCROLL_INTERVAL)
	} catch (e) {
		console.error('启动自动滚动失败:', e)
		isAutoScrolling.value = false
	}
}

// 停止自动滚动
const stopAutoScroll = () => {
	if (scrollTimer) {
		clearInterval(scrollTimer)
		scrollTimer = null
	}
	isAutoScrolling.value = false
}

// 清理资源
const cleanup = () => {
	// 清除所有定时器
	if (scrollTimer) {
		clearInterval(scrollTimer)
		scrollTimer = null
	}
	if (resumeTimer) {
		clearTimeout(resumeTimer)
		resumeTimer = null
	}
	if (scrollDebounceTimer) {
		clearTimeout(scrollDebounceTimer)
		scrollDebounceTimer = null
	}
	if (countdownTimer) {
		clearInterval(countdownTimer)
		countdownTimer = null
	}
	
	// 重置状态
	isAutoScrolling.value = false
	isTouching.value = false
}

// 处理触摸开始
const handleTouchStart = () => {
	isTouching.value = true
	stopAutoScroll()
	
	// 清除恢复定时器
	if (resumeTimer) {
		clearTimeout(resumeTimer)
		resumeTimer = null
	}
}

// 处理触摸结束
const handleTouchEnd = () => {
	isTouching.value = false
	
	// 清除之前的恢复定时器
	if (resumeTimer) {
		clearTimeout(resumeTimer)
		resumeTimer = null
	}
	
	// 3秒后恢复自动滚动
	resumeTimer = setTimeout(() => {
		resumeAutoScroll()
	}, RESUME_DELAY)
}

// 恢复自动滚动
const resumeAutoScroll = () => {
	// 只有在用户未触摸时才恢复
	if (!isTouching.value) {
		startAutoScroll()
	}
}

// 处理手动滚动（带防抖）
const handleScroll = (e) => {
	// 清除之前的防抖定时器
	if (scrollDebounceTimer) {
		clearTimeout(scrollDebounceTimer)
		scrollDebounceTimer = null
	}
	
	// 防抖处理：300ms后执行
	scrollDebounceTimer = setTimeout(() => {
		// 用户手动滚动时，停止自动滚动
		if (!isTouching.value && isAutoScrolling.value) {
			stopAutoScroll()
			
			// 清除之前的恢复定时器
			if (resumeTimer) {
				clearTimeout(resumeTimer)
				resumeTimer = null
			}
			
			// 3秒后恢复自动滚动
			resumeTimer = setTimeout(() => {
				resumeAutoScroll()
			}, RESUME_DELAY)
		}
	}, 300)
}
</script>


<style lang="scss" scoped>
// ==================== 全局变量 ====================
$primary: #ff6b00;
$danger: #ff4757;
$primary-grad: linear-gradient(135deg, #ff8f00, #ff6b00);
$danger-grad: linear-gradient(135deg, #ff6b81, #ff4757);
$bg-body: #f5f7fa;
$text-main: #333;
$text-sub: #999;

.container {
	background: $bg-body;
	min-height: 100vh;
	display: flex;
	flex-direction: column;
}

// ==================== 顶部导航栏 ====================
.custom-navbar {
	background: #fff;
	padding: 80rpx 30rpx 30rpx;
	display: flex;
	justify-content: center;
	align-items: center;
	box-shadow: 0 2rpx 0 rgba(0, 0, 0, 0.05);
}

.segment-control {
	background: #f8f9fb;
	border: 1rpx solid rgba(0, 0, 0, 0.08);
	border-radius: 48rpx;
	padding: 8rpx;
	display: flex;
	position: relative;
	box-shadow: inset 0 2rpx 6rpx rgba(0, 0, 0, 0.03);
}

.segment-slider {
	position: absolute;
	top: 8rpx;
	left: 8rpx;
	width: calc(50% - 8rpx);
	height: calc(100% - 16rpx);
	background: #fff;
	border-radius: 40rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.08);
	transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
	z-index: 1;
}

.segment-btn {
	padding: 16rpx 48rpx;
	font-size: 30rpx;
	font-weight: 600;
	color: $text-sub;
	border-radius: 40rpx;
	z-index: 2;
	transition: all 0.3s ease;
	position: relative;
	
	&.active {
		color: $primary;
		font-weight: 700;
	}
}

// ==================== 主内容区域 ====================
.main-container {
	flex: 1;
	overflow-y: auto;
}

.view-page {
	animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
	from {
		opacity: 0;
		transform: translateY(10rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

// ==================== 搜索框 ====================
.search-box-wrapper {
	padding: 30rpx;
	background: #fff;
}

.search-box {
	background: #f5f5f5;
	height: 76rpx;
	border-radius: 38rpx;
	display: flex;
	align-items: center;
	padding: 0 30rpx;
}

.search-icon {
	font-size: 28rpx;
	margin-right: 16rpx;
	color: #bbb;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: $text-main;
}

.search-placeholder {
	color: #999;
}

.clear-icon {
	font-size: 24rpx;
	color: #999;
	padding: 10rpx;
}

// ==================== 秒杀专区 ====================
.seckill-zone {
	margin: 30rpx;
	padding: 40rpx 30rpx;
	border-radius: 32rpx;
	background: linear-gradient(180deg, rgba(255, 71, 87, 0.08) 0%, rgba(255, 255, 255, 0) 100%), #fff;
	box-shadow: 0 8rpx 30rpx rgba(255, 71, 87, 0.06);
	position: relative;
	overflow: hidden;
}

.hot-tag {
	position: absolute;
	top: -20rpx;
	right: -50rpx;
	background: $danger-grad;
	color: #fff;
	font-size: 20rpx;
	padding: 30rpx 60rpx 10rpx;
	transform: rotate(45deg);
	font-weight: bold;
}

.zone-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 36rpx;
}

.zone-title {
	font-size: 36rpx;
	font-weight: 800;
	color: $text-main;
	display: flex;
	align-items: center;
	gap: 16rpx;
	
	.fire-icon {
		font-size: 40rpx;
	}
}

.timer {
	display: flex;
	align-items: center;
	gap: 8rpx;
}

.timer-block {
	background: #333;
	color: #f0e6d2;
	padding: 6rpx 10rpx;
	border-radius: 12rpx;
	font-size: 26rpx;
	font-weight: bold;
	font-family: monospace;
	box-shadow: 0 4rpx 10rpx rgba(0, 0, 0, 0.2);
}

.timer-sep {
	font-weight: bold;
	color: #333;
}

.seckill-scroll {
	display: flex;
	white-space: nowrap;
	padding: 10rpx 10rpx 30rpx;
}

.seckill-card {
	display: inline-flex;
	flex-direction: column;
	align-items: center;
	width: 220rpx;
	background: #fff;
	border-radius: 24rpx;
	padding: 30rpx 20rpx;
	margin-right: 24rpx;
	box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.06);
	transition: transform 0.2s;
	
	&:active {
		transform: scale(0.98);
	}
	
	&.sold-out {
		opacity: 0.7;
	}
}

.sk-price {
	color: $danger;
	font-weight: 900;
	font-size: 36rpx;
	margin: 12rpx 0;
	text-shadow: 0 4rpx 10rpx rgba(255, 71, 87, 0.15);
	
	.price-symbol {
		font-size: 24rpx;
	}
	
	&.disabled {
		color: #999;
	}
}

.sk-name {
	font-size: 22rpx;
	color: #555;
	margin-bottom: 8rpx;
	max-width: 180rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.sk-progress {
	width: 100%;
	height: 16rpx;
	background: #f0f0f0;
	border-radius: 8rpx;
	margin: 16rpx 0 20rpx;
	overflow: hidden;
	box-shadow: inset 0 2rpx 4rpx rgba(0, 0, 0, 0.05);
}

.sk-fill {
	height: 100%;
	background: $danger-grad;
	border-radius: 8rpx;
}

.btn-grab {
	font-size: 24rpx;
	font-weight: 600;
	background: $danger-grad;
	color: #fff;
	border: none;
	padding: 12rpx 36rpx;
	border-radius: 40rpx;
	box-shadow: 0 8rpx 20rpx rgba(255, 71, 87, 0.3);
	
	&.disabled {
		background: #e0e0e0;
		color: #999;
		box-shadow: none;
	}
}

// ==================== 代金券专区样式 ====================
.voucher-zone {
	background: linear-gradient(180deg, rgba(255, 107, 0, 0.08) 0%, rgba(255, 255, 255, 0) 100%), #fff;
	box-shadow: 0 8rpx 30rpx rgba(255, 107, 0, 0.06);
}

.voucher-tag {
	background: $primary-grad;
}

.voucher-tip {
	font-size: 24rpx;
	color: $primary;
	background: rgba(255, 107, 0, 0.1);
	padding: 6rpx 16rpx;
	border-radius: 20rpx;
	font-weight: 500;
}

.voucher-card {
	background: linear-gradient(180deg, #fffbf5 0%, #fff 100%);
	border: 2rpx solid rgba(255, 107, 0, 0.1);
}

.voucher-price {
	color: $primary !important;
	text-shadow: 0 4rpx 10rpx rgba(255, 107, 0, 0.15) !important;
}

.voucher-shop {
	font-size: 20rpx;
	color: #999;
	margin: 8rpx 0 16rpx;
	max-width: 180rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.voucher-btn {
	background: $primary-grad !important;
	box-shadow: 0 8rpx 20rpx rgba(255, 107, 0, 0.3) !important;
	
	&.voucher-claimed {
		background: #f0f0f0 !important;
		color: #999 !important;
		box-shadow: none !important;
	}
	
	&.voucher-sold-out {
		background: #e0e0e0 !important;
		color: #999 !important;
		box-shadow: none !important;
	}
	
	&.voucher-not-started {
		background: #ffd699 !important;
		color: #996600 !important;
		box-shadow: none !important;
	}
}

// ==================== 好店神券列表 ====================
.shop-coupon-list {
	padding: 0 30rpx 30rpx;
}

.list-title {
	font-size: 34rpx;
	font-weight: bold;
	margin: 30rpx 0;
	border-left: 8rpx solid $primary;
	padding-left: 24rpx;
}

.coupon-row {
	display: flex;
	background: #fff;
	height: 180rpx;
	border-radius: 24rpx;
	margin-bottom: 24rpx;
	box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.03);
	overflow: hidden;
	position: relative;
}

.coupon-circle {
	position: absolute;
	top: 50%;
	left: -16rpx;
	transform: translateY(-50%);
	width: 32rpx;
	height: 32rpx;
	background: $bg-body;
	border-radius: 50%;
}

.cr-left {
	width: 180rpx;
	background: linear-gradient(180deg, #fffbf0 0%, #fff 100%);
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	border-right: 2rpx dashed #ffe0b2;
}

.cr-logo {
	width: 72rpx;
	height: 72rpx;
	border-radius: 50%;
	background: #eee;
	margin-bottom: 10rpx;
	border: 2rpx solid #f0f0f0;
}

.cr-name {
	font-size: 24rpx;
	color: #666;
	font-weight: 500;
	max-width: 160rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.cr-right {
	flex: 1;
	padding: 24rpx 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.cr-info {
	flex: 1;
}

.cr-title {
	display: block;
	font-size: 30rpx;
	font-weight: bold;
	color: $text-main;
	margin-bottom: 12rpx;
}

.cr-condition {
	display: inline-block;
	font-size: 22rpx;
	color: #999;
	background: #f5f5f5;
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
}

.cr-action {
	text-align: right;
}

.price-big {
	color: $primary;
	font-size: 48rpx;
	font-weight: 900;
	line-height: 1;
	
	.price-symbol {
		font-size: 28rpx;
	}
}

.btn-get {
	border: none;
	color: #fff;
	background: $primary-grad;
	padding: 12rpx 32rpx;
	border-radius: 40rpx;
	font-size: 24rpx;
	font-weight: bold;
	margin-top: 12rpx;
	box-shadow: 0 4rpx 12rpx rgba(255, 107, 0, 0.2);
	
	&.btn-claimed {
		background: #f0f0f0;
		color: #999;
		box-shadow: none;
	}
	
	&.btn-sold-out {
		background: #e0e0e0;
		color: #999;
		box-shadow: none;
	}
	
	&.btn-not-started {
		background: #ffd699;
		color: #996600;
		box-shadow: none;
	}
}

// ==================== 图标按钮样式 ====================
.btn-icon-wrapper {
	display: flex;
	justify-content: center;
	align-items: center;
	margin-top: 12rpx;
}

.btn-icon {
	width: 80rpx;
	height: 80rpx;
}

.voucher-btn-wrapper {
	display: flex;
	justify-content: center;
	align-items: center;
	margin-top: 16rpx;
}

.voucher-btn-icon {
	width: 72rpx;
	height: 72rpx;
}

// 已领取状态的卡片样式
.seckill-card.is-claimed {
	opacity: 0.7;
	
	.voucher-price {
		color: #999 !important;
	}
}

// ==================== 我的卡包 ====================
.wallet-tabs {
	display: flex;
	justify-content: space-around;
	background: #fff;
	padding: 24rpx 0 0;
	border-bottom: 2rpx solid #f0f0f0;
	position: sticky;
	top: 0;
	z-index: 10;
}

.w-tab {
	font-size: 30rpx;
	color: #666;
	padding: 16rpx 0 24rpx;
	position: relative;
	font-weight: 500;
	
	&.active {
		color: $primary;
		font-weight: 700;
		
		&::after {
			content: '';
			position: absolute;
			bottom: 0;
			left: 50%;
			transform: translateX(-50%);
			width: 48rpx;
			height: 6rpx;
			background: $primary;
			border-radius: 6rpx;
		}
	}
}

.my-coupon-list {
	padding: 40rpx 30rpx;
	min-height: 600rpx;
}

.my-card {
	display: flex;
	background: #fff;
	border-radius: 32rpx;
	overflow: hidden;
	margin-bottom: 30rpx;
	box-shadow: 0 8rpx 30rpx rgba(0, 0, 0, 0.05);
	position: relative;
	transition: transform 0.2s;
	
	&:active {
		transform: scale(0.99);
	}
	
	&.card-disabled {
		opacity: 0.7;
		filter: grayscale(0.5);
	}
}

.mc-circle {
	position: absolute;
	right: -16rpx;
	top: 50%;
	transform: translateY(-50%);
	width: 32rpx;
	height: 32rpx;
	background: $bg-body;
	border-radius: 50%;
}

.mc-left {
	width: 210rpx;
	background: $primary-grad;
	color: #fff;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	padding: 30rpx;
	text-shadow: 0 4rpx 4rpx rgba(0, 0, 0, 0.1);
}

.mc-amount {
	font-size: 52rpx;
	font-weight: 900;
}

.mc-label {
	font-size: 22rpx;
	font-weight: 500;
	opacity: 0.9;
}

.mc-right {
	flex: 1;
	padding: 36rpx;
	display: flex;
	flex-direction: column;
	justify-content: center;
	position: relative;
}

.mc-title {
	font-size: 32rpx;
	font-weight: bold;
	margin-bottom: 16rpx;
	color: $text-main;
}

.mc-shop {
	font-size: 26rpx;
	color: #666;
	display: flex;
	align-items: center;
	gap: 12rpx;
	font-weight: 500;
	
	.shop-icon {
		color: $primary;
	}
}

.mc-time {
	font-size: 22rpx;
	color: #ccc;
	margin-top: 20rpx;
}

.btn-use {
	position: absolute;
	right: 30rpx;
	bottom: 30rpx;
	background: $primary-grad;
	color: #fff;
	border: none;
	padding: 12rpx 36rpx;
	border-radius: 40rpx;
	font-size: 26rpx;
	font-weight: bold;
	box-shadow: 0 6rpx 16rpx rgba(255, 107, 0, 0.25);
}

.status-seal {
	position: absolute;
	right: 40rpx;
	bottom: 40rpx;
	color: #bbb;
	font-weight: 700;
	transform: rotate(-15deg);
	border: 4rpx solid #bbb;
	padding: 8rpx 20rpx;
	border-radius: 12rpx;
	font-size: 24rpx;
}

// ==================== 空状态 ====================
.empty-center,
.empty-state {
	text-align: center;
	padding: 150rpx 0;
}

.empty-icon {
	display: block;
	font-size: 140rpx;
	margin-bottom: 40rpx;
}

.empty-text {
	display: block;
	font-size: 28rpx;
	color: #999;
	margin-bottom: 50rpx;
}

.empty-btn {
	border: none;
	color: #fff;
	background: $primary-grad;
	padding: 20rpx 56rpx;
	border-radius: 50rpx;
	font-size: 30rpx;
	font-weight: bold;
	box-shadow: 0 8rpx 20rpx rgba(255, 107, 0, 0.3);
}

// ==================== Toast ====================
.toast {
	position: fixed;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%) scale(0.9);
	background: rgba(0, 0, 0, 0.85);
	color: #fff;
	padding: 32rpx 48rpx;
	border-radius: 24rpx;
	font-size: 30rpx;
	opacity: 0;
	pointer-events: none;
	transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
	z-index: 999;
	font-weight: 500;
	display: flex;
	align-items: center;
	gap: 16rpx;
	
	&.show {
		opacity: 1;
		transform: translate(-50%, -50%) scale(1);
	}
}

.toast-icon {
	font-size: 36rpx;
	color: #2ed573;
}

.toast-msg {
	font-size: 30rpx;
}
</style>
