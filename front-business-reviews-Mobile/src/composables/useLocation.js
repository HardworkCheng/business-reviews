import { ref, onUnmounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'

export function useLocation() {
    const location = ref('')
    const latitude = ref(null)
    const longitude = ref(null)

    const chooseLocation = () => {
        // 跳转到自定义位置选择页面
        uni.navigateTo({
            url: '/pages/location-picker/location-picker'
        })
    }

    // 处理位置选择
    const handleLocationSelected = (data) => {
        console.log('✅ 收到位置数据:', data)
        location.value = data.name
        latitude.value = data.latitude
        longitude.value = data.longitude

        uni.showToast({
            title: '位置已选择',
            icon: 'success',
            duration: 1500
        })
    }

    // 监听位置选择事件
    onShow(() => {
        uni.$on('locationSelected', handleLocationSelected)
    })

    onUnmounted(() => {
        uni.$off('locationSelected', handleLocationSelected)
    })

    const clearLocation = () => {
        location.value = ''
        latitude.value = null
        longitude.value = null
    }

    return {
        location,
        latitude,
        longitude,
        chooseLocation,
        clearLocation
    }
}
