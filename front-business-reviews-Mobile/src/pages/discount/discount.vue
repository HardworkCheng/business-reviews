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
          <view class="search-box">
            <text class="search-icon">🔍</text>
            <input 
              v-model="searchKeyword"
              class="search-input"
              placeholder="搜索神券、商家..."
              placeholder-class="search-placeholder"
              @input="handleSearch"
              @confirm="handleSearch"
            />
            <text v-if="searchKeyword" class="clear-icon" @click="clearSearch">✕</text>
          </view>
        </view>

        <!-- 原秒杀活动区域（保留，如果有活动数据） -->
        <SeckillZone 
          v-if="seckillActivity && seckillActivity.coupons && seckillActivity.coupons.length > 0"
          :activity="seckillActivity" 
          :countdown="countdown"
          @click-item="handleSeckillClick"
          @claim="onClaimSeckill"
        />

        <!-- 秒杀券专区（距离结束时间不足一天的优惠券） -->
        <FlashSaleZone 
          :coupons="flashSaleCoupons"
          @click-item="handleCouponClick"
          @claim="onClaimCoupon"
        />

        <!-- 好店神券列表（距离结束时间超过一天的优惠券） -->
        <CouponList 
          :coupons="normalCoupons"
          :empty-text="searchKeyword ? '暂无相关优惠券' : '暂无可领取的优惠券'"
          @click-item="handleCouponClick"
          @claim="onClaimCoupon"
        />
      </view>

      <!-- 我的卡包视图 -->
      <view v-show="currentView === 1" class="view-page" :class="{ show: currentView === 1 }">
        <MyCouponList 
          :coupons="myCoupons"
          @tab-change="onWalletTabChange"
          @go-center="switchView(0)"
        />
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
import { ref, computed, onMounted } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { useCountdown } from '../../composables/useCountdown'
import { useCoupons } from '../../composables/useCoupons'
import SeckillZone from '../../components/discount/SeckillZone.vue'
import FlashSaleZone from '../../components/discount/FlashSaleZone.vue'
import CouponList from '../../components/discount/CouponList.vue'
import MyCouponList from '../../components/discount/MyCouponList.vue'

// Logic
const currentView = ref(0)
const searchKeyword = ref('')
const searchTimer = ref(null)

// Toast State
const toastVisible = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

// Composables
const { countdown, startCountdown } = useCountdown()
const { 
  loading, 
  availableCoupons, 
  myCoupons, 
  seckillActivity, 
  fetchAvailableCoupons, 
  fetchSeckillActivities, 
  fetchMyCoupons, 
  claimCoupon, 
  claimSeckillCoupon 
} = useCoupons()

// Computed Properties
const sliderStyle = computed(() => ({
  transform: `translateX(${currentView.value === 0 ? '0' : '100%'})`
}))

// 一天的毫秒数
const ONE_DAY_MS = 24 * 60 * 60 * 1000

/**
 * 检查优惠券是否在24小时内过期
 */
const isExpiringWithin24Hours = (coupon) => {
  if (!coupon.endTime) return false
  const now = new Date().getTime()
  const endTime = new Date(coupon.endTime).getTime()
  const diff = endTime - now
  return diff > 0 && diff <= ONE_DAY_MS
}

/**
 * 检查优惠券是否在24小时后过期
 */
const isExpiringAfter24Hours = (coupon) => {
  if (!coupon.endTime) return true // 没有结束时间的默认显示在普通列表
  const now = new Date().getTime()
  const endTime = new Date(coupon.endTime).getTime()
  const diff = endTime - now
  return diff > ONE_DAY_MS
}

/**
 * 秒杀券专区：距离结束时间不足一天的优惠券
 */
const flashSaleCoupons = computed(() => {
  let coupons = availableCoupons.value.filter(c => isExpiringWithin24Hours(c))
  
  if (!searchKeyword.value) return coupons
  const keyword = searchKeyword.value.toLowerCase()
  return coupons.filter(c => 
    c.title.toLowerCase().includes(keyword) || 
    (c.shopName && c.shopName.toLowerCase().includes(keyword))
  )
})

/**
 * 好店神券：距离结束时间超过一天的优惠券
 */
