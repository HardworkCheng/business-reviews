<template>
  <view class="filter-container">
    <view class="filters">
      <!-- 分类下拉 -->
      <view 
        class="filter-item"
        :class="{ active: selectedCategory !== '美食' }"
        @click="toggleCategoryDropdown"
      >
        <text>{{ selectedCategory }}</text>
        <text class="arrow" :class="{ 'arrow-up': showCategoryDropdown }">▼</text>
      </view>
      
      <!-- 距离排序 -->
      <view 
        class="filter-item"
        :class="{ active: sortField === 'distance' }"
        @click="toggleSortDropdown('distance')"
      >
        <text>距离</text>
        <text class="arrow" :class="{ 'arrow-up': showSortDropdown === 'distance' }">▼</text>
      </view>
      
      <!-- 人气排序 -->
      <view 
        class="filter-item"
        :class="{ active: sortField === 'popularity' }"
        @click="toggleSortDropdown('popularity')"
      >
        <text>人气</text>
        <text class="arrow" :class="{ 'arrow-up': showSortDropdown === 'popularity' }">▼</text>
      </view>
      
      <!-- 评分排序 -->
      <view 
        class="filter-item"
        :class="{ active: sortField === 'rating' }"
        @click="toggleSortDropdown('rating')"
      >
        <text>评分</text>
        <text class="arrow" :class="{ 'arrow-up': showSortDropdown === 'rating' }">▼</text>
      </view>
      
      <!-- 价格排序 -->
      <view 
        class="filter-item"
        :class="{ active: sortField === 'price' }"
        @click="toggleSortDropdown('price')"
      >
        <text>价格</text>
        <text class="arrow" :class="{ 'arrow-up': showSortDropdown === 'price' }">▼</text>
      </view>
    </view>
    
    <!-- 分类下拉菜单 -->
    <view class="dropdown-menu category-dropdown" v-if="showCategoryDropdown">
      <view 
        class="dropdown-item"
        v-for="(item, index) in categories" 
        :key="index"
        @click="selectCategory(item)"
        :class="{ 'active': selectedCategory === item.name }"
      >
        <image :src="item.icon" class="category-icon-small" mode="aspectFit"></image>
        <text class="dropdown-text">{{ item.name }}</text>
        <text v-if="selectedCategory === item.name" class="check-icon">✓</text>
      </view>
    </view>
    
    <!-- 排序下拉菜单 -->
    <view class="dropdown-menu sort-dropdown" v-if="showSortDropdown">
      <view 
        class="dropdown-item"
        @click="selectSort('asc')"
        :class="{ 'active': sortOrder === 'asc' }"
      >
        <text class="dropdown-text">{{ sortField === 'distance' ? '从近到远' : '从小到大' }}</text>
        <text v-if="sortOrder === 'asc'" class="check-icon">✓</text>
      </view>
      <view 
        class="dropdown-item"
        @click="selectSort('desc')"
        :class="{ 'active': sortOrder === 'desc' }"
      >
        <text class="dropdown-text">{{ sortField === 'distance' ? '从远到近' : '从大到小' }}</text>
        <text v-if="sortOrder === 'desc'" class="check-icon">✓</text>
      </view>
    </view>

    <!-- 遮罩层 -->
    <view 
        class="mask" 
        v-if="showCategoryDropdown || showSortDropdown"
        @click="closeAllDropdowns"
    ></view>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  selectedCategory: String,
  sortField: String,
  sortOrder: String
})

const emit = defineEmits(['update:selectedCategory', 'update:sortField', 'update:sortOrder', 'change'])

const showCategoryDropdown = ref(false)
const showSortDropdown = ref(null)

const categories = ref([
	{ name: '美食', icon: '/static/icons/food.png', id: 1 },
	{ name: 'KTV', icon: '/static/icons/ktv.png', id: 2 },
	{ name: '美发', icon: '/static/icons/beauty.png', id: 3 },
	{ name: '美甲', icon: '/static/icons/nail.png', id: 4 },
	{ name: '足疗', icon: '/static/icons/massage.png', id: 5 },
	{ name: '美容', icon: '/static/icons/spa.png', id: 6 },
	{ name: '游乐', icon: '/static/icons/entertainment.png', id: 7 },
	{ name: '酒吧', icon: '/static/icons/bar.png', id: 8 }
])

const toggleCategoryDropdown = () => {
    showCategoryDropdown.value = !showCategoryDropdown.value
    showSortDropdown.value = null
}

const toggleSortDropdown = (field) => {
    if (showSortDropdown.value === field) {
        showSortDropdown.value = null
    } else {
        showSortDropdown.value = field
        emit('update:sortField', field) // Pre-select the field
    }
    showCategoryDropdown.value = false
}

const selectCategory = (item) => {
    // emit('update:selectedCategory', item.name) // Parent handles ID lookup or we pass object?
    // search.vue logic used name to find ID.
    // Ideally we pass full object or ID up. 
    // To match legacy prop:
    emit('update:selectedCategory', item.name)
    emit('change', { type: 'category', value: item })
    closeAllDropdowns()
}

const selectSort = (order) => {
    emit('update:sortOrder', order)
    emit('change', { type: 'sort', value: order })
    closeAllDropdowns()
}

const closeAllDropdowns = () => {
    showCategoryDropdown.value = false
    showSortDropdown.value = null
}

defineExpose({ closeAllDropdowns })
</script>

<style lang="scss" scoped>
.filter-container {
    position: relative;
    z-index: 50;
}

.filters {
	display: flex;
	align-items: center;
	padding: 24rpx 0;
	border-bottom: 1rpx solid #f5f6fa;
    background: #fff;
    position: relative;
    z-index: 52;
}

.filter-item {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 26rpx;
	color: #666;
	
	&.active {
		color: #ff9f43;
		font-weight: bold;
	}
}

.arrow {
	font-size: 20rpx;
	margin-left: 4rpx;
	transition: transform 0.2s;
	
	&.arrow-up {
		transform: rotate(180deg);
	}
}

.dropdown-menu {
	position: absolute;
	top: 100%; // relative to filters
	left: 0;
	right: 0;
	background: #fff;
	max-height: 600rpx;
	overflow-y: auto;
	z-index: 51;
	border-radius: 0 0 24rpx 24rpx;
	box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	padding: 10rpx 0;
}

.dropdown-item {
	display: flex;
	align-items: center;
	padding: 24rpx 32rpx;
	
	&.active {
		background: #fff9f0;
		.dropdown-text {
			color: #ff9f43;
			font-weight: bold;
		}
	}
	
	&:active {
		background: #f5f6fa;
	}
}

.category-icon-small {
	width: 40rpx;
	height: 40rpx;
	margin-right: 16rpx;
}

.dropdown-text {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}

.check-icon {
	color: #ff9f43;
}

.mask {
	position: fixed;
	top: 0; // Will need to adjust if this component is not at top check
    // Actually fixed top:0 covers everything. We want it to start below the filter bar?
    // In search.vue it was top:0? No, it was just inside the page. 
    // If we use fixed, we cover the header too unless z-index is lower than header.
    // search.vue header had typical flow.
    // Let's use fixed but high z-index below dropdown.
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0, 0, 0, 0.5);
	z-index: 40; 
}
</style>
