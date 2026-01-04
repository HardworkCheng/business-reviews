<template>
  <view class="note-card clay-shadow" @click="handleClick">
    <view class="note-image-wrapper">
      <app-image
        :src="note.image || '/static/images/placeholder.svg'"
        mode="aspectFill"
        width="100%"
        height="100%"
        shape="square"
      ></app-image>
      <view v-if="note.tag" class="note-tag" :class="note.tagClass">
        <text class="tag-text">{{ note.tag }}</text>
      </view>
    </view>
    <view class="note-info">
      <text class="note-title line-clamp-2">{{ note.title }}</text>
      <view class="note-meta">
        <view class="author-info">
          <text class="author">@{{ note.author }}</text>
        </view>
        <view class="like-info">
          <text class="like-icon">❤️</text>
          <text class="like-count">{{ note.likes }}</text>
        </view>
      </view>
      <view class="note-time" v-if="note.createTime">
        <text class="time-text">{{ note.createTime }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import AppImage from '@/components/app-image/app-image.vue'

defineProps({
  note: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

const handleClick = () => {
  emit('click')
}
</script>

<style lang="scss" scoped>
.note-card {
  background: white;
  border-radius: 30rpx;
  overflow: hidden;
  transition: all 0.3s;
  
  &:active {
    transform: scale(0.98);
  }
}

.note-image-wrapper {
  position: relative;
  width: 100%;
  height: 350rpx;
  background-color: #f0f0f0;
}

.note-image {
  width: 100%;
  height: 100%;
}

.note-tag {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  padding: 8rpx 20rpx;
  border-radius: 30rpx;
}

.tag-hot { background: #EF476F; }
.tag-discount { background: #06D6A0; }
.tag-new { background: #FFD166; }
.tag-merchant { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }

.tag-text {
  font-size: 22rpx;
  color: white;
}

.note-info {
  padding: 20rpx;
}

.note-title {
  font-size: 28rpx;
  font-weight: 500;
  margin-bottom: 15rpx;
  line-height: 1.4;
  height: 80rpx; /* Fixed height for 2 lines */
}

.note-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author {
  font-size: 24rpx;
  color: #999;
}

.like-info {
  display: flex;
  align-items: center;
}

.like-icon {
  font-size: 24rpx;
  margin-right: 5rpx;
}

.like-count {
  font-size: 24rpx;
  color: #EF476F;
}

.note-time {
  display: flex;
  justify-content: flex-end;
  margin-top: 10rpx;
}

.time-text {
  font-size: 22rpx;
  color: #999;
}
</style>
