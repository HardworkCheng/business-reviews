<template>
  <div class="shop-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑门店' : '新增门店' }}</span>
        </div>
      </template>
      
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        label-width="120px"
        style="max-width: 600px;"
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
          <el-input-number v-model="form.averagePrice" :min="0" :max="9999" placeholder="人均消费" />
          <span style="margin-left: 10px;">元</span>
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
            :rows="3" 
            placeholder="请输入门店描述" 
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
            <el-icon v-else class="avatar-uploader-icon">
              <Plus v-if="!avatarUploading" />
              <span v-else>上传中...</span>
            </el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="门店封面">
          <el-upload
            class="cover-uploader"
            :show-file-list="false"
            :http-request="customCoverUpload"
            :before-upload="beforeCoverUpload"
          >
            <img v-if="form.cover" :src="form.cover" class="cover" />
            <el-icon v-else class="cover-uploader-icon">
              <Plus v-if="!coverUploading" />
              <span v-else>上传中...</span>
            </el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadProps, UploadRawFile } from 'element-plus'
import { createShop, updateShop, getShopDetail } from '@/api/shop'
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
const categories = ref([
  { id: 1, name: '美食' },
  { id: 2, name: 'KTV' },
  { id: 3, name: '丽人·美发' },
  { id: 4, name: '美睫·美甲' },
  { id: 5, name: '按摩·足疗' },
  { id: 6, name: '美容SPA' },
  { id: 7, name: '亲子游乐' },
  { id: 8, name: '酒吧' }
])

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
onMounted(() => {
  // 检查是否为编辑模式
  if (route.params.id) {
    isEdit.value = true
    shopId.value = Number(route.params.id)
    fetchShopDetail(shopId.value)
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
}

.avatar-uploader:hover {
  border-color: #409eff;
}

.avatar-uploader .avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
}

.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 300px;
  height: 200px;
}

.cover-uploader:hover {
  border-color: #409eff;
}

.cover-uploader .cover {
  width: 300px;
  height: 200px;
  display: block;
  object-fit: cover;
}

.avatar-uploader-icon,
.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>