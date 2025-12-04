<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('App Launch')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
/* 全局样式 */
@import '@/static/styles/common.scss';

page {
  background-color: #F7F9FC;
  font-family: 'Comic Sans MS', 'Marker Felt', 'Arial Rounded MT Bold', sans-serif;
}

/* 粘土风格通用类 */
.clay-border {
  border: 3rpx solid #000;
  border-radius: 40rpx;
}

.clay-icon {
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 6rpx 6rpx 0rpx rgba(0, 0, 0, 0.1);
}

.clay-shadow {
  box-shadow: 10rpx 10rpx 0rpx rgba(0, 0, 0, 0.1);
}

.clay-shadow-hover {
  box-shadow: 16rpx 16rpx 0rpx rgba(0, 0, 0, 0.15);
}
</style>
