import { ref, computed } from 'vue'
import * as couponApi from '../api/coupon'

export function useCoupons() {
    const loading = ref(false)
    const availableCoupons = ref([])
    const myCoupons = ref([])
    const seckillActivity = ref(null)

    // Fetch Available Coupons
    const fetchAvailableCoupons = async (keyword = '') => {
        if (loading.value) return
        loading.value = true

        try {
            const data = await couponApi.getAvailableCoupons(1, 100, keyword)
            console.log('UseCoupons - Fetch Available:', data)
            if (data && data.list) {
                availableCoupons.value = data.list
            } else if (Array.isArray(data)) {
                availableCoupons.value = data
            } else {
                availableCoupons.value = []
            }
        } catch (e) {
            console.error('Fetch Available Coupons Failed:', e)
            availableCoupons.value = []
        } finally {
            loading.value = false
        }
    }

    // Fetch Seckill Activities
    const fetchSeckillActivities = async () => {
        try {
            const data = await couponApi.getSeckillActivities(2) // 2 means ongoing
            if (data && data.length > 0) {
                seckillActivity.value = data[0]
            } else {
                seckillActivity.value = null
            }
        } catch (e) {
            console.error('Fetch Seckill Failed:', e)
            seckillActivity.value = null
        }
    }

    // Fetch My Coupons
    const fetchMyCoupons = async (walletTab = 'all') => {
        const token = uni.getStorageSync('token')
        if (!token) {
            myCoupons.value = []
            return
        }

        try {
            const data = await couponApi.getMyCoupons(walletTab, 1, 100)
            console.log('UseCoupons - Fetch My Coupons:', data)
            if (data && data.list) {
                myCoupons.value = data.list
            } else if (Array.isArray(data)) {
                myCoupons.value = data
            } else {
                myCoupons.value = []
            }
        } catch (e) {
            console.error('Fetch My Coupons Failed:', e)
            myCoupons.value = []
        }
    }

    // Claim Coupon Logic
    const claimCoupon = async (coupon) => {
        const token = uni.getStorageSync('token')
        if (!token) {
            throw new Error('请先登录')
        }

        const response = await couponApi.receiveCoupon(coupon.id)

        // Update local state
        coupon.status = 'claimed'
        const index = availableCoupons.value.findIndex(c => c.id === coupon.id)
        if (index !== -1) {
            availableCoupons.value[index].status = 'claimed'
        }

        return response
    }

    const claimSeckillCoupon = async (activityId, coupon) => {
        const token = uni.getStorageSync('token')
        if (!token) {
            throw new Error('请先登录')
        }

        const response = await couponApi.claimSeckillCoupon(activityId, coupon.id)
        if (response.code === 200) {
            coupon.remainStock--
        }
        return response
    }

    return {
        loading,
        availableCoupons,
        myCoupons,
        seckillActivity,
        fetchAvailableCoupons,
        fetchSeckillActivities,
        fetchMyCoupons,
        claimCoupon,
        claimSeckillCoupon
    }
}
