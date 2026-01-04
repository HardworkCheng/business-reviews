<template>
  <view class="app-image-container" :class="[shape]" :style="style">
    <image
      :src="src || placeholder"
      :mode="mode"
      :lazy-load="lazyLoad"
      class="app-image"
      @error="handleError"
    ></image>
  </view>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  src: String,
  mode: {
    type: String,
    default: 'aspectFill'
  },
  width: String,
  height: String,
  shape: {
    type: String,
    default: 'square' // square, circle, rounded
  },
  lazyLoad: {
    type: Boolean,
    default: true
  },
  placeholder: {
    type: String,
    default: '/static/images/placeholder.svg'
  }
})

const style = ref({
  width: props.width,
  height: props.height
})

const handleError = (e) => {
  // console.log('Image load error', e)
}
</script>

<style lang="scss" scoped>
.app-image-container {
  overflow: hidden;
  position: relative;
  background-color: #f5f5f5;
  
  &.circle { border-radius: 50%; }
  &.rounded { border-radius: 12rpx; }
  &.square { border-radius: 0; }
}

.app-image {
  width: 100%;
  height: 100%;
}
</style>
