<template>
  <div class="note-create-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">{{ isEdit ? '编辑笔记' : '发布笔记' }}</h1>
        <p class="page-desc">创建精彩内容，吸引UniApp用户关注您的店铺</p>
      </div>
      <div class="header-actions">
        <el-button @click="saveDraft" :loading="saving" class="draft-btn">保存草稿</el-button>
        <el-button type="primary" @click="publishNote" :loading="publishing" class="publish-btn">
          <el-icon><Upload /></el-icon>{{ isEdit ? '更新笔记' : '发布笔记' }}
        </el-button>
      </div>
    </div>

    <!-- 表单内容 -->
    <div class="form-container">
      <el-form ref="formRef" :model="noteForm" :rules="formRules" label-width="120px" class="note-form">
        
        <!-- 基本信息 -->
        <div class="form-section">
          <h3 class="section-title">基本信息</h3>
          
          <el-form-item label="笔记标题" prop="title" class="form-item">
            <el-input 
              v-model="noteForm.title" 
              placeholder="输入吸引人的标题，让更多用户发现您的内容"
              maxlength="100"
              show-word-limit
              class="title-input"
            />
          </el-form-item>

          <el-form-item label="关联店铺" prop="shopId" class="form-item">
            <el-select 
              v-model="noteForm.shopId" 
              placeholder="选择要关联的店铺"
              clearable
              filterable
              class="shop-select"
            >
              <el-option 
                v-for="shop in shopList" 
                :key="shop.id" 
                :label="shop.name" 
                :value="shop.id"
              >
                <div class="shop-option">
                  <span class="shop-name">{{ shop.name }}</span>
                  <span class="shop-address">{{ shop.address }}</span>
                </div>
              </el-option>
            </el-select>
            <div class="form-tip">关联店铺后，用户可以通过笔记发现您的店铺</div>
          </el-form-item>
        </div>

        <!-- 图片上传 -->
        <div class="form-section">
          <h3 class="section-title">图片内容</h3>
          
          <el-form-item label="笔记图片" prop="images" class="form-item">
            <div class="image-upload-area">
              <div class="image-grid">
                <!-- 已上传的图片 -->
                <div v-for="(img, index) in noteForm.images" :key="index" class="image-item">
                  <img :src="img" class="uploaded-image" />
                  <div class="image-overlay">
                    <el-button circle size="small" @click="previewImage(index)">
                      <el-icon><View /></el-icon>
                    </el-button>
                    <el-button circle size="small" type="danger" @click="removeImage(index)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                  <div v-if="index === 0" class="cover-badge">封面</div>
                </div>
                
                <!-- 上传按钮 -->
                <div v-if="noteForm.images.length < 9" class="upload-item" @click="triggerImageUpload">
                  <input ref="imageInput" type="file" accept="image/*" multiple @change="handleImageUpload" style="display: none;" />
                  <el-icon :size="40" v-if="!imageUploading"><Plus /></el-icon>
                  <el-icon :size="40" v-else class="is-loading"><Loading /></el-icon>
                  <span class="upload-text">{{ imageUploading ? '上传中...' : '添加图片' }}</span>
                </div>
              </div>
              <div class="upload-tips">
                <p>• 最多上传9张图片，第一张将作为封面图</p>
                <p>• 支持JPG、PNG格式，单张图片不超过10MB</p>
                <p>• 建议图片尺寸：750x750px或以上</p>
              </div>
            </div>
          </el-form-item>
        </div>

        <!-- 内容编辑 -->
        <div class="form-section">
          <h3 class="section-title">内容描述</h3>
          
          <el-form-item label="笔记内容" prop="content" class="form-item">
            <el-input 
              v-model="noteForm.content" 
              type="textarea" 
              :rows="8"
              placeholder="分享您的店铺特色、美食推荐、服务亮点等，让用户了解您的店铺..."
              maxlength="2000"
              show-word-limit
              class="content-textarea"
            />
          </el-form-item>
        </div>

        <!-- 位置信息 -->
        <div class="form-section">
          <h3 class="section-title">位置信息</h3>
          
          <el-form-item label="位置名称" class="form-item">
            <el-input 
              v-model="noteForm.location" 
              placeholder="输入位置名称（可选）"
              class="location-input"
            />
          </el-form-item>

          <el-form-item label="地理坐标" class="form-item">
            <div class="coordinate-row">
              <el-input 
                v-model="noteForm.latitude" 
                placeholder="纬度"
                class="coord-input"
              />
              <span class="coord-separator">,</span>
              <el-input 
                v-model="noteForm.longitude" 
                placeholder="经度"
                class="coord-input"
              />
              <el-button @click="getCurrentLocation" :loading="locating" class="location-btn">
                <el-icon><Aim /></el-icon>获取当前位置
              </el-button>
            </div>
          </el-form-item>
        </div>

        <!-- 发布设置 -->
        <div class="form-section">
          <h3 class="section-title">发布设置</h3>
          
          <el-form-item label="推荐设置" class="form-item">
            <el-switch 
              v-model="noteForm.isRecommend" 
              active-text="推荐到首页"
              inactive-text="普通发布"
              :active-value="1"
              :inactive-value="0"
            />
            <div class="form-tip">推荐到首页的笔记将获得更多曝光机会</div>
          </el-form-item>
        </div>

      </el-form>
    </div>

    <!-- 图片预览 -->
    <el-image-viewer 
      v-if="showImageViewer" 
      :url-list="noteForm.images" 
      :initial-index="previewIndex" 
      @close="showImageViewer = false" 
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Upload, View, Delete, Loading, Aim } from '@element-plus/icons-vue'
import { createNote, updateNote, getNoteDetail } from '@/api/note'
import { getShopList } from '@/api/shop'
import { uploadMultipleFiles } from '@/services/uploadService'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const noteId = computed(() => route.params.id as string)