const normalCoupons = computed(() => {
  let coupons = availableCoupons.value.filter(c => isExpiringAfter24Hours(c))
  
  if (!searchKeyword.value) return coupons
  const keyword = searchKeyword.value.toLowerCase()
  return coupons.filter(c => 
    c.title.toLowerCase().includes(keyword) || 
    (c.shopName && c.shopName.toLowerCase().includes(keyword))
  )
})

// Methods
const switchView = (index) => {
  currentView.value = index
  if (index === 1) {
    onWalletTabChange('all')
  }
}

const handleSearch = () => {
    if (searchTimer.value) clearTimeout(searchTimer.value)
    searchTimer.value = setTimeout(() => {
        fetchAvailableCoupons(searchKeyword.value)
    }, 500)
}

const clearSearch = () => {
  searchKeyword.value = ''
  fetchAvailableCoupons('')
}

const onWalletTabChange = (tab) => {
    fetchMyCoupons(tab)
}

const showToast = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  toastVisible.value = true
  setTimeout(() => {
    toastVisible.value = false
  }, 2000)
}

// Handlers
const handleSeckillClick = (item) => {
    console.log('Click Seckill', item)
}

const handleCouponClick = (coupon) => {
    console.log('Click Coupon', coupon)
}

const onClaimSeckill = async (item) => {
    try {
        await claimSeckillCoupon(seckillActivity.value.id, item)
        showToast('领取成功！已存入卡包', 'success')
        if (currentView.value === 1) fetchMyCoupons()
    } catch (e) {
        showToast(e.message || '领取失败', 'error')
    }
}

const onClaimCoupon = async (coupon) => {
    try {
        const response = await claimCoupon(coupon)
        showToast('领取成功！已存入券包', 'success')
        if (currentView.value === 1) fetchMyCoupons()
    } catch (e) {
        showToast(e.message || '领取失败', 'error')
    }
}

// Lifecycle
onLoad(() => {
  fetchAvailableCoupons()
  fetchSeckillActivities().then(() => {
      startCountdown(() => seckillActivity.value?.endTime)
  })
})

onShow(() => {
  fetchAvailableCoupons()
  if (currentView.value === 1) {
    fetchMyCoupons()
  }
})

</script>

<style lang="scss" scoped>
.container {
  background: #f5f6fa;
  height: 100vh;
  display: flex;
  flex-direction: column;
}

// 顶部自定义导航栏
.custom-navbar {
  height: 88rpx;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: var(--status-bar-height);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1rpx 0 rgba(0,0,0,0.05);
}

.segment-control {
  width: 400rpx;
  height: 72rpx;
  background: #eef1f5;
  border-radius: 36rpx;
  display: flex;
  position: relative;
  padding: 4rpx;
}

.segment-slider {
  position: absolute;
  top: 6rpx;
  left: 6rpx;
  width: 50%;
  bottom: 6rpx;
  background: #fff;
  border-radius: 32rpx;
  box-shadow: 0 4rpx 12rpx rgba(0,0,0,0.08);
  transition: transform 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
}

.segment-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26rpx;
  color: #666;
  z-index: 1;
  transition: color 0.3s;
  
  &.active {
    color: #333;
    font-weight: 600;
  }
}

.main-container {
  flex: 1;
  height: 0;
}

.view-page {
  /* No special styles needed unless transition effects desired */
}

// 搜索框
.search-box-wrapper {
  padding: 20rpx;
  background: #fff;
}

.search-box {
  background: #f5f6fa;
  height: 72rpx;
  border-radius: 36rpx;
  display: flex;
  align-items: center;
  padding: 0 24rpx;
}

.search-icon {
  font-size: 28rpx;
  margin-right: 12rpx;
  color: #999;
}

.search-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
}

.clear-icon {
  font-size: 28rpx;
  color: #ccc;
  padding: 10rpx;
}

// Toast
.toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0,0,0,0.8);
  border-radius: 12rpx;
  padding: 24rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  z-index: 999;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.3s;
  
  &.show {
    opacity: 1;
  }
}

.toast-icon {
  font-size: 48rpx;
  color: #fff;
}

.toast-msg {
  font-size: 28rpx;
  color: #fff;
}
</style>
