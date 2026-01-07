<template>
  <view class="shop-list">
    <view class="shop-item" v-for="(shop, index) in list" :key="index" @click="$emit('click-item', shop)">
      <image :src="shop.image" class="shop-image" mode="aspectFill"></image>
      <view class="shop-info">
        <text class="shop-name">{{ shop.name }}</text>
        <view class="shop-score-row">
          <view class="stars">
            <text class="star-icon" v-for="n in 5" :key="n">⭐</text>
          </view>
          <text class="score-num">{{ shop.rating || 5.0 }}</text>
          <text class="review-count">({{ shop.reviews }}条评价)</text>
        </view>
        <view class="shop-price-row">
          <text class="category-tag">{{ shop.tags[0] || '美食' }}</text>
          <text class="price-text">人均 ¥{{ shop.avgPrice || 85 }}</text>
        </view>
        <view class="shop-loc-row">
          <text class="address">{{ shop.location }}</text>
          <text class="distance" v-if="shop.distance">{{ shop.distance }}</text>
        </view>
      </view>
    </view>
    
    <!-- 空状态 -->
    <view v-if="list.length === 0 && !loading" class="empty-state">
      <view class="empty-icon">🏪</view>
      <text class="empty-text">暂无相关商户</text>
    </view>
    
    <view v-if="loading" class="loading-state">
        <text>加载中...</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  list: {
    type: Array,
    default: () => []
  },
  loading: Boolean
})

defineEmits(['click-item'])
</script>

<style lang="scss" scoped>
.shop-list {
	padding: 0 24rpx;
}

.shop-item {
	display: flex;
	margin-bottom: 32rpx;
	background: #fff;
	padding: 20rpx;
	border-radius: 16rpx;
	box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.02);
	
	&:active {
		background: #f9f9f9;
	}
}

.shop-image {
	width: 160rpx;
	height: 160rpx;
	border-radius: 12rpx;
	background: #eee;
	margin-right: 24rpx;
    flex-shrink: 0;
}

.shop-info {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: space-between;
}

.shop-name {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}

.shop-score-row {
	display: flex;
	align-items: center;
	font-size: 22rpx;
	color: #666;
}

.stars {
	display: flex;
	margin-right: 8rpx;
}

.star-icon {
	font-size: 20rpx;
}

.score-num {
	font-weight: bold;
	color: #ff9f43;
	margin-right: 8rpx;
}

.shop-price-row {
	display: flex;
	align-items: center;
	font-size: 22rpx;
	margin-top: 4rpx;
}

.category-tag {
	color: #666;
	margin-right: 16rpx;
}

.price-text {
	color: #333;
}

.shop-loc-row {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 22rpx;
	color: #999;
	margin-top: 4rpx;
}

.address {
	max-width: 300rpx;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}

.empty-state {
    padding: 100rpx 0;
    text-align: center;
    color: #999;
}
.empty-icon {
    font-size: 60rpx;
    margin-bottom: 20rpx;
}
.loading-state {
    text-align: center;
    padding: 30rpx;
    color: #999;
    font-size: 24rpx;
}
</style>