// 表单数据
const noteForm = ref({
  title: '',
  content: '',
  shopId: undefined as number | undefined,
  images: [] as string[],
  location: '',
  latitude: '',
  longitude: '',
  isRecommend: 0
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入笔记标题', trigger: 'blur' },
    { min: 5, max: 100, message: '标题长度应在5-100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入笔记内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度应在10-2000个字符之间', trigger: 'blur' }
  ],
  images: [
    { 
      validator: (rule: any, value: string[], callback: Function) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少上传一张图片'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ]
}

const formRef = ref()
const imageInput = ref<HTMLInputElement>()
const shopList = ref<any[]>([])
const saving = ref(false)
const publishing = ref(false)
const imageUploading = ref(false)
const locating = ref(false)
const showImageViewer = ref(false)
const previewIndex = ref(0)

// 获取店铺列表
const fetchShops = async () => {
  try {
    const res = await getShopList({ pageNum: 1, pageSize: 100 })
    shopList.value = res.list || res.records || []
  } catch (error) {
    console.error('获取店铺列表失败:', error)
  }
}

// 加载笔记详情（编辑模式）
const loadNoteDetail = async () => {
  if (!isEdit.value) return
  
  try {
    const res = await getNoteDetail(Number(noteId.value))
    if (res) {
      noteForm.value = {
        title: res.title || '',
        content: res.content || '',
        shopId: res.shopId,
        images: res.images || [],
        location: res.location || '',
        latitude: res.latitude?.toString() || '',
        longitude: res.longitude?.toString() || '',
        isRecommend: res.isRecommend || 0
      }
    }
  } catch (error) {
    ElMessage.error('加载笔记详情失败')
    router.push('/notes')
  }
}

// 触发图片上传
const triggerImageUpload = () => {
  if (imageUploading.value) return
  imageInput.value?.click()
}

// 处理图片上传
const handleImageUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return

  const currentCount = noteForm.value.images.length
  const maxUpload = 9 - currentCount
  if (maxUpload <= 0) {
    ElMessage.warning('最多上传9张图片')
    return
  }

  const filesToUpload = Array.from(files).slice(0, maxUpload)
  
  imageUploading.value = true
  try {
    const urls = await uploadMultipleFiles(filesToUpload, 'merchant')
    noteForm.value.images.push(...urls)
    ElMessage.success(`成功上传${urls.length}张图片`)
  } catch (error: any) {
    ElMessage.error('图片上传失败: ' + (error.message || '未知错误'))
  } finally {
    imageUploading.value = false
    target.value = ''
  }
}

// 移除图片
const removeImage = (index: number) => {
  noteForm.value.images.splice(index, 1)
}

// 预览图片
const previewImage = (index: number) => {
  previewIndex.value = index
  showImageViewer.value = true
}

// 获取当前位置
const getCurrentLocation = () => {
  if (!navigator.geolocation) {
    ElMessage.error('浏览器不支持定位功能')
    return
  }

  locating.value = true
  navigator.geolocation.getCurrentPosition(
    (position) => {
      noteForm.value.latitude = position.coords.latitude.toFixed(6)
      noteForm.value.longitude = position.coords.longitude.toFixed(6)
      locating.value = false
      ElMessage.success('定位成功')
    },
    (error) => {
      locating.value = false
      ElMessage.error('定位失败: ' + error.message)
    },
    { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
  )
}

// 保存草稿
const saveDraft = async () => {
  if (!noteForm.value.title.trim()) {
    ElMessage.warning('请输入笔记标题')
    return
  }

  saving.value = true
  try {
    const data = {
      ...noteForm.value,
      status: 0, // 草稿状态
      latitude: noteForm.value.latitude ? parseFloat(noteForm.value.latitude) : null,
      longitude: noteForm.value.longitude ? parseFloat(noteForm.value.longitude) : null
    }

    if (isEdit.value) {
      await updateNote(Number(noteId.value), data)
      ElMessage.success('草稿保存成功')
    } else {
      const res = await createNote(data)
      ElMessage.success('草稿保存成功')
      router.replace(`/notes/edit/${res.noteId}`)
    }
  } catch (error: any) {
    ElMessage.error('保存失败: ' + (error.message || '未知错误'))
  } finally {
    saving.value = false
  }
}

// 发布笔记
const publishNote = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    ElMessage.warning('请完善必填信息')
    return
  }

  publishing.value = true
  try {
    const data = {
      ...noteForm.value,
      status: 1, // 发布状态
      latitude: noteForm.value.latitude ? parseFloat(noteForm.value.latitude) : null,
      longitude: noteForm.value.longitude ? parseFloat(noteForm.value.longitude) : null
    }

    if (isEdit.value) {
      await updateNote(Number(noteId.value), data)
      ElMessage.success('笔记更新成功')
    } else {
      await createNote(data)
      ElMessage.success('笔记发布成功')
    }
    
    router.push('/notes')
  } catch (error: any) {
    ElMessage.error('发布失败: ' + (error.message || '未知错误'))
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  fetchShops()
  loadNoteDetail()
})
</script>

