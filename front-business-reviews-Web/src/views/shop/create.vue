<template>
  <div class="shop-create">
    <div class="info-card">
      <div class="card-header">
        <span>{{ isEdit ? '编辑门店' : '新增门店' }}</span>
      </div>
      
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        label-width="120px"
        style="max-width: 800px;"
      >
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入门店名称" />
        </el-form-item>
        
        <el-form-item label="门店分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择门店分类" style="width: 100%;">
            <el-option 
              v-for="cat in categories" 
              :key="cat.id" 
              :label="cat.name" 
              :value="cat.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="门店地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入详细地址" />
        </el-form-item>
        
        <el-form-item label="人均消费">
          <el-input-number v-model="form.averagePrice" :min="0" :max="9999" placeholder="人均消费" controls-position="right" />
          <span style="margin-left: 10px; color: #5c5e62;">元 / 人</span>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        
        <el-form-item label="营业时间" prop="openingHours">
          <el-input v-model="form.openingHours" placeholder="例如：09:00-22:00" />
        </el-form-item>
        
        <el-form-item label="门店描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入门店描述，吸引更多客户..." 
          />
        </el-form-item>
        
        <el-form-item label="门店图片">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :http-request="customAvatarUpload"
            :before-upload="beforeAvatarUpload"
          >
            <img v-if="form.avatar" :src="form.avatar" class="avatar" />
            <div v-else class="avatar-uploader-icon">
              <el-icon v-if="!avatarUploading"><Plus /></el-icon>
              <span v-else>上传中...</span>
            </div>
          </el-upload>
          <div class="form-tip" style="margin-top: 8px;">建议尺寸：200x200px，展示在搜索列表</div>
        </el-form-item>
        
        <el-form-item label="门店封面">
          <el-upload
            class="cover-uploader"
            :show-file-list="false"
            :http-request="customCoverUpload"
            :before-upload="beforeCoverUpload"
          >
            <img v-if="form.cover" :src="form.cover" class="cover" />
            <div v-else class="cover-uploader-icon">
              <el-icon v-if="!coverUploading"><Plus /></el-icon>
              <span v-else>上传中...</span>
            </div>
          </el-upload>
          <div class="form-tip" style="margin-top: 8px;">建议尺寸：750x450px，展示在店铺详情页顶部</div>
        </el-form-item>
        
        <el-form-item label="营业状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正在营业</el-radio>
            <el-radio :label="0">暂停营业</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item style="margin-top: 40px;">
          <el-button type="primary" class="primary-btn" @click="submitForm" :loading="loading">
            {{ isEdit ? '保存更新' : '立即创建' }}
          </el-button>
          <el-button class="cancel-btn" @click="$router.back()">返回</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadProps, UploadRawFile } from 'element-plus'
import { createShop, updateShop, getShopDetail, getCategories, type CategoryVO } from '@/api/shop'
import { uploadFile } from '@/api/upload'

// 路由
const route = useRoute()
const router = useRouter()

// 表单引用
const formRef = ref()

// 是否为编辑模式
const isEdit = ref(false)
const shopId = ref(0)

// 上传状态
const avatarUploading = ref(false)
const coverUploading = ref(false)

// 分类列表
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
    ElMessage.warning('类目加载失败，使用默认类目')
  }
}

