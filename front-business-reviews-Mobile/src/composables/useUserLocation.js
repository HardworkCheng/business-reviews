import { ref } from 'vue'

export function useUserLocation() {
    const currentCity = ref('定位中...')
    const userLatitude = ref(null)
    const userLongitude = ref(null)

    // WGS84坐标转GCJ02坐标（火星坐标系）
    const wgs84ToGcj02 = (lng, lat) => {
        const a = 6378245.0
        const ee = 0.00669342162296594323

        if (outOfChina(lng, lat)) {
            return [lng, lat]
        }

        let dLat = transformLat(lng - 105.0, lat - 35.0)
        let dLng = transformLng(lng - 105.0, lat - 35.0)
        const radLat = lat / 180.0 * Math.PI
        let magic = Math.sin(radLat)
        magic = 1 - ee * magic * magic
        const sqrtMagic = Math.sqrt(magic)
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI)
        dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI)
        const mgLat = lat + dLat
        const mgLng = lng + dLng
        return [mgLng, mgLat]
    }

    const transformLat = (lng, lat) => {
        let ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng))
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0
        ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    const transformLng = (lng, lat) => {
        let ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng))
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0
        ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0
        ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0
        return ret
    }

    const outOfChina = (lng, lat) => {
        return (lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271)
    }

    const setDefaultCity = () => {
        currentCity.value = '杭州'
        uni.setStorageSync('selectedCity', '杭州')
    }

    // IP定位
    const getCityByIP = (callback) => {
        console.log('🔍 开始IP定位...')
        const key = '1521141ae4ee08e1a0e37b59d2fd2438'
        const url = `https://restapi.amap.com/v3/ip?key=${key}`

        uni.request({
            url: url,
            method: 'GET',
            success: (res) => {
                if (res.data.status === '1') {
                    let cityName = ''
                    if (res.data.city && typeof res.data.city === 'string' && res.data.city !== '') {
                        cityName = res.data.city
                    } else if (res.data.province && typeof res.data.province === 'string') {
                        cityName = res.data.province
                    }

                    if (cityName) {
                        cityName = cityName.replace('市', '').replace('省', '').replace('自治区', '').replace('特别行政区', '')
                        currentCity.value = cityName
                        uni.setStorageSync('selectedCity', cityName)
                        console.log('✅ IP定位成功:', cityName)
                        if (callback) callback(cityName)
                    } else {
                        console.warn('⚠️ IP定位无城市信息')
                        if (!currentCity.value || currentCity.value === '定位中...') setDefaultCity()
                    }
                } else {
                    console.warn('⚠️ IP定位失败')
                    if (!currentCity.value || currentCity.value === '定位中...') setDefaultCity()
                }
            },
            fail: (err) => {
                console.error('❌ IP定位请求失败:', err)
                if (!currentCity.value || currentCity.value === '定位中...') setDefaultCity()
            }
        })
    }

    // 逆地理编码
    const reverseGeocode = (lat, lng) => {
        const key = '1521141ae4ee08e1a0e37b59d2fd2438'
        const url = `https://restapi.amap.com/v3/geocode/regeo?key=${key}&location=${lng},${lat}&poitype=&radius=1000&extensions=base&batch=false&roadlevel=0`

        uni.request({
            url: url,
            method: 'GET',
            success: (res) => {
                if (res.data.status === '1' && res.data.regeocode) {
                    const addressComponent = res.data.regeocode.addressComponent
                    let cityName = ''

                    if (addressComponent) {
                        if (addressComponent.province && typeof addressComponent.province === 'string') {
                            cityName = addressComponent.province
                        }
                        if (addressComponent.city) {
                            if (Array.isArray(addressComponent.city)) {
                                if (addressComponent.city.length > 0 && typeof addressComponent.city[0] === 'string') {
                                    cityName = addressComponent.city[0]
                                }
                            } else if (typeof addressComponent.city === 'string' && addressComponent.city !== '') {
                                cityName = addressComponent.city
                            }
                        }
                    }

                    if (cityName && typeof cityName === 'string') {
                        cityName = cityName.replace('市', '').replace('省', '').replace('自治区', '').replace('特别行政区', '')
                        currentCity.value = cityName
                        uni.setStorageSync('selectedCity', cityName)
                        console.log('✅ GPS逆地理编码成功:', cityName)
                    }
                }
            }
        })
    }

    const getUserLocation = () => {
        return new Promise((resolve) => {
            // #ifdef H5
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    (position) => {
                        const { latitude, longitude } = position.coords
                        const gcj02Coords = wgs84ToGcj02(longitude, latitude)
                        userLongitude.value = gcj02Coords[0]
                        userLatitude.value = gcj02Coords[1]
                        console.log('✅ H5定位成功:', userLatitude.value, userLongitude.value)
                        resolve(true)
                    },
                    (error) => {
                        console.warn('H5定位失败:', error.message)
                        resolve(false)
                    },
                    { enableHighAccuracy: false, timeout: 5000, maximumAge: 300000 }
                )
            } else {
                resolve(false)
            }
            // #endif

            // #ifndef H5
            uni.getLocation({
                type: 'gcj02',
                success: (res) => {
                    userLatitude.value = res.latitude
                    userLongitude.value = res.longitude
                    console.log('✅ APP定位成功:', userLatitude.value, userLongitude.value)
                    resolve(true)
                },
                fail: (err) => {
                    console.error('APP定位失败:', err)
                    resolve(false)
                }
            })
            // #endif
        })
    }

    const getCurrentCity = () => {
        const storedCity = uni.getStorageSync('selectedCity')
        if (storedCity) {
            currentCity.value = storedCity
            return
        }

        currentCity.value = '定位中...'

        // 1. Try IP Location
        getCityByIP()

        // 2. Try GPS + Reverse Geocode
        getUserLocation().then(success => {
            if (success) {
                reverseGeocode(userLatitude.value, userLongitude.value)
            }
        })
    }

    const reLocation = () => {
        uni.removeStorageSync('selectedCity')
        currentCity.value = '定位中...'
        uni.showLoading({ title: '正在定位...', mask: true })

        // Use simpler logic for re-location reuse
        getUserLocation().then(success => {
            if (success) {
                reverseGeocode(userLatitude.value, userLongitude.value)
                uni.hideLoading()
            } else {
                getCityByIP()
                uni.hideLoading()
            }
        })
    }

    return {
        currentCity,
        userLatitude,
        userLongitude,
        getCurrentCity,
        getUserLocation,
        reLocation
    }
}