<style scoped>
.note-create-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid #E5E5E5;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #171717;
  margin: 0 0 8px 0;
}

.page-desc {
  font-size: 14px;
  color: #737373;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.draft-btn {
  padding: 12px 24px;
  border-radius: 8px;
}

.publish-btn {
  background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%);
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
}

.form-container {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.form-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #171717;
  margin: 0 0 24px 0;
  padding-bottom: 12px;
  border-bottom: 2px solid #FF7D00;
  display: inline-block;
}

.form-item {
  margin-bottom: 24px;
}

.title-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 12px 16px;
  font-size: 16px;
}

.shop-select {
  width: 100%;
}

.shop-option {
  display: flex;
  flex-direction: column;
}

.shop-name {
  font-weight: 500;
  color: #171717;
}

.shop-address {
  font-size: 12px;
  color: #737373;
  margin-top: 2px;
}

.form-tip {
  font-size: 12px;
  color: #737373;
  margin-top: 8px;
}

/* 图片上传区域 */
.image-upload-area {
  width: 100%;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #E5E5E5;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.image-item:hover .image-overlay {
  opacity: 1;
}

.cover-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: #FF7D00;
  color: white;
  padding: 4px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
}

.upload-item {
  aspect-ratio: 1;
  border: 2px dashed #D4D4D4;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #FAFAFA;
  transition: all 0.3s ease;
  color: #A3A3A3;
}

.upload-item:hover {
  border-color: #FF7D00;
  color: #FF7D00;
  background: #FFF7ED;
}

.upload-text {
  font-size: 14px;
  margin-top: 8px;
}

.upload-tips {
  background: #F8F9FA;
  padding: 16px;
  border-radius: 8px;
  border-left: 4px solid #FF7D00;
}

.upload-tips p {
  margin: 0 0 4px 0;
  font-size: 13px;
  color: #666;
}

.upload-tips p:last-child {
  margin-bottom: 0;
}

.content-textarea :deep(.el-textarea__inner) {
  border-radius: 10px;
  padding: 16px;
  font-size: 14px;
  line-height: 1.6;
}

.coordinate-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.coord-input {
  flex: 1;
}

.coord-separator {
  color: #737373;
  font-weight: 500;
}

.location-btn {
  background: #FF7D00;
  border-color: #FF7D00;
  color: white;
}

.location-btn:hover {
  background: #E67000;
  border-color: #E67000;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  
  .header-actions {
    justify-content: stretch;
  }
  
  .header-actions .el-button {
    flex: 1;
  }
  
  .image-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 12px;
  }
  
  .coordinate-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>