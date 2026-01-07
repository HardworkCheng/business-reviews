import { ref } from 'vue'
import { getShopList, getNearbyShops } from '../api/shop'

export function useShopSearch() {
    const keyword = ref('')
    const shopList = ref([])
    const loading = ref(false)
    const pageNum = ref(1)
    const pageSize = ref(10)
    const hasMore = ref(true)

    // Filter & Sort State
    const selectedCategory = ref('美食')
    const selectedCategoryId = ref(1)
    const sortField = ref(null) // 'distance', 'popularity', 'rating', 'price'
    const sortOrder = ref('asc')

    const processShopResult = (result) => {
        if (result && result.list) {
            const newList = result.list.map(shop => ({
                id: shop.id,
                name: shop.name,
                image: shop.image || 'https://via.placeholder.com/400x300/FF9E64/FFFFFF?text=Shop',
                rating: shop.rating || 0,
                reviews: shop.noteCount || 0,
                tags: shop.category ? [shop.category] : [],
                avgPrice: shop.avgPrice || 85,
                location: shop.address || '',
                distance: shop.distance || ''
            }))

            if (pageNum.value === 1) {
                shopList.value = newList
            } else {
                shopList.value = [...shopList.value, ...newList]
            }

            hasMore.value = result.list.length >= pageSize.value
        } else {
            if (pageNum.value === 1) {
                shopList.value = []
            }
            hasMore.value = false
        }
    }

    const fetchNearbyShopsAction = async (lat, lng) => {
        const params = {
            latitude: lat,
            longitude: lng,
            distance: 10,
            pageNum: pageNum.value,
            pageSize: pageSize.value,
            categoryId: selectedCategoryId.value,
            sortOrder: sortOrder.value  // 传递排序方向到后端
        }

        console.log('获取附近商家参数:', params)
        const result = await getNearbyShops(params)

        // 后端已处理排序，无需前端反转
        processShopResult(result)
    }

    const fetchShopList = async (userLat, userLng) => {
        if (loading.value) return
        loading.value = true

        try {
            // 1. Distance Sort Check
            if (sortField.value === 'distance') {
                if (!userLat || !userLng) {
                    console.warn('Distance sort requires location')
                    // Fallback handled by caller usually or throw
                    // Here we assume caller handles availability or we fail gracefully
                    // Ensure legacy fallback logic is preserved if needed:
                    // Original: degraded to normal list if no location.
                    // We will assume location is provided if sortField is distance
                    if (!userLat) throw new Error('NO_LOCATION')
                    await fetchNearbyShopsAction(userLat, userLng)
                } else {
                    await fetchNearbyShopsAction(userLat, userLng)
                }
            } else {
                // 2. Normal Sort / Keyword Search
                let sortBy = null
                if (sortField.value) {
                    if (sortField.value === 'rating') sortBy = 'rating'
                    else if (sortField.value === 'popularity') sortBy = 'popular'
                    else if (sortField.value === 'price') sortBy = sortOrder.value === 'asc' ? 'price_asc' : 'price_desc'
                }

                const params = {
                    pageNum: pageNum.value,
                    pageSize: pageSize.value
                }

                if (keyword.value && keyword.value.trim()) {
                    params.keyword = keyword.value.trim()
                } else {
                    params.categoryId = selectedCategoryId.value
                }

                if (sortBy) params.sortBy = sortBy

                console.log('获取商家列表参数:', params)
                const result = await getShopList(params)
                processShopResult(result)
            }
        } catch (e) {
            console.error('Fetch Shop List Error:', e)
            if (e.message !== 'NO_LOCATION') {
                uni.showToast({ title: '加载失败', icon: 'none' })
            } else {
                // Fallback logic for NO_LOCATION could be handled here if we want to mimic original exactly
                // Original: if location fails, reset sortField to null and fetch defaults.
                // Because we changed state (sortField), we might want to let the component handle the degradation logic?
                // Or handle it here:
                console.warn('Degrading to default sort')
                sortField.value = null
                await fetchShopList(null, null) // Recursive call without distance
            }
        } finally {
            loading.value = false
        }
    }

    const resetList = () => {
        pageNum.value = 1
        shopList.value = []
        hasMore.value = true
    }

    return {
        keyword,
        shopList,
        loading,
        pageNum,
        hasNext: hasMore,
        selectedCategory,
        selectedCategoryId,
        sortField,
        sortOrder,
        fetchShopList,
        resetList
    }
}