// 表单数据
const form = ref({
  name: '',
  categoryId: 1,
  address: '',
  phone: '',
  openingHours: '',
  description: '',
  avatar: '',
  cover: '',
  averagePrice: 0,
  status: 1
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入门店名称', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入门店地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ],
  openingHours: [
    { required: true, message: '请输入营业时间', trigger: 'blur' }
  ]
}

// 加载状态
const loading = ref(false)

// 自定义头像上传
const customAvatarUpload: UploadProps['httpRequest'] = async (options) => {
  const file = options.file as File
  avatarUploading.value = true
  try {
    const result = await uploadFile(file)
    form.value.avatar = result.url
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
  } finally {
    avatarUploading.value = false
  }
}

// 自定义封面上传
const customCoverUpload: UploadProps['httpRequest'] = async (options) => {
  const file = options.file as File
  coverUploading.value = true
  try {
    const result = await uploadFile(file)
    form.value.cover = result.url
    ElMessage.success('封面上传成功')
  } catch (error) {
    ElMessage.error('封面上传失败')
  } finally {
    coverUploading.value = false
  }
}

// 上传前检查 - 增加文件大小限制到10MB
const beforeAvatarUpload: UploadProps['beforeUpload'] = (rawFile: UploadRawFile) => {
  const isImage = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png' || rawFile.type === 'image/gif' || rawFile.type === 'image/webp'
  const isLt10M = rawFile.size / 1024 / 1024 < 10
  
  if (!isImage) {
    ElMessage.error('图片只能是 JPG/PNG/GIF/WEBP 格式!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

const beforeCoverUpload: UploadProps['beforeUpload'] = (rawFile: UploadRawFile) => {
  const isImage = rawFile.type === 'image/jpeg' || rawFile.type === 'image/png' || rawFile.type === 'image/gif' || rawFile.type === 'image/webp'
  const isLt10M = rawFile.size / 1024 / 1024 < 10
  
  if (!isImage) {
    ElMessage.error('图片只能是 JPG/PNG/GIF/WEBP 格式!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB!')
    return false
  }
  return true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        loading.value = true
        
        if (isEdit.value) {
          // 编辑模式
          await updateShop(shopId.value, form.value)
          ElMessage.success('更新成功')
        } else {
          // 新增模式
          await createShop(form.value)
          ElMessage.success('创建成功')
        }
        
        router.push('/shops')
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 获取门店详情（编辑模式）
const fetchShopDetail = async (id: number) => {
  try {
    const data = await getShopDetail(id)
    form.value = { ...data }
  } catch (error) {
    ElMessage.error('获取门店详情失败')
  }
}

// 页面加载时初始化
onMounted(async () => {
  // 加载类目列表
  await loadCategories()
  
  // 检查是否为编辑模式
  if (route.params.id) {
    isEdit.value = true
    shopId.value = Number(route.params.id)
    fetchShopDetail(shopId.value)
  }
})
</script>

<style scoped>
.shop-create { 
  max-width: 1400px; 
  margin: 0 auto; 
  padding: 40px; 
  background-color: #f9f9f9;
  min-height: 100vh;
}

.info-card { 
  background: white; 
  border-radius: 8px; 
  padding: 40px; 
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); 
  border: 1px solid #e5e5e5;
}

.card-header {
  font-size: 24px;
  font-weight: 600;
  color: #171a20;
  margin-bottom: 32px;
  padding-left: 12px;
  border-left: 3px solid #3e6ae1;
  line-height: 1.2;
}

/* 表单输入框样式 */
:deep(.el-input__wrapper),
:deep(.el-textarea__inner),
:deep(.el-input-number__wrapper) { 
  border-radius: 4px; 
  box-shadow: none !important;
  border: 1px solid #dcdfe6;
  background-color: #f4f4f4;
  transition: all 0.2s;
}

:deep(.el-input__wrapper:hover),
:deep(.el-textarea__inner:hover) {
  background-color: #e8e8e8;
}

:deep(.el-input__wrapper.is-focus),
:deep(.el-textarea__inner:focus) {
  background-color: #fff;
  border-color: #8e8e8e;
}

/* 上传器共同样式 */
.avatar-uploader, .cover-uploader {
  border: 1px dashed #cccccc;
  border-radius: 4px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  background-color: #fafafa;
  transition: all 0.3s;
}

.avatar-uploader:hover, .cover-uploader:hover {
  border-color: #3e6ae1;
  background-color: #f0f4ff;
}

.avatar-uploader { width: 120px; height: 120px; }
.cover-uploader { width: 300px; height: 180px; }

.avatar { width: 120px; height: 120px; object-fit: cover; }
.cover { width: 300px; height: 180px; object-fit: cover; }

.avatar-uploader-icon, .cover-uploader-icon {
  font-size: 24px;
  color: #8e8e8e;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.avatar-uploader-icon span, .cover-uploader-icon span {
  font-size: 13px;
  font-weight: 500;
}

/* 按钮样式 */
.primary-btn {
  background-color: #3e6ae1 !important;
  border-color: #3e6ae1 !important;
  padding: 12px 32px;
  border-radius: 4px;
}

.cancel-btn {
  padding: 12px 32px;
  border-radius: 4px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #393c41;
}

:deep(.el-radio__input.is-checked .el-radio__inner) {
  background-color: #3e6ae1;
  border-color: #3e6ae1;
}

:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #3e6ae1;
}
</style>
```