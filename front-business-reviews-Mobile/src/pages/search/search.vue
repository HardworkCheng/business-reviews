<template>
  <view class="container">
    <view class="header">
      <view class="search-bar">
        <button class="back-btn" @click="goBack">←</button>
        <view class="location" @click="goToCitySelect">
          <image src="/static/icons/location.png" class="location-icon-img" mode="aspectFit"></image>
          <text class="location-text">{{ currentCity }}</text>
          <image src="/static/icons/arrow-down.png" class="location-arrow-img" mode="aspectFit"></image>
        </view>
        <view class="search-input-wrapper">
          <view class="search-input">
            <image src="/static/icons/search.png" class="search-icon-img" mode="aspectFit"></image>
            <input 
              type="text" 
              placeholder="搜索商户名或地点" 
              v-model="keyword"
              @confirm="onSearchConfirm"
            />
          </view>
        </view>
        <image 
          v-if="userAvatar" 
          :src="userAvatar" 
          class="user-avatar"
          mode="aspectFill"
          @click="goToProfile"
        ></image>
        <view v-else class="user-icon" @click="goToProfile">
          <text>👤</text>
        </view>
      </view>

      <FilterBar 
        v-model:selectedCategory="selectedCategory"
        v-model:sortField="sortField"
        v-model:sortOrder="sortOrder"
        @change="onFilterChange"
        ref="filterBarRef"
      />
    </view>
    
    <ShopResultList 
      :list="shopList"
      :loading="loading"
      @click-item="goToShopDetail"
    />
  </view>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { onLoad, onShow, onReachBottom } from '@dcloudio/uni-app'
import { getUserInfo } from '../../api/user'
import { useUserLocation } from '../../composables/useUserLocation'
import { useShopSearch } from '../../composables/useShopSearch'
import FilterBar from '../../components/search/FilterBar.vue'
import ShopResultList from '../../components/search/ShopResultList.vue'

// Composables
const { 
  currentCity, 
  userLatitude, 
  userLongitude, 
  getCurrentCity, 
  reLocation 
} = useUserLocation()

const {
  keyword,
  shopList,
  loading,
  hasNext,
  pageNum,
  selectedCategory,
  selectedCategoryId, // Note: FilterBar updates selectedCategory name, we might need ID lookup or sync
  sortField,
  sortOrder,
  fetchShopList,
  resetList
} = useShopSearch()

const userAvatar = ref('')
const filterBarRef = ref(null)

// Helper to map category Name to ID (Logic from original search.vue)
// We need the categories list here or accessible? 
// Original search.vue had local categories list. FilterBar has it now.
// We can ask FilterBar to emit value? 
// `FilterBar` emits `change` with `{type, value}`. Value is the object.
const onFilterChange = (event) => {
    if (event.type === 'category') {
        selectedCategoryId.value = event.value.id
    }
    
    // Trigger Refresh
    resetList()
    doFetch()
}

const doFetch = () => {
    fetchShopList(userLatitude.value, userLongitude.value)
}

const onSearchConfirm = () => {
    resetList()
    doFetch()
}

// Watch keyword debounce
let searchTimer = null
watch(keyword, () => {
    if (searchTimer) clearTimeout(searchTimer)
    searchTimer = setTimeout(() => {
        resetList()
        doFetch()
    }, 500)
    
    // Close dropdowns if searching?
    // filterBarRef.value?.closeAllDropdowns()
})

// Lifecycle
onLoad((options) => {
  if (options.category) {
    keyword.value = options.category
    selectedCategory.value = options.category
    // We need to set ID too. 
    // Optimization: Just rely on keyword search if passed via category? 
    // Original code: found ID from list.
    // We can assume backend handles keyword search correctly or we need that mapping.
    // For now, if keyword is set, use keyword search (handled in useShopSearch).
  }
  
  fetchUserInfo()
  getCurrentCity()
  
  // Initial Fetch
  doFetch()
})

onShow(() => {
    // Check city change logic
    const storedCity = uni.getStorageSync('selectedCity')
    if (storedCity && storedCity !== currentCity.value) {
        currentCity.value = storedCity
        resetList()
        doFetch()
    }
})

onReachBottom(() => {
    if (hasNext.value && !loading.value) {
        pageNum.value++
        doFetch()
    }
})

// Navigation
const goBack = () => uni.navigateBack()
const goToProfile = () => uni.switchTab({ url: '/pages/profile/profile' })
const goToShopDetail = (shop) => uni.navigateTo({ url: `/pages/shop-detail/shop-detail?id=${shop.id}` })

const goToCitySelect = () => {
  uni.showActionSheet({
    itemList: ['重新定位', '选择城市'],
    success: (res) => {
      if (res.tapIndex === 0) {
        reLocation()
      } else if (res.tapIndex === 1) {
        uni.navigateTo({ url: '/pages/city-select/city-select' })
      }
    }
  })
}

const fetchUserInfo = async () => {
  try {
    const result = await getUserInfo()
    if (result && result.avatar) {
      userAvatar.value = result.avatar
    }
  } catch (e) {
    console.error('Fetch User Info Error', e)
  }
}

// Watch user location update to refresh if sort is distance
watch([userLatitude, userLongitude], ([newLat, newLng]) => {
    if (sortField.value === 'distance' && newLat) {
        // Refresh only if we are waiting for it?
        // Or if distance sort is active, location update should trigger refresh?
        // Yes.
        resetList()
        doFetch()
    }
})

</script>

<style lang="scss" scoped>
.container {
  background: #f5f6fa;
  min-height: 100vh;
}

.header {
  background: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
}

.search-bar {
  display: flex;
  align-items: center;
  padding: 20rpx;
  /* status bar height padding handled by wrapper or global style usually? */
  /* safe area handled? Original didn't specify. */
  padding-top: calc(var(--status-bar-height) + 20rpx); 
}

.back-btn {
  font-size: 36rpx;
  color: #333;
  background: none;
  padding: 0;
  margin: 0;
  line-height: 1;
  border: none;
  margin-right: 16rpx;
  
  &::after { border: none; }
}

.location {
  display: flex;
  align-items: center;
  margin-right: 20rpx;
  max-width: 160rpx;
}

.location-icon-img {
  width: 28rpx;
  height: 28rpx;
  margin-right: 4rpx;
}

.location-text {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.location-arrow-img {
  width: 20rpx;
  height: 20rpx;
  margin-left: 4rpx;
}

.search-input-wrapper {
  flex: 1;
  margin-right: 20rpx;
}

.search-input {
  background: #f5f6fa;
  border-radius: 32rpx;
  height: 64rpx;
  display: flex;
  align-items: center;
  padding: 0 20rpx;
}

.search-icon-img {
  width: 28rpx;
  height: 28rpx;
  margin-right: 8rpx;
}

.search-input input {
  flex: 1;
  font-size: 26rpx;
  color: #333;
}

.user-avatar {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #eee;
}

.user-icon {
  width: 64rpx;
  height: 64rpx;
  border-radius: 50%;
  background: #f5f6fa;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
}
</style>
