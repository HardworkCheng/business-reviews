<template>
  <div class="note-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑笔记' : '发布笔记' }}</span>
        </div>
      </template>
      
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        label-width="120px"
      >
        <el-form-item label="笔记标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入笔记标题" />
        </el-form-item>
        
        <el-form-item label="关联门店" prop="shopId">
          <el-select v-model="form.shopId" placeholder="请选择关联门店" style="width: 100%;">
            <el-option 
              v-for="shop in shopList" 
              :key="shop.id" 
              :label="shop.name" 
              :value="shop.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="笔记内容" prop="content">
          <el-input 
            v-model="form.content" 
            type="textarea" 
            :rows="10" 
            placeholder="请输入笔记内容" 
          />
        </el-form-item>
        
        <el-form-item label="话题标签">
          <el-select 
            v-model="form.tags" 
            multiple 
            filterable 
            allow-create 
            default-first-option
            placeholder="请输入话题标签" 
            style="width: 100%;"
          >
            <el-option 
              v-for="tag in tagOptions" 
              :key="tag" 
              :label="tag" 
              :value="tag" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="笔记图片">
          <el-upload
            class="upload-container"
            list-type="picture-card"
            :file-list="fileList"
            :http-request="customUpload"
            :on-preview="handlePictureCardPreview"
            :on-remove="handleRemove"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="定时发布">
          <el-date-picker
            v-model="form.publishTime"
            type="datetime"
            placeholder="选择发布时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="0">保存为草稿</el-radio>
            <el-radio :label="1">提交审核</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            {{ isEdit ? '更新' : '发布' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 图片预览 -->
    <el-dialog v-model="dialogVisible">
      <img w-full :src="dialogImageUrl" alt="Preview Image" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { UploadProps, UploadFile } from 'element-plus'
import { createNote, updateNote, getNoteDetail } from '@/api/note'
import { getShopList } from '@/api/shop'
import { uploadFile } from '@/api/upload'

// 路由
const route = useRoute()
const router = useRouter()

// 表单引用
const formRef = ref()

// 是否为编辑模式
const isEdit = ref(false)
const noteId = ref(0)

// 表单数据
const form = ref({
  title: '',
  shopId: undefined as number | undefined,
  content: '',
  tags: [] as string[],
  images: [] as string[],
  publishTime: '',
  status: 0 // 0: 草稿, 1: 提交审核
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入笔记标题', trigger: 'blur' }
  ],
  shopId: [
    { required: true, message: '请选择关联门店', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入笔记内容', trigger: 'blur' }
  ]
}

// 门店列表
const shopList = ref<any[]>([])

// 标签选项
const tagOptions = ref(['美食探店', '新品推荐', '优惠活动', '环境展示', '特色菜品'])

// 文件列表
const fileList = ref<any[]>([])

// 图片预览
const dialogImageUrl = ref('')
const dialogVisible = ref(false)

// 加载状态
const loading = ref(false)

// 自定义图片上传
const customUpload: UploadProps['httpRequest'] = async (options) => {
  const file = options.file as File
  try {
    const result = await uploadFile(file)
    form.value.images.push(result.url)
    // 更新fileList以显示上传的图片
    fileList.value.push({
      name: file.name,
      url: result.url
    })
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error('图片上传失败')
  }
}

// 移除图片
const handleRemove = (uploadFile: UploadFile, uploadFiles: UploadFile[]) => {
  const index = form.value.images.indexOf(uploadFile.url || '')
  if (index > -1) {
    form.value.images.splice(index, 1)
  }
  // 同步更新fileList
  const fileIndex = fileList.value.findIndex(f => f.url === uploadFile.url)
  if (fileIndex > -1) {
    fileList.value.splice(fileIndex, 1)
  }
}

// 图片预览
const handlePictureCardPreview = (file: any) => {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

// 获取门店列表
const fetchShopList = async () => {
  try {
    const res = await getShopList({ pageNum: 1, pageSize: 100 })
    shopList.value = res.list || res.records || []
  } catch (error) {
    ElMessage.error('获取门店列表失败')
  }
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
          await updateNote(noteId.value, form.value)
          ElMessage.success('更新成功')
        } else {
          // 新增模式
          await createNote(form.value)
          ElMessage.success('发布成功')
        }
        
        router.push('/notes')
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '发布失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 获取笔记详情（编辑模式）
const fetchNoteDetail = async (id: number) => {
  try {
    const data = await getNoteDetail(id)
    form.value = { ...data }
    // 处理图片
    if (data.images && data.images.length > 0) {
      fileList.value = data.images.map((url: string, index: number) => ({
        name: `image_${index}.jpg`,
        url: url
      }))
    }
  } catch (error) {
    ElMessage.error('获取笔记详情失败')
  }
}

// 页面加载时初始化
onMounted(() => {
  // 获取门店列表
  fetchShopList()
  
  // 检查是否为编辑模式
  if (route.params.id) {
    isEdit.value = true
    noteId.value = Number(route.params.id)
    fetchNoteDetail(noteId.value)
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.upload-container :deep(.el-upload--picture-card) {
  width: 120px;
  height: 120px;
  line-height: 120px;
}

.upload-container :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 120px;
  height: 120px;
}
</style>