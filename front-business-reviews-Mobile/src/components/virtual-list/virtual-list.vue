<template>
  <scroll-view 
    class="virtual-list-container" 
    scroll-y 
    :style="{ height: containerHeight + 'px' }" 
    @scroll="handleScroll"
  >
    <!-- Phantom container to mimic total height -->
    <view class="phantom" :style="{ height: totalHeight + 'px' }"></view>
    <!-- Visible content container -->
    <view class="content" :style="{ transform: `translate3d(0, ${offset}px, 0)` }">
      <view class="item-wrapper" v-for="(item, index) in visibleData" :key="item._vid || index">
        <slot :item="item" :index="start + index"></slot>
      </view>
    </view>
  </scroll-view>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  list: {
    type: Array,
    default: () => []
  },
  itemHeight: {
    type: Number,
    required: true
  },
  buffer: {
    type: Number,
    default: 5
  },
  containerHeight: {
    type: Number,
    default: 600 // Default height in px
  }
})

const scrollTop = ref(0)
const start = ref(0)
const end = ref(0)
const offset = ref(0)

// Calculate total height of the list
const totalHeight = computed(() => props.list.length * props.itemHeight)

// Calculate visible count based on container height
const visibleCount = computed(() => Math.ceil(props.containerHeight / props.itemHeight))

// Slice the data to show only visible items + buffer
const visibleData = computed(() => {
  return props.list.slice(start.value, Math.min(end.value, props.list.length))
})

const handleScroll = (e) => {
  scrollTop.value = e.detail.scrollTop
  calculateScroll()
}

const calculateScroll = () => {
  start.value = Math.floor(scrollTop.value / props.itemHeight)
  end.value = start.value + visibleCount.value + props.buffer
  offset.value = start.value * props.itemHeight
}

watch(() => props.list, () => {
  calculateScroll()
})

onMounted(() => {
  calculateScroll()
})
</script>

<style lang="scss" scoped>
.virtual-list-container {
  position: relative;
  overflow-y: auto;
}

.phantom {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  z-index: -1;
}

.content {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
}
</style>
