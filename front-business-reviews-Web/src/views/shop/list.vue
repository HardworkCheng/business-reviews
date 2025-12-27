<template>
  <div class="shop-info-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-text">
          <h1 class="page-title">店铺信息管理</h1>
          <p class="page-desc">管理您的店铺基本信息、经营类目、地理位置和运营数据</p>
          <div v-if="lastUpdateTime" class="update-info">
            <span class="update-text">最后更新：{{ formatTime(lastUpdateTime) }}</span>
            <el-tag v-if="dataStatus" :type="dataStatus === 'synced' ? 'success' : 'warning'" size="small">
              {{ dataStatus === 'synced' ? '数据已同步' : '数据更新中' }}
            </el-tag>
          </div>
        </div>
        <!-- 编辑控制按钮 -->
        <div class="header-actions">
          <el-button v-if="!isEditing" @click="refreshShopData" :loading="loading" class="refresh-btn">
            <el-icon><Refresh /></el-icon>
            刷新数据
          </el-button>
          <el-button v-if="!isEditing" type="primary" @click="enableEdit" class="edit-btn">
            <el-icon><Edit /></el-icon>
            修改信息
          </el-button>
          <div v-else class="edit-mode-buttons">
            <el-button @click="cancelEdit" class="btn-cancel">取消</el-button>
            <el-button type="primary" @click="saveShopInfo" :loading="saving" class="btn-save">保存修改</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 店铺信息内容 -->
    <div class="content-section">
      <!-- 运营数据 -->
      <div class="info-card">
        <div class="stats-header">
          <h3 class="card-title">运营数据</h3>
          <div class="stats-actions">
            <el-button size="small" @click="refreshStats" :loading="statsLoading" class="refresh-btn">
              <el-icon><Refresh /></el-icon>
              刷新数据
            </el-button>
            <div class="auto-refresh-toggle">
              <el-switch 
                v-model="autoRefresh" 
                @change="toggleAutoRefresh"
                active-text="自动刷新"
                inactive-text="手动刷新"
                size="small"
              />
            </div>
          </div>
        </div>
        <div class="form-container" v-loading="statsLoading">
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-label">综合评分</div>
              <div class="stat-value">{{ formatScore(shopForm.rating) }}</div>
              <el-rate v-model="ratingDisplay" disabled show-score text-color="#FF7D00" />
            </div>
            <div class="stat-item">
              <div class="stat-label">口味评分</div>
              <div class="stat-value">{{ formatScore(shopForm.tasteScore) }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">环境评分</div>
              <div class="stat-value">{{ formatScore(shopForm.environmentScore) }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">服务评分</div>
              <div class="stat-value">{{ formatScore(shopForm.serviceScore) }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">评价数量</div>
              <div class="stat-value">{{ shopForm.reviewCount || 0 }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">人气值</div>
              <div class="stat-value">{{ shopForm.popularity || 0 }}</div>
            </div>
          </div>
          <div class="stats-footer">
            <p class="last-update" v-if="lastStatsUpdate">最后更新：{{ formatTime(lastStatsUpdate) }}</p>
          </div>
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="info-card">
        <h3 class="card-title">基本信息</h3>
        <div class="form-container" v-loading="loading">
          <div class="form-layout-horizontal">
            <!-- 左侧：店铺封面上传区 -->
            <div class="left-cover-section">
              <div class="cover-label-top">
                <span class="cover-title">店铺封面</span>
                <span class="required">*</span>
              </div>
              <div class="cover-upload-box" @click="triggerLogoUpload">
                <input ref="logoInput" type="file" accept="image/*" @change="handleLogoChange" style="display: none;" />
                <img v-if="shopForm.headerImage" :src="shopForm.headerImage" class="cover-preview" :class="{ editable: isEditing }" />
                <div v-else class="cover-placeholder-new" :class="{ editable: isEditing }">
                  <el-icon :size="40" class="upload-icon"><Plus /></el-icon>
                  <span class="upload-text">点击上传封面</span>
                </div>
                <div v-if="logoUploading" class="upload-loading-overlay">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span class="loading-text">上传中...</span>
                </div>
                <div v-if="isEditing && shopForm.headerImage" class="cover-hover-overlay">
                  <el-icon :size="24"><Edit /></el-icon>
                  <span>更换封面</span>
                </div>
              </div>
            </div>

            <!-- 右侧：表单字段区（2列网格） -->
            <div class="right-form-grid">
              <div class="form-field">
                <label class="field-label-new">店铺名称 <span class="required">*</span></label>
                <el-input v-model="shopForm.name" placeholder="请输入店铺名称" class="field-input-new" :readonly="!isEditing" />
              </div>

              <div class="form-field">
                <label class="field-label-new">经营类目 <span class="required">*</span></label>
                <el-select v-model="shopForm.categoryId" placeholder="请选择经营类目" class="field-input-new" :disabled="!isEditing" :class="{ 'select-readonly': !isEditing }">
                  <el-option 
                    v-for="cat in categories" 
                    :key="cat.id" 
                    :label="cat.name" 
                    :value="cat.id" 
                  />
                </el-select>
              </div>

              <div class="form-field">
                <label class="field-label-new">店铺状态 <span class="required">*</span></label>
                <el-select v-model="shopForm.status" placeholder="请选择店铺状态" class="field-input-new" :disabled="!isEditing" :class="{ 'select-readonly': !isEditing }">
                  <el-option label="营业中" :value="1" />
                  <el-option label="休息中" :value="2" />
                  <el-option label="已关闭" :value="3" />
                </el-select>
              </div>

              <div class="form-field">
                <label class="field-label-new">人均消费 (元)</label>
                <el-input v-model="shopForm.averagePrice" placeholder="请输入人均消费金额" class="field-input-new" :readonly="!isEditing">
                  <template #append>元</template>
                </el-input>
              </div>

              <div class="form-field">
                <label class="field-label-new">联系电话 <span class="required">*</span></label>
                <el-input v-model="shopForm.phone" placeholder="请输入联系电话" class="field-input-new" :readonly="!isEditing" />
              </div>

              <div class="form-field">
                <label class="field-label-new">营业时间</label>
                <el-input v-model="shopForm.businessHours" placeholder="例如：09:00-22:00" class="field-input-new" :readonly="!isEditing" />
              </div>

              <div class="form-field form-field-full">
                <label class="field-label-new">店铺位置 <span class="required">*</span></label>
                <div class="location-input-group">
                  <el-input v-model="shopForm.address" placeholder="输入详细地址" class="field-input-new location-input-flex" :readonly="!isEditing" @blur="geocodeAddress" />
                  <!-- 隐藏的经纬度输入框，保留功能 -->
                  <input type="hidden" v-model="shopForm.longitude" />
                  <input type="hidden" v-model="shopForm.latitude" />
                  <el-button type="primary" size="default" @click="locateCurrentPosition" class="locate-btn-new" :disabled="!isEditing" :loading="locating">
                    <el-icon v-if="!locating"><Aim /></el-icon>
                    <span v-if="!locating">定位</span>
                  </el-button>
                  <el-button type="primary" @click="openLocationPicker" class="location-btn-new" :disabled="!isEditing">
                    <el-icon><Location /></el-icon>地图选择
                  </el-button>
                </div>
                <div v-if="showMap" class="map-wrapper-new">
                  <div id="amap-container" class="amap-container"></div>
                  <div class="map-center-marker">📍</div>
                  <div class="map-controls">
                    <el-button size="small" @click="relocateToCurrentPosition" class="relocate-btn"><el-icon><Aim /></el-icon>重新定位</el-button>
                  </div>
                  <div class="map-search">
                    <el-input v-model="searchKeyword" placeholder="搜索地点" @input="onSearchInput" class="search-input" clearable>
                      <template #prefix><el-icon><Search /></el-icon></template>
                    </el-input>
                  </div>
                  <div v-if="poiList.length > 0" class="poi-list">
                    <div v-for="(poi, index) in poiList" :key="index" class="poi-item" :class="{ active: selectedPoi?.name === poi.name }" @click="selectPoi(poi)">
                      <div class="poi-name">{{ poi.name }}</div>
                      <div class="poi-address">{{ poi.address }}</div>
                    </div>
                  </div>
                  <div v-if="mapLoading" class="map-loading"><el-icon class="is-loading"><Loading /></el-icon>加载中...</div>
                </div>
              </div>

              <div class="form-field form-field-full">
                <label class="field-label-new">店铺简介</label>
                <el-input v-model="shopForm.description" type="textarea" :rows="3" placeholder="请输入店铺简介" maxlength="500" show-word-limit class="field-input-new" :readonly="!isEditing" />
              </div>

              <div class="form-field form-field-full">
                <label class="field-label-new">商家相册 (最多9张)</label>
                <div class="gallery-grid-new">
                  <div v-for="(img, index) in galleryImages" :key="index" class="gallery-item-new">
                    <img :src="img" class="gallery-image-new" @click="previewImage(index)" />
                    <div v-if="isEditing" class="gallery-delete-new" @click.stop="removeGalleryImage(index)">
                      <el-icon><Close /></el-icon>
                    </div>
                  </div>
                  <div v-if="isEditing && galleryImages.length < 9" class="gallery-add-new" @click="triggerGalleryUpload">
                    <input ref="galleryInput" type="file" accept="image/*" multiple @change="handleGalleryChange" style="display: none;" />
                    <el-icon :size="24" v-if="!galleryUploading"><Plus /></el-icon>
                    <span v-else class="uploading-text">上传中...</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>

    <!-- 图片预览 -->
    <el-image-viewer v-if="showImageViewer" :url-list="galleryImages" :initial-index="previewIndex" @close="showImageViewer = false" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Location, Aim, Search, Loading, Edit, Close, Refresh } from '@element-plus/icons-vue'
import { getShopList, updateShop, getShopStats, getCategories, type CategoryVO } from '@/api/shop'
import { uploadSingleFile, uploadMultipleFiles } from '@/services/uploadService'
import { 
  transformApiDataToForm, 
  validateShopData, 
  compareShopData, 
  createDefaultShopFormData,
  type ShopFormData 
} from '@/utils/shopDataTransform'

const AMAP_KEY = '168ca31470201b94eaf74770efdb9f45'
const AMAP_SECURITY_CODE = '47211e50f5d2ee5c60ec4f023c84b553'

const loading = ref(false)
const saving = ref(false)
const logoUploading = ref(false)
const galleryUploading = ref(false)
const isEditing = ref(false)
const locating = ref(false)
const statsLoading = ref(false)
const autoRefresh = ref(false)
const lastStatsUpdate = ref<Date | null>(null)
const lastUpdateTime = ref<Date | null>(null)
const dataStatus = ref<'synced' | 'updating' | null>(null)

const logoInput = ref<HTMLInputElement | null>(null)
const galleryInput = ref<HTMLInputElement | null>(null)
const showMap = ref(false)
const mapLoading = ref(false)
const searchKeyword = ref('')
const poiList = ref<any[]>([])
const selectedPoi = ref<any>(null)
const showImageViewer = ref(false)
const previewIndex = ref(0)
let map: any = null
let currentPosition = { lng: 120.153576, lat: 30.287459 }
let refreshTimer: NodeJS.Timeout | null = null

const shopForm = ref<ShopFormData>(createDefaultShopFormData())
const originalForm = ref({})
const ratingDisplay = computed(() => Number(shopForm.value.rating) || 0)

// 类目列表
const categories = ref<CategoryVO[]>([])

// 加载类目列表
const loadCategories = async () => {
  try {
    console.log('🔄 开始加载类目列表...')
    categories.value = await getCategories()
    console.log('✅ 类目列表加载成功:', categories.value)
  } catch (error: any) {
    console.error('❌ 加载类目失败:', error)
    // 降级方案：使用默认类目
    categories.value = [
      { id: 1, name: '美食' },
      { id: 2, name: 'KTV' },
      { id: 3, name: '美发' },
      { id: 4, name: '美甲' },
      { id: 5, name: '足疗' },
      { id: 6, name: '美容' },
      { id: 7, name: '游乐' },
      { id: 8, name: '酒吧' }
    ]
    console.log('⚠️ 使用降级方案，默认类目:', categories.value)
  }
}

// 解析商家相册图片
const galleryImages = computed(() => {
  if (!shopForm.value.images) return []
  
  console.log('🖼️ 解析图片数据:', shopForm.value.images)
  
  try {
    // 尝试解析JSON格式
    const parsed = JSON.parse(shopForm.value.images)
    if (Array.isArray(parsed)) {
      const validImages = parsed.filter(img => 
        typeof img === 'string' && img.trim() !== ''
      )
      console.log('✅ JSON格式图片解析成功:', validImages)
      return validImages
    }
  } catch (error) {
    console.log('📝 JSON解析失败，尝试逗号分割:', error)
  }
  
  // 回退到逗号分割
  const images = shopForm.value.images
    .split(',')
    .map(img => img.trim())
    .filter(img => img !== '')
  
  console.log('✅ 逗号分割图片解析结果:', images)
  return images
})

const enableEdit = () => { 
  isEditing.value = true
  originalForm.value = { ...shopForm.value }
}

const cancelEdit = async () => { 
  isEditing.value = false
  
  // 取消编辑时，重新加载最新数据确保一致性
  console.log('🔄 取消编辑，重新加载最新数据...')
  await loadShopInfo(true)
  
  ElMessage.info('已取消修改并恢复最新数据')
}

// 手动刷新数据
const refreshShopData = async () => {
  console.log('🔄 用户手动刷新数据...')
  await loadShopInfo(true)
  ElMessage.success('数据已刷新')
}

// 清除前端缓存
const clearCache = () => {
  console.log('🗑️ 清除前端缓存...')
  
  // 清除可能的浏览器缓存
  if ('caches' in window) {
    caches.keys().then(names => {
      names.forEach(name => {
        if (name.includes('api') || name.includes('shop')) {
          caches.delete(name)
          console.log('🗑️ 已清除缓存:', name)
        }
      })
    })
  }
  
  // 清除localStorage中可能的缓存数据
  const cacheKeys = Object.keys(localStorage).filter(key => 
    key.includes('shop') || key.includes('merchant')
  )
  cacheKeys.forEach(key => {
    if (!key.includes('token')) { // 保留token
      localStorage.removeItem(key)
      console.log('🗑️ 已清除本地存储:', key)
    }
  })
}

// 商家相册相关方法
const triggerGalleryUpload = () => {
  console.log('触发相册上传，isEditing:', isEditing.value)
  if (!isEditing.value) {
    ElMessage.warning('请先点击修改信息按钮')
    return
  }
  galleryInput.value?.click()
}

const handleGalleryChange = async (event: Event) => {
  console.log('相册文件选择事件触发')
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) {
    console.log('未选择文件')
    return
  }
  
  console.log('选择的文件数量:', files.length)
  
  const currentCount = galleryImages.value.length
  const maxUpload = 9 - currentCount
  if (maxUpload <= 0) {
    ElMessage.warning('最多上传9张图片')
    return
  }
  
  // 限制上传文件数量
  const filesToUpload = Array.from(files).slice(0, maxUpload)
  console.log('实际上传文件数量:', filesToUpload.length)
  
  galleryUploading.value = true
  const newImages = [...galleryImages.value]
  
  try {
    console.log('开始批量上传相册图片')
    const urls = await uploadMultipleFiles(filesToUpload, 'merchant')
    console.log('相册图片批量上传成功:', urls)
    
    // 添加所有成功上传的图片URL
    newImages.push(...urls)
    shopForm.value.images = JSON.stringify(newImages)
    
    ElMessage.success(`成功上传${urls.length}张图片`)
  } catch (error: any) { 
    console.error('相册图片上传失败:', error)
    ElMessage.error('相册上传失败: ' + (error.message || '未知错误'))
  } finally {
    galleryUploading.value = false
    target.value = ''
  }
}

const removeGalleryImage = (index: number) => {
  const newImages = [...galleryImages.value]
  newImages.splice(index, 1)
  shopForm.value.images = JSON.stringify(newImages)
}

const previewImage = (index: number) => {
  previewIndex.value = index
  showImageViewer.value = true
}

// 定位当前位置
const locateCurrentPosition = async () => {
  if (!navigator.geolocation) {
    ElMessage.error('浏览器不支持定位功能')
    return
  }
  locating.value = true
  try {
    await loadAmapScript()
    navigator.geolocation.getCurrentPosition(
      (position) => {
        const gcj02Coords = wgs84ToGcj02(position.coords.longitude, position.coords.latitude)
        shopForm.value.longitude = gcj02Coords[0].toFixed(6)
        shopForm.value.latitude = gcj02Coords[1].toFixed(6)
        currentPosition = { lng: gcj02Coords[0], lat: gcj02Coords[1] }
        // 反向地理编码获取地址
        reverseGeocode(gcj02Coords[0], gcj02Coords[1])
        locating.value = false
        ElMessage.success('定位成功')
      },
      (error) => {
        locating.value = false
        ElMessage.error('定位失败: ' + (error.message || '未知错误'))
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    )
  } catch { locating.value = false; ElMessage.error('定位服务加载失败') }
}

// 反向地理编码
const reverseGeocode = (lng: number, lat: number) => {
  ;(window as any).AMap.plugin('AMap.Geocoder', function() {
    const geocoder = new (window as any).AMap.Geocoder({ city: '全国' })
    geocoder.getAddress([lng, lat], function(status: string, result: any) {
      if (status === 'complete' && result.regeocode) {
        if (!shopForm.value.address) {
          shopForm.value.address = result.regeocode.formattedAddress || ''
        }
      }
    })
  })
}

const loadAmapScript = () => new Promise<void>((resolve, reject) => {
  if ((window as any).AMap) { resolve(); return }
  ;(window as any)._AMapSecurityConfig = { securityJsCode: AMAP_SECURITY_CODE }
  const script = document.createElement('script')
  script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}&plugin=AMap.Geolocation,AMap.PlaceSearch,AMap.Geocoder`
  script.onload = () => resolve()
  script.onerror = () => reject(new Error('地图加载失败'))
  document.head.appendChild(script)
})

const geocodeAddress = async () => {
  if (!shopForm.value.address || !isEditing.value) return
  try {
    await loadAmapScript()
    ;(window as any).AMap.plugin('AMap.Geocoder', function() {
      const geocoder = new (window as any).AMap.Geocoder({ city: '全国' })
      geocoder.getLocation(shopForm.value.address, function(status: string, result: any) {
        if (status === 'complete' && result.geocodes?.length > 0) {
          const geo = result.geocodes[0]
          shopForm.value.longitude = geo.location.lng.toFixed(6)
          shopForm.value.latitude = geo.location.lat.toFixed(6)
          ElMessage.success('地址解析成功，已自动获取经纬度')
        } else {
          ElMessage.warning('地址解析失败，请检查地址是否正确')
        }
      })
    })
  } catch (e) { 
    ElMessage.error('地址解析服务加载失败')
  }
}

const initMap = () => {
  if (!(window as any).AMap) return
  map = new (window as any).AMap.Map('amap-container', { zoom: 15, center: [currentPosition.lng, currentPosition.lat], viewMode: '2D' })
  map.on('moveend', onMapMoveEnd)
  getCurrentLocation()
}

const getCurrentLocation = () => {
  mapLoading.value = true
  if (!navigator.geolocation) { mapLoading.value = false; searchNearbyPoi(); return }
  navigator.geolocation.getCurrentPosition(
    (position) => {
      const gcj02Coords = wgs84ToGcj02(position.coords.longitude, position.coords.latitude)
      currentPosition = { lng: gcj02Coords[0], lat: gcj02Coords[1] }
      if (map) { map.setCenter([gcj02Coords[0], gcj02Coords[1]]); map.setZoom(15) }
      mapLoading.value = false; searchNearbyPoi()
    },
    () => { mapLoading.value = false; searchNearbyPoi() },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  )
}

const relocateToCurrentPosition = () => getCurrentLocation()
const onMapMoveEnd = () => { if (!map) return; const center = map.getCenter(); currentPosition = { lng: center.lng, lat: center.lat }; if (!searchKeyword.value) searchNearbyPoi() }

const searchNearbyPoi = () => {
  if (!(window as any).AMap) return
  mapLoading.value = true
  ;(window as any).AMap.plugin('AMap.PlaceSearch', function() {
    const placeSearch = new (window as any).AMap.PlaceSearch({ type: '', pageSize: 10, city: '全国', extensions: 'all' })
    placeSearch.searchNearBy('', [currentPosition.lng, currentPosition.lat], 1000, function(status: string, result: any) {
      mapLoading.value = false
      poiList.value = status === 'complete' && result.poiList?.pois ? result.poiList.pois.map((poi: any) => ({ name: poi.name, address: poi.address || (poi.pname + poi.cityname + poi.adname), location: poi.location, latitude: poi.location.lat, longitude: poi.location.lng, pname: poi.pname, cityname: poi.cityname, adname: poi.adname })) : []
    })
  })
}

const onSearchInput = () => {
  if (!searchKeyword.value.trim()) { searchNearbyPoi(); return }
  mapLoading.value = true
  ;(window as any).AMap.plugin('AMap.PlaceSearch', function() {
    const placeSearch = new (window as any).AMap.PlaceSearch({ city: '全国', pageSize: 10, extensions: 'all' })
    placeSearch.search(searchKeyword.value, function(status: string, result: any) {
      mapLoading.value = false
      poiList.value = status === 'complete' && result.poiList?.pois ? result.poiList.pois.map((poi: any) => ({ name: poi.name, address: poi.address || (poi.pname + poi.cityname + poi.adname), location: poi.location, latitude: poi.location.lat, longitude: poi.location.lng, pname: poi.pname, cityname: poi.cityname, adname: poi.adname })) : []
    })
  })
}

const selectPoi = (poi: any) => {
  selectedPoi.value = poi
  if (map && poi.location) { map.setCenter([poi.location.lng, poi.location.lat]); currentPosition = { lng: poi.location.lng, lat: poi.location.lat } }
  shopForm.value.address = poi.name + (poi.address ? ` (${poi.address})` : '')
  shopForm.value.latitude = poi.latitude?.toFixed(6) || ''
  shopForm.value.longitude = poi.longitude?.toFixed(6) || ''
  ElMessage.success('位置已选择')
}

const openLocationPicker = async () => {
  showMap.value = !showMap.value
  if (showMap.value) { try { await loadAmapScript(); setTimeout(() => initMap(), 100) } catch { ElMessage.error('地图加载失败') } }
}

const wgs84ToGcj02 = (lng: number, lat: number): [number, number] => {
  const a = 6378245.0, ee = 0.00669342162296594323
  if ((lng < 72.004 || lng > 137.8347) || (lat < 0.8293 || lat > 55.8271)) return [lng, lat]
  let dLat = -100.0 + 2.0 * (lng - 105) + 3.0 * (lat - 35) + 0.2 * (lat - 35) * (lat - 35) + 0.1 * (lng - 105) * (lat - 35) + 0.2 * Math.sqrt(Math.abs(lng - 105))
  dLat += (20.0 * Math.sin(6.0 * (lng - 105) * Math.PI) + 20.0 * Math.sin(2.0 * (lng - 105) * Math.PI)) * 2.0 / 3.0
  dLat += (20.0 * Math.sin((lat - 35) * Math.PI) + 40.0 * Math.sin((lat - 35) / 3.0 * Math.PI)) * 2.0 / 3.0
  dLat += (160.0 * Math.sin((lat - 35) / 12.0 * Math.PI) + 320 * Math.sin((lat - 35) * Math.PI / 30.0)) * 2.0 / 3.0
  let dLng = 300.0 + (lng - 105) + 2.0 * (lat - 35) + 0.1 * (lng - 105) * (lng - 105) + 0.1 * (lng - 105) * (lat - 35) + 0.1 * Math.sqrt(Math.abs(lng - 105))
  dLng += (20.0 * Math.sin(6.0 * (lng - 105) * Math.PI) + 20.0 * Math.sin(2.0 * (lng - 105) * Math.PI)) * 2.0 / 3.0
  dLng += (20.0 * Math.sin((lng - 105) * Math.PI) + 40.0 * Math.sin((lng - 105) / 3.0 * Math.PI)) * 2.0 / 3.0
  dLng += (150.0 * Math.sin((lng - 105) / 12.0 * Math.PI) + 300.0 * Math.sin((lng - 105) / 30.0 * Math.PI)) * 2.0 / 3.0
  const radLat = lat / 180.0 * Math.PI; let magic = Math.sin(radLat); magic = 1 - ee * magic * magic; const sqrtMagic = Math.sqrt(magic)
  dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI); dLng = (dLng * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI)
  return [lng + dLng, lat + dLat]
}

const loadShopInfo = async (forceRefresh: boolean = false) => {
  try {
    loading.value = true
    dataStatus.value = 'updating'
    console.log('🔄 开始加载店铺信息...', { forceRefresh })
    
    // 添加时间戳参数防止缓存
    const params = { 
      pageNum: 1, 
      pageSize: 1,
      _t: Date.now()  // 始终添加时间戳防止缓存
    }
    
    const res = await getShopList(params)
    console.log('📡 API响应数据:', res)
    
    if (res.list?.length > 0) {
      const shop = res.list[0]
      console.log('🏪 原始店铺数据:', shop)
      console.log('🏪 原始店铺数据字段:', Object.keys(shop))
      
      // 使用数据转换函数
      const transformedData = transformApiDataToForm(shop)
      console.log('🔄 转换后的数据:', transformedData)
      
      // 强制刷新时总是更新数据，否则比较新旧数据
      const shouldUpdate = forceRefresh || !compareShopData(shopForm.value, transformedData)
      
      if (shouldUpdate) {
        console.log('📝 更新表单数据', forceRefresh ? '(强制刷新)' : '(检测到变化)')
        shopForm.value = transformedData
        
        // 更新地图位置
        if (transformedData.latitude && transformedData.longitude) {
          const lng = parseFloat(transformedData.longitude)
          const lat = parseFloat(transformedData.latitude)
          if (!isNaN(lng) && !isNaN(lat)) {
            currentPosition = { lng, lat }
            console.log('🗺️ 更新地图位置:', currentPosition)
          }
        }
        
        // 更新原始表单数据
        originalForm.value = { ...shopForm.value }
        console.log('✅ 店铺信息加载完成')
      } else {
        console.log('📋 数据无变化，跳过更新')
      }
      
      // 更新状态
      lastUpdateTime.value = new Date()
      dataStatus.value = 'synced'
    } else {
      console.warn('⚠️ 未获取到店铺数据')
      ElMessage.warning('未找到店铺信息')
      dataStatus.value = null
    }
  } catch (error: any) {
    console.error('❌ 加载店铺信息失败:', error)
    ElMessage.error('加载店铺信息失败: ' + (error.message || '未知错误'))
    dataStatus.value = null
  } finally {
    loading.value = false
  }
}

const triggerLogoUpload = () => {
  console.log('触发Logo上传，isEditing:', isEditing.value)
  if (!isEditing.value) {
    ElMessage.warning('请先点击修改信息按钮')
    return
  }
  logoInput.value?.click()
}

const handleLogoChange = async (event: Event) => {
  console.log('封面文件选择事件触发')
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) {
    console.log('未选择文件')
    return
  }
  
  console.log('选择的文件:', file.name, file.size, file.type)
  
  logoUploading.value = true
  try { 
    console.log('开始上传封面文件')
    const url = await uploadSingleFile(file, 'merchant')
    console.log('封面上传成功，URL:', url)
    shopForm.value.headerImage = url
    ElMessage.success('封面上传成功') 
  } catch (error: any) { 
    console.error('封面上传失败:', error)
    ElMessage.error('封面上传失败: ' + (error.message || '未知错误'))
  } finally { 
    logoUploading.value = false
    target.value = '' 
  }
}



const saveShopInfo = async () => {
  // 数据验证
  const validation = validateShopData(shopForm.value)
  if (!validation.isValid) {
    validation.errors.forEach(error => ElMessage.warning(error))
    return
  }
  
  // 处理数据，避免后端BigDecimal异常
  const submitData = {
    ...shopForm.value,
    // 确保数字字段为null而不是空字符串，避免BigDecimal构造异常
    averagePrice: shopForm.value.averagePrice && shopForm.value.averagePrice.trim() !== '' ? shopForm.value.averagePrice : null,
    latitude: shopForm.value.latitude && shopForm.value.latitude.trim() !== '' ? shopForm.value.latitude : null,
    longitude: shopForm.value.longitude && shopForm.value.longitude.trim() !== '' ? shopForm.value.longitude : null
  }
  
  console.log('💾 提交的数据:', submitData)
  console.log('🏪 店铺ID:', shopForm.value.id)
  
  if (!shopForm.value.id) {
    ElMessage.error('店铺ID不存在，无法保存')
    return
  }
  
  try { 
    saving.value = true
    console.log('🚀 开始调用updateShop API...')
    
    await updateShop(shopForm.value.id as number, submitData)
    console.log('✅ API调用成功')
    
    ElMessage.success('保存成功')
    
    // 等待一小段时间确保后端数据已更新
    await new Promise(resolve => setTimeout(resolve, 500))
    
    // 强制刷新数据，确保显示最新内容
    console.log('🔄 强制刷新数据以确保同步...')
    await loadShopInfo(true)
    
    // 验证数据是否正确更新
    const currentData = shopForm.value
    console.log('🔍 验证更新后的数据:', currentData)
    
    isEditing.value = false
    console.log('✅ 保存流程完成')
  } catch (error: any) { 
    console.error('❌ 保存失败:', error)
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  } finally { 
    saving.value = false 
  }
}

// 运营数据相关方法
const refreshStats = async () => {
  if (!shopForm.value.id) return
  
  try {
    statsLoading.value = true
    console.log('🔄 刷新运营数据，店铺ID:', shopForm.value.id)
    
    const stats = await getShopStats(shopForm.value.id)
    console.log('📊 获取到的运营数据:', stats)
    
    // 安全更新运营数据，确保数字类型正确
    const updateStats = {
      rating: typeof stats.avgRating === 'number' ? stats.avgRating : (stats.rating || 0),
      tasteScore: typeof stats.tasteScore === 'number' ? stats.tasteScore : 0,
      environmentScore: typeof stats.environmentScore === 'number' ? stats.environmentScore : 0,
      serviceScore: typeof stats.serviceScore === 'number' ? stats.serviceScore : 0,
      reviewCount: typeof stats.totalReviews === 'number' ? stats.totalReviews : (stats.reviewCount || 0),
      popularity: typeof stats.totalViews === 'number' ? stats.totalViews : (stats.popularity || 0)
    }
    
    console.log('📈 处理后的运营数据:', updateStats)
    
    // 更新表单数据
    Object.assign(shopForm.value, updateStats)
    
    lastStatsUpdate.value = new Date()
    ElMessage.success('运营数据已更新')
  } catch (error: any) {
    console.error('❌ 刷新运营数据失败:', error)
    ElMessage.error('刷新运营数据失败: ' + (error.message || '未知错误'))
  } finally {
    statsLoading.value = false
  }
}

const toggleAutoRefresh = (enabled: boolean) => {
  if (enabled) {
    // 开启自动刷新，每30秒刷新一次
    refreshTimer = setInterval(() => {
      refreshStats()
    }, 30000)
    ElMessage.info('已开启自动刷新，每30秒更新一次运营数据')
  } else {
    // 关闭自动刷新
    if (refreshTimer) {
      clearInterval(refreshTimer)
      refreshTimer = null
    }
    ElMessage.info('已关闭自动刷新')
  }
}

const formatScore = (score: number | string) => {
  const num = Number(score) || 0
  return num.toFixed(2)
}

const formatTime = (date: Date) => {
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

onMounted(async () => {
  console.log('🚀 页面初始化，加载店铺信息...')
  console.log('📋 当前环境信息:', {
    userAgent: navigator.userAgent,
    url: window.location.href,
    timestamp: new Date().toISOString()
  })
  
  try {
    // 清除可能的缓存，确保获取最新数据
    clearCache()
    
    // 加载类目列表
    await loadCategories()
    
    // 强制刷新数据
    await loadShopInfo(true)
    
    console.log('✅ 初始数据加载完成，当前店铺数据:', shopForm.value)
    
    // 页面加载后自动刷新一次运营数据
    setTimeout(() => {
      if (shopForm.value.id) {
        console.log('🔄 开始刷新运营数据...')
        refreshStats()
      } else {
        console.warn('⚠️ 店铺ID不存在，跳过运营数据刷新')
      }
    }, 1000)
  } catch (error) {
    console.error('❌ 页面初始化失败:', error)
  }
})

onUnmounted(() => { 
  if (map) { 
    map.destroy(); 
    map = null 
  }
  // 清理定时器
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
})
</script>

<style scoped>
.shop-info-page { background: #f0f2f5; min-height: 100vh; padding-bottom: 40px; }
.page-header { padding: 24px 40px; background: #FFFFFF; }
.header-content { display: flex; justify-content: space-between; align-items: flex-start; max-width: 1400px; margin: 0 auto; }
.header-text { flex: 1; }
.page-title { font-size: 24px; font-weight: 600; color: #333; margin: 0 0 8px 0; }
.page-desc { font-size: 13px; color: #999; margin: 0; }
.edit-btn { 
  background: #FF6B00 !important; 
  border-color: #FF6B00 !important; 
  padding: 8px 20px; 
  border-radius: 4px; 
  font-weight: 500;
  font-size: 14px;
  display: inline-flex !important;
  align-items: center;
  gap: 8px;
}
.edit-btn:hover { background: #E65E00 !important; border-color: #E65E00 !important; }
.header-actions {
  display: flex;
  align-items: center;
}
.edit-mode-buttons {
  display: flex;
  gap: 12px;
  align-items: center;
}
.content-section { padding: 20px 40px; max-width: 1400px; margin: 0 auto; }
.info-card { 
  background: #FFFFFF; 
  border-radius: 8px; 
  padding: 30px; 
  margin-bottom: 20px; 
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.card-title { 
  font-size: 16px; 
  font-weight: 600; 
  color: #333; 
  margin: 0 0 30px 0;
  padding-left: 12px;
  border-left: 4px solid #FF6B00;
  line-height: 1.2;
}

.update-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.update-text {
  font-size: 13px;
  color: #666;
}

.refresh-btn {
  margin-right: 12px;
  background: #f5f5f5 !important;
  border-color: #d9d9d9 !important;
  color: #666 !important;
}

.refresh-btn:hover {
  background: #e6f7ff !important;
  border-color: #91d5ff !important;
  color: #1890ff !important;
}

.form-layout { display: grid; grid-template-columns: 280px 1fr; gap: 32px; }

/* 店铺封面样式 */
.cover-section { 
  display: flex; 
  flex-direction: column; 
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, #FFF7ED 0%, #FFFBF0 100%);
  border-radius: 12px;
  border: 1px solid #FFE4CC;
}

.cover-label {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.cover-title {
  font-size: 16px;
  font-weight: 600;
  color: #171717;
}

.cover-subtitle {
  font-size: 12px;
  color: #737373;
}

.cover-upload-wrapper { 
  position: relative; 
  width: 100%; 
  height: 160px; 
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.cover-upload-wrapper:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.cover-image, .cover-placeholder { 
  width: 100%; 
  height: 100%; 
  border-radius: 8px; 
  border: 1px solid #E5E5E5; 
  object-fit: cover; 
  transition: all 0.3s ease; 
}

.cover-placeholder { 
  display: flex; 
  flex-direction: column;
  align-items: center; 
  justify-content: center; 
  background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%); 
  color: #A3A3A3;
  gap: 12px;
  cursor: pointer;
}

.cover-placeholder:hover {
  background: linear-gradient(135deg, #FFF7ED 0%, #FFFBF0 100%);
  border-color: #FF7D00;
  color: #FF7D00;
}

.upload-icon {
  opacity: 0.6;
  transition: all 0.3s ease;
}

.cover-placeholder:hover .upload-icon {
  opacity: 1;
  transform: scale(1.1);
}

.upload-text {
  font-size: 14px;
  font-weight: 500;
}

.cover-image.editable { 
  cursor: pointer; 
}

.cover-image.editable:hover { 
  border-color: #FF7D00; 
}

.cover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  opacity: 0;
  transition: all 0.3s ease;
  cursor: pointer;
}

.cover-upload-wrapper:hover .cover-overlay {
  opacity: 1;
}

.upload-loading { 
  position: absolute; 
  top: 0; 
  left: 0; 
  width: 100%; 
  height: 100%; 
  background: rgba(255,255,255,0.9); 
  display: flex; 
  flex-direction: column;
  align-items: center; 
  justify-content: center; 
  border-radius: 12px;
  gap: 8px;
}

.loading-text {
  font-size: 14px;
  color: #FF7D00;
  font-weight: 500;
}

.cover-tip { 
  font-size: 11px; 
  color: #737373; 
  text-align: center; 
  line-height: 1.4; 
  margin: 0;
  padding: 8px 12px;
  background: rgba(255, 125, 0, 0.05);
  border-radius: 6px;
  border: 1px solid rgba(255, 125, 0, 0.1);
}
.form-fields { 
  display: flex; 
  flex-direction: column; 
  gap: 24px;
  padding: 24px;
  background: #FAFBFC;
  border-radius: 12px;
  border: 1px solid #F0F1F3;
}

.field-row { 
  display: grid; 
  grid-template-columns: 1fr 1fr; 
  gap: 24px; 
}

.field-item { 
  display: flex; 
  flex-direction: column;
  gap: 8px;
}

.field-full { grid-column: 1 / -1; }
.field-half { flex: 1; }
.field-small { flex: 1; min-width: 120px; }
.field-coord { flex: 2; min-width: 280px; }

.field-label { 
  font-size: 15px; 
  font-weight: 600; 
  color: #374151; 
  margin-bottom: 8px; 
  display: flex;
  align-items: center;
  gap: 4px;
}

.required { 
  color: #EF4444; 
  font-weight: 700;
}
.field-input { 
  width: 100%; 
}

.field-input :deep(.el-input__wrapper) { 
  border-radius: 10px; 
  padding: 12px 16px; 
  border: 2px solid #E5E7EB; 
  transition: all 0.3s ease;
  background: #FFFFFF;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.field-input :deep(.el-input__wrapper:hover) { 
  border-color: #FF7D00; 
  box-shadow: 0 2px 8px rgba(255, 125, 0, 0.1);
}

.field-input :deep(.el-input__wrapper.is-focus) { 
  border-color: #FF7D00; 
  box-shadow: 0 0 0 3px rgba(255, 125, 0, 0.15), 0 2px 8px rgba(255, 125, 0, 0.1);
}

.field-input :deep(.el-textarea__inner) { 
  border-radius: 10px; 
  padding: 12px 16px; 
  border: 2px solid #E5E7EB; 
  transition: all 0.3s ease;
  background: #FFFFFF;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
  resize: vertical;
  min-height: 100px;
}

.field-input :deep(.el-textarea__inner:hover) { 
  border-color: #FF7D00; 
  box-shadow: 0 2px 8px rgba(255, 125, 0, 0.1);
}

.field-input :deep(.el-textarea__inner:focus) { 
  border-color: #FF7D00; 
  box-shadow: 0 0 0 3px rgba(255, 125, 0, 0.15), 0 2px 8px rgba(255, 125, 0, 0.1);
}

/* 禁用状态样式优化 - 移除灰色文字和禁止符号 */
.field-input :deep(.el-input__wrapper.is-disabled) { 
  background-color: #FFFFFF !important; 
  color: #171717 !important;
  cursor: default !important;
}
.field-input :deep(.el-input__wrapper.is-disabled .el-input__inner) { 
  color: #171717 !important; 
  cursor: default !important;
  -webkit-text-fill-color: #171717 !important;
}
.field-input :deep(.el-textarea.is-disabled .el-textarea__inner) { 
  background-color: #FFFFFF !important; 
  color: #171717 !important; 
  cursor: default !important;
  -webkit-text-fill-color: #171717 !important;
}
.field-input :deep(.el-select.is-disabled .el-input__wrapper) { 
  background-color: #FFFFFF !important; 
  color: #171717 !important;
  cursor: default !important;
}
.field-input :deep(.el-select.is-disabled .el-input__inner) { 
  color: #171717 !important; 
  cursor: default !important;
  -webkit-text-fill-color: #171717 !important;
}

/* 店铺简介和相册并排布局 */
.description-gallery-row { display: flex; gap: 20px; }
.description-gallery-row .field-half { flex: 1; }

/* 商家相册样式 */
.gallery-section { width: 100%; }
.gallery-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.gallery-item { position: relative; aspect-ratio: 1; border-radius: 8px; overflow: hidden; }
.gallery-image { width: 100%; height: 100%; object-fit: cover; cursor: pointer; transition: transform 0.2s; }
.gallery-image:hover { transform: scale(1.05); }
.gallery-delete { position: absolute; top: 4px; right: 4px; width: 24px; height: 24px; background: rgba(0,0,0,0.6); border-radius: 50%; display: flex; align-items: center; justify-content: center; cursor: pointer; color: white; font-size: 12px; }
.gallery-delete:hover { background: #EF4444; }
.gallery-upload { aspect-ratio: 1; border: 2px dashed #D4D4D4; border-radius: 8px; display: flex; align-items: center; justify-content: center; cursor: pointer; background: #FAFAFA; transition: all 0.2s; color: #A3A3A3; }
.gallery-upload:hover { border-color: #FF7D00; color: #FF7D00; }
.uploading-text { font-size: 12px; }
.gallery-tip { font-size: 12px; color: #737373; margin-top: 8px; }

/* 联系信息和位置信息盒子样式 */
.section-divider {
  margin: 32px 0 24px 0;
  text-align: center;
  position: relative;
}

.section-divider::before {
  content: '';
  position: absolute;
  left: 0;
  right: 0;
  top: 50%;
  height: 1px;
  background: linear-gradient(90deg, transparent 0%, #E5E7EB 20%, #E5E7EB 80%, transparent 100%);
}

.divider-text {
  position: relative;
  display: inline-block;
  padding: 0 20px;
  background: #FAFBFC;
  font-size: 14px;
  font-weight: 600;
  color: #6B7280;
  letter-spacing: 0.5px;
}

/* 经纬度和定位按钮样式 */
.locate-btn { 
  padding: 8px 16px; 
  background: #FF7D00; 
  border-color: #FF7D00;
  margin-left: 8px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.locate-btn:hover { 
  background: #E67000; 
  border-color: #E67000; 
}
.locate-btn.is-disabled { 
  background: #F5F5F5 !important; 
  border-color: #E5E5E5 !important; 
  color: #A3A3A3 !important;
  cursor: default !important;
}

.location-picker-container { width: 100%; }
.location-input-row { 
  display: flex; 
  gap: 12px; 
  align-items: center;
  flex-wrap: wrap;
}
.location-input { 
  flex: 2; 
  min-width: 200px;
}
.location-btn { background: #FF7D00; border-color: #FF7D00; padding: 10px 20px; border-radius: 8px; font-weight: 500; }
.location-btn:hover { background: #E67000; border-color: #E67000; }
.location-btn.is-disabled { 
  background: #F5F5F5 !important; 
  border-color: #E5E5E5 !important; 
  color: #A3A3A3 !important;
  cursor: default !important;
}
.map-wrapper { position: relative; margin-top: 16px; border-radius: 12px; overflow: hidden; border: 1px solid #E5E5E5; }
.amap-container { width: 100%; height: 400px; }
.map-center-marker { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -100%); font-size: 40px; z-index: 10; pointer-events: none; filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3)); }
.map-controls { position: absolute; right: 16px; bottom: 16px; z-index: 10; }
.relocate-btn { background: white; border: 1px solid #E5E5E5; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.map-search { position: absolute; top: 16px; left: 16px; right: 16px; z-index: 10; }
.search-input { background: white; border-radius: 8px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); }
.search-input :deep(.el-input__wrapper) { border-radius: 8px; }
.poi-list { position: absolute; top: 60px; left: 16px; right: 16px; max-height: 200px; overflow-y: auto; background: white; border-radius: 8px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); z-index: 10; }
.poi-item { padding: 12px 16px; cursor: pointer; border-bottom: 1px solid #F5F5F5; transition: background 0.2s; }
.poi-item:hover, .poi-item.active { background: #FFF7ED; }
.poi-name { font-size: 14px; font-weight: 500; color: #171717; }
.poi-address { font-size: 12px; color: #737373; margin-top: 4px; }
.map-loading { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: rgba(255,255,255,0.9); padding: 16px 24px; border-radius: 8px; display: flex; align-items: center; gap: 8px; z-index: 20; }

/* 运营数据样式 */
.stats-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.stats-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.refresh-btn {
  background: #FF7D00;
  border-color: #FF7D00;
  color: white;
}

.refresh-btn:hover {
  background: #E67000;
  border-color: #E67000;
}

.auto-refresh-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stats-grid { 
  display: grid; 
  grid-template-columns: repeat(3, 1fr); 
  gap: 24px; 
  margin-bottom: 20px;
}

.stat-item { 
  text-align: center; 
  padding: 20px; 
  background: linear-gradient(135deg, #FAFAFA 0%, #F5F5F5 100%); 
  border-radius: 12px;
  border: 1px solid #E5E5E5;
  transition: all 0.3s ease;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-label { 
  font-size: 14px; 
  color: #737373; 
  margin-bottom: 12px; 
  font-weight: 500;
}

.stat-value { 
  font-size: 28px; 
  font-weight: 700; 
  color: #171717; 
  margin-bottom: 8px;
}

.stats-footer {
  padding-top: 16px;
  border-top: 1px solid #E5E5E5;
}

.stats-note { 
  font-size: 12px; 
  color: #A3A3A3; 
  text-align: center; 
  margin: 0 0 8px 0;
}

.last-update {
  font-size: 11px;
  color: #999999;
  text-align: center;
  margin: 0;
}
.action-buttons { display: flex; justify-content: flex-end; gap: 12px; margin-top: 24px; }
.btn-cancel { padding: 12px 32px; border-radius: 8px; }
.btn-save { background: #FF7D00; border-color: #FF7D00; padding: 12px 32px; border-radius: 8px; font-weight: 500; }
.btn-save:hover { background: #E67000; border-color: #E67000; }

/* 响应式设计 */
@media (max-width: 768px) {
  .form-layout {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .cover-upload-wrapper {
    height: 160px;
  }
  
  .field-row {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .location-input-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .info-card {
    padding: 20px;
  }
  
  .form-fields {
    padding: 20px;
  }
  
  .cover-section {
    padding: 20px;
  }
}

@media (max-width: 480px) {
  .cover-upload-wrapper {
    height: 140px;
  }
  
  .cover-title {
    font-size: 16px;
  }
  
  .card-title {
    font-size: 20px;
  }
  
  .edit-mode-buttons {
    flex-direction: column;
    width: 100%;
  }
  
  .edit-mode-buttons .el-button {
    width: 100%;
  }
  
  .stats-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .stats-actions {
    justify-content: space-between;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
  
  .stat-item {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 24px;
  }
}

/* 新的水平布局样式 */
.form-layout-horizontal { 
  display: flex; 
  gap: 50px; 
}

/* 左侧封面区域 */
.left-cover-section { 
  width: 280px; 
  flex-shrink: 0; 
}

.cover-label-top {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.cover-upload-box {
  width: 100%;
  height: 280px;
  background-color: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: border-color 0.3s;
  position: relative;
  overflow: hidden;
}

.cover-upload-box:hover {
  border-color: #FF6B00;
}

.cover-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder-new {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #999;
}

.cover-upload-box:hover .upload-icon {
  color: #FF6B00;
}

.upload-loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.cover-hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: white;
  opacity: 0;
  transition: all 0.3s ease;
}

.cover-upload-box:hover .cover-hover-overlay {
  opacity: 1;
}

.cover-hint-text {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
  text-align: center;
  line-height: 1.5;
}

/* 右侧表单网格 */
.right-form-grid {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 1fr;
  column-gap: 30px;
  row-gap: 24px;
}

.form-field {
  display: flex;
  flex-direction: column;
}

.form-field-full {
  grid-column: span 2;
}

.field-label-new {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 8px;
}

.field-input-new :deep(.el-input__wrapper) { 
  border-radius: 4px; 
  padding: 10px 12px; 
  border: 1px solid #d9d9d9; 
  transition: all 0.3s ease;
  box-shadow: none;
}

.field-input-new :deep(.el-input__wrapper:hover) { 
  border-color: #FF6B00; 
}

.field-input-new :deep(.el-input__wrapper.is-focus) { 
  border-color: #FF6B00; 
  box-shadow: 0 0 0 2px rgba(255, 107, 0, 0.1);
}

.field-input-new :deep(.el-textarea__inner) { 
  border-radius: 4px; 
  padding: 10px 12px; 
  border: 1px solid #d9d9d9; 
  transition: all 0.3s ease;
  box-shadow: none;
  resize: vertical;
  min-height: 80px;
}

.field-input-new :deep(.el-textarea__inner:hover) { 
  border-color: #FF6B00; 
}

.field-input-new :deep(.el-textarea__inner:focus) { 
  border-color: #FF6B00; 
  box-shadow: 0 0 0 2px rgba(255, 107, 0, 0.1);
}

/* Readonly状态样式 - 保持白色背景和清晰文字 */
.field-input-new :deep(.el-input__wrapper) {
  background-color: #FFFFFF !important;
}

.field-input-new :deep(.el-input__inner[readonly]) {
  background-color: #FFFFFF !important;
  color: #333 !important;
  -webkit-text-fill-color: #333 !important;
  cursor: text !important;
  border-color: #d9d9d9 !important;
}

.field-input-new :deep(.el-textarea__inner[readonly]) {
  background-color: #FFFFFF !important;
  color: #333 !important;
  -webkit-text-fill-color: #333 !important;
  cursor: text !important;
  border-color: #d9d9d9 !important;
}

/* Select组件的禁用状态优化 - 强制覆盖灰色样式 */
.field-input-new.select-readonly :deep(.el-input__wrapper),
.field-input-new.select-readonly :deep(.el-input__wrapper.is-disabled) {
  background-color: #FFFFFF !important;
  cursor: default !important;
  box-shadow: none !important;
}

.field-input-new.select-readonly :deep(.el-input__inner),
.field-input-new.select-readonly :deep(.el-input__wrapper.is-disabled .el-input__inner) {
  color: #333 !important;
  -webkit-text-fill-color: #333 !important;
  cursor: default !important;
}

.field-input-new.select-readonly :deep(.el-select__caret),
.field-input-new.select-readonly :deep(.el-select__wrapper.is-disabled .el-select__caret) {
  cursor: default !important;
  color: #333 !important;
}

/* 强制覆盖 Element Plus 的 disabled 样式 */
.field-input-new.select-readonly :deep(.el-select.is-disabled .el-input__wrapper) {
  background-color: #FFFFFF !important;
  box-shadow: none !important;
}

.field-input-new.select-readonly :deep(.el-select.is-disabled .el-input__inner) {
  color: #333 !important;
  -webkit-text-fill-color: #333 !important;
}

/* 禁用状态的按钮样式 */
.field-input-new :deep(.el-input__wrapper.is-disabled),
.field-input-new :deep(.el-textarea.is-disabled .el-textarea__inner),
.field-input-new :deep(.el-select.is-disabled .el-input__wrapper) { 
  background-color: #FFFFFF !important; 
  color: #333 !important;
  border-color: #d9d9d9 !important;
  cursor: default !important;
  box-shadow: none !important;
}

.field-input-new :deep(.el-input__wrapper.is-disabled .el-input__inner),
.field-input-new :deep(.el-select.is-disabled .el-input__inner) { 
  color: #333 !important; 
  -webkit-text-fill-color: #333 !important;
  cursor: default !important;
}

/* 额外的 Select 禁用状态覆盖 */
.field-input-new :deep(.el-select.is-disabled) {
  cursor: default !important;
}

.field-input-new :deep(.el-select.is-disabled .el-select__wrapper) {
  background-color: #FFFFFF !important;
  cursor: default !important;
}

.field-input-new :deep(.el-select.is-disabled .el-select__placeholder) {
  color: #333 !important;
}

/* 全局覆盖 - 确保所有 select 在非编辑模式下都是白色 */
.field-input-new :deep(.el-select .el-input.is-disabled .el-input__wrapper) {
  background-color: #FFFFFF !important;
  box-shadow: none !important;
}

.field-input-new :deep(.el-select .el-input.is-disabled .el-input__inner) {
  color: #333 !important;
  -webkit-text-fill-color: #333 !important;
}

/* 移除 disabled 状态的灰色背景 */
.field-input-new :deep(.el-input.is-disabled .el-input__wrapper),
.field-input-new :deep(.el-select .el-input.is-disabled .el-input__wrapper) {
  background-color: #FFFFFF !important;
}

/* 位置输入组 */
.location-input-group {
  display: flex;
  gap: 10px;
}

.location-input-flex {
  flex: 1;
}

.locate-btn-new,
.location-btn-new {
  padding: 10px 15px;
  background: #FF6B00;
  border-color: #FF6B00;
  border-radius: 4px;
}

.locate-btn-new:hover,
.location-btn-new:hover {
  background: #E65E00;
  border-color: #E65E00;
}

.locate-btn-new.is-disabled,
.location-btn-new.is-disabled {
  background: #f5f5f5 !important;
  border-color: #d9d9d9 !important;
  color: #999 !important;
}

/* 地图样式 */
.map-wrapper-new { 
  position: relative; 
  margin-top: 16px; 
  border-radius: 8px; 
  overflow: hidden; 
  border: 1px solid #d9d9d9; 
}

/* 商家相册网格 */
.gallery-grid-new {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.gallery-item-new {
  width: 100px;
  height: 100px;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  border: 1px solid #eee;
}

.gallery-image-new {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: pointer;
  transition: transform 0.2s;
}

.gallery-image-new:hover {
  transform: scale(1.05);
}

.gallery-delete-new {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: white;
}

.gallery-delete-new:hover {
  background: #ff4d4f;
}

.gallery-add-new {
  width: 100px;
  height: 100px;
  background-color: #fafafa;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  color: #999;
  transition: all 0.2s;
}

.gallery-add-new:hover {
  border-color: #FF6B00;
  color: #FF6B00;
}

.gallery-hint-new {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 响应式 */
@media (max-width: 900px) {
  .form-layout-horizontal {
    flex-direction: column;
  }
  
  .left-cover-section {
    width: 100%;
  }
  
  .right-form-grid {
    grid-template-columns: 1fr;
  }
  
  .form-field-full {
    grid-column: span 1;
  }
}
</style>